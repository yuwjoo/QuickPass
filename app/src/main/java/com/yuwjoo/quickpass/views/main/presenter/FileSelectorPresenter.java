package com.yuwjoo.quickpass.views.main.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class FileSelectorPresenter implements IFileSelectorPresenter {
    private final int REQUEST_CODE_PICK_FILE = 1001;
    private final Activity activity;

    public FileSelectorPresenter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    @Override
    public Uri getFileSelectResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            return data != null ? data.getData() : null;
        }
        return null;
    }
}
