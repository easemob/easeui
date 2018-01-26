package com.hyphenate.easeui.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.model.EaseDingMessageHelper;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 18-1-23.
 */

public class EaseDingAckUserListActivity extends EaseBaseActivity {
    private static final String TAG = "EaseDingAckUserListActi";

    private ListView ackUserListView;
    private EaseTitleBar titleBar;

    private EMMessage msg;

    private AckUserAdapter userAdapter;
    private List<String> userList;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.ease_activity_ding_ack_user_list);
        ackUserListView = (ListView) findViewById(R.id.list_view);
        titleBar = (EaseTitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getString(R.string.title_ack_read_list));

        // Set the title bar left layout click listener to back to previous activity.
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back(v);
            }
        });

        msg = getIntent().getParcelableExtra("msg");
        EMLog.i(TAG, "Get msg from intent, msg: " + msg.toString());

        userList = new ArrayList<>();
        userAdapter = new AckUserAdapter(this, userList);

        ackUserListView.setAdapter(userAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> list = EaseDingMessageHelper.get().getAckUsers(msg);
        userList.clear();
        if (list != null) {
            userList.addAll(list);
        }
        userAdapter.notifyDataSetChanged();

        // Set ack-user change listener.
        EaseDingMessageHelper.get().setUserUpdateListener(msg, userUpdateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove ack-user change listener.
        EaseDingMessageHelper.get().setUserUpdateListener(msg, null);
    }

    private EaseDingMessageHelper.IAckUserUpdateListener userUpdateListener =
            new EaseDingMessageHelper.IAckUserUpdateListener() {
                @Override
                public void onUpdate(List<String> list) {
                    EMLog.i(TAG, "onUpdate: " + list.size());

                    userList.clear();
                    userList.addAll(list);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private static class AckUserAdapter extends BaseAdapter {
        private Context context;
        private List<String> userList;

        public AckUserAdapter(Context context, List<String> userList) {
            this.context = context;
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.ease_row_ding_ack_user, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.nameView.setText(userList.get(position));

            return convertView;
        }

        private static class ViewHolder {
            public TextView nameView;

            public ViewHolder(View contentView) {
                nameView = (TextView) contentView.findViewById(R.id.username);
            }
        }
    }
}
