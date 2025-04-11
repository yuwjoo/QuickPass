package com.yuwjoo.quickpass.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.yuwjoo.quickpass.R;
import com.yuwjoo.quickpass.model.FileItem;
import com.yuwjoo.quickpass.server.FileShareHttpServer;

import java.util.List;

/**
 * 文件列表适配器
 * 用于在RecyclerView中显示选择的文件列表
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<FileItem> fileItems;
    private final Context context;
    private final FileShareHttpServer fileShareHttpServer;
    private final OnFileRemovedListener onFileRemovedListener;

    /**
     * 构造函数
     * @param context 上下文
     * @param fileItems 文件项列表
     * @param fileShareHttpServer 文件分享服务器
     * @param onFileRemovedListener 文件移除监听器
     */
    public FileAdapter(Context context, List<FileItem> fileItems, FileShareHttpServer fileShareHttpServer, OnFileRemovedListener onFileRemovedListener) {
        this.context = context;
        this.fileItems = fileItems;
        this.fileShareHttpServer = fileShareHttpServer;
        this.onFileRemovedListener = onFileRemovedListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileItems.get(position);
        holder.tvFileName.setText(fileItem.getFileName());
        holder.tvFileSize.setText(fileItem.getFormattedSize());

        // 设置删除按钮点击事件
        holder.btnDeleteFile.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                FileItem item = fileItems.get(adapterPosition);
                // 从服务器移除文件
                if (item.getShareId() != null) {
                    fileShareHttpServer.removeFile(item.getShareId());
                }
                // 从列表移除文件
                fileItems.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                // 通知监听器
                if (onFileRemovedListener != null) {
                    onFileRemovedListener.onFileRemoved(item);
                }
            }
        });

        // 设置显示二维码按钮点击事件
        holder.btnShowQR.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                FileItem item = fileItems.get(adapterPosition);
                showShareDialog(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    /**
     * 显示分享对话框
     * @param fileItem 文件项
     */
    private void showShareDialog(FileItem fileItem) {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_share_qr, null);
        builder.setView(dialogView);

        // 获取对话框控件
        ImageView ivQRCode = dialogView.findViewById(R.id.ivQRCode);
        EditText etShareLink = dialogView.findViewById(R.id.etShareLink);
        Button btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);

        // 获取分享链接
        String shareLink = fileShareHttpServer.getShareLink(fileItem.getShareId());
        etShareLink.setText(shareLink);

        // 生成二维码并显示
        try {
            Bitmap qrCodeBitmap = generateQRCode(shareLink);
            if (qrCodeBitmap != null) {
                ivQRCode.setImageBitmap(qrCodeBitmap);
            }
        } catch (Exception e) {
            Log.e("FileAdapter", "Error generating QR code", e);
            Toast.makeText(context, "生成二维码失败", Toast.LENGTH_SHORT).show();
        }

        // 设置复制链接按钮点击事件
        btnCopyLink.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("分享链接", shareLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.link_copied), Toast.LENGTH_SHORT).show();
        });

        // 显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    /**
     * 生成二维码
     * @param content 二维码内容
     * @return 二维码位图
     */
    private Bitmap generateQRCode(String content) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Log.e("FileAdapter", "Error generating QR code", e);
            return null;
        }
    }

    /**
     * 文件视图持有者
     */
    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        TextView tvFileSize;
        ImageButton btnShowQR;
        ImageButton btnDeleteFile;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            btnShowQR = itemView.findViewById(R.id.btnShowQR);
            btnDeleteFile = itemView.findViewById(R.id.btnDeleteFile);
        }
    }

    /**
     * 文件移除监听器接口
     */
    public interface OnFileRemovedListener {
        /**
         * 当文件被移除时调用
         * @param fileItem 被移除的文件项
         */
        void onFileRemoved(FileItem fileItem);
    }
}