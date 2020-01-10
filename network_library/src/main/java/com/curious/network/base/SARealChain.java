package com.curious.network.base;

import java.io.IOException;
import java.util.List;

public class SARealChain implements SAInterceptor.Chain {

    private final List<SAInterceptor> interceptors;
    private final int index;
    private final SACall call;
    private final SARequest request;

    public SARealChain(List<SAInterceptor> interceptors, int index, SARequest request, SACall call) {
        this.interceptors = interceptors;
        this.index = index;
        this.call = call;
        this.request = request;
    }

    @Override
    public SARequest request() {
        return request;
    }

    @Override
    public SAResponse proceed(SARequest request) throws IOException {
        if (index >= interceptors.size()) throw new AssertionError();
        SARealChain realChain = new SARealChain(interceptors, index + 1, request, call);
        SAInterceptor interceptor = interceptors.get(index);
        SAResponse response = interceptor.intercept(realChain);
        if (response == null) {
            throw new NullPointerException("interceptor " + interceptor + " returned null");
        }
        return response;
    }
}
