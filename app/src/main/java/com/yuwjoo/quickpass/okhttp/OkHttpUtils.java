package com.yuwjoo.quickpass.okhttp;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new BaseInterceptor()).build();
    private static final MediaType JSON = MediaType.get("application/json");

    /**
     * 获取okhttp客户端实例
     *
     * @return okhttp客户端实例
     */
    public static OkHttpClient getInstance() {
        return okHttpClient;
    }

    /**
     * 发送get请求
     *
     * @param url 请求url
     * @return 响应
     */
    public static Response get(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送post请求
     *
     * @param url  请求url
     * @param data 请求json对象
     * @return 响应
     */
    public static Response post(String url, JSONObject data) {
        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
