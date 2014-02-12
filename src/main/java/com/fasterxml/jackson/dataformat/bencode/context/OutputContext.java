package com.fasterxml.jackson.dataformat.bencode.context;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class OutputContext {
    private final Charset charset;
    private OutputStream outputStream;
    private Writer writer;

    public OutputContext(OutputStream outputStream, Charset charset) {
        this.charset = charset;
        this.outputStream = outputStream;
    }

    public Charset getCharset() {
        return charset;
    }

    public OutputStream getOutputStream() throws IOException{
        if (writer != null) {
            writer.flush();
        }
        writer = null;
        return outputStream;
    }

    public Writer getWriter() throws IOException {
        if (writer != null) return writer;
        return new BufferedWriter(new OutputStreamWriter(outputStream, charset));
    }

    public void write(String text) throws IOException {
        outputStream.write(text.getBytes(charset));
    }

    public void write(byte b) throws IOException {
        outputStream.write(b);
    }

    public void write(int i) throws IOException {
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        byte[] buf = new byte[size];
        getBytes(i, size, buf);
        write(buf);
    }

    public void write(long i) throws IOException {
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        byte[] buf = new byte[size];
        getBytes(i, size, buf);
        write(buf);
    }

    /**
     * @param i big int to be encoded
     * @return integer in base 10 as a byte array;
     */
    static byte[] getByteBuf(BigInteger i) {
        int size = (i.signum() < 0) ? stringSize(i.negate()) + 1 : stringSize(i);
        byte[] buf = new byte[size];
        getBytes(i, size, buf);
        return buf;
    }

    public void write(BigInteger i) throws IOException {
        write(getByteBuf(i));
    }

    // Requires positive x
    static int stringSize(int x) {
        for (int i=0; ; i++)
            if (x <= SIZE_TABLE[i])
                return i+1;
    }

    // Requires positive x
    static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) return i;
            p = 10 * p;
        }
        return 19;
    }

    static int stringSize(BigInteger x) {
        BigInteger p = BigInteger.TEN;
        int i = 1;
        while (p.compareTo(x) <= 0) { // TODO binarysearch?
            p = p.multiply(BigInteger.TEN);
            i++;
        }

        return i;
    }

    final static int [] SIZE_TABLE = {
            9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    final static byte[] DIGIT_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    final static byte[] DIGIT_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    final static byte[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    static void getBytes(int i, int index, byte[] buf) {
        int q, r;
        int charPos = index;

        if (i < 0) {
            buf[0] = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DIGIT_ONES[r];
            buf [--charPos] = DIGIT_TENS[r];
        }

        // Fall through to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = digits [r];
            i = q;
            if (i == 0) break;
        }
    }

    static void getBytes(long i, int index, byte[] buf) {
        long q;
        int r;
        int charPos = index;

        if (i < 0) {
            buf[0] = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] = DIGIT_TENS[r];
        }

        getBytes((int) i, charPos, buf); // inline for performance improvement?
    }

    private static BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static BigInteger HUNDRED = BigInteger.valueOf(100);

    static void getBytes(BigInteger i, int index, byte[] buf) {
        BigInteger q;
        int r;
        int charPos = index;

        if (i.signum() < 0) {
            buf[0] = '-';
            i = i.negate();
        }

        while (i.compareTo(MAX_LONG) > 0) {
            q = i.divide(HUNDRED);
            r = i.subtract(q.multiply(HUNDRED)).intValue();
            i = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] = DIGIT_TENS[r];
        }

        getBytes(i.longValue(), charPos, buf);
    }

    public void close() throws IOException {
        outputStream.close();
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void write(byte[] data, int offset, int len) throws IOException {
        outputStream.write(data, offset, len);
    }

    public void write(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }
}