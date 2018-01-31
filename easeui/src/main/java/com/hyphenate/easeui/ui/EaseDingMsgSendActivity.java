package com.hyphenate.easeui.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.EMLog;

/**
 * Created by zhangsong on 18-1-16.
 */

public class EaseDingMsgSendActivity extends EaseBaseActivity {
    private static final String TAG = "DingMsgSendActivity";

    private EaseTitleBar titleBar;
    private EditText msgEidtText;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.ease_acitivity_ding_msg_send);

        titleBar = (EaseTitleBar) findViewById(R.id.title_bar);
        msgEidtText = (EditText) findViewById(R.id.et_sendmessage);

        setupView();
    }

    private void setupView() {
        titleBar.setTitle(getString(R.string.title_group_notification));
        titleBar.setRightLayoutVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        TextView sendView = new TextView(this);
        sendView.setText(getString(R.string.button_send));
        sendView.setTextColor(Color.WHITE);
        sendView.setTextSize(18);
        sendView.setLayoutParams(params);
        titleBar.getRightLayout().addView(sendView);

        // Set the title bar left layout click listener to back to previous activity.
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back(v);
            }
        });

        titleBar.getRightLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send the ding-type msg.
                EMLog.i(TAG, "Click to send ding-type message.");
                String msgContent = msgEidtText.getText().toString();
                Intent i = new Intent();
                i.putExtra("msg", msgContent);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }
}
