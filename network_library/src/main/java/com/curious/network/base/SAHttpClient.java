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

import java.net.CookiePolicy;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

/**
 * 用于初始化一些常规的配置信息，比如连接超时、通用头信息等
 */
public final class SAHttpClient implements SACall.Factory {

    private final Proxy proxy;
    private final boolean followRedirects;
    private final boolean urlConnectionFollowRedirects;
    private final boolean retryOnConnectionFailure;
    private final int callTimeout;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private final SSLSocketFactory sslSocketFactory;
    private final List<SAInterceptor> interceptors;
    private final CookiePolicy cookiePolicy;
    private final int retryTimes;
    private final int maxFollows;

    public SAHttpClient(Builder builder) {
        this.urlConnectionFollowRedirects = builder.urlConnectionFollowRedirects;
        this.followRedirects = builder.followRedirects;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
        this.callTimeout = builder.callTimeout;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.interceptors = builder.interceptors;
        this.cookiePolicy = builder.cookiePolicy;
        this.proxy = builder.proxy;
        this.retryTimes = builder.retryTimes;
        this.maxFollows = builder.maxFollows;
    }

//    //重试失败后是否要增加时间
//    public void incrementReadTimeout() {
//        System.out.println("read time 增加5秒钟");
//        this.readTimeout += 5000;
//    }

    public Proxy proxy() {
        return proxy;
    }

    public boolean isUrlConnectionFollowRedirects() {
        return urlConnectionFollowRedirects;
    }

    /**
     * 是否将重定向交给用户处理
     *
     * @return
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public int callTimeout() {
        return callTimeout;
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public int writeTimeout() {
        return writeTimeout;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public List<SAInterceptor> interceptors() {
        return interceptors;
    }

//    public CookiePolicy cookiePolicy() {
//        return cookiePolicy;
//    }

    public int maxRetryTimes() {
        return retryTimes;
    }

    public int maxFollows() {
        return maxFollows;
    }

    @Override
    public SACall newCall(SARequest request) {
        return SARealCall.newRealCall(this, request);
    }

    public static final class Builder {
        Proxy proxy;
        boolean followRedirects;
        boolean urlConnectionFollowRedirects;
        boolean retryOnConnectionFailure;
        int callTimeout;
        int connectTimeout;
        int readTimeout;
        int writeTimeout;
        SSLSocketFactory sslSocketFactory;
        final List<SAInterceptor> interceptors = new ArrayList<>();
        CookiePolicy cookiePolicy;
        int retryTimes;
        int maxFollows;

        public Builder() {
            followRedirects = true;
            retryOnConnectionFailure = true;
            callTimeout = 0;
            connectTimeout = 10_000;
            readTimeout = 10_000;
            writeTimeout = 10_000;
            cookiePolicy = CookiePolicy.ACCEPT_NONE;
            retryTimes = 3;
            maxFollows = 3;
            urlConnectionFollowRedirects = false;
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * 是否将重定向交给 {@link SAHttpClient} 处理
         *
         * @param followRedirects true 交给 {@link SAHttpClient}  处理，false 交给 HttpURLConnection 处理
         * @return Builder
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * 设置 HTTPURLConnection.setInstanceFollowRedirects，如果此值为 false，会再判断 followRedirects，是否为 true，如果为 true，那么在 FollowAndRetryInterceptor 会处理重定向问题
         *
         * @param isRedirects HTTPURLConnection.setInstanceFollowRedirects
         * @return Builder
         */
        public Builder urlConnectionFollowRedirects(boolean isRedirects) {
            this.urlConnectionFollowRedirects = isRedirects;
            return this;
        }

        /**
         * 重定向的最大次数
         *
         * @param count 默认是 3
         * @return Builder
         */
        public Builder maxFollows(int count) {
            this.maxFollows = count;
            return this;
        }

        public Builder maxRetryTimes(int count) {
            this.retryTimes = count;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder callTimeout(int callTimeout) {
            this.callTimeout = callTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public Builder addInterceptor(SAInterceptor interceptor) {
            if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
            interceptors.add(interceptor);
            return this;
        }

//        public Builder cookiePolicy(CookiePolicy policy) {
//            if (policy == null) throw new IllegalArgumentException("CookiePolicy == null");
//            this.cookiePolicy = policy;
//            return this;
//        }

        public SAHttpClient build() {
            return new SAHttpClient(this);
        }
    }
}
