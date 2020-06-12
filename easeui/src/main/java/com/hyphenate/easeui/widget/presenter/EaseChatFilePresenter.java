package com.hyphenate.easeui.widget.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.BaseAdapter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.easeui.ui.EaseShowNormalFileActivity;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowFile;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.UriUtils;

import java.io.File;

/**
 * Created by zhangsong on 17-10-12.
 */

public class EaseChatFilePresenter extends EaseChatRowPresenter {

    @Override
    protected EaseChatRow onCreateChatRow(Context cxt, EMMessage message, int position, BaseAdapter adapter) {
        return new EaseChatRowFile(cxt, message, position, adapter);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        EMNormalFileMessageBody fileMessageBody = (EMNormalFileMessageBody) message.getBody();
        Uri filePath = fileMessageBody.getLocalUri();
        String fileLocalPath = UriUtils.getFilePath(getContext(), filePath);
        File file = null;
        if(!TextUtils.isEmpty(fileLocalPath)) {
            file = new File(fileLocalPath);
        }
        if (file != null && file.exists()) {
            // open files if it exist
            EaseCompat.openFile(file, (Activity) getContext());
        } else if(UriUtils.isFileExistByUri(getContext(), filePath)){
            EaseCompat.openFile(filePath, UriUtils.getFileMimeType(getContext(), filePath), (Activity) getContext());
        } else {
            // download the file
            getContext().startActivity(new Intent(getContext(), EaseShowNormalFileActivity.class).putExtra("msg", message));
        }
        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
