package com.yuwjoo.quickpass.views.main.presenter;

import android.util.Log;

import com.yuwjoo.quickpass.views.main.utils.UdpBroadcastReceiver;
import com.yuwjoo.quickpass.views.main.utils.UdpBroadcastSender;

public class DeviceManagePresenter implements IDeviceManagePresenter, UdpBroadcastReceiver.BroadcastListener {
    private final String TAG = "DeviceManagePresenter";
    private static final int UDP_PORT = 8888; // 自定义端口
    private final UdpBroadcastReceiver receiver;

    public DeviceManagePresenter() {
        this.receiver = new UdpBroadcastReceiver();
        receiver.startListening(UDP_PORT, this); // 开始监听UDP广播
    }

    @Override
    public void searchAllDevice() {
        UdpBroadcastSender.sendBroadcast("Hello from Android!", UDP_PORT); // 发送广播
    }

    @Override
    public void onBroadcastReceived(String message, String senderIp) {
        Log.i(TAG, "UDP广播监听成功：" + message + ", ip: " + senderIp);
    }

    public void destroy() {
        receiver.stopListening();
        UdpBroadcastReceiver.shutdown();
        UdpBroadcastSender.shutdown();
    }
}
