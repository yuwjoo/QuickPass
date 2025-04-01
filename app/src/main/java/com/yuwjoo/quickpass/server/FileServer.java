package com.yuwjoo.quickpass.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件服务器模块
 * 负责处理HTTP服务器相关功能，包括服务器启动、文件路径分配和文件传输
 */
public class FileServer {
    private static final int PORT = 3400;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private final Map<String, File> fileMap;
    private final Context context;

    public FileServer(Context context) {
        this.context = context;
        this.fileMap = new HashMap<>();
    }

    /**
     * 启动HTTP服务器
     * @return 服务器IP地址
     * @throws IOException 启动服务器失败时抛出异常
     */
    public String start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;

        // 获取本机IP地址
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        // 启动监听线程
        new Thread(this::handleConnections).start();

        return ipAddress;
    }

    /**
     * 停止服务器
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 为文件分配唯一路径
     * @param file 要分享的文件
     * @return 文件访问路径
     */
    public String assignFilePath(File file) {
        String fileId = UUID.randomUUID().toString();
        fileMap.put(fileId, file);
        return String.format("http://%s:%d/download/%s", 
            getLocalIpAddress(), PORT, fileId);
    }

    /**
     * 处理客户端连接
     */
    private void handleConnections() {
        while (isRunning) {
            try {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理客户端请求
     * @param client 客户端Socket
     */
    private void handleClient(Socket client) {
        try {
            // 解析请求路径
            String fileId = parseFileId(client);
            if (fileId != null && fileMap.containsKey(fileId)) {
                // 发送文件
                sendFile(client, fileMap.get(fileId));
            } else {
                // 发送404响应
                send404Response(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析请求中的文件ID
     * @param client 客户端Socket
     * @return 文件ID
     */
    private String parseFileId(Socket client) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = client.getInputStream().read(buffer);
        if (bytes > 0) {
            String request = new String(buffer, 0, bytes);
            String[] lines = request.split("\r\n");
            if (lines.length > 0) {
                String[] parts = lines[0].split(" ");
                if (parts.length > 1 && parts[1].startsWith("/download/")) {
                    return parts[1].substring(10);
                }
            }
        }
        return null;
    }

    /**
     * 发送文件
     * @param client 客户端Socket
     * @param file 要发送的文件
     */
    private void sendFile(Socket client, File file) throws IOException {
        OutputStream output = client.getOutputStream();

        // 发送HTTP响应头
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Length: " + file.length() + "\r\n\r\n";
        output.write(header.getBytes());

        // 发送文件内容
        try (FileInputStream fileInput = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = fileInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytes);
            }
            output.flush();
        }
    }

    /**
     * 发送404响应
     * @param client 客户端Socket
     */
    private void send404Response(Socket client) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 13\r\n\r\n" +
                "File not found";
        client.getOutputStream().write(response.getBytes());
    }

    /**
     * 获取本机IP地址
     * @return IP地址
     */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return Formatter.formatIpAddress(ipAddress);
    }
}