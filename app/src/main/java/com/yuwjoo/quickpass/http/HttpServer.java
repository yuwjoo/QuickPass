package com.yuwjoo.quickpass.http;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP服务器核心类
 * 负责管理服务器的生命周期和请求处理
 */
public class HttpServer {
    private static final String TAG = "HttpServer";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_BACKLOG = 50;

    private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private final ExecutorService executorService;
    private final RequestHandler requestHandler;

    /**
     * 构造函数
     * @param port 服务器端口
     */
    public HttpServer(int port) {
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
        this.requestHandler = new RequestHandler();
    }

    /**
     * 使用默认端口构造服务器
     */
    public HttpServer() {
        this(DEFAULT_PORT);
    }

    /**
     * 启动服务器
     * @throws IOException 如果服务器启动失败
     */
    public void start() throws IOException {
        if (isRunning) {
            Log.w(TAG, "Server is already running");
            return;
        }

        serverSocket = new ServerSocket(port, DEFAULT_BACKLOG);
        isRunning = true;
        Log.i(TAG, "Server started on port " + port);

        // 在新线程中接受连接
        new Thread(this::acceptConnections).start();
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server socket", e);
        }

        executorService.shutdown();
        Log.i(TAG, "Server stopped");
    }

    /**
     * 接受客户端连接
     */
    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (isRunning) {
                    Log.e(TAG, "Error accepting client connection", e);
                }
            }
        }
    }

    /**
     * 处理客户端连接
     * @param clientSocket 客户端socket
     */
    private void handleClient(Socket clientSocket) {
        try {
            requestHandler.handle(clientSocket);
        } catch (IOException e) {
            Log.e(TAG, "Error handling client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket", e);
            }
        }
    }

    /**
     * 获取服务器运行状态
     * @return 服务器是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 获取服务器端口
     * @return 服务器端口号
     */
    public int getPort() {
        return port;
    }
}