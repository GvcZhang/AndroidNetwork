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
