/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.semux.Kernel;
import org.semux.api.exception.ApiHandlerException;
import org.semux.api.response.AddNodeResponse;
import org.semux.api.response.ApiHandlerResponse;
import org.semux.api.response.CreateAccountResponse;
import org.semux.api.response.DoTransactionResponse;
import org.semux.api.response.GetAccountResponse;
import org.semux.api.response.GetAccountTransactionsResponse;
import org.semux.api.response.GetBlockResponse;
import org.semux.api.response.GetDelegateResponse;
import org.semux.api.response.GetDelegatesResponse;
import org.semux.api.response.GetInfoResponse;
import org.semux.api.response.GetLatestBlockNumberResponse;
import org.semux.api.response.GetLatestBlockResponse;
import org.semux.api.response.GetPeersResponse;
import org.semux.api.response.GetPendingTransactionsResponse;
import org.semux.api.response.GetRootResponse;
import org.semux.api.response.GetTransactionResponse;
import org.semux.api.response.GetValidatorsResponse;
import org.semux.api.response.GetVoteResponse;
import org.semux.api.response.GetVotesResponse;
import org.semux.api.response.ListAccountsResponse;
import org.semux.api.response.SendTransactionResponse;
import org.semux.core.Transaction;
import org.semux.core.TransactionType;
import org.semux.crypto.EdDSA;
import org.semux.crypto.Hex;
import org.semux.util.Bytes;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Semux RESTful API handler implementation.
 *
 */
public class ApiHandlerImpl implements ApiHandler {

    private Kernel kernel;

    /**
     * Required parameters of each type of transaction
     */
    private static final EnumMap<TransactionType, List<String>> TRANSACTION_REQUIRED_PARAMS = new EnumMap<>(
            TransactionType.class);
    static {
        TRANSACTION_REQUIRED_PARAMS.put(TransactionType.TRANSFER, Arrays.asList("from", "to", "value", "fee"));
        TRANSACTION_REQUIRED_PARAMS.put(TransactionType.TRANSFER_MANY, Arrays.asList("from", "to[]", "value", "fee"));
        TRANSACTION_REQUIRED_PARAMS.put(TransactionType.DELEGATE, Arrays.asList("from", "fee"));
        TRANSACTION_REQUIRED_PARAMS.put(TransactionType.VOTE, Arrays.asList("from", "to", "value", "fee"));
        TRANSACTION_REQUIRED_PARAMS.put(TransactionType.UNVOTE, Arrays.asList("from", "to", "value", "fee"));
    }

    /**
     * Create an API handler.
     *
     * @param kernel
     */
    public ApiHandlerImpl(Kernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public ApiHandlerResponse service(String uri, Map<String, Object> params, HttpHeaders headers)
            throws ApiHandlerException {
        if ("/".equals(uri)) {
            return new GetRootResponse(true, "Semux API works");
        }

        Command cmd = Command.of(uri.substring(1));
        if (cmd == null) {
            return failure("Invalid request: uri = " + uri);
        }

        try {
            switch (cmd) {
            case GET_INFO: {
                return new GetInfoResponse(true, new GetInfoResponse.Result(kernel));
            }

            case GET_PEERS: {
                return new GetPeersResponse(true, kernel.getChannelManager()
                        .getActivePeers()
                        .parallelStream()
                        .map(GetPeersResponse.Result::new).collect(Collectors.toList()));
            }

            case ADD_NODE: {
                String node = (String) params.get("node");
                if (node != null) {
                    String[] tokens = node.trim().split(":");
                    kernel.getNodeManager().addNode(new InetSocketAddress(tokens[0], Integer.parseInt(tokens[1])));
                    return new AddNodeResponse(true);
                } else {
                    return failure("Invalid parameter: node can't be null");
                }
            }

            case ADD_TO_BLACKLIST: {
                return addToBlackList(params);
            }

            case ADD_TO_WHITELIST: {
                return addToWhiteList(params);
            }

            case GET_LATEST_BLOCK_NUMBER: {
                return new GetLatestBlockNumberResponse(true, kernel.getBlockchain().getLatestBlockNumber());
            }

            case GET_LATEST_BLOCK: {
                return new GetLatestBlockResponse(true,
                        new GetBlockResponse.Result(kernel.getBlockchain().getLatestBlock()));
            }

            case GET_BLOCK: {
                String number = (String) params.get("number");
                String hash = (String) params.get("hash");

                if (number != null) {
                    return new GetBlockResponse(true,
                            new GetBlockResponse.Result(kernel.getBlockchain().getBlock(Long.parseLong(number))));
                } else if (hash != null) {
                    return new GetBlockResponse(true,
                            new GetBlockResponse.Result(kernel.getBlockchain().getBlock(Hex.parse(hash))));
                } else {
                    return failure("Invalid parameter: number or hash can't be null");
                }
            }

            case GET_PENDING_TRANSACTIONS: {
                return new GetPendingTransactionsResponse(true, kernel.getPendingManager()
                        .getTransactions()
                        .parallelStream()
                        .map(GetTransactionResponse.Result::new)
                        .collect(Collectors.toList()));
            }

            case GET_ACCOUNT_TRANSACTIONS: {
                String addr = (String) params.get("address");
                String from = (String) params.get("from");
                String to = (String) params.get("to");
                if (addr != null && from != null && to != null) {
                    return new GetAccountTransactionsResponse(true, kernel.getBlockchain()
                            .getTransactions(Hex.parse(addr), Integer.parseInt(from), Integer.parseInt(to))
                            .parallelStream()
                            .map(GetTransactionResponse.Result::new)
                            .collect(Collectors.toList()));
                } else {
                    return failure("Invalid parameter: address = " + addr + ", from = " + from + ", to = " + to);
                }
            }

            case GET_TRANSACTION: {
                String hash = (String) params.get("hash");
                if (hash != null) {
                    Transaction transaction = kernel.getBlockchain().getTransaction(Hex.parse(hash));
                    return new GetTransactionResponse(true, new GetTransactionResponse.Result(transaction));
                } else {
                    return failure("Invalid parameter: hash can't be null");
                }
            }

            case SEND_TRANSACTION: {
                String raw = (String) params.get("raw");
                if (raw != null) {
                    byte[] bytes = Hex.parse(raw);
                    kernel.getPendingManager().addTransaction(Transaction.fromBytes(bytes));
                    return new SendTransactionResponse(true);
                } else {
                    return failure("Invalid parameter: raw can't be null");
                }
            }

            case GET_ACCOUNT: {
                String addr = (String) params.get("address");
                if (addr != null) {
                    return new GetAccountResponse(
                            true,
                            new GetAccountResponse.Result(
                                    kernel.getBlockchain().getAccountState().getAccount(Hex.parse(addr))));
                } else {
                    return failure("Invalid parameter: address can't be null");
                }
            }

            case GET_DELEGATE: {
                String address = (String) params.get("address");

                if (address != null) {
                    return new GetDelegateResponse(
                            true,
                            new GetDelegateResponse.Result(
                                    kernel.getBlockchain().getValidatorStats(Hex.parse(address)),
                                    kernel.getBlockchain().getDelegateState()
                                            .getDelegateByAddress(Hex.parse(address))));
                } else {
                    return failure("Invalid parameter: address can't be null");
                }
            }

            case GET_VALIDATORS: {
                return new GetValidatorsResponse(
                        true,
                        kernel.getBlockchain().getValidators().parallelStream()
                                .map(v -> Hex.PREF + v)
                                .collect(Collectors.toList()));
            }

            case GET_DELEGATES: {
                return new GetDelegatesResponse(
                        true,
                        kernel.getBlockchain()
                                .getDelegateState().getDelegates().parallelStream()
                                .map(delegate -> new GetDelegateResponse.Result(
                                        kernel.getBlockchain().getValidatorStats(delegate.getAddress()),
                                        delegate))
                                .collect(Collectors.toList()));
            }

            case GET_VOTE: {
                String voter = (String) params.get("voter");
                String delegate = (String) params.get("delegate");

                if (voter != null && delegate != null) {
                    return new GetVoteResponse(
                            true,
                            kernel.getBlockchain().getDelegateState()
                                    .getVote(Hex.parse(voter), Hex.parse(delegate)));
                } else {
                    return failure("Invalid parameter: voter = " + voter + ", delegate = " + delegate);
                }
            }

            case GET_VOTES: {
                String delegate = (String) params.get("delegate");

                if (delegate != null) {
                    return new GetVotesResponse(
                            true,
                            kernel.getBlockchain().getDelegateState().getVotes(Hex.parse(delegate)).entrySet()
                                    .parallelStream()
                                    .collect(Collectors.toMap(
                                            entry -> Hex.PREF + entry.getKey().toString(),
                                            entry -> entry.getValue())));
                } else {
                    return failure("Invalid parameter: delegate can't be null");
                }
            }

            case LIST_ACCOUNTS: {
                return new ListAccountsResponse(
                        true,
                        kernel.getWallet().getAccounts().parallelStream()
                                .map(acc -> Hex.PREF + acc.toAddressString())
                                .collect(Collectors.toList()));
            }

            case CREATE_ACCOUNT: {
                EdDSA key = new EdDSA();
                kernel.getWallet().addAccount(key);
                kernel.getWallet().flush();
                return new CreateAccountResponse(true, Hex.PREF + key.toAddressString());
            }

            case TRANSFER:
            case TRANSFER_MANY:
            case DELEGATE:
            case VOTE:
            case UNVOTE:
                return doTransaction(cmd, params);
            }
        } catch (Exception e) {
            throw new ApiHandlerException("Internal error: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }

        throw new ApiHandlerException("Not implemented: command = " + cmd, HttpResponseStatus.NOT_IMPLEMENTED);
    }

    private ApiHandlerResponse addToBlackList(Map<String, Object> params) {
        try {
            String ip = (String) params.get("ip");
            if (ip == null || ip.trim().length() == 0) {
                return failure("Invalid parameter: ip can't be empty");
            }

            kernel.getChannelManager().getIpFilter().blacklistIp(ip.trim());
            return new ApiHandlerResponse(true, null);
        } catch (UnknownHostException | IllegalArgumentException ex) {
            return failure(ex.getMessage());
        }
    }

    private ApiHandlerResponse addToWhiteList(Map<String, Object> params) {
        try {
            String ip = (String) params.get("ip");
            if (ip == null || ip.trim().length() == 0) {
                return failure("Invalid parameter: ip can't be empty");
            }

            kernel.getChannelManager().getIpFilter().whitelistIp(ip.trim());
            return new ApiHandlerResponse(true, null);
        } catch (UnknownHostException | IllegalArgumentException ex) {
            return failure(ex.getMessage());
        }
    }

    private DoTransactionResponse doTransaction(Command cmd, Map<String, Object> params) {
        // [1] check if kernel.getWallet().is unlocked
        if (!kernel.getWallet().unlocked()) {
            return new DoTransactionResponse(false, "Wallet is locked", null);
        }

        // [2] parse transaction type
        TransactionType type;
        switch (cmd) {
        case TRANSFER:
            type = TransactionType.TRANSFER;
            break;
        case TRANSFER_MANY:
            type = TransactionType.TRANSFER_MANY;
            break;
        case DELEGATE:
            type = TransactionType.DELEGATE;
            break;
        case VOTE:
            type = TransactionType.VOTE;
            break;
        case UNVOTE:
            type = TransactionType.UNVOTE;
            break;
        default:
            return new DoTransactionResponse(false, ("Unsupported transaction type: " + cmd), null);
        }

        try {
            Transaction tx = new TransactionFactory(type, params).createTransaction();
            if (kernel.getPendingManager().addTransactionSync(tx)) {
                return new DoTransactionResponse(true, null, Hex.encode0x(tx.getHash()));
            } else {
                return new DoTransactionResponse(false, "Transaction rejected by pending manager", null);
            }
        } catch (IllegalArgumentException ex) {
            return new DoTransactionResponse(false, ex.getMessage(), null);
        }
    }

    /**
     * Construct a failure response.
     *
     * @param message
     * @return
     */
    protected ApiHandlerResponse failure(String message) {
        return new ApiHandlerResponse(false, message);
    }

    private class TransactionFactory {

        TransactionType type;

        String pFrom;

        String pTo;

        List<String> pToList;

        String pValue;

        String pFee;

        String pData;

        EdDSA from;

        byte[] to;

        long value;

        long fee;

        long nonce;

        long timestamp;

        byte[] data;

        private TransactionFactory(TransactionType type, Map<String, Object> params) {
            this.type = type;

            List<String> requiredParams = TRANSACTION_REQUIRED_PARAMS.get(type);
            for (String param : requiredParams) {
                if (!param.contains(param)) {
                    throw new IllegalArgumentException(String.format("parameter '%s' is required", param));
                }
            }

            pFrom = params.containsKey("from") ? ((String) params.get("from")).trim() : null;
            pTo = params.containsKey("to") ? ((String) params.get("to")).trim() : null;
            pValue = params.containsKey("value") ? ((String) params.get("value")).trim() : null;
            pFee = params.containsKey("fee") ? ((String) params.get("fee")).trim() : null;
            pData = params.containsKey("data") ? ((String) params.get("data")) : null;

            // get the array of recipients for TRANSFER_MANY
            if (params.containsKey("to[]")) {
                pToList = (List<String>) params.get("to[]");
                pToList = pToList.parallelStream().map(String::trim).collect(Collectors.toList());
            }

            // value and fee
            value = (type == TransactionType.DELEGATE) ? kernel.getConfig().minDelegateFee()
                    : Long.parseLong(pValue);
            fee = Long.parseLong(pFee);

            // from address
            from = kernel.getWallet().getAccount(Hex.parse(pFrom));
            if (from == null) {
                throw new IllegalArgumentException("Invalid parameter: from = " + pFrom);
            }

            // to address
            if (type == TransactionType.DELEGATE) {
                to = from.toAddress();
            } else if (type == TransactionType.TRANSFER_MANY) {
                // merge all addresses into a byte array
                to = pToList.stream().map(Hex::parse).reduce(ArrayUtils::addAll).orElse(new byte[0]);
            } else {
                to = Hex.parse(pTo);
            }

            if (to == null || Array.getLength(to) == 0) {
                throw new IllegalArgumentException(
                        "Invalid parameter: to = " + (pTo == null ? pTo : StringUtils.join(pToList, ",")));
            }

            // nonce, timestamp and data
            nonce = kernel.getPendingManager().getNonce(from.toAddress());
            timestamp = System.currentTimeMillis();
            data = (pData == null) ? Bytes.EMPTY_BYTES : Hex.parse(pData);
        }

        private Transaction createTransaction() {
            // sign
            Transaction tx = new Transaction(type, to, value, fee, nonce, timestamp, data);
            tx.sign(from);

            return tx;
        }
    }
}
