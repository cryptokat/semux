/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semux.Config;
import org.semux.core.Block;
import org.semux.core.BlockHeader;
import org.semux.core.Blockchain;
import org.semux.core.Genesis;
import org.semux.core.Genesis.Premine;
import org.semux.core.Transaction;
import org.semux.core.TransactionResult;
import org.semux.core.TransactionType;
import org.semux.core.Unit;
import org.semux.core.Wallet;
import org.semux.core.state.AccountState;
import org.semux.core.state.DelegateState;
import org.semux.crypto.EdDSA;
import org.semux.crypto.Hex;
import org.semux.util.ByteArray;
import org.semux.util.Bytes;
import org.semux.util.MerkleUtil;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class APIHandlerTest {

    private static File file = new File("wallet_test.data");
    private static String password = "password";

    private static Wallet wallet;
    private static APIServerMock api;

    private static AccountState as;
    private static DelegateState ds;

    @BeforeClass
    public static void setup() {
        Config.init();

        wallet = new Wallet(file);
        wallet.unlock(password);
        wallet.addAccount(new EdDSA());

        api = new APIServerMock();
        api.start(wallet, Config.API_LISTEN_IP, Config.API_LISTEN_PORT);

        as = api.chain.getAccountState();
        ds = api.chain.getDelegateState();
    }

    @Before
    public void rollbackState() {
        as.rollback();
        ds.rollback();
        as.adjustAvailable(wallet.getAccount(0).toAddress(), 5000 * Unit.SEM);

        api.pendingMgr.clear();
    }

    private static JsonObject request(String uri) throws IOException {
        URL u = new URL("http://127.0.0.1:" + Config.API_LISTEN_PORT + uri);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestProperty("Authorization", "Basic "
                + Base64.getEncoder().encodeToString(Bytes.of(Config.API_USERNAME + ":" + Config.API_PASSWORD)));

        try (JsonReader jsonReader = Json.createReader(con.getInputStream())) {
            return jsonReader.readObject();
        }
    }

    @Test
    public void testRoot() throws IOException {
        String uri = "/";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
    }

    @Test
    public void testGetInfo() throws IOException {
        String uri = "/get_info";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        JsonObject result = response.getJsonObject("result");

        assertNotNull(result);
        assertEquals(0, result.getInt("latestBlockNumber"));
    }

    @Test
    public void testGetPeers() throws IOException {
        String uri = "/get_peers";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        JsonArray result = response.getJsonArray("result");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testAddNode() throws IOException {
        String uri = "/add_node?node=127.0.0.1:5162";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));

        assertEquals(1, api.nodeMgr.queueSize());
    }

    @Test
    public void testBlockIp() throws IOException {
        String uri = "/block_ip?ip=8.8.8.8";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));

        InetSocketAddress inetSocketAddress = mock(InetSocketAddress.class);
        when(inetSocketAddress.getAddress()).thenReturn(InetAddress.getByName("8.8.8.8"));

        assertTrue(api.channelMgr.isBlocked(inetSocketAddress));
    }

    @Test
    public void testGetLatestBlockNumber() throws IOException {
        String uri = "/get_latest_block_number";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertEquals(0, response.getJsonNumber("result").longValueExact());
    }

    @Test
    public void testGetLatestBlock() throws IOException {
        String uri = "/get_latest_block";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        JsonObject block = response.getJsonObject("result");

        Genesis gen = Genesis.getInstance();
        assertArrayEquals(gen.getHash(), Hex.parse(block.getString("hash")));
        assertEquals(gen.getNumber(), block.getJsonNumber("number").longValueExact());
        assertArrayEquals(gen.getCoinbase(), Hex.parse(block.getString("coinbase")));
        assertArrayEquals(gen.getPrevHash(), Hex.parse(block.getString("prevHash")));
        assertEquals(gen.getTimestamp(), block.getJsonNumber("timestamp").longValueExact());
        assertArrayEquals(gen.getTransactionsRoot(), Hex.parse(block.getString("transactionsRoot")));
        assertArrayEquals(gen.getData(), Hex.parse(block.getString("data")));
    }

    @Test
    public void testGetBlock() throws IOException {
        Genesis gen = Genesis.getInstance();

        String uri = "/get_block?number=" + gen.getNumber();
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertArrayEquals(gen.getHash(), Hex.parse(response.getJsonObject("result").getString("hash")));

        uri = "/get_block?hash=" + Hex.encode(gen.getHash());
        response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertArrayEquals(gen.getHash(), Hex.parse(response.getJsonObject("result").getString("hash")));
    }

    @Test
    public void testGetPendingTransactions() throws IOException {
        Transaction tx = createTransaction();
        TransactionResult res = new TransactionResult(true);
        Block block = createBlock(api.chain, Collections.singletonList(tx), Collections.singletonList(res));
        api.chain.addBlock(block);

        try {
            String uri = "/get_pending_transactions";
            JsonObject response = request(uri);
            assertTrue(response.getBoolean("success"));

            JsonArray arr = response.getJsonArray("result");
            assertNotNull(arr);
        } finally {
            // Reset the API server
            teardown();
            setup();
        }
    }

    @Test
    public void testGetAccountTransactions() throws IOException {
        Transaction tx = createTransaction();
        TransactionResult res = new TransactionResult(true);
        Block block = createBlock(api.chain, Collections.singletonList(tx), Collections.singletonList(res));
        api.chain.addBlock(block);

        try {
            String uri = "/get_account_transactions?address=" + Hex.encode(tx.getFrom()) + "&from=0&to=1024";
            JsonObject response = request(uri);
            assertTrue(response.getBoolean("success"));

            JsonArray arr = response.getJsonArray("result");
            assertNotNull(arr);
        } finally {
            // Reset the API server
            teardown();
            setup();
        }
    }

    @Test
    public void testGetTransaction() throws IOException {
        Transaction tx = createTransaction();
        TransactionResult res = new TransactionResult(true);
        Block block = createBlock(api.chain, Collections.singletonList(tx), Collections.singletonList(res));
        api.chain.addBlock(block);

        try {
            String uri = "/get_transaction?hash=" + Hex.encode(tx.getHash());
            JsonObject response = request(uri);
            assertTrue(response.getBoolean("success"));

            JsonObject obj = response.getJsonObject("result");
            assertArrayEquals(tx.getHash(), Hex.parse(obj.getString("hash")));
        } finally {
            // Reset the API server
            teardown();
            setup();
        }
    }

    @Test
    public void testSendTransaction() throws IOException, InterruptedException {
        Transaction tx = createTransaction();

        String uri = "/send_transaction?raw=" + Hex.encode(tx.toBytes());
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));

        Thread.sleep(200);
        List<Transaction> list = api.pendingMgr.getQueue();
        assertFalse(list.isEmpty());
        assertArrayEquals(list.get(list.size() - 1).getHash(), tx.getHash());
    }

    @Test
    public void testGetAccount() throws IOException {
        Genesis gen = Genesis.getInstance();
        Entry<ByteArray, Premine> entry = gen.getPremines().entrySet().iterator().next();

        String uri = "/get_account?address=" + entry.getKey();
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertEquals(entry.getValue().getAmount(),
                response.getJsonObject("result").getJsonNumber("available").longValueExact());
    }

    @Test
    public void testGetDelegate() throws IOException {
        Genesis gen = Genesis.getInstance();
        Entry<String, byte[]> entry = gen.getDelegates().entrySet().iterator().next();

        String uri = "/get_delegate?address=" + Hex.encode(entry.getValue());
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertEquals(entry.getKey(), response.getJsonObject("result").getString("name"));
    }

    @Test
    public void testGetDelegates() throws IOException {
        String uri = "/get_delegates";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertTrue(response.getJsonArray("result").size() > 0);
    }

    @Test
    public void testGetValidators() throws IOException {
        String uri = "/get_validators";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertTrue(response.getJsonArray("result").size() > 0);
    }

    @Test
    public void testGetVote() throws IOException {
        EdDSA key = new EdDSA();
        EdDSA key2 = new EdDSA();
        DelegateState ds = api.chain.getDelegateState();
        ds.register(key2.toAddress(), Bytes.of("test"));
        ds.vote(key.toAddress(), key2.toAddress(), 200L);

        String uri = "/get_vote?voter=" + key.toAddressString() + "&delegate=" + key2.toAddressString();
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertEquals(200L, response.getJsonNumber("result").longValueExact());
    }

    @Test
    public void testGetAccounts() throws IOException {
        String uri = "/list_accounts?password=" + password;
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertNotNull(response.getJsonArray("result"));
    }

    @Test
    public void testCreateAccount() throws IOException {
        int size = wallet.getAccounts().size();

        String uri = "/create_account?password=" + password;
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertEquals(size + 1, wallet.getAccounts().size());
    }

    @Test
    public void testTransfer() throws IOException, InterruptedException {
        EdDSA key = new EdDSA();
        String uri = "/transfer?&from=" + wallet.getAccount(0).toAddressString() + "&to=" + key.toAddressString()
                + "&value=1000000000&fee=" + Config.MIN_TRANSACTION_FEE + "&data=test";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertNotNull(response.getString("result"));

        Thread.sleep(200);

        List<Transaction> list = api.pendingMgr.getTransactions();
        assertFalse(list.isEmpty());
        assertArrayEquals(list.get(list.size() - 1).getHash(), Hex.parse(response.getString("result")));
        assertEquals(list.get(list.size() - 1).getType(), TransactionType.TRANSFER);
    }

    @Test
    public void testDelegate() throws IOException, InterruptedException {
        String uri = "/delegate?&from=" + wallet.getAccount(0).toAddressString() + "&fee=" + Config.MIN_TRANSACTION_FEE
                + "&data=" + Hex.encode(Bytes.of("test_delegate"));
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertNotNull(response.getString("result"));

        Thread.sleep(200);

        List<Transaction> list = api.pendingMgr.getTransactions();
        assertFalse(list.isEmpty());
        assertArrayEquals(list.get(list.size() - 1).getHash(), Hex.parse(response.getString("result")));
        assertEquals(list.get(list.size() - 1).getType(), TransactionType.DELEGATE);
    }

    @Test
    public void testVote() throws IOException, InterruptedException {
        EdDSA delegate = new EdDSA();
        ds.register(delegate.toAddress(), Bytes.of("test_vote"));

        String uri = "/vote?&from=" + wallet.getAccount(0).toAddressString() + "&to=" + delegate.toAddressString()
                + "&value=1000000000&fee=50000000";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertNotNull(response.getString("result"));

        Thread.sleep(200);

        List<Transaction> list = api.pendingMgr.getTransactions();
        assertFalse(list.isEmpty());
        assertArrayEquals(list.get(0).getHash(), Hex.parse(response.getString("result")));
        assertEquals(list.get(0).getType(), TransactionType.VOTE);
    }

    @Test
    public void testUnvote() throws IOException, InterruptedException {
        EdDSA delegate = new EdDSA();
        ds.register(delegate.toAddress(), Bytes.of("test_unvote"));

        long amount = 1000000000;
        byte[] voter = wallet.getAccounts().get(0).toAddress();
        as.adjustLocked(voter, amount);
        ds.vote(voter, delegate.toAddress(), amount);

        String uri = "/unvote?&from=" + wallet.getAccount(0).toAddressString() + "&to=" + delegate.toAddressString()
                + "&value=" + amount + "&fee=50000000";
        JsonObject response = request(uri);
        assertTrue(response.getBoolean("success"));
        assertNotNull(response.getString("result"));

        Thread.sleep(200);

        List<Transaction> list = api.pendingMgr.getTransactions();
        assertFalse(list.isEmpty());
        assertArrayEquals(list.get(list.size() - 1).getHash(), Hex.parse(response.getString("result")));
        assertEquals(list.get(list.size() - 1).getType(), TransactionType.UNVOTE);
    }

    @AfterClass
    public static void teardown() throws IOException {
        api.stop();
        if (wallet.exists()) {
            wallet.delete();
        }
    }

    private Block createBlock(Blockchain chain, List<Transaction> transactions, List<TransactionResult> results) {
        EdDSA key = new EdDSA();

        long number = chain.getLatestBlockNumber() + 1;
        byte[] coinbase = key.toAddress();
        byte[] prevHash = chain.getLatestBlockHash();
        long timestamp = System.currentTimeMillis();
        byte[] transactionsRoot = MerkleUtil.computeTransactionsRoot(transactions);
        byte[] resultsRoot = MerkleUtil.computeResultsRoot(results);
        byte[] stateRoot = Bytes.EMPTY_HASH;
        byte[] data = {};

        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        return new Block(header, transactions, results);
    }

    private Transaction createTransaction() {
        EdDSA key = new EdDSA();

        TransactionType type = TransactionType.TRANSFER;
        byte[] to = key.toAddress();
        long value = 0;
        long fee = 0;
        long nonce = 1;
        long timestamp = System.currentTimeMillis();
        byte[] data = {};

        Transaction tx = new Transaction(type, to, value, fee, nonce, timestamp, data);
        tx.sign(key);

        return tx;
    }
}
