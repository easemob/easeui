package com.hyphenate.easeui.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseMessageUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * 展示群组消息已读成员统计列表
 * Created by lzan13 on 2017/3/17.
 */
public class EaseGroupReadActivity extends EaseBaseActivity {

    // 界面控件
    private EaseTitleBar easeTitleBar;
    private ListView listView;

    // 定义分享的消息
    private EMMessage message;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.ease_activity_simple_list);

        message = (EMMessage) getIntent().getExtras().get(EaseConstant.GROUP_READ_MEMBER_ARRAY);
        JSONArray memberArray = EaseMessageUtils.getReadMembers(message);

        easeTitleBar = (EaseTitleBar) findViewById(R.id.title_bar);
        easeTitleBar.setTitle("已读成员");
        easeTitleBar.setLeftImageResource(R.drawable.ease_mm_title_back);
        easeTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.shared_list_view);

        List<Map<String, String>> members = new ArrayList<>();
        for (int i = 0; i < memberArray.length(); i++) {
            try {
                String name = memberArray.getString(i);
                HashMap<String, String> map = new HashMap<>();
                map.put("name", memberArray.getString(i));
                members.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 设置adapter
        SimpleAdapter adapter =
                new SimpleAdapter(this, members, R.layout.ease_row_contact, new String[] { "name" },
                        new int[] { R.id.name });
        listView.setAdapter(adapter);
    }
}
