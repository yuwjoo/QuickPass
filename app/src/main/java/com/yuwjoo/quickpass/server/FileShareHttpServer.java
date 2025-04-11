package com.yuwjoo.quickpass.server;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FileShareHttpServer {
    private static final String TAG = "FileShareHttpServer";
    private static final int PORT = 3400; // 端口号
    private final Context context; // 当前上下文
    private final AsyncHttpServer server = new AsyncHttpServer();
    private final Map<String, Uri> shareFileMap = new HashMap<>();// 要分享的文件map

    public FileShareHttpServer(Context context) {
        this.context = context;

        server.get("/", this::handleRootRoute);
        server.get("/downloadFile", this::handleDownloadFile);
    }

    /**
     * 处理根路由
     */
    /**
     * 处理根路由
     * 返回特定的标识消息，用于设备发现
     */
    private void handleRootRoute(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.send("QuickPass-Device");
    }

    /**
     * 处理下载文件路由
     */
    private void handleDownloadFile(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        Multimap queryMultimap = request.getQuery();
        String fileId = queryMultimap.getString("id");
        Uri fileUri = shareFileMap.get(fileId);

        if (fileUri != null) {
            try {
                // 获取文件名
                String fileName = getFileNameFromUri(fileUri);
                // 获取文件MIME类型
                String mimeType = getMimeTypeFromUri(fileUri);

                // 打开文件流
                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // 设置响应头
                    response.getHeaders().add("Content-Type", mimeType);
                    response.getHeaders().add("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
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

    /**
     * 启动HTTP服务器
     * 在指定端口上启动异步HTTP服务器
     */
    public void start() {
        try {
            server.listen(PORT);
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
            Log.i(TAG, "HTTP server stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping server", e);
        }
    }

    /**
     * 添加文件到分享列表
     *
     * @param uri 文件的Uri
     * @return 文件id
     */
    public String addFile(Uri uri) {
        String id = UUID.randomUUID().toString();
        shareFileMap.put(id, uri);
        return id;
    }

    /**
     * 从分享列表删除文件
     *
     * @param id 文件id
     */
    public void removeFile(String id) {
        shareFileMap.remove(id);
    }

    /**
     * 获取服务器地址
     *
     * @return 返回服务器的协议，IP地址和端口
     */
    public String getServerAddress() {
        try {
            WifiManager wifiManager = (android.net.wifi.WifiManager)
                    context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String ip = String.format(Locale.ROOT, "%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));
            return "http://" + ip + ":" + PORT;
        } catch (Exception e) {
            Log.e(TAG, "Error getting server address", e);
            return "http://localhost:" + PORT;
        }
    }

    /**
     * 获取文件分享链接
     *
     * @param fileId 文件ID
     * @return 完整的文件下载链接
     */
    public String getShareLink(String fileId) {
        if (!shareFileMap.containsKey(fileId)) {
            return null;
        }
        return getServerAddress() + "/downloadFile?id=" + fileId;
    }

    /**
     * 从Uri获取文件MIME类型
     *
     * @param uri 文件Uri
     * @return MIME类型
     */
    private String getMimeTypeFromUri(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * 从Uri获取文件名
     *
     * @param uri 文件Uri
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }

        return fileName != null ? fileName : "download";
    }
}
