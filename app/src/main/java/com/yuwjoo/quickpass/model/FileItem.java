package com.yuwjoo.quickpass.model;

import android.net.Uri;

/**
 * 文件项模型类
 * 用于表示选择的文件信息
 */
public class FileItem {
    private String fileName;
    private long fileSize;
    private Uri fileUri;
    private String shareId;

    /**
     * 构造函数
     * @param fileName 文件名
     * @param fileSize 文件大小（字节）
     * @param fileUri 文件URI
     */
    public FileItem(String fileName, long fileSize, Uri fileUri) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUri = fileUri;
    }

    /**
     * 获取文件名
     * @return 文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     * @param fileName 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件大小
     * @return 文件大小（字节）
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小
     * @param fileSize 文件大小（字节）
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取文件URI
     * @return 文件URI
     */
    public Uri getFileUri() {
        return fileUri;
    }

    /**
     * 设置文件URI
     * @param fileUri 文件URI
     */
    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    /**
     * 获取分享ID
     * @return 分享ID
     */
    public String getShareId() {
        return shareId;
    }

    /**
     * 设置分享ID
     * @param shareId 分享ID
     */
    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    /**
     * 获取格式化的文件大小字符串
     * @return 格式化的文件大小（如：1.5 MB）
     */
    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}