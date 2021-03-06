package com.curious.network.base;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public abstract class SARequestBody {

    /**
     * 创建带 name-value 请求体的数据
     *
     * @param contentType contentType
     * @param queryList queryList
     * @return SARequestBody
     */
    public static SARequestBody create(String contentType, List<SAKeyValue> queryList) {
        String result = "";
        if (queryList != null && !queryList.isEmpty()) {
            Uri.Builder uriBuilder = new Uri.Builder();
            for (SAKeyValue keyValue : queryList) {
                uriBuilder.appendQueryParameter(keyValue.name, keyValue.value);
            }
            result = uriBuilder.build().getEncodedQuery();
            if(TextUtils.isEmpty(result)){
                return null;
            }
        }
        return create(contentType, result);
    }

    public static SARequestBody create(String contentType, String content) {
        if (contentType == null) {
            throw new NullPointerException("contentType == null");
        }
        if (!contentType.contains("charset")) {
            contentType += "; charset=utf-8";
        }
        byte[] bytes = null;
        if (content != null) {
            bytes = content.getBytes(Charset.forName("utf-8"));
        }
        return create(contentType, bytes);
    }

    public static SARequestBody create(final String contentType, final byte[] content) {
        if (contentType == null) {
            throw new NullPointerException("contentType == null");
        }
        return new SARequestBody() {

            @Override
            public long contentLength() {
                if (content != null) {
                    return content.length;
                } else {
                    return -1;
                }
            }

            @Override
            public String contentType() {
                return contentType;
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                if (content != null) {
                    out.write(content);
                }
            }

            @Override
            public String stringContent() {
                return new String(content, Charset.forName("utf-8"));
            }
        };
    }

    public long contentLength() {
        return -1;
    }

    public abstract String contentType();

    public abstract void writeTo(OutputStream out) throws IOException;

    public abstract String stringContent();
}
