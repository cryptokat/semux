/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.db;

public enum DatabaseName {

    /**
     * Block and transaction index.
     * 
     * @deprecated merged into ${@link this#BLOCK} since db version 2
     */
    INDEX,

    /**
     * Block raw data.
     */
    BLOCK,

    /**
     * Account related data.
     * 
     * @deprecated merged into ${@link this#BLOCK} since db version 2
     */
    ACCOUNT,

    /**
     * Delegate core data.
     * 
     * @deprecated merged into ${@link this#BLOCK} since db version 2
     */
    DELEGATE,

    /**
     * Delegate vote data.
     * 
     * @deprecated merged into ${@link this#BLOCK} since db version 2
     */
    VOTE
}