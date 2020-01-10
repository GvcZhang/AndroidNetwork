# AndroidNetwork

## 介绍  
基于原生 HttpURLConnection 的网络请求封装，是学习设计模式、OKHttp的好教材


## 使用 
```java
       //可以全局配置
       httpClient = new SAHttpClient.Builder()
                .urlConnectionFollowRedirects(false)
                .maxFollows(3)
                .followRedirects(true)
                .retryOnConnectionFailure(true)
                .maxRetryTimes(3)
                .addInterceptor(new SAHttpLogInterceptor())
                .callTimeout(30_000)
                .connectTimeout(30_000)
                .build();

        //在线程中启动访问
        executorService.submit(() -> {
            executorService.submit(() -> {
                SARequest.Builder builder = new SARequest.Builder()
                        .method(SARequest.HttpMethod.GET)
                        .url(new SAHttpUrl.Builder()
                                .url("https://github.com")
                                .build());

                try {
                    SAResponse response = httpClient.newCall(builder.build()).execute();
                    if (response != null) {
                        if (response.isSuccessful()) {
                            Log.i("http result", response.body().string());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
```