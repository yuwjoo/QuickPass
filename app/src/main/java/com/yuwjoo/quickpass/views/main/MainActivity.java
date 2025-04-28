package com.yuwjoo.quickpass.views.main;

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
import com.yuwjoo.quickpass.views.fileShare.FileShareActivity;
import com.yuwjoo.quickpass.R;
import com.yuwjoo.quickpass.views.main.presenter.FileSelectorPresenter;
import com.yuwjoo.quickpass.views.main.presenter.FileSharePresenter;
import com.yuwjoo.quickpass.views.main.view.IFileShareView;

public class MainActivity extends AppCompatActivity implements IFileShareView {
    private EditText shareUrlEditText;
    private FileSelectorPresenter fileSelectorPresenter;
    private FileSharePresenter fileSharePresenter;

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

        initView();
        initPresenter();
    }

    private void initView() {
        shareUrlEditText = findViewById(R.id.shareUrlEditText);
        Button selectFileButton = findViewById(R.id.selectFileButton);
        Button copyButton = findViewById(R.id.copyButton);
        Button openFileShareButton = findViewById(R.id.btnOpenFileShare);

        selectFileButton.setOnClickListener(v -> fileSelectorPresenter.openFilePicker());
        copyButton.setOnClickListener(v -> fileSharePresenter.copyShareUrl(shareUrlEditText.getText().toString()));
        openFileShareButton.setOnClickListener(v -> openFileShareActivity());
    }

    private void initPresenter() {
        fileSelectorPresenter = new FileSelectorPresenter(this); // 文件选择功能
        fileSharePresenter = new FileSharePresenter(this, this); // 文件分享功能
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = fileSelectorPresenter.getFileSelectResult(requestCode, resultCode, data);
        fileSharePresenter.addFile(uri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 打开文件分享活动
     * 跳转到高级文件分享页面
     */
    private void openFileShareActivity() {
        Intent intent = new Intent(this, FileShareActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAddFileSuccess(String link) {
        shareUrlEditText.setText(link);
        Toast.makeText(this, "分享链接已生成", Toast.LENGTH_SHORT).show();
    }
}