package com.hyphenate.easeui.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hyphenate.util.HanziToPinyin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EaseUser implements Serializable {
    /**
     * \~chinese
     * 此用户的唯一标示名, 即用户的环信id
     *
     * \~english
     * the user name assigned from app, which should be unique in the application
     */
    @NonNull
    private String username;
    private String nickname;
    /**
     * initial letter from nickname
     */
    private String initialLetter;
    /**
     * user's avatar
     */
    private String avatar;

    /**
     * contact 0: normal, 1: black
     */
    private int contact;

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String  username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname == null ? username : nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getInitialLetter() {
        if(initialLetter == null) {
            if(!TextUtils.isEmpty(nickname)) {
                return getInitialLetter(nickname);
            }
            return getInitialLetter(username);
        }
        return initialLetter;
    }

    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public String getInitialLetter(String name) {
        return new GetInitialLetter().getLetter(name);
    }

    public EaseUser() {
    }

    public EaseUser(@NonNull String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "EaseUser{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", initialLetter='" + initialLetter + '\'' +
                ", avatar='" + avatar + '\'' +
                ", contact=" + contact +
                '}';
    }

    public static List<EaseUser> parse(List<String> ids) {
        List<EaseUser> users = new ArrayList<>();
        if(ids == null || ids.isEmpty()) {
            return users;
        }
        EaseUser user;
        for (String id : ids) {
            user = new EaseUser(id);
            users.add(user);
        }
        return users;
    }

    public class GetInitialLetter {
        private String defaultLetter = "#";

        /**
         * 获取首字母
         * @param name
         * @return
         */
        public String getLetter(String name) {
            if(TextUtils.isEmpty(name)) {
                return defaultLetter;
            }
            char char0 = name.toLowerCase().charAt(0);
            if(Character.isDigit(char0)) {
                return defaultLetter;
            }
            ArrayList<HanziToPinyin.Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));
            if(l != null && !l.isEmpty() && l.get(0).target.length() > 0) {
                HanziToPinyin.Token token = l.get(0);
                String letter = token.target.substring(0, 1).toUpperCase();
                char c = letter.charAt(0);
                if(c < 'A' || c > 'Z') {
                    return defaultLetter;
                }
                return letter;
            }
            return defaultLetter;
        }
    }

}
