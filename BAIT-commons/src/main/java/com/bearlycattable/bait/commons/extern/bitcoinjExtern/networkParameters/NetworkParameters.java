/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bearlycattable.bait.commons.extern.bitcoinjExtern.networkParameters;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.commons.extern.bitcoinjExtern.Sha256Hash;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a Bitcoin chain.</p>
 *
 * <p>This is an abstract class, concrete instantiations can be found in the params package. There are four:
 * one for the main network ({@link MainNetParams}), one for the public test network, and two others that are
 * intended for unit testing and local app development purposes. Although this class contains some aliases for
 * them, you are encouraged to call the static get() methods on each specific params class directly.</p>
 */
public abstract class NetworkParameters {
    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.bitcoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.bitcoin.test";
    /** The string returned by getId() for regtest mode. */
    public static final String ID_REGTEST = "org.bitcoin.regtest";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "org.bitcoinj.unittest";

    /** The string used by the payment protocol to represent the main net. */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    /** The string used by the payment protocol to represent the test net. */
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";
    /** The string used by the payment protocol to represent unit testing (note that this is non-standard). */
    public static final String PAYMENT_PROTOCOL_ID_UNIT_TESTS = "unittest";
    public static final String PAYMENT_PROTOCOL_ID_REGTEST = "regtest";

    // TODO: Seed nodes should be here as well.

    protected BigInteger maxTarget;
    protected int port;
    protected long packetMagic;  // Indicates message origin network and is used to seek to the next message when stream state is unknown.
    protected int addressHeader;
    protected int p2shHeader;
    protected int dumpedPrivateKeyHeader;
    protected String segwitAddressHrp;
    protected int interval;
    protected int targetTimespan;
    protected int bip32HeaderP2PKHpub;
    protected int bip32HeaderP2PKHpriv;
    protected int bip32HeaderP2WPKHpub;
    protected int bip32HeaderP2WPKHpriv;

    /** Used to check majorities for block version upgrade */
    protected int majorityEnforceBlockUpgrade;
    protected int majorityRejectBlockOutdated;
    protected int majorityWindow;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    protected String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    protected int spendableCoinbaseDepth;
    protected int subsidyDecreaseBlockCount;

    protected String[] dnsSeeds;
    protected int[] addrSeeds;
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<>();

    protected NetworkParameters() {
    }

    public static final int TARGET_TIMESPAN = 14 * 24 * 60 * 60;  // 2 weeks per difficulty cycle, on average.
    public static final int TARGET_SPACING = 10 * 60;  // 10 minutes per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;
    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId().equals(((NetworkParameters)o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_MAINNET)) {
            return MainNetParams.get();
        } else if (id.equals(ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (id.equals(ID_UNITTESTNET)) {
            return UnitTestParams.get();
        } else if (id.equals(ID_REGTEST)) {
            return RegTestParams.get();
        } else {
            return null;
        }
    }

    /** Returns the network parameters for the given string paymentProtocolID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
            return MainNetParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_UNIT_TESTS)) {
            return UnitTestParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_REGTEST)) {
            return RegTestParams.get();
        } else {
            return null;
        }
    }

    /** Default TCP port on which to connect to nodes. */
    public int getPort() {
        return port;
    }

    /**
     * First byte of a base58 encoded address. See {@link LegacyAddress}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /** First byte of a base58 encoded dumped private key. See {DumpedPrivateKey}. */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /** Human readable part of bech32 encoded segwit address. */
    public String getSegwitAddressHrp() {
        return segwitAddressHrp;
    }

    /** Returns the 4 byte header for BIP32 wallet P2PKH - public key part. */
    public int getBip32HeaderP2PKHpub() {
        return bip32HeaderP2PKHpub;
    }

    /** Returns the 4 byte header for BIP32 wallet P2PKH - private key part. */
    public int getBip32HeaderP2PKHpriv() {
        return bip32HeaderP2PKHpriv;
    }

    /** Returns the 4 byte header for BIP32 wallet P2WPKH - public key part. */
    public int getBip32HeaderP2WPKHpub() {
        return bip32HeaderP2WPKHpub;
    }

    /** Returns the 4 byte header for BIP32 wallet P2WPKH - private key part. */
    public int getBip32HeaderP2WPKHpriv() {
        return bip32HeaderP2WPKHpriv;
    }

    public abstract int getProtocolVersionNum(final ProtocolVersion version);

    public static enum ProtocolVersion {
        MINIMUM(70000),
        PONG(60001),
        BLOOM_FILTER(70000), // BIP37
        BLOOM_FILTER_BIP111(70011), // BIP111
        WITNESS_VERSION(70012),
        FEEFILTER(70013), // BIP133
        CURRENT(70013);

        private final int bitcoinProtocol;

        ProtocolVersion(final int bitcoinProtocol) {
            this.bitcoinProtocol = bitcoinProtocol;
        }

        public int getBitcoinProtocolVersion() {
            return bitcoinProtocol;
        }
    }
}
