package com.hyphenate.easeui.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.UriUtils;

import java.io.File;

/**
 * show the video
 * 
 */
public class EaseShowVideoActivity extends EaseBaseActivity{
	private static final String TAG = "ShowVideoActivity";
	
	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;
	private Uri localFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.ease_showvideo_activity);
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final EMMessage message = getIntent().getParcelableExtra("msg");
		if (!(message.getBody() instanceof EMVideoMessageBody)) {
			Toast.makeText(EaseShowVideoActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		EMVideoMessageBody messageBody = (EMVideoMessageBody)message.getBody();

		localFilePath = messageBody.getLocalUrlUri();
		EMLog.d(TAG, "localFilePath = "+localFilePath);
		EMLog.d(TAG, "local filename = "+messageBody.getFileName());

		String filePath = UriUtils.getFilePath(localFilePath);
		if (filePath != null && new File(filePath).exists()) {
			showLocalVideo(filePath);
		} else if(UriUtils.uriStartWithContent(localFilePath)){
			showLocalVideo(localFilePath);
		} else {
			EMLog.d(TAG, "download remote video file");
			downloadVideo(message);
		}
	}

	/**
	 * show local video
	 * @param localPath -- local path of the video file
	 */
	private void showLocalVideo(String localPath){
		EMLog.d(TAG,"localPath = "+localPath);
		showLocalVideo(EaseCompat.getUriForFile(this, new File(localPath)));
	}

	private void showLocalVideo(Uri videoUri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String mimeType = UriUtils.getMimeType(this, videoUri);
		EMLog.d(TAG, "video uri = "+videoUri);
		EMLog.d(TAG, "video mimeType = "+mimeType);
		intent.setDataAndType(videoUri, mimeType);
		// 注意添加该flag,用于Android7.0以上设备获取相册文件权限.
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		startActivity(intent);
		finish();
	}

	/**
	 * download video file
	 */
	private void downloadVideo(EMMessage message) {
		message.setMessageStatusCallback(new EMCallBack() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loadingLayout.setVisibility(View.GONE);
						progressBar.setProgress(0);
						showLocalVideo(localFilePath);
					}
				});
			}

			@Override
			public void onProgress(final int progress,String status) {
				Log.d("ease", "video progress:" + progress);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressBar.setProgress(progress);
					}
				});

			}

			@Override
			public void onError(final int error, String msg) {
				Log.e("###", "offline file transfer error:" + msg);
				String filePath = UriUtils.getFilePath(localFilePath);
				if(TextUtils.isEmpty(filePath)) {
				    EaseShowVideoActivity.this.getContentResolver().delete(localFilePath, null, null);
				}else {
					File file = new File(filePath);
					if (file.exists()) {
						file.delete();
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (error == EMError.FILE_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), R.string.Video_expired, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
		EMClient.getInstance().chatManager().downloadAttachment(message);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
 

}
