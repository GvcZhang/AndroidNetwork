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
