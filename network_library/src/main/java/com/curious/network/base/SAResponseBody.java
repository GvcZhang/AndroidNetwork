package com.curious.network.base;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class SAResponseBody {

    public abstract String contentType();

    public abstract long contentLength();

    public abstract InputStream byteStream();

    private byte[] tmpData;

    /**
     * 将 InputStream 流转换成字符串并且关闭流
     *
     * @return string result
     * @throws IOException
     */
    public final String string() throws IOException {
        byte[] tmp = bytes();
        //此处 charset 需要根据 contentType 中解析出来
        return new String(tmp, Charset.forName("utf-8"));
    }

    /**
     * 从 InputStream 流中获取字节数组并且关闭流
     *
     * @return byte array result
     * @throws IOException
     */
    public final byte[] bytes() throws IOException {
        if (tmpData == null) {
            long contentLength = contentLength();
            if (contentLength > Integer.MAX_VALUE) {
                throw new IOException("Cannot buffer entire body for content length: " + contentLength);
            }
            InputStream inputStream = byteStream();
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024];
                int length;
                while ((length = inputStream.read(tmp)) != -1) {
                    baos.write(tmp, 0, length);
                }
                return tmpData = baos.toByteArray();
            } finally {
                closeQuietly(baos);
                closeQuietly(inputStream);
            }
        } else {
            return tmpData;
        }
    }

    /**
     * 自己处理完 InputStream 流以后，需要调用 close 关闭
     */
    public void close() {
        closeQuietly(byteStream());
    }

    public static SAResponseBody create(final String contentType, final int contentLength, final InputStream stream) {
        return new SAResponseBody() {
            @Override
            public String contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public InputStream byteStream() {
                return stream;
            }
        };
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignored) {
                //do nothing now
            }
        }
    }
}
