package com.curious.network.base.internal;


import com.curious.network.base.SAInterceptor;
import com.curious.network.base.SARequest;
import com.curious.network.base.SAResponse;

import java.io.IOException;

/**
 * 缓存处理，TODO 暂未实现
 */
public class CookiesInterceptor implements SAInterceptor {

    @Override
    public SAResponse intercept(Chain chain) throws IOException {
        SARequest request = chain.request();
        return chain.proceed(request);
    }
}
