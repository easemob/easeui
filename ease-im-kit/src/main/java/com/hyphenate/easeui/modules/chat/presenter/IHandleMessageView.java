package com.hyphenate.easeui.modules.chat.presenter;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.modules.ILoadDataView;

public interface IHandleMessageView extends ILoadDataView {
    /**
     * 生成视频封面失败
     * @param message
     */
    void createThumbFileFail(String message);

    /**
     * 在发送消息前，添加消息属性，如设置ext等
     * @param message
     */
    void addMsgAttrBeforeSend(EMMessage message);

    /**
     * 发送消息失败
     * @param message
     */
    void sendMessageFail(String message);

    /**
     * 完成发送消息动作
     * @param message
     */
    void sendMessageFinish(EMMessage message);

    /**
     * 删除本地消息
     * @param message
     */
    void deleteLocalMessageSuccess(EMMessage message);

    /**
     * 完成撤回消息
     * @param message
     */
    void recallMessageFinish(EMMessage message);

    /**
     * 撤回消息失败
     * @param code
     * @param message
     */
    void recallMessageFail(int code, String message);

    /**
     * message send success
     * @param message
     */
    void onPresenterMessageSuccess(EMMessage message);

    /**
     * message send fail
     * @param message
     * @param code
     * @param error
     */
    void onPresenterMessageError(EMMessage message, int code, String error);

    /**
     * message in sending progress
     * @param message
     * @param progress
     */
    void onPresenterMessageInProgress(EMMessage message, int progress);

    /**
     * 翻译消息成功
     * @param message
     */
    void translateMessageSuccess(EMMessage message);

    /**
     * 翻译消息失败
     * @param message
     * @param code
     * @param error
     */
    void translateMessageFail(EMMessage message, int code, String error);
}
