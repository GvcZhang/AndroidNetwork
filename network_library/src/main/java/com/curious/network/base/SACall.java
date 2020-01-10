package com.curious.network.base;


import com.curious.network.HttpCallback;

import java.io.IOException;

public interface SACall {

    /** 返回初始化这个 Call 的原始 Request 信息. */
    SARequest request();

    /** 同步请求 */
    SAResponse execute() throws IOException;

    /** 异步请求 */
    void enqueue(HttpCallback callback);

    /** 取消请求 */
    void cancel();


    interface Factory {
        SACall newCall(SARequest request);
    }
}
