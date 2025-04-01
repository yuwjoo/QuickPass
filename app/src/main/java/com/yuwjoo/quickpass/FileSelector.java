package com.yuwjoo.quickpass;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * 文件选择器类
 * 用于处理文件选择相关的逻辑
 */
public class FileSelector {
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    private final Activity activity;

    /**
     * 构造函数
     * @param activity 当前活动的实例
     */
    public FileSelector(Activity activity) {
        this.activity = activity;
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
     * @return 选中文件的Uri，如果选择被取消则返回null
     */
    public Uri handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                return data.getData();
            }
        }
        return null;
    }
}