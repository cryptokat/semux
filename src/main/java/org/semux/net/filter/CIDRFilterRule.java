/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.filter;

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CIDRFilterRule composes IpSubnetFilterRule and IpSingleFilterRule
 */
public class CIDRFilterRule implements IpFilterRule {

    private static final Pattern CIDR_PATTERN = Pattern
            .compile("^(?<address>[0-9.a-fA-F:]+?)(/(?<cidrPrefix>\\d{1,3}))?$");

    private static final InetAddressValidator inetAddressValidator = new InetAddressValidator();

    private final IpFilterRule ipFilterRule;

    private final IpFilterRuleType ruleType;

    public CIDRFilterRule(String cidrNotation, IpFilterRuleType ruleType) throws UnknownHostException {
        this.ruleType = ruleType;

        Matcher matcher = CIDR_PATTERN.matcher(cidrNotation);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid cidrNotation");
        }

        String address = matcher.group("address");
        if (!inetAddressValidator.isValid(address)) {
            throw new IllegalArgumentException(String.format("%s is not a valid ip address", address));
        }

        if (matcher.group("cidrPrefix") != null) {
            int cidrPrefix = Integer.parseInt(matcher.group("cidrPrefix"));
            ipFilterRule = new IpSubnetFilterRule(address, cidrPrefix, ruleType);
        } else {
            ipFilterRule = new IpSingleFilterRule(address, ruleType);
        }
    }

    @Override
    public boolean matches(InetSocketAddress remoteAddress) {
        return ipFilterRule.matches(remoteAddress);
    }

    @Override
    public IpFilterRuleType ruleType() {
        return ruleType;
    }
}
