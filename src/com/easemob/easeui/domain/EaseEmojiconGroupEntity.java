package com.easemob.easeui.domain;

import java.util.List;

/**
 * 一组表情所对应的实体类
 *
 */
public class EaseEmojiconGroupEntity {
    private List<EaseEmojicon> emojiconList;
    private int icon;
    private String name;
    public List<EaseEmojicon> getEmojiconList() {
        return emojiconList;
    }
    public void setEmojiconList(List<EaseEmojicon> emojiconList) {
        this.emojiconList = emojiconList;
    }
    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
}
