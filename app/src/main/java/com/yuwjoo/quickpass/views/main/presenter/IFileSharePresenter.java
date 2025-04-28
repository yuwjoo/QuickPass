package com.yuwjoo.quickpass.views.main.presenter;

import android.net.Uri;

public interface IFileSharePresenter {
    /**
     * 从分享列表添加文件
     *
     * @param uri 文件的Uri
     */
    void addFile(Uri uri);

    /**
     * 从分享列表删除文件
     *
     * @param id 文件id
     */
    void removeFile(String id);

    /**
     * 获取文件分享链接
     *
     * @param id 文件id
     * @return 文件下载链接
     */
    String getShareLink(String id);


    /**
     * 复制分享链接到剪贴板
     */
    void copyShareUrl(String shareUrl);
}
