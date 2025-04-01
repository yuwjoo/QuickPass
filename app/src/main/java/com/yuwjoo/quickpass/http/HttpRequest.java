package com.yuwjoo.quickpass.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求类
 * 封装HTTP请求的相关信息
 */
public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private String body;

    /**
     * 构造HTTP请求对象
     * @param method HTTP方法（GET, POST等）
     * @param path 请求路径
     */
    public HttpRequest(String method, String path) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
    }

    /**
     * 添加请求头
     * @param name 请求头名称
     * @param value 请求头值
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * 获取请求头值
     * @param name 请求头名称
     * @return 请求头值，如果不存在返回null
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * 设置请求体
     * @param body 请求体内容
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 获取请求体
     * @return 请求体内容
     */
    public String getBody() {
        return body;
    }

    /**
     * 获取请求方法
     * @return HTTP请求方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取请求路径
     * @return 请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取所有请求头
     * @return 请求头映射
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
}