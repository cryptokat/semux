/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api.response;

import java.util.List;
import java.util.stream.Collectors;

import org.semux.Kernel;
import org.semux.api.v1_0_1.BlockType;
import org.semux.api.v1_0_1.TransactionType;
import org.semux.core.Block;
import org.semux.core.BlockchainImpl;
import org.semux.core.Transaction;
import org.semux.core.state.Account;
import org.semux.core.state.Delegate;
import org.semux.crypto.Hex;
import org.semux.net.Peer;
import org.semux.util.TimeUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Types {

    public static class AccountType {

        public final String address;
        public final long available;
        public final long locked;
        public final long nonce;
        public final int transactionCount;

        @JsonCreator
        public AccountType(
                @JsonProperty("address") String address,
                @JsonProperty("available") long available,
                @JsonProperty("locked") long locked,
                @JsonProperty("nonce") long nonce,
                @JsonProperty("transactionCount") int transactionCount) {
            this.address = address;
            this.available = available;
            this.locked = locked;
            this.nonce = nonce;
            this.transactionCount = transactionCount;
        }

        public AccountType(Account account, int transactionCount) {
            this(Hex.encode0x(account.getAddress()),
                    account.getAvailable(),
                    account.getLocked(),
                    account.getNonce(),
                    transactionCount);
        }
    }

    public static BlockType blockType(Block block) {
        return new BlockType()
                .hash(Hex.encode0x(block.getHash()))
                .number(block.getNumber())
                .view(block.getView())
                .coinbase(Hex.encode0x(block.getCoinbase()))
                .parentHash(Hex.encode0x(block.getParentHash()))
                .timestamp(block.getTimestamp())
                .date(TimeUtil.formatTimestamp(block.getTimestamp()))
                .transactionsRoot(Hex.encode0x(block.getTransactionsRoot()))
                .resultsRoot(Hex.encode0x(block.getResultsRoot()))
                .stateRoot(Hex.encode0x(block.getStateRoot()))
                .data(Hex.encode0x(block.getData()))
                .transactions(block.getTransactions().stream()
                        .map(tx -> transactionType(block.getNumber(), tx))
                        .collect(Collectors.toList()));
    }

    public static class DelegateType {

        @JsonProperty("address")
        public final String address;

        @JsonProperty("name")
        public final String name;

        @JsonProperty("registeredAt")
        public final Long registeredAt;

        @JsonProperty("votes")
        public final Long votes;

        @JsonProperty("blocksForged")
        public final Long blocksForged;

        @JsonProperty("turnsHit")
        public final Long turnsHit;

        @JsonProperty("turnsMissed")
        public final Long turnsMissed;

        public DelegateType(BlockchainImpl.ValidatorStats validatorStats, Delegate delegate) {
            this(Hex.PREF + delegate.getAddressString(),
                    delegate.getNameString(),
                    delegate.getRegisteredAt(),
                    delegate.getVotes(),
                    validatorStats.getBlocksForged(),
                    validatorStats.getTurnsHit(),
                    validatorStats.getTurnsMissed());
        }

        public DelegateType(
                @JsonProperty("address") String address,
                @JsonProperty("name") String name,
                @JsonProperty("registeredAt") Long registeredAt,
                @JsonProperty("votes") Long votes,
                @JsonProperty("blocksForged") Long blocksForged,
                @JsonProperty("turnsHit") Long turnsHit,
                @JsonProperty("turnsMissed") Long turnsMissed) {
            this.address = address;
            this.name = name;
            this.registeredAt = registeredAt;
            this.votes = votes;
            this.blocksForged = blocksForged;
            this.turnsHit = turnsHit;
            this.turnsMissed = turnsMissed;
        }
    }

    public static class InfoType {
        @JsonProperty("clientId")
        public final String clientId;

        @JsonProperty("coinbase")
        public final String coinbase;

        @JsonProperty("latestBlockNumber")
        public final Long latestBlockNumber;

        @JsonProperty("latestBlockHash")
        public final String latestBlockHash;

        @JsonProperty("activePeers")
        public final Integer activePeers;

        @JsonProperty("pendingTransactions")
        public final Integer pendingTransactions;

        public InfoType(
                @JsonProperty("clientId") String clientId,
                @JsonProperty("coinbase") String coinbase,
                @JsonProperty("latestBlockNumber") Long latestBlockNumber,
                @JsonProperty("latestBlockHash") String latestBlockHash,
                @JsonProperty("activePeers") Integer activePeers,
                @JsonProperty("pendingTransactions") Integer pendingTransactions) {
            this.clientId = clientId;
            this.coinbase = coinbase;
            this.latestBlockNumber = latestBlockNumber;
            this.latestBlockHash = latestBlockHash;
            this.activePeers = activePeers;
            this.pendingTransactions = pendingTransactions;
        }

        public InfoType(Kernel kernel) {
            this(kernel.getConfig().getClientId(),
                    Hex.PREF + kernel.getCoinbase(),
                    kernel.getBlockchain().getLatestBlockNumber(),
                    Hex.encode0x(kernel.getBlockchain().getLatestBlockHash()),
                    kernel.getChannelManager().getActivePeers().size(),
                    kernel.getPendingManager().getPendingTransactions().size());
        }
    }

    public static class PeerType {

        @JsonProperty("ip")
        public final String ip;

        @JsonProperty("port")
        public final Integer port;

        @JsonProperty("networkVersion")
        public final Short networkVersion;

        @JsonProperty("clientId")
        public final String clientId;

        @JsonProperty("peerId")
        public final String peerId;

        @JsonProperty("latestBlockNumber")
        public final Long latestBlockNumber;

        @JsonProperty("latency")
        public final Long latency;

        @JsonProperty("capabilities")
        public final List<String> capabilities;

        public PeerType(
                @JsonProperty("ip") String ip,
                @JsonProperty("port") int port,
                @JsonProperty("networkVersion") short networkVersion,
                @JsonProperty("clientId") String clientId,
                @JsonProperty("peerId") String peerId,
                @JsonProperty("latestBlockNumber") long latestBlockNumber,
                @JsonProperty("latency") long latency,
                @JsonProperty("capabilities") List<String> capabilities) {
            this.ip = ip;
            this.port = port;
            this.networkVersion = networkVersion;
            this.clientId = clientId;
            this.peerId = peerId;
            this.latestBlockNumber = latestBlockNumber;
            this.latency = latency;
            this.capabilities = capabilities;
        }

        public PeerType(Peer peer) {
            this(peer.getIp(),
                    peer.getPort(),
                    peer.getNetworkVersion(),
                    peer.getClientId(),
                    Hex.PREF + peer.getPeerId(),
                    peer.getLatestBlockNumber(),
                    peer.getLatency(),
                    peer.getCapabilities().toList());
        }
    }

    public static class TransactionLimitsType {

        @JsonProperty("maxTransactionDataSize")
        public final Integer maxTransactionDataSize;

        @JsonProperty("minTransactionFee")
        public final Long minTransactionFee;

        @JsonProperty("minDelegateBurnAmount")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final Long minDelegateBurnAmount;

        @JsonCreator
        public TransactionLimitsType(
                @JsonProperty("maxTransactionDataSize") Integer maxTransactionDataSize,
                @JsonProperty("minTransactionFee") Long minTransactionFee,
                @JsonProperty("minDelegateBurnAmount") Long minDelegateBurnAmount) {
            this.maxTransactionDataSize = maxTransactionDataSize;
            this.minTransactionFee = minTransactionFee;
            this.minDelegateBurnAmount = minDelegateBurnAmount;
        }
    }

    public static TransactionType transactionType(Long blockNumber, Transaction tx) {
        return new TransactionType()
                .blockNumber(blockNumber)
                .hash(Hex.encode0x(tx.getHash()))
                .from(Hex.encode0x(tx.getFrom()))
                .to(Hex.encode0x(tx.getTo()))
                .value(tx.getValue())
                .fee(tx.getFee())
                .nonce(tx.getNonce())
                .timestamp(tx.getTimestamp())
                .data(Hex.encode0x(tx.getData()));
    }

}
