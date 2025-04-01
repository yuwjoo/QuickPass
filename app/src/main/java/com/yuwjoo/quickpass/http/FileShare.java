package com.yuwjoo.quickpass.http;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * 文件分享管理类
 * 用于处理文件选择和分享链接生成
 */
public class FileShare {
    private static final int REQUEST_CODE_PICK_FILE = 2001;
    private final Activity activity;
    private final HttpServer httpServer;

    /**
     * 构造函数
     * @param activity 当前活动的实例
     */
    public FileShare(Activity activity) {
        this.activity = activity;
        this.httpServer = new HttpServer(activity);
        this.httpServer.start();
    }

    /**
     * 打开文件选择器
     * 使用系统的Storage Access Framework来选择文件
     */
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    /**
     * 处理文件选择结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     * @return 生成的分享链接，如果选择被取消则返回null
     */
    public String handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 获取永久访问权限
                    activity.getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    // 生成分享链接
                    return httpServer.addFile(uri);
                }
            }
        }
        return null;
    }

    /**
     * 停止文件分享服务
     */
    public void stop() {
        httpServer.stop();
    }
}