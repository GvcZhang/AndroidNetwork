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
