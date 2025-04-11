package com.yuwjoo.quickpass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yuwjoo.quickpass.server.FileShareHttpServer;

/**
 * 主活动类
 * 包含应用的主要界面和文件分享功能
 */
public class MainActivity extends AppCompatActivity {

    private FileSelector fileSelector;
    private FileShareHttpServer fileShareHttpServer;
    private EditText shareUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 设置系统栏边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化文件选择管理器
        fileSelector = new FileSelector(this);

        // 初始化文件分享HTTP服务器
        fileShareHttpServer = new FileShareHttpServer(this);
        fileShareHttpServer.start();

        // 初始化UI组件
        shareUrlEditText = findViewById(R.id.shareUrlEditText);
        Button selectFileButton = findViewById(R.id.selectFileButton);
        Button copyButton = findViewById(R.id.copyButton);
        Button openFileShareButton = findViewById(R.id.btnOpenFileShare);

        // 设置按钮点击事件
        selectFileButton.setOnClickListener(v -> fileSelector.openFilePicker());
        copyButton.setOnClickListener(v -> copyShareUrl());
        openFileShareButton.setOnClickListener(v -> openFileShareActivity());
    }

    /**
     * 处理活动结果
     * 用于处理文件选择的返回结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = fileSelector.handleActivityResult(requestCode, resultCode, data);
        if (uri != null) {
            // 添加文件到分享列表
            String fileId = fileShareHttpServer.addFile(uri);
            // 显示分享链接
            shareUrlEditText.setText(fileShareHttpServer.getShareLink(fileId));
            Toast.makeText(this, "分享链接已生成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 活动销毁时停止文件分享服务
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileShareHttpServer != null) {
            fileShareHttpServer.stop();
        }
    }

    /**
     * 复制分享链接到剪贴板
     */
    private void copyShareUrl() {
        String shareUrl = shareUrlEditText.getText().toString();
        if (shareUrl.isEmpty()) {
            Toast.makeText(this, "暂无分享链接", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("分享链接", shareUrl);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 打开文件分享活动
     * 跳转到高级文件分享页面
     */
    private void openFileShareActivity() {
        Intent intent = new Intent(this, FileShareActivity.class);
        startActivity(intent);
    }
}