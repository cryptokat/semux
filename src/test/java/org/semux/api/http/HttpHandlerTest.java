/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api.http;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.semux.KernelMock;
import org.semux.api.ApiHandlerResponse;
import org.semux.rules.KernelRule;
import org.semux.util.BasicAuth;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.netty.handler.codec.http.HttpHeaders;

public class HttpHandlerTest {

    @Rule
    public KernelRule kernelRule = new KernelRule(51610, 51710);

    private String uri = null;
    private Map<String, String> params = null;
    private HttpHeaders headers = null;

    private KernelMock kernel;
    private SemuxApiService server;

    private String ip;
    private int port;
    private String auth;

    @Before
    public void setUp() {
        kernel = kernelRule.getKernel();
        server = new SemuxApiService(kernel);

        ip = kernel.getConfig().apiListenIp();
        port = kernel.getConfig().apiListenPort();
        auth = BasicAuth.generateAuth(kernel.getConfig().apiUsername(), kernel.getConfig().apiPassword());
    }

    @After
    public void tearDown() {
        server.stop();
    }

    private void startServer(HttpChannelInitializer httpChannelInitializer) {
        // wait for server to boot up
        new Thread(() -> server.start(ip, port, httpChannelInitializer == null ? new HttpChannelInitializer() {
            @Override
            HttpHandler initHandler() {
                return new HttpHandler(kernel.getConfig(), (u, p, h) -> {
                    uri = u;
                    params = p;
                    headers = h;

                    return new ApiHandlerResponse(true, "test");
                });
            }
        } : httpChannelInitializer)).start();

        await().until(() -> server.isRunning());
    }

    @Test(expected = IOException.class)
    public void testAuth() throws IOException {
        startServer(null);

        URL url = new URL("http://" + ip + ":" + port + "/getinfo");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("c", "d");
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.getOutputStream().write("e=f".getBytes());

        Scanner s = new Scanner(con.getInputStream());
        s.nextLine();
        s.close();
    }

    @Test
    public void testPOST() throws IOException {
        startServer(null);

        URL url = new URL("http://" + ip + ":" + port + "/test?a=b");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("c", "d");
        con.setRequestProperty("Authorization", auth);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.getOutputStream().write("e=f".getBytes());

        Scanner s = new Scanner(con.getInputStream());
        s.nextLine();
        s.close();

        assertEquals("/test", uri);
        assertEquals("b", params.get("a"));
        assertEquals("f", params.get("e"));
        assertEquals("d", headers.get("c"));
    }

    @Test
    public void testGET() throws IOException {
        startServer(null);

        URL url = new URL("http://" + ip + ":" + port + "/test?a=b&e=f");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("c", "d");
        con.setRequestProperty("Authorization", auth);
        Scanner s = new Scanner(con.getInputStream());
        s.nextLine();
        s.close();

        assertEquals("/test", uri);
        assertEquals("b", params.get("a"));
        assertEquals("f", params.get("e"));
        assertEquals("d", headers.get("c"));
    }

    @Test
    public void testReturnNumbersAsStringsTrue() throws IOException {
        startServer(new HttpChannelInitializer() {
            @Override
            HttpHandler initHandler() {
                when(kernel.getConfig().apiReturnNumbersAsStrings()).thenReturn(true);
                return new HttpHandler(kernel.getConfig(), (u, p, h) -> new TestResponse(true, new TestType((byte) 1, (short) 2, 3, 4L)));
            }
        });

        assertEquals("{\"success\":true,\"result\":{\"aByte\":\"1\",\"aShort\":\"2\",\"aInteger\":\"3\",\"aLong\":\"4\"}}", get(new URL("http://" + ip + ":" + port + "/test")));
    }

    @Test
    public void testReturnNumbersAsStringsFalse() throws IOException {
        startServer(new HttpChannelInitializer() {
            @Override
            HttpHandler initHandler() {
                when(kernel.getConfig().apiReturnNumbersAsStrings()).thenReturn(false);
                return new HttpHandler(kernel.getConfig(), (u, p, h) -> new TestResponse(true, new TestType((byte) 1, (short) 2, 3, 4L)));
            }
        });

        assertEquals("{\"success\":true,\"result\":{\"aByte\":1,\"aShort\":2,\"aInteger\":3,\"aLong\":4}}", get(new URL("http://" + ip + ":" + port + "/test")));
    }

    private String get(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", auth);
        String response = "";
        Scanner s = new Scanner(con.getInputStream());
        response += s.nextLine();
        s.close();
        return response;
    }

    private static class TestResponse extends ApiHandlerResponse {

        @JsonProperty("result")
        TestType result;

        public TestResponse(@JsonProperty("success") Boolean success, @JsonProperty("result") TestType result) {
            super(success, null);
            this.result = result;
        }
    }

    private static class TestType {
        @JsonProperty("aByte")
        public final Byte aByte;

        @JsonProperty("aShort")
        public final Short aShort;

        @JsonProperty("aInteger")
        public final Integer aInteger;

        @JsonProperty("aLong")
        public final Long aLong;

        public TestType(@JsonProperty("aByte") Byte aByte, @JsonProperty("aShort") Short aShort, @JsonProperty("aInteger") Integer aInteger, @JsonProperty("aLong") Long aLong) {
            this.aByte = aByte;
            this.aShort = aShort;
            this.aInteger = aInteger;
            this.aLong = aLong;
        }
    }
}
