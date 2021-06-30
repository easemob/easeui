package com.hyphenate.easeui.viewholder;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.ui.EaseShowNormalFileActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowFile;
import com.hyphenate.exceptions.HyphenateException;


public class EaseFileViewHolder extends EaseChatRowViewHolder{

    public EaseFileViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    public static EaseChatRowViewHolder create(ViewGroup parent,
                                               boolean isSender, MessageListItemClickListener itemClickListener) {
        return new EaseFileViewHolder(new EaseChatRowFile(parent.getContext(), isSender), itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
        EMNormalFileMessageBody fileMessageBody = (EMNormalFileMessageBody) message.getBody();
        Uri filePath = fileMessageBody.getLocalUri();
        //检查Uri读权限
        EaseFileUtils.takePersistableUriPermission(getContext(), filePath);
        if(EaseFileUtils.isFileExistByUri(getContext(), filePath)){
            EaseCompat.openFile(getContext(), filePath);
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
