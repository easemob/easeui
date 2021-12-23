package com.hyphenate.easeui.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;

public class EaseShowNormalFileActivity extends EaseBaseActivity {
    private static final String TAG = EaseShowNormalFileActivity.class.getSimpleName();
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ease_activity_show_file);
        setFitSystemForTheme(true, R.color.transparent, true);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final EMMessage message = getIntent().getParcelableExtra("msg");
        if (!(message.getBody() instanceof EMFileMessageBody)) {
            Toast.makeText(EaseShowNormalFileActivity.this, getApplicationContext().getString(R.string.unsupported_message_body), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EaseCompat.openFile(EaseShowNormalFileActivity.this,
                                ((EMFileMessageBody) message.getBody()).getLocalUri());
                        finish();
                    }
                });

            }

            @Override
            public void onError(final int code, final String error) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EaseCompat.deleteFile(EaseShowNormalFileActivity.this, ((EMFileMessageBody) message.getBody()).getLocalUri());
                        String str4 = getResources().getString(R.string.Failed_to_download_file);
                        if (code == EMError.FILE_NOT_FOUND) {
                            str4 = getResources().getString(R.string.File_expired);
                        }
                        Toast.makeText(getApplicationContext(), str4+message, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onProgress(final int progress, String status) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
            }
        });
        EMClient.getInstance().chatManager().downloadAttachment(message);
	}
}
