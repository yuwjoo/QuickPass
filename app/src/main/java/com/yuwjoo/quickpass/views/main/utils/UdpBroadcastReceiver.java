package com.yuwjoo.quickpass.views.main.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpBroadcastReceiver {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private DatagramSocket socket;
    private boolean isListening = false;

    public void startListening(int port, BroadcastListener listener) {
        executorService.execute(() -> {
            try {
                // 创建DatagramSocket并绑定到指定端口
                socket = new DatagramSocket(port);
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                isListening = true;
                while (isListening) {
                    // 接收数据包
                    socket.receive(receivePacket);

                    // 将接收到的数据转换为字符串
                    String message = new String(
                            receivePacket.getData(),
                            0,
                            receivePacket.getLength()
                    );

                    // 回调监听器
                    if (listener != null) {
                        String senderIp = receivePacket.getAddress().getHostAddress();
                        listener.onBroadcastReceived(message, senderIp);
                    }
                }
            } catch (Exception e) {
                System.err.println("UDP listening error: " + e.getMessage());
            } finally {
                stopListening();
            }
        });
    }

    public void stopListening() {
        isListening = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void shutdown() {
        executorService.shutdown();
    }

    // 定义回调接口
    public interface BroadcastListener {
        void onBroadcastReceived(String message, String senderIp);
    }
}
