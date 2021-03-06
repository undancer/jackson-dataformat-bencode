package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NumberContextTest {
    public static final Charset LATIN_1 = Charset.forName("ISO-8859-1");

    @Test
    public void testDetermineNumberLengthOnInsufficientInput() throws Exception {
        NumberContext numberContext = createNumberContext("-", false);

        try {
            numberContext.guessType();
            fail("should throw if no digits after \"-\" sign");
        } catch (JsonParseException e) {
            assertThat(e.getMessage(), startsWith("tried to guess number with insufficient input available"));
        }

        try {
            numberContext.guessType();
            fail("should throw if no input available");
        } catch (JsonParseException e) {
            assertThat(e.getMessage(), startsWith("tried to guess number with insufficient input available"));
        }
    }

    @Test
    public void testGuessType() throws Exception {
        NumberContext numberContext = createNumberContext("2147483647", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));

        numberContext = createNumberContext("-2147483648", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));

        numberContext = createNumberContext("2147483648", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("-2147483649", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("9223372036854775807xz", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("-9223372036854775808", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("9223372036854775808", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.BIG_INTEGER));

        numberContext = createNumberContext("-9223372036854775809", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.BIG_INTEGER));

        numberContext = createNumberContext("-1", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));
    }

    @Test
    public void testCompareBytes() throws Exception {
        NumberContext numberContext = createNumberContext("1234567890", true);

        assertThat(numberContext.compareBytes("1234567890".getBytes(LATIN_1), 0), is(0));
        assertThat(numberContext.compareBytes("2234567890".getBytes(LATIN_1), 0), is(1));
        assertThat(numberContext.compareBytes("1233567890".getBytes(LATIN_1), 0), is(-1));
        assertThat(numberContext.compareBytes("123456789".getBytes(LATIN_1), 0), is(0));
        assertThat(numberContext.compareBytes("23456789".getBytes(LATIN_1), 1), is(0));
        assertThat(numberContext.compareBytes("23456799".getBytes(LATIN_1), 1), is(1));
        assertThat(numberContext.compareBytes("23356799".getBytes(LATIN_1), 1), is(-1));
    }

    @Test
    public void testParseInt() throws Exception {
        NumberContext numberContext = createNumberContext("21474836476", false);
        try {
            numberContext.parseInt();
            fail("should throw if no guess performed before");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), startsWith("number size should be guessed before parse"));
        }
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        try {
            numberContext.parseInt();
            fail("should throw if parsing with lower than sufficient capacity type is attempted");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), startsWith("integer overflow"));
        }

        assertThat(numberContext.parseLong(), is(21474836476L));

        numberContext = createNumberContext("345", true);
        assertThat(numberContext.parseInt(), is(345));
        numberContext = createNumberContext("-5678", true);
        assertThat(numberContext.parseInt(), is(-5678));
        numberContext = createNumberContext("-2147483648", true);
        assertThat(numberContext.parseInt(), is(-2147483648));
    }

    @Test
    public void testParseLong() throws Exception {
        NumberContext numberContext = createNumberContext("9223372036854775807", true);
        assertThat(numberContext.parseLong(), is(9223372036854775807L));

        numberContext = createNumberContext("-9223372036854775808", true);
        assertThat(numberContext.parseLong(), is(-9223372036854775808L));
    }

    @Test
    public void testParseBigInteger() throws Exception {
        NumberContext numberContext = createNumberContext("9223372036854775808", true);
        assertThat(numberContext.parseBigInteger(), is(new BigInteger("9223372036854775808")));

        numberContext = createNumberContext(
                "45678951506897056489087656679877941321034809041089384467986411", true);
        assertThat(numberContext.parseBigInteger(),
                is(new BigInteger("45678951506897056489087656679877941321034809041089384467986411")));
    }

    NumberContext createNumberContext(String input, boolean guess) throws Exception {
        NumberContext numberContext = new NumberContext(
                new StreamInputContext(
                        new ByteArrayInputStream(input.getBytes(LATIN_1))));
        if (guess) {
            numberContext.guessType();
        }
        return numberContext;
    }
}
