package com.curious.network.base;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * url encode 转码规则
 * 1.资源路径中含有空格时应该转码为%20，既 RFC1738、RFC2396 规则
 * 举例：http://www.baidu.com/he he/index.jsp -----> http://www.baidu.com/he%20he/index.jsp
 * 2.get请求的QueryString里含有空格的话应该转码为%20，既RFC1738、RFC2396 规则
 * 举例：http://www.abc.com?wo=he he ------> http://www.abc.com?wo=he%20he
 * 3.post请求时,content-type = application/x-www-form-urlencoded (一般默认都是这个)时，空格应该转码为+;
 * 举例：向http://www.abc.com/发post请求,参数的值有空格,最终的参数键值对是 wo=he+he；
 */
public final class SAHttpUrl {
    /** Either "http" or "https". */
    final String scheme;

    /** Canonical hostname. */
    final String host;

    /** Either 80, 443 or a user-specified port. In range [1..65535]. */
    final int port;

    /**
     * A list of canonical path segments. This list always contains at least one element, which may be
     * the empty string. Each segment is formatted with a leading '/', so if path segments were ["a",
     * "b", ""], then the encoded path would be "/a/b/".
     */
    private final List<String> pathSegments;

    /**
     * Alternating, decoded query names and values, or null for no query. Names may be empty or
     * non-empty, but never null. Values are null if the name has no corresponding '=' separator, or
     * empty, or non-empty.
     */
    private final Map<String, String> queryNamesAndValues;

    /** Canonical URL. */
    private final String url;

    private SAHttpUrl(Builder builder) {
        this.scheme = builder.scheme;
        this.host = builder.host;
        this.port = builder.effectivePort();
        this.pathSegments = builder.pathSegments;
        this.queryNamesAndValues = builder.queryParamMap;
        this.url = builder.url;
    }

    /** Returns this URL as a {@link URL java.net.URL}. */
    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // Unexpected!
        }
    }

    /** Returns either "http" or "https". */
    public String scheme() {
        return scheme;
    }

    public boolean isHttps() {
        return scheme.equals("https");
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    /**
     * Returns 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
     * otherwise.
     */
    public static int defaultPort(String scheme) {
        if (scheme.equals("http")) {
            return 80;
        } else if (scheme.equals("https")) {
            return 443;
        } else {
            return -1;
        }
    }

    public int pathSize() {
        return pathSegments.size();
    }

    public String encodedPath() {
        int pathStart = url.indexOf('/', scheme.length() + 3); // "://".length() == 3.
        int pathEnd = delimiterOffset(url, pathStart, url.length(), "?#");
        return url.substring(pathStart, pathEnd);
    }

    private static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1) return i;
        }
        return limit;
    }

    private static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter) return i;
        }
        return limit;
    }

    /**
     * 将路径片段转换成 list 返回
     *
     * @return list
     */
    public List<String> encodedPathSegments() {
        int pathStart = url.indexOf('/', scheme.length() + 3);
        int pathEnd = delimiterOffset(url, pathStart, url.length(), "?#");
        List<String> result = new ArrayList<>();
        for (int i = pathStart; i < pathEnd; ) {
            i++; // Skip the '/'.
            int segmentEnd = delimiterOffset(url, i, pathEnd, '/');
            result.add(url.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    public List<String> pathSegments() {
        return pathSegments;
    }

    public String encodedQuery() {
        if (queryNamesAndValues == null) return null; // No query.
        int queryStart = url.indexOf('?') + 1;
        int queryEnd = delimiterOffset(url, queryStart, url.length(), '#');
        return url.substring(queryStart, queryEnd);
    }

    static void namesAndValuesToQueryString(StringBuilder out, Map<String, String> namesAndValues) {
        if (namesAndValues == null) return;
        Set<Map.Entry<String, String>> set = namesAndValues.entrySet();
        int index = 0;
        for (Map.Entry<String, String> entry : set) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (index++ > 0) out.append('&');
            out.append(name);
            if (value != null) {
                out.append('=');
                out.append(value);
            }
        }
    }

    static Map<String, String> queryStringToNamesAndValues(String encodedQuery) {
        Map<String, String> result = new HashMap<>();
        for (int pos = 0; pos <= encodedQuery.length(); ) {
            int ampersandOffset = encodedQuery.indexOf('&', pos);
            if (ampersandOffset == -1) ampersandOffset = encodedQuery.length();

            int equalsOffset = encodedQuery.indexOf('=', pos);
            if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
                result.put(encodedQuery.substring(pos, ampersandOffset), null);
            } else {
                result.put(encodedQuery.substring(pos, equalsOffset), encodedQuery.substring(equalsOffset + 1, ampersandOffset));
            }
            pos = ampersandOffset + 1;
        }
        return result;
    }

    public String query() {
        if (queryNamesAndValues == null) return null; // No query.
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, queryNamesAndValues);
        return result.toString();
    }

    public int querySize() {
        return queryNamesAndValues != null ? queryNamesAndValues.size() : 0;
    }

    public String queryParameter(String name) {
        if (queryNamesAndValues == null) return null;
        return queryNamesAndValues.get(name);
    }

    public Set<String> queryParameterNames() {
        if (queryNamesAndValues == null) return Collections.emptySet();
        return Collections.unmodifiableSet(queryNamesAndValues.keySet());
    }

    public static SAHttpUrl get(String url) {
        return new Builder().url(url).build();
    }

    /**
     * 转码 content-type = application/x-www-form-urlencoded 类型的 key-value,
     * 转码失败则返回原值
     *
     * @param value
     * @return
     */
    public static String paramEncode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return url;
    }

    public final static class Builder {
        private String url;
        private String scheme;
        private String host;
        private int port;
        private Map<String, String> queryParamMap = new HashMap<>();
        private List<String> pathSegments = new ArrayList<>();

        public Builder url(String url) {
            this.url = url;
            try {
                URI uri = new URI(url);
                scheme = uri.getScheme();
                host = uri.getHost();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("the url is invalid, please check it");
            }
            return this;
        }

        /**
         * 不支持参数数组，请使用 “,” 替代
         *
         * @param key
         * @param value
         */
        public Builder setQueryParam(String key, String value) {
            this.queryParamMap.put(key, value);
            return this;
        }

        public Builder pathSegements(String... pathSegements) {
            this.pathSegments.addAll(Arrays.asList(pathSegements));
            return this;
        }

        int effectivePort() {
            return port != -1 ? port : defaultPort(scheme);
        }

        private void encodeUrl() {
            StringBuilder builder = new StringBuilder(url);
            for (String pathSegment : pathSegments) {
                if (builder.charAt(builder.length() - 1) != '/') {
                    builder.append('/');
                }
                builder.append(pathSegment);
            }
            Set<Map.Entry<String, String>> set = queryParamMap.entrySet();
            if (!queryParamMap.isEmpty()) {
                if (builder.charAt(builder.length() - 1) != '?') {
                    builder.append('?');
                }
                int index = 0;
                for (Map.Entry<String, String> entry : set) {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    if (index++ > 0) builder.append('&');
                    builder.append(name);
                    if (value != null) {
                        builder.append('=');
                        builder.append(value);
                    }
                }
            }

            try {
                URL urlTmp = new URL(builder.toString());
                URI uri = new URI(urlTmp.getProtocol(), urlTmp.getUserInfo(), urlTmp.getHost(), urlTmp.getPort(), urlTmp.getPath(), urlTmp.getQuery(), urlTmp.getRef());
                this.url = uri.toASCIIString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public SAHttpUrl build() {
            encodeUrl();
            return new SAHttpUrl(this);
        }
    }
}
