package com.curious.network.sample;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.curious.network.SAHttpLogInterceptor;
import com.curious.network.base.SAHttpClient;
import com.curious.network.base.SAHttpUrl;
import com.curious.network.base.SARequest;
import com.curious.network.base.SAResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private SAHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }
}
