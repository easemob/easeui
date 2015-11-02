package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;

/**
 * 大表情(动态表情)
 * @author wei
 *
 */
public class EaseChatRowBigExpression extends EaseChatRowText{

    private ImageView imageView;


    public EaseChatRowBigExpression(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ? 
                R.layout.ease_row_received_bigexpression : R.layout.ease_row_sent_bigexpression, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }


    @Override
    public void onSetUpView() {
        int icon = message.getIntAttribute(EaseConstant.MESSAGE_ATTR_BIG_EXPRESSION_ICON, 0);
        String url = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_BIG_EXPRESSION_URL, null);
        if(icon != 0){
            Glide.with(activity).load(icon).placeholder(R.drawable.ease_default_expression).into(imageView);
        }else{
            Glide.with(activity).load(url).placeholder(R.drawable.ease_default_expression).into(imageView);
        }
        
        handleTextMessage();
    }
}
