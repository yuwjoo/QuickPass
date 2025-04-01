package com.yuwjoo.quickpass.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求处理器
 * 负责解析HTTP请求并根据路由配置处理请求
 */
public class RequestHandler {
    private static final String TAG = "RequestHandler";
    private final Map<String, RouteHandler> routes;

    public RequestHandler() {
        this.routes = new HashMap<>();
        // 注册默认路由
        registerRoute("/", (request, response) -> {
            response.setContentType("text/plain");
            response.write("Welcome to QuickPass HTTP Server");
        });
    }

    /**
     * 注册路由处理器
     * @param path 路由路径
     * @param handler 路由处理器
     */
    public void registerRoute(String path, RouteHandler handler) {
        routes.put(path, handler);
    }

    /**
     * 处理客户端请求
     * @param clientSocket 客户端socket连接
     * @throws IOException 如果IO操作失败
     */
    public void handle(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

        // 解析请求行
        String requestLine = reader.readLine();
        if (requestLine == null) {
            return;
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) {
            sendError(writer, 400, "Bad Request");
            return;
        }

        String method = requestParts[0];
        String path = requestParts[1];

        // 创建请求和响应对象
        HttpRequest request = new HttpRequest(method, path);
        HttpResponse response = new HttpResponse(writer);

        // 读取请求头
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] header = headerLine.split(": ", 2);
            if (header.length == 2) {
                request.addHeader(header[0], header[1]);
            }
        }

        // 查找并执行路由处理器
        RouteHandler handler = routes.get(path);
        if (handler != null) {
            try {
                handler.handle(request, response);
            } catch (Exception e) {
                Log.e(TAG, "Error handling route: " + path, e);
                sendError(writer, 500, "Internal Server Error");
            }
        } else {
            sendError(writer, 404, "Not Found");
        }
    }

    /**
     * 发送错误响应
     * @param writer 响应写入器
     * @param statusCode HTTP状态码
     * @param message 错误信息
     */
    private void sendError(PrintWriter writer, int statusCode, String message) {
        writer.println("HTTP/1.1 " + statusCode + " " + message);
        writer.println("Content-Type: text/plain");
        writer.println();
        writer.println(message);
    }

    /**
     * 路由处理器接口
     */
    public interface RouteHandler {
        void handle(HttpRequest request, HttpResponse response) throws IOException;
    }
}