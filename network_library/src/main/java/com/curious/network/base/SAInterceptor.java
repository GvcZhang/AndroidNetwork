package com.curious.network.base;

import java.io.IOException;

public interface SAInterceptor {
    SAResponse intercept(Chain chain) throws IOException;

    interface Chain {
        SARequest request();

        SAResponse proceed(SARequest request) throws IOException;
    }
}
