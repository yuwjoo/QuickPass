package com.yuwjoo.quickpass.httpServer;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.koushikdutta.async.http.server.AsyncHttpServer;

import java.util.Locale;

public class HttpServer {
    private static final String TAG = "HttpServer";
    private static final int PORT = 3400; // 监听端口号
    private static final AsyncHttpServer server = new AsyncHttpServer();
    private static boolean isEnable = false; // http服务是否开启

    public HttpServer() {
        start();
    }

    /**
     * 获取http服务实例
     *
     * @return http服务实例
     */
    public AsyncHttpServer getServer() {
        return server;
    }

    /**
     * http服务是否开启
     *
     * @return 是否开启
     */
    public boolean getIsEnable() {
        return isEnable;
    }

    public int getPort() {
        return PORT;
    }

    /**
     * 启动HTTP服务器
     * 在指定端口上启动异步HTTP服务器
     */
    public void start() {
        if (isEnable) return;
        try {
            server.listen(PORT);
            isEnable = true;
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
        if (!isEnable) return;
        try {
            server.stop();
            isEnable = false;
            Log.i(TAG, "HTTP server stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping server", e);
        }
    }

    /**
     * 获取服务器地址
     *
     * @return 返回服务器的协议，IP地址和端口
     */
    public String getServerAddress(Context context) {
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
}
