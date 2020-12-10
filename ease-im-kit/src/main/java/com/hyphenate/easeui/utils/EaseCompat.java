package com.hyphenate.easeui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.R;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.VersionUtils;

import java.io.File;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
     * take picture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    public static File takePicture(Activity context, int requestCode) {
        if(!EaseCommonUtils.isSdcardExist()) {
            return null;
        }
        File cameraFile = getCameraFile();
        Intent intent = getCameraIntent(context, cameraFile);
        context.startActivityForResult(intent, requestCode);
        return cameraFile;
    }

    /**
     * take picture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    public static File takePicture(Fragment context, int requestCode) {
        if(!EaseCommonUtils.isSdcardExist()) {
            return null;
        }
        File cameraFile = getCameraFile();
        Intent intent = getCameraIntent(context.getContext(), cameraFile);
        context.startActivityForResult(intent, requestCode);
        return cameraFile;
    }

    /**
     * take video capture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    public static File takeVideo(Activity context, int requestCode) {
        if(!EaseCommonUtils.isSdcardExist()) {
            return null;
        }
        File videoFile = getVideoFile();
        Intent intent = getVideoIntent(context, videoFile);
        context.startActivityForResult(intent, requestCode);
        return videoFile;
    }

    /**
     * take video capture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    public static File takeVideo(Fragment context, int requestCode) {
        if(!EaseCommonUtils.isSdcardExist()) {
            return null;
        }
        File videoFile = getVideoFile();
        Intent intent = getVideoIntent(context.getContext(), videoFile);
        context.startActivityForResult(intent, requestCode);
        return videoFile;
    }

    private static Intent getCameraIntent(Context context, File cameraFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(context, cameraFile));
        return intent;
    }

    private static File getCameraFile() {
        File cameraFile = new File(PathUtil.getInstance().getImagePath()
                , EMClient.getInstance().getCurrentUser() + System.currentTimeMillis() + ".jpg");
        //noinspection ResultOfMethodCallIgnored
        cameraFile.getParentFile().mkdirs();
        return cameraFile;
    }

    private static Intent getVideoIntent(Context context, File videoFile) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(context, videoFile));
        return intent;
    }

    private static File getVideoFile() {
        File videoFile = new File(PathUtil.getInstance().getVideoPath()
                , System.currentTimeMillis() + ".mp4");
        //noinspection ResultOfMethodCallIgnored
        videoFile.getParentFile().mkdirs();
        return videoFile;
    }

    /**
     * open file
     *
     * @param f
     * @param context
     */
    public static void openFile(File f, Activity context) {
        openFile(context, f);
    }

    /**
     * 打开文件
     * @param context
     * @param filePath
     */
    public static void openFile(Context context, String filePath) {
        if(TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            EMLog.e(TAG, "文件不存在！");
            return;
        }
        openFile(context, new File(filePath));
    }

    /**
     * 打开文件
     * @param context
     * @param file
     */
    public static void openFile(Context context, File file) {
        String filename = file.getName();
        String mimeType = getMimeType(context, file);
        /* get uri */
        Uri uri = getUriForFile(context, file);
        //为了解决本地视频文件打不开的问题
        if(isVideoFile(context, filename)) {
            uri = Uri.parse(file.getAbsolutePath());
        }
        openFile(context, uri, filename, mimeType);
    }

    /**
     * 打开文件
     * @param context
     * @param uri
     */
    public static void openFile(Context context, Uri uri) {
        String filePath = EaseFileUtils.getFilePath(context, uri);
        //如果可以获取文件的绝对路径，则需要根据sdk版本处理FileProvider的问题
        if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            openFile(context, new File(filePath));
            return;
        }
        String filename = getFileNameByUri(context, uri);
        String mimeType = getMimeType(context, filename);
        openFile(context, uri, filename, mimeType);
    }

    /**
     * 打开文件
     * @param context
     * @param uri 此uri由FileProvider及ContentProvider生成
     * @param filename
     * @param mimeType
     */
    public static void openFile(Context context, Uri uri, String filename, String mimeType) {
        if(openApk(context, uri)) {
            return;
        }
        EMLog.d(TAG, "openFile filename = "+filename + " mimeType = "+mimeType);
        EMLog.d(TAG, "openFile uri = "+ (uri != null ? uri.toString() : "uri is null"));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        setIntentByType(context, filename, intent);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /* set intent's file and MimeType */
        intent.setDataAndType(uri, mimeType);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            EMLog.e(TAG, e.getMessage());
            Toast.makeText(context, "Can't find proper app to open this file", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 删除文件
     * @param context
     * @param uri
     */
    public static void deleteFile(Context context, Uri uri) {
        EaseFileUtils.deleteFile(context, uri);
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

    /**
     * 获取视频第一帧图片
     * @param context
     * @param videoUri
     * @return
     */
    public static String getVideoThumbnail(Context context, @NonNull Uri videoUri) {
        File file = new File(PathUtil.getInstance().getVideoPath(), "thvideo" + System.currentTimeMillis());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(context, videoUri);
            Bitmap frameAtTime = media.getFrameAtTime();
            frameAtTime.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * 用于检查从多媒体获取文件是否是视频
     * @param context
     * @param uri
     * @return
     */
    public static boolean isVideoType(Context context, @NonNull Uri uri) {
        String mimeType = getMimeType(context, uri);
        if(TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.startsWith("video");
    }

    /**
     * 用于检查从多媒体获取文件是否是图片
     * @param context
     * @param uri
     * @return
     */
    public static boolean isImageType(Context context, @NonNull Uri uri) {
        String mimeType = getMimeType(context, uri);
        if(TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.startsWith("image");
    }

    /**
     * 获取文件mime type
     * @param context
     * @param uri
     * @return
     */
    public static String getMimeType(Context context, @NonNull Uri uri) {
        return context.getContentResolver().getType(uri);
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
        String filename = getFileNameByUri(context, uri);
        return openApk(context, uri, filename);
    }

    public static boolean openApk(Context context, Uri uri, @NonNull String filename) {
        String filePath = EaseFileUtils.getFilePath(context, uri);
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
     * 获取文件名
     * @param context
     * @param fileUri
     * @return
     */
    public static String getFileNameByUri(Context context, Uri fileUri) {
        return EaseFileUtils.getFileNameByUri(context, fileUri);
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

        return EaseFileUtils.getFilePath(context, uri);
    }

}
