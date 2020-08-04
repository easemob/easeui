package com.hyphenate.easeui.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.hyphenate.util.EMLog;
import com.hyphenate.util.UriUtils;
import com.hyphenate.util.VersionUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class EasePathUtils {

    private static final String TAG = EasePathUtils.class.getSimpleName();

    /**
     * 获取文件名
     * @param context
     * @param fileUri
     * @return
     */
    public static String getFileNameByUri(Context context, Uri fileUri) {
        if(fileUri == null) {
            return "";
        }
        //target 小于Q
        if(!VersionUtils.isTargetQ(context)) {
            String filePath = getFilePath(context, fileUri);
            if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                return new File(filePath).getName();
            }
            return "";
        }
        //target 大于Q
        if(UriUtils.uriStartWithFile(fileUri)) {
            File file = new File(fileUri.getPath());
            return file.exists() ? file.getName() : "";
        }
        if(!UriUtils.uriStartWithContent(fileUri)) {
            if(fileUri.toString().startsWith("/") && new File(fileUri.toString()).exists()) {
                return new File(fileUri.toString()).getName();
            }
            return "";
        }

        DocumentFile documentFile = getDocumentFile(context, fileUri);
        if(documentFile == null) {
            return "";
        }
        return documentFile.getName();
    }

    private static DocumentFile getDocumentFile(Context context, Uri uri) {
        if(uri == null) {
            EMLog.e(TAG, "uri is null");
            return null;
        }
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if(documentFile == null) {
            EMLog.e(TAG, "DocumentFile is null");
            return null;
        }
        return documentFile;
    }


    public static String getMimeType(Context context, Uri fileUri) {
        String filePath = getFilePath(context, fileUri);
        if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            return getMimeType(new File(filePath));
        }
        return context.getContentResolver().getType(fileUri);
    }

    public static String getMimeType(File sourceFile){
        String sourceName=sourceFile.getName();
        return getMimeType(sourceName);
    }

    public static String getMimeType(String fileName) {
        if (fileName.endsWith(".3gp") || fileName.endsWith(".amr")) {
            return "audio/3gp";
        }if(fileName.endsWith(".jpe")||fileName.endsWith(".jpeg")||fileName.endsWith(".jpg")){
            return "image/jpeg";
        }if(fileName.endsWith(".amr")){
            return "audio/amr";
        }if(fileName.endsWith(".mp4")){
            return "video/mp4";
        }if(fileName.endsWith(".mp3")){
            return "audio/mpeg";
        }else {
            return "application/octet-stream";
        }
    }

    public static String getFilePath(Context context, String path) {
        if(TextUtils.isEmpty(path)) {
            return path;
        }
        Uri uri = Uri.parse(path);
        return UriUtils.getFilePath(context, uri);
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
    public static String getFilePath(final Context context, final Uri uri) {
        if(uri == null) {
            return "";
        }
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        //sdk版本在29之前的
        if(!VersionUtils.isTargetQ(context)) {
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            else if(isFileProvider(context, uri)) {
                return getFPUriToPath(context, uri);
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }else if(uri.toString().startsWith("/")) {//如果是路径的话，返回路径
                return uri.toString();
            }
        }else {
            //29之后，判断是否是file开头及是否是以"/"开头
            if(UriUtils.uriStartWithFile(uri)) {
                return uri.getPath();
            }
            if(uri.toString().startsWith("/")) {
                return uri.toString();
            }
        }
        return "";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * 从FileProvider获取文件路径
     * @param context
     * @param uri
     * @return
     */
    private static String getFPUriToPath(Context context, Uri uri) {
        try {
            List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs != null) {
                String fileProviderClassName = FileProvider.class.getName();
                for (PackageInfo pack : packs) {
                    ProviderInfo[] providers = pack.providers;
                    if (providers != null) {
                        for (ProviderInfo provider : providers) {
                            if (uri.getAuthority().equals(provider.authority)) {
                                if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                                    Class<FileProvider> fileProviderClass = FileProvider.class;
                                    try {
                                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                                        getPathStrategy.setAccessible(true);
                                        Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
                                        if (invoke != null) {
                                            String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                                            Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                                            Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                                            getFileForUri.setAccessible(true);
                                            Object invoke1 = getFileForUri.invoke(invoke, uri);
                                            if (invoke1 instanceof File) {
                                                String filePath = ((File) invoke1).getAbsolutePath();
                                                return filePath;
                                            }
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 需要与项目中的FileProvider的authorities属性对应上，例如demo中的authorities为
     * android:authorities="${applicationId}.fileProvider"，则下面的代码为context.getApplicationInfo().packageName + ".fileProvider"
     * @param context
     * @param uri
     * @return
     */
    public static boolean isFileProvider(Context context, Uri uri) {
        return (context.getApplicationInfo().packageName + ".fileProvider").equalsIgnoreCase(uri.getAuthority());
    }
}

