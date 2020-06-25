package com.hyphenate.easeui.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.hyphenate.easeui.EaseUI;

import java.util.Set;

public class EasePreferenceManager {
    private SharedPreferences.Editor editor;
    private SharedPreferences mSharedPreferences;
    private static final String KEY_AT_GROUPS = "AT_GROUPS";
    private static String SHARED_KEY_SETTING_RECORD_ON_SERVER = "shared_key_setting_record_on_server";
    private static String SHARED_KEY_SETTING_MERGE_STREAM = "shared_key_setting_merge_stream";

    @SuppressLint("CommitPrefEdits")
    private EasePreferenceManager(){
        mSharedPreferences = EaseUI.getInstance().getContext().getSharedPreferences("EM_SP_AT_MESSAGE", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }
    private static EasePreferenceManager instance;
    
    public synchronized static EasePreferenceManager getInstance(){
        if(instance == null){
            instance = new EasePreferenceManager();
        }
        return instance;
        
    }
    
    
    public void setAtMeGroups(Set<String> groups) {
        editor.remove(KEY_AT_GROUPS);
        editor.putStringSet(KEY_AT_GROUPS, groups);
        editor.apply();
    }
    
    public Set<String> getAtMeGroups(){
        return mSharedPreferences.getStringSet(KEY_AT_GROUPS, null);
    }

    public void setRecordOnServer(boolean value) {
        editor.putBoolean(SHARED_KEY_SETTING_RECORD_ON_SERVER, value);
        editor.apply();
    }

    public boolean isRecordOnServer() {
        return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_RECORD_ON_SERVER, false);
    }

    public void setMergeStream(boolean value) {
        editor.putBoolean(SHARED_KEY_SETTING_MERGE_STREAM, value);
        editor.apply();
    }

    public boolean isMergeStream() {
        return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_MERGE_STREAM, false);
    }
    
}
