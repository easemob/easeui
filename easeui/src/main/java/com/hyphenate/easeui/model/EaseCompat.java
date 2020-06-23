package com.hyphenate.easeui.model;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.hyphenate.easeui.R;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.FileUtils;
import com.hyphenate.util.UriUtils;
import com.hyphenate.util.VersionUtils;

import java.io.File;

/**
 * Created by zhangsong on 18-6-6.
 */

public class EaseCompat {
    private static final String TAG = "EaseCompat";

    public static void openImage(Activity context, int requestCode) {
        Intent intent = null;
        if(VersionUtils.isTargetQ(context)) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        context.startActivityForResult(intent, requestCode);
    }

    public static void openImage(Fragment context, int requestCode) {
        Intent intent = null;
        if(VersionUtils.isTargetQ(context.getActivity())) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }else {
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
            } else {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
        }
        intent.setType("image/*");
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * open file
     *
     * @param f
     * @param context
     */
    public static void openFile(File f, Activity context) {
        /* get uri */
        Uri uri = getUriForFile(context, f);
        //为了解决本地视频文件打不开的问题
        if(isVideoFile(context, f.getName())) {
            uri = Uri.parse(f.getAbsolutePath());
        }
        openFile(uri, context);
    }

    public static void openFile(Uri uri, String type, Activity context) {
        if(openApk(context, uri)) {
            return;
        }
        EMLog.e(TAG, "openFile uri = "+uri + " type = "+type);
        String filename = UriUtils.getFileNameByUri(context, uri);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        setIntentByType(context, filename, intent);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /* set intent's file and MimeType */
        intent.setDataAndType(uri, type);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            EMLog.e(TAG, e.getMessage());
            Toast.makeText(context, "Can't find proper app to open this file", Toast.LENGTH_LONG).show();
        }
    }

    public static void openFile(Uri uri, Activity context) {
        String mimeType = UriUtils.getMimeType(context, uri);
        if(TextUtils.isEmpty(mimeType) || TextUtils.equals(mimeType, "application/octet-stream")) {
            mimeType = getMimeType(context, UriUtils.getFileNameByUri(context, uri));
        }
        EMLog.d(TAG, "mimeType = "+mimeType);
        openFile(uri, mimeType, context);
    }

    public static Uri getUriForFile(Context context, @NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static int getSupportedWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

    public static String getMimeType(Context context, @NonNull File file) {
        return getMimeType(context, file.getName());
    }

    public static String getMimeType(Context context, String filename) {
        String mimeType = null;
        Resources resources = context.getResources();
        //先设置常用的后缀

        if(checkSuffix(filename, resources.getStringArray(R.array.ease_image_file_suffix))) {
            mimeType = "image/*";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_video_file_suffix))) {
            mimeType = "video/*";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_audio_file_suffix))) {
            mimeType = "audio/*";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_file_file_suffix))) {
            mimeType = "text/plain";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_word_file_suffix))) {
            mimeType = "application/msword";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_excel_file_suffix))) {
            mimeType = "application/vnd.ms-excel";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_pdf_file_suffix))) {
            mimeType = "application/pdf";
        }else if(checkSuffix(filename, resources.getStringArray(R.array.ease_apk_file_suffix))) {
            mimeType = "application/vnd.android.package-archive";
        }else {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    /**
     * 判断是否是视频文件
     * @param context
     * @param filename
     * @return
     */
    public static boolean isVideoFile(Context context, String filename) {
        return checkSuffix(filename, context.getResources().getStringArray(R.array.ease_video_file_suffix));
    }

    public static void setIntentByType(Context context, String filename, Intent intent) {
        Resources rs = context.getResources();
        if(checkSuffix(filename, rs.getStringArray(R.array.ease_audio_file_suffix))
            || checkSuffix(filename, rs.getStringArray(R.array.ease_video_file_suffix))) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
        }else if(checkSuffix(filename, rs.getStringArray(R.array.ease_image_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }else if(checkSuffix(filename, rs.getStringArray(R.array.ease_excel_file_suffix))
                || checkSuffix(filename, rs.getStringArray(R.array.ease_word_file_suffix))
                || checkSuffix(filename, rs.getStringArray(R.array.ease_pdf_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }else {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    public static boolean openApk(Context context, Uri uri) {
        String filename = UriUtils.getFileNameByUri(context, uri);
        String filePath = UriUtils.getFilePath(context, uri);
        if(filename.endsWith(".apk")) {
            if(TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
                Toast.makeText(context, "Can't find proper app to open this file", Toast.LENGTH_LONG).show();
                return true;
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(filePath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, getMimeType(context, filename));
                context.startActivity(intent);
            }else {
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setDataAndType(Uri.fromFile(new File(filePath)), getMimeType(context, filename));
                context.startActivity(installIntent);
            }
            return true;
        }
        return false;
    }

    /**
     * 检查后缀
     * @param filename
     * @param fileSuffix
     * @return
     */
    private static boolean checkSuffix(String filename, String[] fileSuffix) {
        if(TextUtils.isEmpty(filename) || fileSuffix == null || fileSuffix.length <= 0) {
            return false;
        }
        int length = fileSuffix.length;
        for(int i = 0; i < length; i++) {
            String suffix = fileSuffix[i];
            if(filename.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        return UriUtils.getFilePath(context, uri);
    }

}
