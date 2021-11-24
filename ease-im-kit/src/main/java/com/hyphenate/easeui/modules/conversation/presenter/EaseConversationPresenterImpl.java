package com.hyphenate.easeui.modules.conversation.presenter;

import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EaseConversationPresenterImpl extends EaseConversationPresenter {

    /**
     * 注意：默认conversation中设置extField值为时间戳后，是将该会话置顶
     * 如果有不同的逻辑，请自己实现，并调用{@link #sortData(List)}方法即可
     */
    @Override
    public void loadData() {
        // get all conversations
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        if(conversations.isEmpty()) {
            runOnUI(() -> {
                if(!isDestroy()) {
                    mView.loadConversationListNoData();
                }
            });
            return;
        }
        List<EaseConversationInfo> infos = new ArrayList<>();
        synchronized (this) {
            EaseConversationInfo info = null;
            for (EMConversation conversation : conversations.values()) {
                if(conversation.getAllMessages().size() != 0) {
                    //如果不展示系统消息，则移除相关系统消息
                    if(!showSystemMessage) {
                        if(TextUtils.equals(conversation.conversationId(), EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID)) {
                            continue;
                        }
                    }
                    info = new EaseConversationInfo();
                    info.setInfo(conversation);
                    String extField = conversation.getExtField();
                    long lastMsgTime=conversation.getLastMessage().getMsgTime();
                    if(!TextUtils.isEmpty(extField) && EaseCommonUtils.isTimestamp(extField)) {
                        info.setTop(true);
                        long makeTopTime=Long.parseLong(extField);
                        if(makeTopTime>lastMsgTime) {
                            info.setTimestamp(makeTopTime);
                        }else{
                            info.setTimestamp(lastMsgTime);
                        }
                    }else{
                        info.setTimestamp(lastMsgTime);
                    }
                    infos.add(info);
                }
            }
        }
        if(isActive()) {
            runOnUI(()-> mView.loadConversationListSuccess(infos));
        }
    }

    /**
     * 排序数据
     * @param data
     */
    @Override
    public void sortData(List<EaseConversationInfo> data) {
        if(data == null || data.isEmpty()) {
            runOnUI(() -> {
                if(!isDestroy()) {
                    mView.loadConversationListNoData();
                }

            });
            return;
        }
        List<EaseConversationInfo> sortList = new ArrayList<>();
        List<EaseConversationInfo> topSortList = new ArrayList<>();
        synchronized (this) {
            for(EaseConversationInfo info : data) {
                if(info.isTop()) {
                    topSortList.add(info);
                }else {
                    sortList.add(info);
                }
            }
            sortByTimestamp(topSortList);
            sortByTimestamp(sortList);
            sortList.addAll(0, topSortList);
        }
        runOnUI(() -> {
            if(!isDestroy()) {
                mView.sortConversationListSuccess(sortList);
            }
        });
    }

    /**
     * 排序
     * @param list
     */
    private void sortByTimestamp(List<EaseConversationInfo> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        Collections.sort(list, new Comparator<EaseConversationInfo>() {
            @Override
            public int compare(EaseConversationInfo o1, EaseConversationInfo o2) {
                if(o2.getTimestamp() > o1.getTimestamp()) {
                    return 1;
                }else if(o2.getTimestamp() == o1.getTimestamp()) {
                    return 0;
                }else {
                    return -1;
                }
            }
        });
    }

    @Override
    public void makeConversionRead(int position, EaseConversationInfo info) {
        if(info.getInfo() instanceof EMConversation) {
            ((EMConversation) info.getInfo()).markAllMessagesAsRead();
        }
        if(!isDestroy()) {
            mView.refreshList(position);
        }
    }

    @Override
    public void makeConversationTop(int position, EaseConversationInfo info) {
        if(info.getInfo() instanceof EMConversation) {
            long timestamp = System.currentTimeMillis();
            ((EMConversation) info.getInfo()).setExtField(timestamp +"");
            info.setTop(true);
            info.setTimestamp(timestamp);
        }
        if(!isDestroy()) {
            mView.refreshList();
        }
    }

    @Override
    public void cancelConversationTop(int position, EaseConversationInfo info) {
        if(info.getInfo() instanceof EMConversation) {
            ((EMConversation) info.getInfo()).setExtField("");
            info.setTop(false);
            info.setTimestamp(((EMConversation) info.getInfo()).getLastMessage().getMsgTime());
        }
        if(!isDestroy()) {
            mView.refreshList();
        }
    }

    @Override
    public void deleteConversation(int position, EaseConversationInfo info) {
        if(info.getInfo() instanceof EMConversation) {
            //如果是系统通知，则不删除系统消息
            boolean isDelete = EMClient.getInstance().chatManager()
                                .deleteConversation(((EMConversation) info.getInfo()).conversationId()
                                        , !TextUtils.equals(((EMConversation) info.getInfo()).conversationId(), EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID));
            if(!isDestroy()) {
                if(isDelete) {
                    mView.deleteItem(position);
                    EMClient.getInstance().translationManager().removeResultsByConversationId(((EMConversation) info.getInfo()).conversationId());
                }else {
                    mView.deleteItemFail(position, "");
                }
            }

        }
    }
}

