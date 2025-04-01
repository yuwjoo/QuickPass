package com.yuwjoo.quickpass.http;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP响应类
 * 封装HTTP响应的相关信息和操作
 */
public class HttpResponse {
    private final PrintWriter writer;
    private final Map<String, String> headers;
    private int statusCode;
    private String statusMessage;
    private boolean headersSent;

    /**
     * 构造HTTP响应对象
     * @param writer 响应写入器
     */
    public HttpResponse(PrintWriter writer) {
        this.writer = writer;
        this.headers = new HashMap<>();
        this.statusCode = 200;
        this.statusMessage = "OK";
        this.headersSent = false;
        setContentType("text/html");
    }

    /**
     * 设置响应状态
     * @param statusCode HTTP状态码
     * @param statusMessage 状态描述信息
     */
    public void setStatus(int statusCode, String statusMessage) {
        if (headersSent) {
            throw new IllegalStateException("Headers already sent");
        }
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * 设置响应头
     * @param name 响应头名称
     * @param value 响应头值
     */
    public void setHeader(String name, String value) {
        if (headersSent) {
            throw new IllegalStateException("Headers already sent");
        }
        headers.put(name, value);
    }

    /**
     * 设置内容类型
     * @param contentType MIME类型
     */
    public void setContentType(String contentType) {
        setHeader("Content-Type", contentType);
    }

    /**
     * 写入响应内容
     * @param content 响应内容
     */
    public void write(String content) {
        if (!headersSent) {
            sendHeaders();
        }
        writer.println(content);
        writer.flush();
    }

    /**
     * 发送响应头
     */
    private void sendHeaders() {
        writer.println(String.format("HTTP/1.1 %d %s", statusCode, statusMessage));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            writer.println(String.format("%s: %s", header.getKey(), header.getValue()));
        }
        writer.println();
        headersSent = true;
    }

    /**
     * 获取当前状态码
     * @return HTTP状态码
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 获取响应头
     * @param name 响应头名称
     * @return 响应头值
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
}