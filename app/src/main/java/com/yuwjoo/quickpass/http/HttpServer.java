package com.yuwjoo.quickpass.http;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP服务器类
 * 使用androidasync库实现的异步HTTP服务器，用于处理文件分享请求
 */
public class HttpServer {
    private static final String TAG = "HttpServer";
    private static final int PORT = 3400;

    private final Context context;
    private final AsyncHttpServer server;
    private final AsyncServer asyncServer;
    private final Map<String, Uri> fileMap;

    /**
     * 构造函数
     *
     * @param context 应用上下文
     */
    public HttpServer(Context context) {
        this.context = context;
        this.server = new AsyncHttpServer();
        this.asyncServer = new AsyncServer();
        this.fileMap = new HashMap<>();

        // 配置文件下载路由
        setupFileRoute();
    }

    /**
     * 配置文件下载的路由处理
     * 处理/files/{fileId}格式的请求，返回对应的文件内容
     */
    private void setupFileRoute() {
        server.get("/files/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String path = request.getPath();
                String fileId = path.substring(7); // 去除"/files/"前缀
                Uri fileUri = fileMap.get(fileId);

                if (fileUri != null) {
                    try {
                        // 打开文件流
                        InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                        if (inputStream != null) {
                            // 设置响应头
                            response.getHeaders().add("Content-Type", "application/octet-stream");
                            // 发送文件内容
                            response.sendStream(inputStream, inputStream.available());
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending file", e);
                    }
                }

                // 文件不存在或发生错误时返回404
                response.code(404);
                response.end();
            }
        });
    }

    /**
     * 启动HTTP服务器
     * 在指定端口上启动异步HTTP服务器
     */
    public void start() {
        try {
            server.listen(asyncServer, PORT);
            Log.i(TAG, "HTTP server started on port " + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Error starting server", e);
        }
    }

    /**
     * 停止HTTP服务器
     * 关闭服务器并释放资源
     */
    public void stop() {
        try {
            server.stop();
            asyncServer.stop();
            Log.i(TAG, "HTTP server stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping server", e);
        }
    }

    /**
     * 添加文件到分享列表
     *
     * @param uri 文件的Uri
     * @return 生成的分享链接
     */
    public String addFile(Uri uri) {
        String id = UUID.randomUUID().toString();
        fileMap.put(id, uri);
        return "http://localhost:" + PORT + "/files/" + id;
    }
}