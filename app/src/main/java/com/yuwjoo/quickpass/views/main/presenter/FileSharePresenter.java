package com.yuwjoo.quickpass.views.main.presenter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.yuwjoo.quickpass.httpServer.HttpServer;
import com.yuwjoo.quickpass.okhttp.OkHttpUtils;
import com.yuwjoo.quickpass.views.main.view.IFileShareView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Response;

public class FileSharePresenter implements IFileSharePresenter {
    private final String TAG = "FileSharePresenter";
    private final Map<String, Uri> shareFileMap = new HashMap<>();// 分享文件Map
    private final Activity activity;
    private final IFileShareView fileShareView;
    private final HttpServer httpServer;

    public FileSharePresenter(Activity activity, IFileShareView fileShareView) {
        this.activity = activity;
        this.fileShareView = fileShareView;
        this.httpServer = new HttpServer();
        this.httpServer.getServer().get("/downloadFile", this::handleDownloadFile);
    }

    @Override
    public void addFile(Uri uri) {
        String id = UUID.randomUUID().toString();
        String link = getShareLink(id);
        shareFileMap.put(id, uri);
        fileShareView.onAddFileSuccess(link);
        try {
            JSONObject data = new JSONObject();
            data.put("link", link);
            try(Response response = OkHttpUtils.post("http://localhost:3000", data)){
                response.body();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFile(String id) {
        shareFileMap.remove(id);
    }

    @Override
    public String getShareLink(String id) {
        if (!shareFileMap.containsKey(id)) {
            return null;
        }
        return httpServer.getServerAddress(activity) + "/downloadFile?id=" + id;
    }

    @Override
    public void copyShareUrl(String shareUrl) {
        if (shareUrl.isEmpty()) {
            Toast.makeText(activity, "暂无分享链接", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("分享链接", shareUrl);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(activity, "链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理下载文件路由
     */
    private void handleDownloadFile(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        Multimap queryMultimap = request.getQuery();
        String fileId = queryMultimap.getString("id");
        Uri fileUri = shareFileMap.get(fileId);

        if (fileUri != null) {
            try {
                // 获取文件名
                String fileName = getFileNameFromUri(fileUri);
                // 获取文件MIME类型
                String mimeType = getMimeTypeFromUri(fileUri);

                // 打开文件流
                InputStream inputStream = activity.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // 设置响应头
                    response.getHeaders().add("Content-Type", mimeType);
                    response.getHeaders().add("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
                    // 发送文件内容
                    response.sendStream(inputStream, inputStream.available());
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending file", e);
            }
        }

        // 文件不存在或发生错误时返回404
        response.code(404);
        response.end();
    }

    /**
     * 从Uri获取文件名
     *
     * @param uri 文件Uri
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }

        return fileName != null ? fileName : "download";
    }

    /**
     * 从Uri获取文件MIME类型
     *
     * @param uri 文件Uri
     * @return MIME类型
     */
    private String getMimeTypeFromUri(Uri uri) {
        String mimeType = activity.getContentResolver().getType(uri);
        return mimeType != null ? mimeType : "application/octet-stream";
    }
}
