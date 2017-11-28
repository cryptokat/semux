/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.semux.Config;
import org.semux.core.Blockchain;
import org.semux.core.PendingManager;
import org.semux.net.msg.MessageQueue;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Channel {
    private static AtomicLong cnt = new AtomicLong(0);

    private long id;

    private MessageQueue msgQueue;

    private boolean isInbound;
    private boolean isDiscoveryMode;

    private Blockchain chain;
    private ChannelManager channelMgr;
    private PendingManager pendingMgr;
    private NodeManager nodeMgr;

    private PeerClient client;
    private InetSocketAddress remoteAddress;
    private Peer remotePeer;

    private ReadTimeoutHandler timeoutHandler;
    private SemuxFrameHandler frameHandler;
    private SemuxMessageHandler messageHandler;
    private SemuxP2pHandler p2pHandler;

    /**
     * Creates a new channel instance.
     * 
     * @param chain
     * @param channelMgr
     * @param pendingMgr
     * @param nodeMgr
     */
    public Channel(Blockchain chain, ChannelManager channelMgr, PendingManager pendingMgr, NodeManager nodeMgr) {
        this.id = cnt.getAndIncrement();

        this.chain = chain;
        this.pendingMgr = pendingMgr;
        this.channelMgr = channelMgr;
        this.nodeMgr = nodeMgr;
    }

    /**
     * Initializes this channel.
     * 
     * @param pipe
     * @param isInbound
     * @param isDiscoveryMode
     * @param client
     * @param remoteAddress
     */
    public void init(ChannelPipeline pipe, boolean isInbound, boolean isDiscoveryMode, PeerClient client,
            InetSocketAddress remoteAddress) {
        this.isInbound = isInbound;
        this.isDiscoveryMode = isDiscoveryMode;

        this.client = client;
        this.remotePeer = null;
        this.remoteAddress = remoteAddress;

        this.msgQueue = new MessageQueue();

        this.timeoutHandler = new ReadTimeoutHandler(Config.NET_TIMEOUT_IDLE, TimeUnit.MILLISECONDS);
        this.frameHandler = new SemuxFrameHandler(this);
        this.messageHandler = new SemuxMessageHandler();
        this.p2pHandler = new SemuxP2pHandler(this);

        // register channel handlers
        pipe.addLast("readTimeoutHandler", timeoutHandler);
        pipe.addLast("frameHandler", frameHandler);
        pipe.addLast("messageHandler", messageHandler);
        pipe.addLast("p2pHandler", p2pHandler);
    }

    /**
     * Returns the channel id.
     * 
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the blockchain instance.
     * 
     * @return
     */
    public Blockchain getBlockchain() {
        return chain;
    }

    /**
     * Returns the pending manager.
     * 
     * @return
     */
    public PendingManager getPendingManager() {
        return pendingMgr;
    }

    /**
     * Returns the channel manager.
     * 
     * @return
     */
    public ChannelManager getChannelManager() {
        return channelMgr;
    }

    /**
     * Returns the node manager.
     * 
     * @return
     */
    public NodeManager getNodeManager() {
        return nodeMgr;
    }

    /**
     * Returns the message queue.
     * 
     * @return
     */
    public MessageQueue getMessageQueue() {
        return msgQueue;
    }

    /**
     * Returns whether this is an inbound channel.
     * 
     * @return
     */
    public boolean isInbound() {
        return isInbound;
    }

    /**
     * Returns whether this channel is in discovery mode.
     * 
     * @return
     */
    public boolean isDiscoveryMode() {
        return isDiscoveryMode;
    }

    /**
     * Returns the peer client.
     * 
     * @return
     */
    public PeerClient getClient() {
        return client;
    }

    /**
     * Returns the remote peer.
     * 
     * @return
     */
    public Peer getRemotePeer() {
        return remotePeer;
    }

    /**
     * Returns whether this channel is active.
     * 
     * @return
     */
    public boolean isActive() {
        return remotePeer != null;
    }

    /**
     * When peer connection become active.
     * 
     * @param remotePeer
     */
    public void onActive(Peer remotePeer) {
        this.remotePeer = remotePeer;

        channelMgr.onChannelActive(this); // notify channel manager
    }

    /**
     * When peer disconnects.
     */
    public void onDisconnect() {
        this.remotePeer = null;
    }

    /**
     * Returns the remote address.
     * 
     * @return
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Returns remote IP address.
     * 
     * @return
     */
    public String getRemoteIp() {
        return remoteAddress.getAddress().getHostAddress();
    }

    /**
     * Returns remote port.
     * 
     * @return
     */
    public int getRemotePort() {
        return remoteAddress.getPort();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Channel other = (Channel) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "Channel [id=" + id + ", " + (isInbound ? "IN" : "OUT") + ", remotePeer=" + remotePeer + "]";
    }

}
