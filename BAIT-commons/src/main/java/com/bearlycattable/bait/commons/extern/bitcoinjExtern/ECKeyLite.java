package com.bearlycattable.bait.commons.extern.bitcoinjExtern;

import java.math.BigInteger;
import java.util.Objects;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.commons.extern.guavaExtern.UtilsGuava;

public class ECKeyLite {

    // The parameters of the secp256k1 curve that Bitcoin uses.
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    /** The parameters of the secp256k1 curve that Bitcoin uses. */
    public static final ECDomainParameters CURVE;

    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
     * sure what this is about, you can ignore it.
     */
    public static final BigInteger HALF_CURVE_ORDER;

    static {
        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
        FixedPointUtil.precompute(CURVE_PARAMS.getG());
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
        // secureRandom = new SecureRandom();
    }
    @Nullable protected final BigInteger priv;  // A field element.
    protected final LazyECPoint pub;
    private byte[] pubKeyHash;

    public ECKeyLite(@Nullable BigInteger priv, LazyECPoint pub) {
        if (priv != null) {
            UtilsGuava.checkArgument(priv.bitLength() <= 32 * 8, "private key exceeds 32 bytes: %s bits", priv.bitLength());
            // Try and catch buggy callers or bad key imports, etc. Zero and one are special because these are often
            // used as sentinel values and because scripting languages have a habit of auto-casting true and false to
            // 1 and 0 or vice-versa. Type confusion bugs could therefore result in private keys with these values.
            UtilsGuava.checkArgument(!priv.equals(BigInteger.ZERO));
            UtilsGuava.checkArgument(!priv.equals(BigInteger.ONE));
        }
        this.priv = priv;
        this.pub = Objects.requireNonNull(pub);
    }

    /**
     * Returns public key point from the given private key. To convert a byte array into a BigInteger,
     * use {@code new BigInteger(1, bytes);}
     */
    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order,
         * but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    /** Gets the hash160 form of the public key (as seen in addresses). */
    public byte[] getPubKeyHash() {
        if (pubKeyHash == null)
            pubKeyHash = Utils.sha256hash160(this.pub.getEncoded());
        return pubKeyHash;
    }
}
