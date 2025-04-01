package com.yuwjoo.quickpass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yuwjoo.quickpass.manager.FileManager;
import com.yuwjoo.quickpass.server.FileServer;

import java.io.File;
import java.io.IOException;

/**
 * 主活动类
 * 实现文件选择和分享的用户界面
 */
public class MainActivity extends AppCompatActivity {
    private FileManager fileManager;
    private FileServer fileServer;
    private TextView shareUrlText;
    private Button selectFileButton;
    private Button startServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化组件
        initComponents();
        // 初始化模块
        initModules();
        // 设置点击事件
        setupClickListeners();
    }

    /**
     * 初始化UI组件
     */
    private void initComponents() {
        shareUrlText = findViewById(R.id.shareUrlText);
        selectFileButton = findViewById(R.id.selectFileButton);
        startServerButton = findViewById(R.id.startServerButton);
    }

    /**
     * 初始化功能模块
     */
    private void initModules() {
        fileManager = new FileManager(this);
        fileServer = new FileServer(this);
    }

    /**
     * 设置按钮点击事件
     */
    private void setupClickListeners() {
        // 选择文件按钮
        selectFileButton.setOnClickListener(v -> {
            if (fileManager.checkAndRequestPermissions()) {
                fileManager.openFilePicker();
            }
        });

        // 启动服务器按钮
        startServerButton.setOnClickListener(v -> startFileServer());
    }

    /**
     * 启动文件服务器
     */
    private void startFileServer() {
        try {
            String ipAddress = fileServer.start();
            startServerButton.setEnabled(false);
            Toast.makeText(this, "服务器已启动: " + ipAddress, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "服务器启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 处理文件选择结果
        File selectedFile = fileManager.handleFilePickerResult(requestCode, resultCode, data);
        if (selectedFile != null) {
            // 为选中的文件生成分享链接
            String shareUrl = fileServer.assignFilePath(selectedFile);
            shareUrlText.setText(shareUrl);
            shareUrlText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止文件服务器
        if (fileServer != null) {
            fileServer.stop();
        }
    }
}