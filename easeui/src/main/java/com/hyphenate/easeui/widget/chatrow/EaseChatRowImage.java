package com.hyphenate.easeui.widget.chatrow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.UriUtils;

import java.io.File;
import java.io.IOException;

import static com.hyphenate.chat.EMClient.TAG;

public class EaseChatRowImage extends EaseChatRowFile{

    protected ImageView imageView;
    private EMImageMessageBody imgBody;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ? R.layout.ease_row_received_picture : R.layout.ease_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    
    @Override
    protected void onSetUpView() {
        imgBody = (EMImageMessageBody) message.getBody();

        // received messages
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            return;
        }

        Uri filePath = imgBody.getLocalUri();
        Uri thumbnailUrl = imgBody.thumbnailLocalUri();
        showImageView(thumbnailUrl, filePath, message);
    }

    @Override
    protected void onViewUpdate(EMMessage msg) {
        if (msg.direct() == EMMessage.Direct.SEND) {
            if(EMClient.getInstance().getOptions().getAutodownloadThumbnail()){
                super.onViewUpdate(msg);
            }else{
                if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                        imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING ||
                            imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED) {
                    progressBar.setVisibility(View.INVISIBLE);
                    percentageView.setVisibility(View.INVISIBLE);
                    imageView.setImageResource(R.drawable.ease_default_image);
                } else {
                    progressBar.setVisibility(View.GONE);
                    percentageView.setVisibility(View.GONE);
                    imageView.setImageResource(R.drawable.ease_default_image);
                    Uri thumbPath = imgBody.thumbnailLocalUri();
                    showImageView(thumbPath, imgBody.getLocalUri(), message);
                }
            }
            return;
        }

        // received messages
        if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
            if(EMClient.getInstance().getOptions().getAutodownloadThumbnail()){
                imageView.setImageResource(R.drawable.ease_default_image);
            }else {
                progressBar.setVisibility(View.INVISIBLE);
                percentageView.setVisibility(View.INVISIBLE);
                imageView.setImageResource(R.drawable.ease_default_image);
            }
        } else if(imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED){
            if(EMClient.getInstance().getOptions().getAutodownloadThumbnail()){
                progressBar.setVisibility(View.VISIBLE);
                percentageView.setVisibility(View.VISIBLE);
            }else {
                progressBar.setVisibility(View.INVISIBLE);
                percentageView.setVisibility(View.INVISIBLE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            percentageView.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.ease_default_image);
            Uri thumbPath = imgBody.thumbnailLocalUri();
            showImageView(thumbPath, imgBody.getLocalUri(), message);
        }
    }

    /**
     * load image into image view
     * 
     */
    @SuppressLint("StaticFieldLeak")
    private void showImageView(final Uri thumbernailPath, final Uri localFullSizePath, final EMMessage message) {
        // first check if the thumbnail image already loaded into cache s
        Bitmap bitmap = EaseImageCache.getInstance().get(thumbernailPath.toString());

        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            imageView.setImageBitmap(checkBitmap(bitmap));
        } else {
            imageView.setImageResource(R.drawable.ease_default_image);
            new AsyncTask<Object, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Object... args) {
                    if (UriUtils.isFileExistByUri(context, thumbernailPath)) {
                        return getCacheBitmap(thumbernailPath);
                    } else if(UriUtils.isFileExistByUri(context, localFullSizePath)) {
                        return getCacheBitmap(localFullSizePath);
                    } else {
                        if (message.direct() == EMMessage.Direct.SEND) {
                            if (UriUtils.isFileExistByUri(context, localFullSizePath)) {
                                String filePath = UriUtils.getFilePath(context, localFullSizePath);
                                if(!TextUtils.isEmpty(filePath)) {
                                    return EaseImageUtils.decodeScaleImage(filePath, 160, 160);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    try {
                                        return EaseImageUtils.decodeScaleImage(context, localFullSizePath, 160, 160);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                }
                            }
                            return null;
                        }
                        return null;
                    }
                }

                protected void onPostExecute(Bitmap image) {
                    if (image != null) {
                        EMLog.d("img", "bitmap width = "+image.getWidth() + " height = "+image.getHeight());
                        imageView.setImageBitmap(image);
                        EaseImageCache.getInstance().put(thumbernailPath.toString(), image);
                    }
                }

                private Bitmap getCacheBitmap(Uri fileUri) {
                    String filePath = UriUtils.getFilePath(context, fileUri);
                    EMLog.d(EaseChatRow.TAG, "fileUri = "+fileUri);
                    if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                        return EaseImageUtils.decodeScaleImage(filePath, 160, 160);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            return EaseImageUtils.decodeScaleImage(context, fileUri, 160, 160);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            }.execute();
        }
    }

    private Bitmap checkBitmap(Bitmap bitmap) {
        if(bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.e("TAG", "width = "+width+ " height = "+height);
            if(width > 160 || height > 160) {
                return Bitmap.createScaledBitmap(bitmap, 160, 160, true);
            }
        }
        return bitmap;
    }


}
