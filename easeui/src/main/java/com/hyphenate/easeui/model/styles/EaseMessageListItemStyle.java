package com.hyphenate.easeui.model.styles;

import android.graphics.drawable.Drawable;

/**
 * Created by wei on 2016/11/29.
 */

public class EaseMessageListItemStyle {
    private boolean showUserNick;
    private boolean showAvatar;
    private Drawable myBubbleBg;
    private Drawable otherBubbleBg;

    public EaseMessageListItemStyle(Builder builder){
        showUserNick = builder.showUserNick;
        showAvatar = builder.showAvatar;
        myBubbleBg = builder.myBubbleBg;
        otherBubbleBg = builder.otherBubbleBg;
    }

    public boolean isShowUserNick() {
        return showUserNick;
    }

    public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }

    public boolean isShowAvatar() {
        return showAvatar;
    }

    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }

    public Drawable getMyBubbleBg() {
        return myBubbleBg;
    }

    public void setMyBubbleBg(Drawable myBubbleBg) {
        this.myBubbleBg = myBubbleBg;
    }

    public Drawable getOtherBubbleBg() {
        return otherBubbleBg;
    }

    public void setOtherBubbleBg(Drawable otherBubbleBg) {
        this.otherBubbleBg = otherBubbleBg;
    }


    public static final class Builder{
        private boolean showUserNick;
        private boolean showAvatar;
        private Drawable myBubbleBg;
        private Drawable otherBubbleBg;

        public Builder showUserNick(boolean showUserNick){
            this.showUserNick = showUserNick;
            return  this;
        }

        public Builder showAvatar(boolean showAvatar){
            this.showAvatar = showAvatar;
            return  this;
        }

        public Builder myBubbleBg(Drawable myBubbleBg){
            this.myBubbleBg = myBubbleBg;
            return  this;
        }

        public Builder otherBuddleBg(Drawable otherBuddleBg){
            this.otherBubbleBg = otherBuddleBg;
            return  this;
        }


        public EaseMessageListItemStyle build(){
            return new EaseMessageListItemStyle(this);
        }
    }

}
