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


import com.curious.network.HttpCallback;
import com.curious.network.base.internal.ConnectInterceptor;
import com.curious.network.base.internal.CookiesInterceptor;
import com.curious.network.base.internal.FollowAndRetryInterceptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 实际的 Http 请求操作在这里发生
 */
public class SARealCall implements SACall {
    private static final String TAG = "SA.SARealCall";
    final SAHttpClient client;
    /** The application's original request unadulterated by redirects or auth headers. */
    final SARequest originalRequest;

    // Guarded by this.
    private boolean executed;
    private boolean isCanceled;
    private HttpURLConnection connection = null;

    private SARealCall(SAHttpClient client, SARequest originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }

    static SARealCall newRealCall(SAHttpClient client, SARequest originalRequest) {
        SARealCall call = new SARealCall(client, originalRequest);
        return call;
    }

    @Override
    public SARequest request() {
        return this.originalRequest;
    }


    @Override
    public SAResponse execute() throws IOException {

        //回调请求前
        SAResponse response = getResponseWithInterceptorChain();
        //回调请求后

        return response;
    }

    @Override
    public void enqueue(HttpCallback callback) {
        //TODO 暂未实现，后续再补充
    }

    @Override
    public void cancel() {
        isCanceled = false;

    }

    public boolean isCanceled() {
        return isCanceled;
    }

    private SAResponse getResponseWithInterceptorChain() throws IOException {
        List<SAInterceptor> interceptors = new ArrayList<>(client.interceptors());
        interceptors.add(new FollowAndRetryInterceptor(client));
        interceptors.add(new CookiesInterceptor());
        interceptors.add(new ConnectInterceptor(client));
        SAInterceptor.Chain chain = new SARealChain(interceptors, 0, originalRequest, this);
        return chain.proceed(originalRequest);
    }
}
