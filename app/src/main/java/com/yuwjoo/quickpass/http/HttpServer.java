package com.yuwjoo.quickpass.http;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP服务器类
 * 用于启动本地HTTP服务器，处理文件分享请求
 */
public class HttpServer {
    private static final String TAG = "HttpServer";
    private static final int PORT = 3400;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private final Context context;
    private final ExecutorService executorService;
    private final Map<String, Uri> fileMap;

    /**
     * 构造函数
     * @param context 应用上下文
     */
    public HttpServer(Context context) {
        this.context = context;
        this.executorService = Executors.newCachedThreadPool();
        this.fileMap = new HashMap<>();
    }

    /**
     * 启动HTTP服务器
     */
    public void start() {
        if (isRunning) return;

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            isRunning = true;

            // 在新线程中监听连接请求
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket client = serverSocket.accept();
                        executorService.execute(() -> handleClient(client));
                    } catch (IOException e) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting client connection", e);
                        }
                    }
                }
            }).start();

            Log.i(TAG, "HTTP server started on port " + PORT);
        } catch (IOException e) {
            Log.e(TAG, "Error starting server", e);
        }
    }

    /**
     * 停止HTTP服务器
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error stopping server", e);
        }
        executorService.shutdown();
    }

    /**
     * 添加文件到分享列表
     * @param uri 文件的Uri
     * @return 生成的分享链接
     */
    public String addFile(Uri uri) {
        String id = UUID.randomUUID().toString();
        fileMap.put(id, uri);
        return String.format("http://localhost:%d/files/%s", PORT, id);
    }

    /**
     * 处理客户端连接
     * @param client 客户端Socket
     */
    private void handleClient(Socket client) {
        try {
            byte[] buffer = new byte[8192];
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            // 读取请求头
            StringBuilder request = new StringBuilder();
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                request.append(new String(buffer, 0, bytesRead));
                if (request.toString().contains("\r\n\r\n")) break;
            }

            // 解析请求路径
            String[] requestLines = request.toString().split("\r\n");
            String[] requestLine = requestLines[0].split(" ");
            String path = requestLine[1];

            if (path.startsWith("/files/")) {
                String fileId = path.substring(7);
                Uri fileUri = fileMap.get(fileId);

                if (fileUri != null) {
                    // 发送文件内容
                    try (InputStream fileInput = context.getContentResolver().openInputStream(fileUri)) {
                        String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/octet-stream\r\n" +
                                "\r\n";
                        output.write(response.getBytes());

                        while ((bytesRead = fileInput.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    // 文件不存在
                    String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                    output.write(response.getBytes());
                }
            } else {
                // 无效路径
                String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                output.write(response.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling client", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing client connection", e);
            }
        }
    }
}