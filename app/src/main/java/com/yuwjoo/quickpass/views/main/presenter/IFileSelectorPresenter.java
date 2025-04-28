package com.yuwjoo.quickpass.views.main.presenter;

import android.content.Intent;
import android.net.Uri;

public interface IFileSelectorPresenter {
    /**
     * 打开文件选择器
     * 使用系统的Storage Access Framework来选择文件
     */
    void openFilePicker();

    /**
     * 获取文件选择结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     * @return 选中文件的Uri，如果选择被取消则返回null
     */
    Uri getFileSelectResult(int requestCode, int resultCode, Intent data);
}
