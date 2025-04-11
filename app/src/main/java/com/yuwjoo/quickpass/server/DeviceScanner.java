package com.yuwjoo.quickpass.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 设备扫描器类
 * 用于在局域网中扫描运行QuickPass服务的设备
 */
public class DeviceScanner {
    private static final String TAG = "DeviceScanner";
    private static final int SCAN_PORT = 3400;
    private static final int TIMEOUT_MS = 500;
    private static final int THREAD_POOL_SIZE = 20;

    private final Context context;
    private final ExecutorService executorService;
    private final List<String> discoveredDevices;

    /**
     * 构造函数
     * @param context 应用程序上下文
     */
    public DeviceScanner(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.discoveredDevices = new ArrayList<>();
    }

    /**
     * 获取当前WiFi的IP地址
     * @return IP地址字符串，格式为xxx.xxx.xxx.xxx
     */
    public String getCurrentIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager)
                    context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.ROOT, "%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address", e);
            return null;
        }
    }

    /**
     * 扫描局域网内的设备
     * @param callback 扫描完成后的回调函数
     */
    public void scanDevices(ScanCallback callback) {
        String currentIp = getCurrentIpAddress();
        if (currentIp == null) {
            callback.onScanComplete(new ArrayList<>());
            return;
        }

        // 清除之前的扫描结果
        discoveredDevices.clear();

        // 获取IP地址的前三段
        String ipPrefix = currentIp.substring(0, currentIp.lastIndexOf(".") + 1);
        List<Future<?>> futures = new ArrayList<>();

        // 并发扫描1-254的IP地址
        for (int i = 1; i <= 254; i++) {
            final String targetIp = ipPrefix + i;
            Future<?> future = executorService.submit(() -> scanAddress(targetIp));
            futures.add(future);
        }

        // 等待所有扫描任务完成
        executorService.submit(() -> {
            try {
                for (Future<?> future : futures) {
                    future.get(TIMEOUT_MS + 100, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error waiting for scan completion", e);
            }
            // 在主线程中调用回调
            callback.onScanComplete(new ArrayList<>(discoveredDevices));
        });
    }

    /**
     * 扫描单个IP地址
     * @param ipAddress 要扫描的IP地址
     */
    private void scanAddress(String ipAddress) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, SCAN_PORT), TIMEOUT_MS);
            socket.close();
            // 如果连接成功，说明该IP上有设备在运行指定端口的服务
            synchronized (discoveredDevices) {
                discoveredDevices.add(ipAddress);
            }
        } catch (IOException ignored) {
            // 连接失败，说明该IP上没有运行服务
        }
    }

    /**
     * 关闭扫描器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * 扫描回调接口
     */
    public interface ScanCallback {
        /**
         * 扫描完成时调用
         * @param devices 发现的设备IP地址列表
         */
        void onScanComplete(List<String> devices);
    }
}