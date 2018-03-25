/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api.http;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.semux.api.ApiHandler;
import org.semux.api.ApiHandlerResponse;
import org.semux.config.Config;
import org.semux.util.BasicAuth;
import org.semux.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

/**
 * HTTP handler for Semux API.
 * 
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private static final int MAX_BODY_SIZE = 512 * 1024; // 512KB
    private static final Charset CHARSET = CharsetUtil.UTF_8;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private Config config;
    private ApiHandler apiHandler;

    private boolean keepAlive;
    private String uri;
    private Map<String, List<String>> params;
    private HttpHeaders headers;
    private ByteBuf body;

    private Object response = null;
    private HttpResponseStatus status;

    public HttpHandler(Config config, ApiHandler apiHandler) {
        this.config = config;
        this.apiHandler = apiHandler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            keepAlive = HttpUtil.isKeepAlive(request);
            uri = request.uri();
            params = new QueryStringDecoder(request.uri(), CHARSET).parameters();
            if (params.isEmpty()) {
                // empty params has to be reinitialized as a instance of HashMap to avoid
                // UnsupportedOperationException
                params = new HashMap<>();
            }
            headers = request.headers();
            body = Unpooled.buffer(MAX_BODY_SIZE);

            checkDecoderResult(request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            int length = content.readableBytes();
            if (length > 0) {
                body.writeBytes(content, length);
            }

            if (msg instanceof LastHttpContent) {
                LastHttpContent trailer = (LastHttpContent) msg;
                checkDecoderResult(trailer);

                // trailing headers are ignored

                // basic authentication
                if (!checkBasicAuth(headers)) {
                    FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);

                    resp.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"Semux RESTful API\"");
                    resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
                    if (keepAlive) {
                        resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    }

                    ctx.write(resp);
                    return;
                }

                // process uri
                if (uri.contains("?")) {
                    uri = uri.substring(0, uri.indexOf('?'));
                }

                // parse parameter from body
                if ("application/x-www-form-urlencoded".equals(headers.get("Content-type"))
                        && body.readableBytes() > 0) {
                    QueryStringDecoder decoder = new QueryStringDecoder("?" + body.toString(CHARSET));
                    Map<String, List<String>> map = decoder.parameters();
                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        if (params.containsKey(entry.getKey())) {
                            params.get(entry.getKey()).addAll(entry.getValue());
                        } else {
                            params.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                // filter parameters
                Map<String, String> map = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                    List<String> v = entry.getValue();
                    // duplicate names are not allowed.
                    if (!v.isEmpty()) {
                        map.put(entry.getKey(), v.get(0));
                    }
                }

                // delegate the request to api handler if a response has not been generated
                if (response == null) {
                    response = apiHandler.service(uri, map, headers);
                    status = HttpResponseStatus.OK;
                }

                // write response
                String responseString;
                try {
                    ObjectWriter writer = objectMapper.writer();

                    if (config.apiReturnNumbersAsStrings()) {
                        writer = writer.with(WRITE_NUMBERS_AS_STRINGS);
                    } else {
                        writer = writer.without(WRITE_NUMBERS_AS_STRINGS);
                    }

                    if (map.containsKey("returnNumbersAsStrings")) {
                        if (Boolean.parseBoolean(map.get("returnNumbersAsStrings"))) {
                            writer = writer.with(WRITE_NUMBERS_AS_STRINGS);
                        } else {
                            writer = writer.without(WRITE_NUMBERS_AS_STRINGS);
                        }
                    }

                    if (Boolean.parseBoolean(map.get("pretty"))) {
                        writer = writer.withDefaultPrettyPrinter();
                    }

                    responseString = writer.writeValueAsString(response);
                } catch (JsonProcessingException e) {
                    responseString = "{\"success\":false,\"message\":\"Internal server error\"}";
                }

                if (!writeResponse(ctx, status, responseString)) {
                    // if keep-alive is off, close the connection after flushing
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private void checkDecoderResult(HttpObject o) {
        DecoderResult result = o.decoderResult();
        if (result.isSuccess()) {
            return;
        }

        response = new ApiHandlerResponse(false, BAD_REQUEST.toString());
        status = BAD_REQUEST;
    }

    private void reset() {
        response = null;
        status = null;
    }

    private boolean checkBasicAuth(HttpHeaders headers) {
        Pair<String, String> auth = BasicAuth.parseAuth(headers.get(HttpHeaderNames.AUTHORIZATION));

        return auth != null
                && MessageDigest.isEqual(Bytes.of(auth.getLeft()), Bytes.of(config.apiUsername()))
                && MessageDigest.isEqual(Bytes.of(auth.getRight()), Bytes.of(config.apiPassword()));
    }

    private boolean writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String responseBody) {
        // construct a HTTP response
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, status,
                Unpooled.copiedBuffer(responseBody == null ? "" : responseBody, CHARSET));

        // set response headers
        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        if (keepAlive) {
            resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // write response
        ctx.write(resp);

        reset();

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("Exception in API http handler", cause);
        ctx.close();
    }
}
