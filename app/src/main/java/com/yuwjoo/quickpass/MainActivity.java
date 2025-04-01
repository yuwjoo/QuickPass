package com.yuwjoo.quickpass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yuwjoo.quickpass.http.FileShare;

/**
 * 主活动类
 * 包含应用的主要界面和文件分享功能
 */
public class MainActivity extends AppCompatActivity {

    private FileShare fileShare;
    private EditText shareUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 初始化文件分享管理器
        fileShare = new FileShare(this);

        // 设置系统栏边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化UI组件
        shareUrlEditText = findViewById(R.id.shareUrlEditText);
        Button selectFileButton = findViewById(R.id.selectFileButton);
        Button copyButton = findViewById(R.id.copyButton);

        // 设置按钮点击事件
        selectFileButton.setOnClickListener(v -> fileShare.openFilePicker());
        copyButton.setOnClickListener(v -> copyShareUrl());
    }

    /**
     * 处理活动结果
     * 用于处理文件选择的返回结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String shareUrl = fileShare.handleActivityResult(requestCode, resultCode, data);
        if (shareUrl != null) {
            // 显示分享链接
            shareUrlEditText.setText(shareUrl);
            Toast.makeText(this, "分享链接已生成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 活动销毁时停止文件分享服务
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileShare != null) {
            fileShare.stop();
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
}