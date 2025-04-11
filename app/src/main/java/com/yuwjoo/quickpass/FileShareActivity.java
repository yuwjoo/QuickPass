package com.yuwjoo.quickpass;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yuwjoo.quickpass.adapter.DeviceAdapter;
import com.yuwjoo.quickpass.adapter.FileAdapter;
import com.yuwjoo.quickpass.model.FileItem;
import com.yuwjoo.quickpass.server.DeviceScanner;
import com.yuwjoo.quickpass.server.FileShareHttpServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件分享活动类
 * 用于实现文件选择、列表展示、设备扫描和文件分享功能
 */
public class FileShareActivity extends AppCompatActivity implements FileAdapter.OnFileRemovedListener, DeviceAdapter.OnDeviceClickListener {

    private static final int REQUEST_CODE_PICK_FILE = 1001;

    private RecyclerView rvSelectedFiles;
    private RecyclerView rvDevices;
    private Button btnSelectFiles;
    private Button btnScanDevices;

    private FileAdapter fileAdapter;
    private DeviceAdapter deviceAdapter;
    private FileShareHttpServer fileShareHttpServer;
    private DeviceScanner deviceScanner;

    private List<FileItem> selectedFiles;
    private List<String> discoveredDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_file_share);

        // 设置系统栏边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fileShareLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化数据
        selectedFiles = new ArrayList<>();
        discoveredDevices = new ArrayList<>();

        // 初始化服务
        fileShareHttpServer = new FileShareHttpServer(this);
        fileShareHttpServer.start();
        deviceScanner = new DeviceScanner(this);

        // 初始化视图
        initViews();
        setupRecyclerViews();
        setupListeners();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        rvSelectedFiles = findViewById(R.id.rvSelectedFiles);
        rvDevices = findViewById(R.id.rvDevices);
        btnSelectFiles = findViewById(R.id.btnSelectFiles);
        btnScanDevices = findViewById(R.id.btnScanDevices);
    }

    /**
     * 设置RecyclerView
     */
    private void setupRecyclerViews() {
        // 设置文件列表
        fileAdapter = new FileAdapter(this, selectedFiles, fileShareHttpServer, this);
        rvSelectedFiles.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedFiles.setAdapter(fileAdapter);

        // 设置设备列表
        deviceAdapter = new DeviceAdapter(this, discoveredDevices, this);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(deviceAdapter);
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 选择文件按钮点击事件
        btnSelectFiles.setOnClickListener(v -> openFilePicker());

        // 扫描设备按钮点击事件
        btnScanDevices.setOnClickListener(v -> scanDevices());
    }

    /**
     * 打开文件选择器
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    /**
     * 扫描内网设备
     */
    private void scanDevices() {
        btnScanDevices.setEnabled(false);
        btnScanDevices.setText(getString(R.string.scanning));

        deviceScanner.scanDevices(devices -> {
            runOnUiThread(() -> {
                discoveredDevices.clear();
                discoveredDevices.addAll(devices);
                deviceAdapter.notifyDataSetChanged();
                btnScanDevices.setEnabled(true);
                btnScanDevices.setText(getString(R.string.scan_devices));

                if (devices.isEmpty()) {
                    Toast.makeText(FileShareActivity.this, getString(R.string.no_devices_found), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FileShareActivity.this, getString(R.string.devices_found, devices.size()), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * 处理文件选择结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 获取文件信息
                    String fileName = getFileName(uri);
                    long fileSize = getFileSize(uri);

                    // 创建文件项
                    FileItem fileItem = new FileItem(fileName, fileSize, uri);

                    // 添加到服务器
                    String fileId = fileShareHttpServer.addFile(uri);
                    fileItem.setShareId(fileId);

                    // 添加到列表
                    selectedFiles.add(fileItem);
                    fileAdapter.notifyItemInserted(selectedFiles.size() - 1);
                }
            }
        }
    }

    /**
     * 获取文件名
     * @param uri 文件URI
     * @return 文件名
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    /**
     * 获取文件大小
     * @param uri 文件URI
     * @return 文件大小（字节）
     */
    private long getFileSize(Uri uri) {
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 文件移除监听器回调
     * @param fileItem 被移除的文件项
     */
    @Override
    public void onFileRemoved(FileItem fileItem) {
        // 可以在这里添加额外的处理逻辑
    }

    /**
     * 设备点击监听器回调
     * @param deviceIp 被点击的设备IP地址
     */
    @Override
    public void onDeviceClick(String deviceIp) {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_files_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示分享进度提示
        Toast.makeText(this, getString(R.string.sending_files, deviceIp), Toast.LENGTH_SHORT).show();
        
        // 创建分享链接列表
        StringBuilder shareLinksBuilder = new StringBuilder();
        for (FileItem fileItem : selectedFiles) {
            String shareLink = fileShareHttpServer.getShareLink(fileItem.getShareId());
            if (shareLink != null) {
                shareLinksBuilder.append(fileItem.getFileName())
                        .append(": ")
                        .append(shareLink)
                        .append("\n");
            }
        }
        
        // 创建分享意图
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_file));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareLinksBuilder.toString());
        
        // 启动分享活动
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_file)));
    }

    /**
     * 活动销毁时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileShareHttpServer != null) {
            fileShareHttpServer.stop();
        }
        if (deviceScanner != null) {
            deviceScanner.shutdown();
        }
    }
}