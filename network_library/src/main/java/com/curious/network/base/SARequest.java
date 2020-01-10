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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Http Request 构建
 */
public final class SARequest {
    private final SAHttpUrl url;
    private final HttpMethod method;
    private final SARequestBody body;
    private final List<SAHeader> headers;


    public SARequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.body = builder.body;
        this.headers = Collections.unmodifiableList(builder.headers);
    }

    public SAHttpUrl url() {
        return url;
    }

    public List<SAHeader> headers() {
        return headers;
    }

    public SARequestBody body() {
        return body;
    }

    public HttpMethod method() {
        return method;
    }

    public boolean isHttps() {
        return url.isHttps();
    }

    public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder {
        private SAHttpUrl url;
        private HttpMethod method;
        private SARequestBody body;
        private List<SAHeader> headers = new ArrayList<>();

        Builder(SARequest request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.headers = request.headers;
        }

        public Builder() {
            method = HttpMethod.GET;
        }

        public Builder url(SAHttpUrl url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder body(SARequestBody body) {
            this.body = body;
            return this;
        }

        /**
         * Set a field with the specified value. If the field is not found, it is added. If the field is
         * found, the existing values are replaced.
         *
         * @param name name
         * @param value value
         * @return Builder
         */
        public Builder setHeader(String name, String value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            Iterator<SAHeader> iterator = headers.iterator();
            while (iterator.hasNext()) {
                SAHeader header = iterator.next();
                if (header.getName().equals(name)) {
                    iterator.remove();
                }
            }
            this.headers.add(new SAHeader(name, value.trim(), true));
            return this;
        }

        /**
         * Add a header with the specified name and value.
         *
         * @param name name
         * @param value value
         * @return Builder
         */
        public Builder addHeader(String name, String value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            this.headers.add(new SAHeader(name, value.trim(), false));
            return this;
        }

        public SARequest build() {
            if (body == null) {
                body = SARequestBody.create("application/json; charset=UTF-8", "");
            }
            return new SARequest(this);
        }
    }

    public enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");
//        PATCH("PATCH"),
//        HEAD("HEAD"),
//        MOVE("MOVE"),
//        COPY("COPY"),

//        OPTIONS("OPTIONS"),
//        TRACE("TRACE"),
//        CONNECT("CONNECT");

        private final String value;

        HttpMethod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static boolean permitsRetry(HttpMethod method) {
            return method == GET;
        }

        public static boolean permitsCache(HttpMethod method) {
            return method == GET || method == POST;
        }

        public static boolean permitsRequestBody(HttpMethod method) {
            return method == POST
                    || method == PUT
                    || method == DELETE;
        }
    }


}
