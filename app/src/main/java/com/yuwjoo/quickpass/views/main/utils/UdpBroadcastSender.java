package com.yuwjoo.quickpass.views.main.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpBroadcastSender {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void sendBroadcast(String message, int port) {
        executorService.execute(() -> {
            DatagramSocket socket = null;
            try {
                // 创建DatagramSocket
                socket = new DatagramSocket();
                socket.setBroadcast(true);

                // 将消息转换为字节数组
                byte[] sendData = message.getBytes();

                // 创建DatagramPacket，指定广播地址和端口
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), port
                );

                // 发送数据包
                socket.send(sendPacket);
                System.out.println("Broadcast message sent to: 255.255.255.255:" + port);
            } catch (Exception e) {
                System.err.println("Failed to send broadcast: " + e.getMessage());
            } finally {
                // 确保socket关闭
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
