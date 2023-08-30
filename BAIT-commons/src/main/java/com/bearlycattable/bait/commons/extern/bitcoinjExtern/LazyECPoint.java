/*
 * Copyright by the original author or authors.
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

package com.bearlycattable.bait.commons.extern.bitcoinjExtern;

import java.util.Arrays;
import java.util.Objects;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A wrapper around ECPoint that delays decoding of the point for as long as possible. This is useful because point
 * encode/decode in Bouncy Castle is quite slow especially on Dalvik, as it often involves decompression/recompression.
 */
public class LazyECPoint {
    // If curve is set, bits is also set. If curve is unset, point is set and bits is unset. Point can be set along
    // with curve and bits when the cached form has been accessed and thus must have been converted.

    private final ECCurve curve;
    private final byte[] bits;
    private final boolean compressed;

    // This field is effectively final - once set it won't change again. However it can be set after
    // construction.
    @Nullable
    private ECPoint point;

    // /**
    //  * Construct a LazyECPoint from a public key. Due to the delayed decoding of the point the validation of the
    //  * public key is delayed too, e.g. until a getter is called.
    //  *
    //  * @param curve a curve the point is on
    //  * @param bits  public key bytes
    //  */
    // public LazyECPoint(ECCurve curve, byte[] bits) {
    //     this.curve = curve;
    //     this.bits = bits;
    //     this.compressed = ECKey.isPubKeyCompressed(bits);
    // }

    /**
     * Construct a LazyECPoint from an already decoded point.
     *
     * @param point      the wrapped point
     * @param compressed true if the represented public key is compressed
     */
    public LazyECPoint(ECPoint point, boolean compressed) {
        this.point = Objects.requireNonNull(point).normalize();
        this.compressed = compressed;
        this.curve = null;
        this.bits = null;
    }

    public ECPoint get() {
        if (point == null)
            point = curve.decodePoint(bits);
        return point;
    }

    public byte[] getEncoded() {
        if (bits != null)
            return Arrays.copyOf(bits, bits.length);
        else
            return get().getEncoded(compressed);
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isValid() {
        return get().isValid();
    }

    public boolean equals(ECPoint other) {
        return get().equals(other);
    }

    public byte[] getEncoded(boolean compressed) {
        if (compressed == isCompressed() && bits != null)
            return Arrays.copyOf(bits, bits.length);
        else
            return get().getEncoded(compressed);
    }

    public ECPoint add(ECPoint b) {
        return get().add(b);
    }

    public ECPoint normalize() {
        return get().normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(getCanonicalEncoding(), ((LazyECPoint)o).getCanonicalEncoding());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCanonicalEncoding());
    }

    private byte[] getCanonicalEncoding() {
        return getEncoded(true);
    }
}
