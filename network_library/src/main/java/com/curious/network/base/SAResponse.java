/*
 * Created by zhangwei on 2019/08/19.
 * Copyright 2015－2019 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.curious.network.base;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * 请求的相应
 */
public final class SAResponse implements Closeable {
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_PERM_REDIRECT = 308;
    public static final int HTTP_CONTINUE = 100;

    final int code;
    final String message;
    final List<SAHeader> headers;
    final SAResponseBody body;
    final SARequest request;


    public SAResponse() {
        this(new Builder());
    }

    private SAResponse(Builder builder) {
        this.body = builder.body;
        this.code = builder.code;
        this.headers = Collections.unmodifiableList(builder.headers);
        this.message = builder.message;
        this.request = builder.request;
    }

    public List<SAHeader> headers() {
        return headers;
    }

    public List<String> headerValue(String name) {
        List<String> tmpList = new ArrayList<>();
        for (SAHeader header : this.headers) {
            if (header.getName() != null && header.getName().equals(name)) {
                tmpList.add(header.getValue());
            }
        }
        return tmpList;
    }

    public SAResponseBody body() {
        return body;
    }

    /**
     * Http Status Code
     *
     * @return
     */
    public int code() {
        return code;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was successfully received,
     * understood, and accepted.
     *
     * @return code
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * Http Status Message
     *
     * @return message
     */
    public String message() {
        return message;
    }

    /**
     * Returns true if this response redirects to another resource.
     *
     * @return boolean
     */
    public boolean isRedirect() {
        switch (code) {
            case HTTP_PERM_REDIRECT:
            case HTTP_TEMP_REDIRECT:
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void close() throws IOException {
        if (body != null) {
            body.close();
        }
    }

    @Override
    public String toString() {
        return "Response{protocol="
                + ", code="
                + code
                + ", message="
                + message
                + ", url="
                + request.url()
                + '}';
    }

    public static class Builder {
        int code;
        String message;
        List<SAHeader> headers = new ArrayList<>();
        SAResponseBody body;
        SARequest request;

        public Builder() {
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder headers(List<SAHeader> headers) {
            this.headers.addAll(headers);
            return this;
        }


        public Builder body(SAResponseBody body) {
            this.body = body;
            return this;
        }

        public Builder request(SARequest request) {
            this.request = request;
            return this;
        }

        public SAResponse build() {
            return new SAResponse(this);
        }
    }
}
