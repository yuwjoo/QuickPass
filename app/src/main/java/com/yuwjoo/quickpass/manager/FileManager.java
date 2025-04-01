package com.yuwjoo.quickpass.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件管理模块
 * 负责处理文件选择和管理相关功能
 */
public class FileManager {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int FILE_PICKER_REQUEST_CODE = 1002;
    private final Activity activity;

    public FileManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * 检查并请求必要的权限
     * @return 是否已获得所有必要权限
     */
    public boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本需要请求所有文件访问权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivity(intent);
                return false;
            }
        } else {
            // 检查存储权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 打开文件选择器
     */
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "选择要分享的文件"), FILE_PICKER_REQUEST_CODE);
    }

    /**
     * 处理文件选择结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data Intent数据
     * @return 选择的文件，如果选择失败则返回null
     */
    public File handleFilePickerResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    // 将Uri转换为File
                    return uriToFile(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 将Uri转换为File
     * @param uri 文件Uri
     * @return 转换后的File对象
     */
    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = activity.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("无法打开文件流");
        }

        // 创建临时文件
        String fileName = getFileNameFromUri(uri);
        File tempFile = new File(activity.getCacheDir(), fileName);

        // 复制文件内容
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } finally {
            inputStream.close();
        }

        return tempFile;
    }

    /**
     * 从Uri中获取文件名
     * @param uri 文件Uri
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    result = cursor.getString(index);
                }
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}