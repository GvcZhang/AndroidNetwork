package com.curious.network.base.internal;

import android.os.Build;


import com.curious.network.base.SAHeader;
import com.curious.network.base.SAHttpClient;
import com.curious.network.base.SAInterceptor;
import com.curious.network.base.SARequest;
import com.curious.network.base.SARequestBody;
import com.curious.network.base.SAResponse;
import com.curious.network.base.SAResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * 网络请求拦截器，需要作为最后一个，实现闭环操作
 */
public class ConnectInterceptor implements SAInterceptor {
    private SAHttpClient client;

    public ConnectInterceptor(SAHttpClient client) {
        this.client = client;
    }

    @Override
    public SAResponse intercept(Chain chain) throws IOException {
        SARequest request = chain.request();
        SAResponse response = sendHttpRequest(request);
        return response;
    }

    /**
     * 实际发送网络请求
     *
     * @param originalRequest request
     * @return SAResponse
     * @throws IOException
     */
    private SAResponse sendHttpRequest(SARequest originalRequest) throws IOException {
        HttpURLConnection connection;
        URL url = originalRequest.url().url();
        if (client.proxy() != null) {
            connection = (HttpURLConnection) url.openConnection(client.proxy());
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        if (connection == null) {
            throw new IllegalStateException(String.format("can not connect %s, it shouldn't happen", url.toString()));
        }
        if (originalRequest.isHttps() && client.sslSocketFactory() != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(client.sslSocketFactory());
        }

        connection.setReadTimeout(client.readTimeout());
        connection.setConnectTimeout(client.connectTimeout());
        connection.setInstanceFollowRedirects(client.isUrlConnectionFollowRedirects());

        // headers about
        // try to fix bug: accidental EOFException before API 19
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            connection.setRequestProperty("Connection", "close");
        }
        List<SAHeader> headerList = originalRequest.headers();
        for (SAHeader header : headerList) {
            if (header.isSetHeader()) {
                connection.setRequestProperty(header.getName(), header.getValue());
            } else {
                connection.addRequestProperty(header.getName(), header.getValue());
            }
        }
        try {
            connection.setRequestMethod(originalRequest.method().toString());
        } catch (ProtocolException ex) {
            try { // fix: HttpURLConnection not support PATCH method.
                Field methodField = HttpURLConnection.class.getDeclaredField("method");
                methodField.setAccessible(true);
                methodField.set(connection, originalRequest.method().toString());
            } catch (Throwable ignored) {
                throw ex;
            }
        }
        SARequestBody requestBody = originalRequest.body();
        connection.setRequestProperty("Content-Type", requestBody.contentType());
        // cookie about TODO 暂未实现
        if (SARequest.HttpMethod.permitsRequestBody(originalRequest.method())) {
            long contentLength = requestBody.contentLength();
            if (contentLength < 0) {
                connection.setChunkedStreamingMode(256 * 1024);
            } else {
                if (contentLength < Integer.MAX_VALUE) {
                    connection.setFixedLengthStreamingMode((int) contentLength);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    connection.setFixedLengthStreamingMode(contentLength);
                } else {
                    connection.setChunkedStreamingMode(256 * 1024);
                }
            }
            connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            requestBody.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        }

        SAResponse.Builder responseBuilder = new SAResponse.Builder().code(connection.getResponseCode()).headers(wrapHeaders(connection.getHeaderFields()));
        responseBuilder.request(originalRequest);
        responseBuilder.body(SAResponseBody.create("not implement content type", connection.getContentLength(), getInputStream(connection)));
        responseBuilder.message("no message");
        return responseBuilder.build();
    }

    private InputStream getInputStream(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() >= 400 ?
                connection.getErrorStream() : connection.getInputStream();
    }

    private List<SAHeader> wrapHeaders(Map<String, List<String>> headerMap) {
        List<SAHeader> headerList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            List<String> valueList = entry.getValue();
            for (String value : valueList) {
                headerList.add(new SAHeader(entry.getKey(), value, true));
            }
        }
        return headerList;
    }
}


