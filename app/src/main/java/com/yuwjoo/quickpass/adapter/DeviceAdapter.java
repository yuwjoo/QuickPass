package com.yuwjoo.quickpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yuwjoo.quickpass.R;

import java.util.List;

/**
 * 设备列表适配器
 * 用于在RecyclerView中显示扫描到的内网设备列表
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<String> deviceIps;
    private final Context context;
    private final OnDeviceClickListener onDeviceClickListener;

    /**
     * 构造函数
     * @param context 上下文
     * @param deviceIps 设备IP地址列表
     * @param onDeviceClickListener 设备点击监听器
     */
    public DeviceAdapter(Context context, List<String> deviceIps, OnDeviceClickListener onDeviceClickListener) {
        this.context = context;
        this.deviceIps = deviceIps;
        this.onDeviceClickListener = onDeviceClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        String deviceIp = deviceIps.get(position);
        holder.tvDeviceIp.setText(deviceIp);

        // 设置发送按钮点击事件
        holder.btnSendToDevice.setOnClickListener(v -> {
            if (onDeviceClickListener != null) {
                onDeviceClickListener.onDeviceClick(deviceIp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceIps.size();
    }

    /**
     * 更新设备列表
     * @param newDevices 新的设备列表
     */
    public void updateDevices(List<String> newDevices) {
        deviceIps.clear();
        deviceIps.addAll(newDevices);
        notifyDataSetChanged();
    }

    /**
     * 设备视图持有者
     */
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceIp;
        Button btnSendToDevice;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceIp = itemView.findViewById(R.id.tvDeviceIp);
            btnSendToDevice = itemView.findViewById(R.id.btnSendToDevice);
        }
    }

    /**
     * 设备点击监听器接口
     */
    public interface OnDeviceClickListener {
        /**
         * 当设备被点击时调用
         * @param deviceIp 被点击的设备IP地址
         */
        void onDeviceClick(String deviceIp);
    }
}