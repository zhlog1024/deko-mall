package com.deko.mall.system.utils;

import lombok.Data;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * http请求工具，基于OKHTTP3
 */
public class OkHttpUtil {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded");

    private Map<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

    private OkHttpClient httpClient;

    public OkHttpUtil() {
        this(new HttpToolConfig());
    }

    public OkHttpUtil(HttpToolConfig httpToolConfig) {
        this.initHttpClient(httpToolConfig);
    }

    public static Request.Builder buildRequestBuilder(String url, Map<String, ?> form, HTTPMethod method) {
        switch (method) {
            case GET:
                return new Request.Builder()
                        .url(buildHttpUrl(url, form))
                        .get();
            case HEAD:
                return new Request.Builder()
                        .url(buildHttpUrl(url, form))
                        .head();
            case PUT:
                return new Request.Builder()
                        .url(url)
                        .put(buildFormBody(form));
            case DELETE:
                return new Request.Builder()
                        .url(url)
                        .delete(buildFormBody(form));
            default:
                return new Request.Builder()
                        .url(url)
                        .post(buildFormBody(form));
        }
    }

    public static HttpUrl buildHttpUrl(String url, Map<String, ?> form) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, ?> entry : form.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return urlBuilder.build();
    }

    public static FormBody buildFormBody(Map<String, ?> form) {
        FormBody.Builder paramBuilder = new FormBody.Builder(StandardCharsets.UTF_8);
        for (Map.Entry<String, ?> entry : form.entrySet()) {
            paramBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return paramBuilder.build();
    }

    protected void initHttpClient(HttpToolConfig httpToolConfig) {
        httpClient = new OkHttpClient.Builder()
                // 设置链接超时时间，默认10秒
                .connectTimeout(httpToolConfig.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(httpToolConfig.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(httpToolConfig.writeTimeoutSeconds, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                        cookieStore.put(httpUrl.host(), list);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                        List<Cookie> cookies = cookieStore.get(httpUrl.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).build();
    }

    /**
     * get请求
     */
    public String get(String url, Map<String, String> header) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        // 添加header
        addHeader(builder, header);

        Request request = builder.build();
        Response response = httpClient.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 提交表单
     *
     * @param url    url
     * @param form   参数
     * @param header header
     * @param method 请求方式，post，get等
     */
    public String request(String url, Map<String, ?> form, Map<String, String> header, HTTPMethod method) throws IOException {
        Request.Builder requestBuilder = buildRequestBuilder(url, form, method);
        // 添加header
        addHeader(requestBuilder, header);

        Request request = requestBuilder.build();
        Response response = httpClient
                .newCall(request)
                .execute();
        try {
            return response.body().string();
        } finally {
            response.close();
        }
    }

    public String postForm(String url, Map<String, ?> form, Map<String, String> header) throws IOException {
        Request.Builder requestBuilder = buildRequestBuilder(url, form, HTTPMethod.POST);
        // 添加header
        addHeader(requestBuilder, header);

        Request request = requestBuilder.build();
        Response response = httpClient
                .newCall(request)
                .execute();
        try {
            return response.body().string();
        } finally {
            response.close();
        }
    }

    public String postForm(String url, Map<String, ?> form) throws IOException {
        Request.Builder requestBuilder = buildRequestBuilder(url, form, HTTPMethod.POST);
        Request request = requestBuilder.build();
        Response response = httpClient
                .newCall(request)
                .execute();
        try {
            return response.body().string();
        } finally {
            response.close();
        }
    }

    /**
     * 请求json数据，contentType=application/json
     *
     * @param url    请求路径
     * @param json   json数据
     * @param header header
     * @return 返回响应结果
     */
    public String requestJson(String url, String json, Map<String, String> header) throws IOException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        // 添加header
        addHeader(requestBuilder, header);

        Request request = requestBuilder.build();
        Response response = httpClient
                .newCall(request)
                .execute();
        try {
            return response.body().string();
        } finally {
            response.close();
        }
    }

    private void addHeader(Request.Builder builder, Map<String, String> header) {
        if (header != null) {
            Set<Map.Entry<String, String>> entrySet = header.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    public enum HTTPMethod {
        /**
         * http GET
         */
        GET,
        /**
         * http POST
         */
        POST,
        /**
         * http PUT
         */
        PUT,
        /**
         * http HEAD
         */
        HEAD,
        /**
         * http DELETE
         */
        DELETE;

        private HTTPMethod() {
        }

        public static HTTPMethod fromValue(String v) {
            return valueOf(v.toUpperCase());
        }

        public String value() {
            return this.name();
        }
    }

    @Data
    public static class HttpToolConfig {
        /**
         * 请求超时时间
         */
        private int connectTimeoutSeconds = 60;
        /**
         * http读取超时时间
         */
        private int readTimeoutSeconds = 30;
        /**
         * http写超时时间
         */
        private int writeTimeoutSeconds = 30;
    }

}
