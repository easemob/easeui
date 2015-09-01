package com.easemob.easeui.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.easemob.easeui.domain.EaseUser;
import com.squareup.picasso.Picasso;

public class EaseUserUtils {
    
    static EaseUserProfileProvider userProvider;
    
    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }
    
    /**
     * 根据username获取相应user
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username){
        if(userProvider != null)
            return userProvider.getUser(username);
        
        return null;
    }
    
    /**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EaseUser user = getUserInfo(username);
        if(user != null && user.getAvatar() != null){
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Picasso.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //正常的string路径
                Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        }else{
            Picasso.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }
    
    /**
     * 设置用户昵称
     */
    public static void setUserNick(String username,TextView textView){
        if(textView != null){
        	EaseUser user = getUserInfo(username);
        	if(user != null && user.getNick() != null){
        		textView.setText(user.getNick());
        	}else{
        		textView.setText(username);
        	}
        }
    }
    
}
