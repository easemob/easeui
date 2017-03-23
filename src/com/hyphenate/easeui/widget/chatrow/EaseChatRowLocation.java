package com.hyphenate.easeui.widget.chatrow;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.ui.EaseBaiduMapActivity;
import com.hyphenate.easeui.utils.EaseACKUtil;
import com.hyphenate.easeui.utils.EaseMessageUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.LatLng;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EaseChatRowLocation extends EaseChatRow{

    private TextView locationView;
    private EMLocationMessageBody locBody;

	public EaseChatRowLocation(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_location : R.layout.ease_row_sent_location, this);
    }

    @Override
    protected void onFindViewById() {
    	locationView = (TextView) findViewById(R.id.tv_location);
    }


    @Override
    protected void onSetUpView() {
		locBody = (EMLocationMessageBody) message.getBody();
        if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_BURN, false)) {
            locationView.setText(R.string.attach_burn);
        }else{
            locationView.setText(locBody.getAddress());
        }

		// handle sending message
		if (message.direct() == EMMessage.Direct.SEND) {
		    setMessageSendCallback();
            switch (message.status()) {
            case CREATE: 
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            case FAIL:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                break;
            default:
               break;
            }
        }else{
            if(!message.isAcked() && message.getChatType() == ChatType.Chat
                    && !message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_BURN, false)){
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            } else if (!message.isAcked() && message.getChatType() == ChatType.GroupChat) {
                EaseMessageUtils.sendGroupReadMessage(message.getFrom(), message.getTo(),
                        message.getMsgId());
                message.setAcked(true);
                EMClient.getInstance().chatManager().updateMessage(message);
            }
        }
    }
    
    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onBubbleClick() {
        sendACKMessage();
        Intent intent = new Intent(context, EaseBaiduMapActivity.class);
        intent.putExtra("latitude", locBody.getLatitude());
        intent.putExtra("longitude", locBody.getLongitude());
        intent.putExtra("address", locBody.getAddress());
        activity.startActivity(intent);
    }
    
    /*
	 * listener for map clicked
	 */
	protected class MapClickListener implements View.OnClickListener {

		LatLng location;
		String address;

		public MapClickListener(LatLng loc, String address) {
			location = loc;
			this.address = address;

		}

		@Override
		public void onClick(View v) {
		   
		}
	}


    /**
     * ACK 消息的发送，根据是否发送成功做些相应的操作，这里是把发送失败的消息id和username保存在序列化类中
     */
    private void sendACKMessage() {
        try {
            if(EMClient.getInstance().isConnected()){
                EMClient.getInstance()
                        .chatManager()
                        .ackMessageRead(message.getFrom(), message.getMsgId());
            }else{
                EaseACKUtil.getInstance(context).saveACKDataId(message.getMsgId(), message.getFrom());
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
            EaseACKUtil.getInstance(context).saveACKDataId(message.getMsgId(), message.getFrom());
        } finally {
            EMClient.getInstance()
                    .chatManager()
                    .getConversation(message.getFrom())
                    .removeMessage(message.getMsgId());
            onUpdateView();
        }
    }

}
