package com.curious.network.base.internal;

import android.text.TextUtils;
import android.util.Log;

import com.curious.network.base.SAHttpClient;
import com.curious.network.base.SAHttpUrl;
import com.curious.network.base.SAInterceptor;
import com.curious.network.base.SARequest;
import com.curious.network.base.SAResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * 重定向和重试拦截器
 */
public class FollowAndRetryInterceptor implements SAInterceptor {

    /**
     * Http 状态码 307
     */
    private static final int HTTP_307 = 307;
    private static final String TAG = "FollowAndRetry";
    private final SAHttpClient client;

    public FollowAndRetryInterceptor(SAHttpClient client) {
        this.client = client;
    }

    @Override
    public SAResponse intercept(Chain chain) throws IOException {
        SARequest request = chain.request();
        SAResponse response = null;
        int retryTimes = 0, followTimes = 0;
        while (true) {
            try {
                response = chain.proceed(request);
            } catch (IOException e) {
                if (client.isRetryOnConnectionFailure()) {
                    if (retryTimes++ >= client.maxRetryTimes()) {
                        Log.i(TAG, retryTimes + " retry times has be executed. ");
                        throw e;
                    }
                    if (!isOneShotRequest(e) && isRecoverable(e)) {
                        Log.i(TAG,"retry connection, times: " + retryTimes);
                        continue;
                    } else {
                        break;
                    }
                } else {
                    throw e;
                }
            }

            //目前遇到重定向，只是将数据按照新的 Location 重发，此处需要完善
            //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Redirections
            if (!client.isUrlConnectionFollowRedirects() && client.isFollowRedirects() && needRedirects(response.code())) {
                if (followTimes++ >= client.maxFollows()) {
                    throw new ProtocolException(followTimes + " follow times has be executed. You should check your request url.");
                }
                response.close();
                String location = getLocation(response, request.url().toString());
                SARequest.HttpMethod method = request.method();
                if (!TextUtils.isEmpty(location)) {
                    Log.i(TAG,"start new redirect, times: " + followTimes + ",location:" + location);
                    //重新设置 url 地址
                    request = request.newBuilder().method(method).url(SAHttpUrl.get(location)).build();
                    continue;
                }
            }
            break;
        }
        return response;
    }

    /**
     * 判断网络请求是否只用触发一次
     *
     * @return
     */
    private boolean isOneShotRequest(IOException exception) {
        return exception instanceof FileNotFoundException;
    }

    /**
     * 判断出错异常是否需要重试
     *
     * @param e
     * @return
     */
    private boolean isRecoverable(IOException e) {
        //ProtocolException 不重试
        if (e instanceof ProtocolException) {
            return false;
        }
        //SocketTimeoutException 重试
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException;
        }
        //SSLHandshakeException 不重试
        if (e instanceof SSLHandshakeException) {
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            return false;
        }
        return true;
    }

    private boolean needRedirects(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == HTTP_307;
    }

    private String getLocation(SAResponse response, String originalUrl) throws MalformedURLException {
        String location = "";
        List<String> locationList = response.headerValue("Location");
        if (locationList != null && !locationList.isEmpty()) {
            location = locationList.get(0);
        } else {
            locationList = response.headerValue("location");
            if (locationList != null && !locationList.isEmpty()) {
                location = locationList.get(0);
            }
        }
        if (TextUtils.isEmpty(location)) {
            return null;
        }
        if (!(location.startsWith("http://") || location.startsWith("https://"))) {
            //某些时候会省略host，只返回后面的path，所以需要补全url
            URL originUrl = new URL(originalUrl);
            location = originUrl.getProtocol() + "://" + originUrl.getHost() + location;
        }
        return location;
    }
}
