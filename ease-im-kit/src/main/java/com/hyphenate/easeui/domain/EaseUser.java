package com.hyphenate.easeui.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMUserInfo;
import com.hyphenate.easeui.utils.HanziToPinyin;
import com.hyphenate.util.EMLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * contact 0: normal, 1: black ,3: no friend
     */
    private int contact;

    /**
     * the timestamp when last modify
     */
    private long lastModifyTimestamp;

    /**
     * the timestamp when set initialLetter
     */
    private long modifyInitialLetterTimestamp;

    /**
     * user's email;
     */
    private String email;

    /**
     * user's phone;
     */
    private String phone;

    /**
     * user's gender;
     */
    private int gender;

    /**
     * user's birth;
     */
    private String sign;

    /**
     * user's birth;
     */
    private String birth;

    /**
     * user's ext;
     */
    private String ext;


    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String  username) {
        this.username = username;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getNickname() {
        return TextUtils.isEmpty(nickname) ? username : nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getInitialLetter() {
        if(initialLetter == null || lastModifyTimestamp > modifyInitialLetterTimestamp) {
            if(!TextUtils.isEmpty(nickname)) {
                initialLetter = getInitialLetter(nickname);
            }else {
                initialLetter = getInitialLetter(username);
            }
            modifyInitialLetterTimestamp = System.currentTimeMillis();
            return initialLetter;
        }
        return initialLetter;
    }

    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter;
        modifyInitialLetterTimestamp = System.currentTimeMillis();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        this.phone = phone;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public int getGender() { return gender; }

    public void setGender(int gender) {
        this.gender = gender;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getBirth() { return birth; }

    public void setBirth(String birth) {
        this.birth = birth;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getExt() { return ext; }

    public void setExt(String ext) {
        this.ext = ext;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
        lastModifyTimestamp = System.currentTimeMillis();
        setLastModifyTimestamp(lastModifyTimestamp);
    }

    public String getInitialLetter(String name) {
        return new GetInitialLetter().getLetter(name);
    }

    public long getLastModifyTimestamp() {
        return lastModifyTimestamp;
    }

    public void setLastModifyTimestamp(long modifyNicknameTimestamp) {
        this.lastModifyTimestamp = modifyNicknameTimestamp;
    }

    public long getModifyInitialLetterTimestamp() {
        return modifyInitialLetterTimestamp;
    }

    public void setModifyInitialLetterTimestamp(long modifyInitialLetterTimestamp) {
        this.modifyInitialLetterTimestamp = modifyInitialLetterTimestamp;
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
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", sign='" + sign + '\'' +
                ", birth='" + birth + '\'' +
                ", ext='" + ext + '\'' +
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

    public static List<EaseUser> parse(String[] ids) {
        List<EaseUser> users = new ArrayList<>();
        if(ids == null || ids.length == 0) {
            return users;
        }
        EaseUser user;
        for (String id : ids) {
            user = new EaseUser(id);
            users.add(user);
        }
        return users;
    }

    public static List<EaseUser> parseUserInfo(Map<String, EMUserInfo> userInfos) {
        List<EaseUser> users = new ArrayList<>();
        if(userInfos == null || userInfos.isEmpty()) {
            return users;
        }
        EaseUser user;
        Set<String> userSet = userInfos.keySet();
        Iterator<String> it=userSet.iterator();
        while(it.hasNext()){
            String userId=it.next();
            EMUserInfo info = userInfos.get(userId);
            user = new EaseUser(info.getUserId());
            user.setNickname(info.getNickName());
            user.setAvatar(info.getAvatarUrl());
            user.setEmail(info.getEmail());
            user.setGender(info.getGender());
            user.setBirth(info.getBirth());
            user.setSign(info.getSignature());
            user.setExt(info.getExt());
            if(!info.getUserId().equals(EMClient.getInstance().getCurrentUser())){
                users.add(user);
            }

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
            String pinyin = HanziToPinyin.getPinyin(name);
            EMLog.e("pinyin", "letter: "+pinyin);
            if(!TextUtils.isEmpty(pinyin)) {
                String letter = pinyin.substring(0, 1).toUpperCase();
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
