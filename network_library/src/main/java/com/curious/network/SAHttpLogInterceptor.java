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

package com.curious.network;

import android.util.Log;

import com.curious.network.base.SAHeader;
import com.curious.network.base.SAInterceptor;
import com.curious.network.base.SARequest;
import com.curious.network.base.SAResponse;

import java.io.IOException;
import java.util.List;

/**
 * request，response log
 */
public class SAHttpLogInterceptor implements SAInterceptor {

    private static final String TAG = "SAHttpLogInterceptor";

    @Override
    public SAResponse intercept(SAInterceptor.Chain chain) throws IOException {
        SARequest request = chain.request();
        StringBuilder builder = new StringBuilder();
        builder.append("=====request part=====\n");
        builder.append(request.method().toString()).append("  ").append(request.url().toString()).append("\n");
        List<SAHeader> headerList = request.headers();
        if (!headerList.isEmpty()) {
            for (SAHeader header : headerList) {
                builder.append(header.getName()).append(":").append(header.getValue()).append("  ").append(header.isSetHeader()).append("\n");
            }
        }
        builder.append("\n\n").append(request.body().stringContent()).append("\n");
        builder.append("=====response part=====\n");
        SAResponse response = chain.proceed(request);
        if (response != null) {
            builder.append(response.code()).append("  \n");
        }
        List<SAHeader> responseHeaderList = response.headers();
        if (!responseHeaderList.isEmpty()) {
            for (SAHeader header : responseHeaderList) {
                builder.append(header.getName()).append(":").append(header.getValue()).append("\n");
            }
        }
        builder.append("\n\n").append(response.body().string()).append("\n");

        Log.i(TAG, builder.toString());
        return response;
    }
}
