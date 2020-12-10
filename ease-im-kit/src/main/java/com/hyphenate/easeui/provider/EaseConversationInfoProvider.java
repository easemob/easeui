package com.hyphenate.easeui.provider;

import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntegerRes;

import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.domain.EaseUser;

import java.util.Map;

public interface EaseConversationInfoProvider {
    /**
     * 获取默认类型头像
     * @param type
     * @return
     */
    Drawable getDefaultTypeAvatar(String type);
}
