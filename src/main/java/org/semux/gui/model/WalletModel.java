/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui.model;

import static org.semux.core.Amount.ZERO;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.semux.core.Amount;
import org.semux.core.Block;
import org.semux.core.SyncManager;
import org.semux.core.state.Account;
import org.semux.crypto.Key;
import org.semux.gui.Action;
import org.semux.net.Peer;
import org.semux.util.ByteArray;

/**
 * A Model stores all the data that GUI needs. The thread-safety of this class
 * is achieved by swapping pointers instead of synchronization.
 */
public class WalletModel {

    private final List<ActionListener> listeners = new CopyOnWriteArrayList<>();
    private final List<ActionListener> lockableComponents = new CopyOnWriteArrayList<>();

    private SyncManager.Progress syncProgress;

    private Block latestBlock;

    private Key coinbase;
    private Status status;

    private volatile Map<ByteArray, Integer> accountsIndex = new HashMap<>();
    private volatile List<WalletAccount> accounts = new ArrayList<>();
    private volatile List<WalletDelegate> delegates = new ArrayList<>();

    private volatile WalletDelegate primaryValidator;
    private volatile WalletDelegate nextPrimaryValidator;
    private volatile Long nextValidatorSetUpdate;

    private Map<String, Peer> activePeers = new HashMap<>();

    /**
     * Fires an model update event.
     */
    public void fireUpdateEvent() {
        updateView();
    }

    /**
     * Fires an lock event.
     */
    public void fireLockEvent() {
        lockView();
    }

    /**
     * Add a listener.
     * 
     * @param listener
     */
    public void addListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Add a component for locking.<br />
     * This component has to provide Action.LOCK as ActionListener Event
     * 
     * @param listener
     */
    public void addLockable(ActionListener listener) {
        lockableComponents.add(listener);
    }

    /**
     * Getter for property ${@link #syncProgress}.
     *
     * @return Value to set for property ${@link #syncProgress}.
     */
    public SyncManager.Progress getSyncProgress() {
        return syncProgress;
    }

    /**
     * Setter for property ${@link #syncProgress}.
     *
     * @param syncProgress
     *            Value to set for property ${@link #syncProgress}.
     */
    public void setSyncProgress(SyncManager.Progress syncProgress) {
        this.syncProgress = syncProgress;
    }

    /**
     * Get the latest block.
     * 
     * @return
     */
    public Block getLatestBlock() {
        return latestBlock;
    }

    /**
     * Set the latest block.
     * 
     * @param latestBlock
     */
    public void setLatestBlock(Block latestBlock) {
        this.latestBlock = latestBlock;
    }

    /**
     * Get the coinbase.
     * 
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }

    /**
     * Set the coinbase.
     * 
     * @param coinbase
     */
    public void setCoinbase(Key coinbase) {
        this.coinbase = coinbase;
    }

    /**
     * Returns the account status.
     * 
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the account status.
     * 
     * @param status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get the total available.
     * 
     * @return
     */
    public Amount getTotalAvailable() {
        return accounts.stream().map(Account::getAvailable).reduce(ZERO, Amount::sum);
    }

    /**
     * Get the total locked.
     * 
     * @return
     */
    public Amount getTotalLocked() {
        return accounts.stream().map(Account::getLocked).reduce(ZERO, Amount::sum);
    }

    public List<WalletAccount> getAccounts() {
        return accounts;
    }

    public int getAccountNumber(byte[] address) {
        Integer n = accountsIndex.get(ByteArray.of(address));
        return n == null ? -1 : n;
    }

    public WalletAccount getAccount(byte[] address) {
        int accountNum = getAccountNumber(address);
        return accountNum >= 0 ? accounts.get(accountNum) : null;
    }

    public void setAccounts(List<WalletAccount> accounts) {
        Map<ByteArray, Integer> map = new HashMap<>();
        for (int i = 0; i < accounts.size(); i++) {
            map.put(ByteArray.of(accounts.get(i).getKey().toAddress()), i);
        }
        this.accounts = accounts;
        this.accountsIndex = map;
    }

    public List<WalletDelegate> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<WalletDelegate> delegates) {
        this.delegates = delegates;
    }

    public Map<String, Peer> getActivePeers() {
        return activePeers;
    }

    public void setActivePeers(Map<String, Peer> activePeers) {
        this.activePeers = activePeers;
    }

    /**
     * Getter for property 'primaryValidator'.
     *
     * @return Value for property 'primaryValidator'.
     */
    public Optional<WalletDelegate> getPrimaryValidator() {
        return Optional.ofNullable(primaryValidator);
    }

    /**
     * Setter for property 'primaryValidator'.
     *
     * @param primaryValidator
     *            Value to set for property 'primaryValidator'.
     */
    public void setPrimaryValidator(WalletDelegate primaryValidator) {
        this.primaryValidator = primaryValidator;
    }

    /**
     * Getter for property 'nextPrimaryValidator'.
     *
     * @return Value for property 'nextPrimaryValidator'.
     */
    public Optional<WalletDelegate> getNextPrimaryValidator() {
        return Optional.ofNullable(nextPrimaryValidator);
    }

    /**
     * Setter for property 'nextPrimaryValidator'.
     *
     * @param nextPrimaryValidator
     *            Value to set for property 'nextPrimaryValidator'.
     */
    public void setNextPrimaryValidator(WalletDelegate nextPrimaryValidator) {
        this.nextPrimaryValidator = nextPrimaryValidator;
    }

    /**
     * Getter for property 'nextValidatorSetUpdate'.
     *
     * @return Value for property 'nextValidatorSetUpdate'.
     */
    public Optional<Long> getNextValidatorSetUpdate() {
        return Optional.ofNullable(nextValidatorSetUpdate);
    }

    /**
     * Setter for property 'nextValidatorSetUpdate'.
     *
     * @param nextValidatorSetUpdate
     *            Value to set for property 'nextValidatorSetUpdate'.
     */
    public void setNextValidatorSetUpdate(Long nextValidatorSetUpdate) {
        this.nextValidatorSetUpdate = nextValidatorSetUpdate;
    }

    /**
     * Updates MVC view.
     */
    protected void updateView() {
        for (ActionListener listener : listeners) {
            EventQueue.invokeLater(() -> listener.actionPerformed(new ActionEvent(this, 0, Action.REFRESH.name())));
        }
    }

    /**
     * Locks components.
     */
    protected void lockView() {
        for (ActionListener listener : lockableComponents) {
            EventQueue.invokeLater(() -> listener.actionPerformed(new ActionEvent(this, 0, Action.LOCK.name())));
        }
    }

    public enum Status {
        NORMAL, DELEGATE, VALIDATOR
    }
}
