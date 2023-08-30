package com.bearlycattable.bait.commons.extern.guavaExtern;

import static java.lang.Math.abs;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Queue;

public class UtilsGuava {

    private static final int BUFFER_SIZE = 8192;

    /** Max array length on JVM. */
    private static final int MAX_ARRAY_LEN = Integer.MAX_VALUE - 8;

    /** Large enough to never need to expand, given the geometric progression of buffer sizes. */
    private static final int TO_BYTE_ARRAY_DEQUE_SIZE = 20;

    /*
     * Copyright (C) 2008 The Guava Authors
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
     * in compliance with the License. You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software distributed under the License
     * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
     * or implied. See the License for the specific language governing permissions and limitations under
     * the License.
     */

    //Methods from Ints class
    /**
     * Returns the {@code int} value whose byte representation is the given 4 bytes, in big-endian
     * order; equivalent to {@code Ints.fromByteArray(new byte[] {b1, b2, b3, b4})}.
     *
     * @since 7.0
     */
    public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    // /**
    //  * Returns the {@code int} value that is equal to {@code value}, if possible.
    //  *
    //  * @param value any value in the range of the {@code int} type
    //  * @return the {@code int} value that equals {@code value}
    //  * @throws IllegalArgumentException if {@code value} is greater than {@link Integer#MAX_VALUE} or
    //  *     less than {@link Integer#MIN_VALUE}
    //  */
    // public static int checkedCast(long value) {
    //     int result = (int) value;
    //     checkArgument(result == value, "Out of range: %s", value);
    //     return result;
    // }

        /**
         * Returns the {@code int} nearest in value to {@code value}.
         *
         * @param value any {@code long} value
         * @return the same value cast to {@code int} if it is in the range of the {@code int} type,
         *     {@link Integer#MAX_VALUE} if it is too large, or {@link Integer#MIN_VALUE} if it is too
         *     small
         */
        public static int saturatedCast(long value) {
            if (value > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (value < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
            return (int) value;
        }

    //Methods from ByteStreams class
    /**
     * Reads all bytes from an input stream into a byte array. Does not close the stream.
     *
     * @param in the input stream to read from
     * @return a byte array containing all the bytes from the stream
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        checkNotNull(in);
        return toByteArrayInternal(in, new ArrayDeque<byte[]>(TO_BYTE_ARRAY_DEQUE_SIZE), 0);
    }

    /**
     * Returns a byte array containing the bytes from the buffers already in {@code bufs} (which have
     * a total combined length of {@code totalLen} bytes) followed by all bytes remaining in the given
     * input stream.
     */
    private static byte[] toByteArrayInternal(InputStream in, Queue<byte[]> bufs, int totalLen)
            throws IOException {
        // Starting with an 8k buffer, double the size of each successive buffer. Buffers are retained
        // in a deque so that there's no copying between buffers while reading and so all of the bytes
        // in each new allocated buffer are available for reading from the stream.
        for (int bufSize = BUFFER_SIZE;
             totalLen < MAX_ARRAY_LEN;
             bufSize = saturatedMultiply(bufSize, 2)) {
            byte[] buf = new byte[Math.min(bufSize, MAX_ARRAY_LEN - totalLen)];
            bufs.add(buf);
            int off = 0;
            while (off < buf.length) {
                // always OK to fill buf; its size plus the rest of bufs is never more than MAX_ARRAY_LEN
                int r = in.read(buf, off, buf.length - off);
                if (r == -1) {
                    return combineBuffers(bufs, totalLen);
                }
                off += r;
                totalLen += r;
            }
        }

        // read MAX_ARRAY_LEN bytes without seeing end of stream
        if (in.read() == -1) {
            // oh, there's the end of the stream
            return combineBuffers(bufs, MAX_ARRAY_LEN);
        } else {
            throw new OutOfMemoryError("input is too large to fit in a byte array");
        }
    }

    private static byte[] combineBuffers(Queue<byte[]> bufs, int totalLen) {
        byte[] result = new byte[totalLen];
        int remaining = totalLen;
        while (remaining > 0) {
            byte[] buf = bufs.remove();
            int bytesToCopy = Math.min(remaining, buf.length);
            int resultOffset = totalLen - remaining;
            System.arraycopy(buf, 0, result, resultOffset, bytesToCopy);
            remaining -= bytesToCopy;
        }
        return result;
    }

    /**
     * Copies all bytes from the input stream to the output stream. Does not close or flush either
     * stream.
     *
     * @param from the input stream to read from
     * @param to the output stream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */

    public static long copy(InputStream from, OutputStream to) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        byte[] buf = createBuffer();
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    /** Creates a new byte array for buffering reads or writes. */
    static byte[] createBuffer() {
        return new byte[BUFFER_SIZE];
    }

    // //Methods from BaseEncoding class
    // /** Encodes the specified byte array, and returns the encoded {@code String}. */
    // public String encode(byte[] bytes) {
    //     return encode(bytes, 0, bytes.length);
    // }
    //
    // /**
    //  * Encodes the specified range of the specified byte array, and returns the encoded {@code
    //  * String}.
    //  */
    // public final String encode(byte[] bytes, int off, int len) {
    //     checkPositionIndexes(off, off + len, bytes.length);
    //     StringBuilder result = new StringBuilder(maxEncodedSize(len));
    //     try {
    //         encodeTo(result, bytes, off, len);
    //     } catch (IOException impossible) {
    //         throw new AssertionError(impossible);
    //     }
    //     return result.toString();
    // }

    //Variables from IntMath
    /** The biggest half power of two that can fit in an unsigned int. */
    static final int MAX_POWER_OF_SQRT2_UNSIGNED = 0xB504F333;

    //Methods from IntMath
    /**
     * Returns the result of dividing {@code p} by {@code q}, rounding using the specified {@code
     * RoundingMode}.
     *
     * @throws ArithmeticException if {@code q == 0}, or if {@code mode == UNNECESSARY} and {@code a}
     *     is not an integer multiple of {@code b}
     */
    @SuppressWarnings("fallthrough")
    public static int divide(int p, int q, RoundingMode mode) {
        checkNotNull(mode);
        if (q == 0) {
            throw new ArithmeticException("/ by zero"); // for GWT
        }
        int div = p / q;
        int rem = p - q * div; // equal to p % q

        if (rem == 0) {
            return div;
        }

        /*
         * Normal Java division rounds towards 0, consistently with RoundingMode.DOWN. We just have to
         * deal with the cases where rounding towards 0 is wrong, which typically depends on the sign of
         * p / q.
         *
         * signum is 1 if p and q are both nonnegative or both negative, and -1 otherwise.
         */
        int signum = 1 | ((p ^ q) >> (Integer.SIZE - 1));
        boolean increment;
        switch (mode) {
            case UNNECESSARY:
                MathPreconditions.checkRoundingUnnecessary(rem == 0);
                // fall through
            case DOWN:
                increment = false;
                break;
            case UP:
                increment = true;
                break;
            case CEILING:
                increment = signum > 0;
                break;
            case FLOOR:
                increment = signum < 0;
                break;
            case HALF_EVEN:
            case HALF_DOWN:
            case HALF_UP:
                int absRem = abs(rem);
                int cmpRemToHalfDivisor = absRem - (abs(q) - absRem);
                // subtracting two nonnegative ints can't overflow
                // cmpRemToHalfDivisor has the same sign as compare(abs(rem), abs(q) / 2).
                if (cmpRemToHalfDivisor == 0) { // exactly on the half mark
                    increment = (mode == HALF_UP || (mode == HALF_EVEN & (div & 1) != 0));
                } else {
                    increment = cmpRemToHalfDivisor > 0; // closer to the UP value
                }
                break;
            default:
                throw new AssertionError();
        }
        return increment ? div + signum : div;
    }

    /**
     * Returns the base-2 logarithm of {@code x}, rounded according to the specified rounding mode.
     *
     * @throws IllegalArgumentException if {@code x <= 0}
     * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and {@code x}
     *     is not a power of two
     */
    @SuppressWarnings("fallthrough")
    // TODO(kevinb): remove after this warning is disabled globally
    public static int log2(int x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        switch (mode) {
            case UNNECESSARY:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
                // fall through
            case DOWN:
            case FLOOR:
                return (Integer.SIZE - 1) - Integer.numberOfLeadingZeros(x);
            case UP:
            case CEILING:
                return Integer.SIZE - Integer.numberOfLeadingZeros(x - 1);
            case HALF_DOWN:
            case HALF_UP:
            case HALF_EVEN:
                // Since sqrt(2) is irrational, log2(x) - logFloor cannot be exactly 0.5
                int leadingZeros = Integer.numberOfLeadingZeros(x);
                int cmp = MAX_POWER_OF_SQRT2_UNSIGNED >>> leadingZeros;
                // floor(2^(logFloor + 0.5))
                int logFloor = (Integer.SIZE - 1) - leadingZeros;
                return logFloor + lessThanBranchFree(cmp, x);

            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns 1 if {@code x < y} as unsigned integers, and 0 otherwise. Assumes that x - y fits into
     * a signed int. The implementation is branch-free, and benchmarks suggest it is measurably (if
     * narrowly) faster than the straightforward ternary expression.
     */

    static int lessThanBranchFree(int x, int y) {
        // The double negation is optimized away by normal Java, but is necessary for GWT
        // to make sure bit twiddling works as expected.
        return ~~(x - y) >>> (Integer.SIZE - 1);
    }

    /**
     * Returns {@code true} if {@code x} represents a power of two.
     *
     * <p>This differs from {@code Integer.bitCount(x) == 1}, because {@code
     * Integer.bitCount(Integer.MIN_VALUE) == 1}, but {@link Integer#MIN_VALUE} is not a power of two.
     */
    public static boolean isPowerOfTwo(int x) {
        return x > 0 & (x & (x - 1)) == 0;
    }

    /**
     * Returns the product of {@code a} and {@code b} unless it would overflow or underflow in which
     * case {@code Integer.MAX_VALUE} or {@code Integer.MIN_VALUE} is returned, respectively.
     *
     * @since 20.0
     */

    public static int saturatedMultiply(int a, int b) {
        return saturatedCast((long) a * b);
    }

    //Variables from Ascii class
    /**
     * The maximum value of an ASCII character.
     *
     * @since 9.0 (was type {@code int} before 12.0)
     */
    public static final char MAX = 127;

    /** A bit mask which selects the bit encoding ASCII character case. */
    private static final char CASE_MASK = 0x20;

    //Methods from Ascii class
    /**
     * Indicates whether {@code c} is one of the twenty-six lowercase ASCII alphabetic characters
     * between {@code 'a'} and {@code 'z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isLowerCase(char c) {
        // Note: This was benchmarked against the alternate expression "(char)(c - 'a') < 26" (Nov '13)
        // and found to perform at least as well, or better.
        return (c >= 'a') && (c <= 'z');
    }

    /**
     * If the argument is an {@linkplain #isUpperCase(char) uppercase ASCII character}, returns the
     * lowercase equivalent. Otherwise returns the argument.
     */
    public static char toLowerCase(char c) {
        return isUpperCase(c) ? (char) (c ^ CASE_MASK) : c;
    }

    /**
     * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
     * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isUpperCase(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    /**
     * If the argument is a {@linkplain #isLowerCase(char) lowercase ASCII character}, returns the
     * uppercase equivalent. Otherwise returns the argument.
     */
    public static char toUpperCase(char c) {
        return isLowerCase(c) ? (char) (c ^ CASE_MASK) : c;
    }

    //Variables from UnsignedLongs
    //Methods from UnsignedLongs
    /**
     * Compares the two specified {@code long} values, treating them as unsigned values between {@code
     * 0} and {@code 2^64 - 1} inclusive.
     *
     * <p><b>Java 8 users:</b> use {@link Long#compareUnsigned(long, long)} instead.
     *
     * @param a the first unsigned {@code long} to compare
     * @param b the second unsigned {@code long} to compare
     * @return a negative value if {@code a} is less than {@code b}; a positive value if {@code a} is
     *     greater than {@code b}; or zero if they are equal
     */
    public static int compareAsUnsignedLongs(long a, long b) {
        return Long.compare(flip(a), flip(b));
    }

    /**
     * A (self-inverse) bijection which converts the ordering on unsigned longs to the ordering on
     * longs, that is, {@code a <= b} as unsigned longs if and only if {@code flip(a) <= flip(b)} as
     * signed longs.
     */
    private static long flip(long a) {
        return a ^ Long.MIN_VALUE;
    }

    //Methods from Preconditions
    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * <p>See {#checkArgument(boolean, String, Object...)} for details.
     *
     * @since 20.0 (varargs overload since 2.0)
     */
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1) {
        if (!b) {
            throw new IllegalArgumentException(StringsG.lenientFormat(errorMessageTemplate, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * <p>See {#checkArgument(boolean, String, Object...)} for details.
     *
     * @since 20.0 (varargs overload since 2.0)
     */
    public static void checkArgument(boolean b, String errorMessageTemplate, char p1) {
        if (!b) {
            throw new IllegalArgumentException(StringsG.lenientFormat(errorMessageTemplate, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * <p>See {#checkArgument(boolean, String, Object...)} for details.
     *
     * @since 20.0 (varargs overload since 2.0)
     */
    public static void checkArgument(boolean b, String errorMessageTemplate, int p1) {
        if (!b) {
            throw new IllegalArgumentException(StringsG.lenientFormat(errorMessageTemplate, p1));
        }
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     * //@see Verify#verifyNotNull Verify.verifyNotNull()
     */

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance, but not
     * involving any parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if {@code expression} is false
     * //@see Verify#verify Verify.verify()
     */
    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures that {@code start} and {@code end} specify valid <i>positions</i> in an array, list or
     * string of size {@code size}, and are in order. A position index may range from zero to {@code
     * size}, inclusive.
     *
     * @param start a user-supplied index identifying a starting position in an array, list or string
     * @param end a user-supplied index identifying an ending position in an array, list or string
     * @param size the size of that array, list or string
     * @throws IndexOutOfBoundsException if either index is negative or is greater than {@code size},
     *     or if {@code end} is less than {@code start}
     * @throws IllegalArgumentException if {@code size} is negative
     */
    public static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        // end < start
        return StringsG.lenientFormat("end index (%s) must not be less than start index (%s)", end, start);
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return StringsG.lenientFormat("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index > size
            return StringsG.lenientFormat("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }

}
