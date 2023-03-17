package com.hyphenate.easeui.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.widget.EaseImageView;

public class EaseUserUtils {

    static {
        // TODO: 2019/12/30 0030 how to provide userProfileProvider
//        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }
    
    /**
     * get EaseUser according username
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username){
        EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
        return provider == null ? null : provider.getUser(username);
    }

    public static EaseUser getGroupUserInfo(String groupId,String username){
        EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
        return provider == null ? null : provider.getGroupUser(groupId,username);
    }

    /**
     * set user's avatar style
     * @param imageView
     */
    public static void setUserAvatarStyle(EaseImageView imageView) {
        EaseAvatarOptions avatarOptions = EaseIM.getInstance().getAvatarOptions();
        if(avatarOptions == null || imageView == null) {
            return;
        }
        if(avatarOptions.getAvatarShape() != 0)
            imageView.setShapeType(avatarOptions.getAvatarShape());
        if(avatarOptions.getAvatarBorderWidth() != 0)
            imageView.setBorderWidth(avatarOptions.getAvatarBorderWidth());
        if(avatarOptions.getAvatarBorderColor() != 0)
            imageView.setBorderColor(avatarOptions.getAvatarBorderColor());
        if(avatarOptions.getAvatarRadius() != 0)
            imageView.setRadius(avatarOptions.getAvatarRadius());
    }
    
    /**
     * set user avatar
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EaseUser user = getUserInfo(username);
        setUserAvatar(context, user, imageView);
    }

    /**
     * set user avatar
     * @param username
     */
    public static void setUserAvatar(Context context,String groupId, String username, ImageView imageView){
        EaseUser user = getGroupUserInfo(groupId,username);
        setUserAvatar(context, user, imageView);
    }

    private static void setUserAvatar(Context context, EaseUser user, ImageView imageView) {
        if(context == null || imageView == null) {
            return;
        }
        if(user == null || TextUtils.isEmpty(user.getAvatar())) {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
            return;
        }
        try {
            int avatarResId = Integer.parseInt(user.getAvatar());
            Glide.with(context).load(avatarResId).into(imageView);
        } catch (Exception e) {
            //use default avatar
            Glide.with(context).load(user.getAvatar())
                    .apply(RequestOptions.placeholderOf(R.drawable.ease_default_avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageView);
        }
    }

    /**
     * show user avatar
     * @param context
     * @param avatar
     * @param imageView
     */
    public static void showUserAvatar(Context context, String avatar, ImageView imageView) {
        if(TextUtils.isEmpty(avatar)) {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
            return;
        }
        try {
            int avatarResId = Integer.parseInt(avatar);
            Glide.with(context).load(avatarResId).into(imageView);
        } catch (Exception e) {
            //use default avatar
            Glide.with(context).load(avatar)
                    .apply(RequestOptions.placeholderOf(R.drawable.ease_default_avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageView);
        }
    }
    
    /**
     * set user's nickname
     */
    public static void setUserNick(String username,TextView textView){
        EaseUser user = getUserInfo(username);
        setUserNickname(user, username, textView);
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String groupId,String username,TextView textView){
        EaseUser user = getGroupUserInfo(groupId,username);
        setUserNickname(user, username, textView);
    }

    private static void setUserNickname(EaseUser user, String userId, TextView textView) {
        if(textView == null) {
            return;
        }
        if(user == null || TextUtils.isEmpty(user.getNickname())) {
            textView.setText(userId);
            return;
        }
        textView.setText(user.getNickname());
    }
}
