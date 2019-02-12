package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.dataformat.bencode.context.StreamOutputContext;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class BEncodeFactory extends JsonFactory {
    /**
     * Name used to identify JSON format
     * (and returned by {@link #getFormatName()}
     */
    public final static String FORMAT_NAME_JSON = "BEncode";

    public BEncodeFactory() {
        this(null);
    }

    public BEncodeFactory(ObjectCodec oc) {
        super(oc);
    }

    public BEncodeFactory(BEncodeFactory src, ObjectCodec codec) {
        super(src, codec);
    }

    @Override
    public BEncodeFactory copy() {
        _checkInvalidCopy(BEncodeFactory.class);
        return new BEncodeFactory(this, null);
    }

    @Override
    protected Object readResolve() {
        return new BEncodeFactory(this, _objectCodec);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public boolean canHandleBinaryNatively() {
        return true;
    }

    @Override
    public String getFormatName() {
        return FORMAT_NAME_JSON;
    }

    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException {
        // TODO implement according to com.fasterxml.jackson.core.json.ByteSourceJsonBootstrapper.hasJSONFormat()
        return MatchStrength.INCONCLUSIVE;
//        if (!acc.hasMoreBytes()) {
//            return MatchStrength.INCONCLUSIVE;
//        }
    }

    @Override
    public boolean canUseSchema(FormatSchema schema) {
        return super.canUseSchema(schema);
    }

    @Override
    public BEncodeGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        return new BEncodeGenerator(0, _objectCodec, new StreamOutputContext(out, Charset.forName(enc.getJavaName()))); // TODO handle features
    }

    @Override
    public BEncodeGenerator createGenerator(OutputStream out) throws IOException {
        return createGenerator(out, JsonEncoding.UTF8);
    }

    @Override
    public BEncodeGenerator createGenerator(Writer out) throws IOException {
        throw new UnsupportedOperationException("BEncode doesn't support writer");
    }

    @Override
    public BEncodeGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
        OutputStream os = new FileOutputStream(f); // , enc.getJavaName())
        return createGenerator(os, enc);
    }

    @Override
    public JsonParser createParser(InputStream in) throws IOException {
        return new BEncodeParser(in, _objectCodec);
    }

    public JsonParser createParser(File f) throws IOException {
        return createParser(new FileInputStream(f));
    }

    @Override
    public JsonParser createParser(Reader r) throws IOException {
        throw new UnsupportedOperationException("BEncode doesn't support reader");
    }

    @Override
    public JsonParser createParser(String content) throws IOException {
        return super.createParser(content.getBytes(BEncodeFormat.LATIN_1));
    }

    @Override
    public JsonParser createParser(byte[] data, int offset, int len) throws IOException {
        return createParser(new ByteArrayInputStream(data, offset, len));
    }

    @Override
    public JsonParser createParser(URL url) throws IOException {
        return super.createParser(_optimizedStreamFromURL(url));
    }

    @Override
    public JsonParser createParser(byte[] data) throws IOException {
        return createParser(data, 0, data.length);
    }
}
