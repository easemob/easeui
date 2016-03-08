package com.easemob.easeui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.easemob.chat.EMChatManager;

import android.content.Context;

/**
 * 保存阅后即焚阅读后但是发送ack失败的消息id
 * 
 * @author lzan13
 *
 */
public class EaseACKUtil {
    
    private String filePath = "";
    
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private ObjectInputStream mObjInputStream;
    private ObjectOutputStream mObjOutputStream;

    private EaseACKData mACKData;
    private EaseACKUtil instance;

    /**
     * 私有的构造方法，这里来获取保存msgId的序列化类对象
     */
    private EaseACKUtil(Context context) {
        try {
            // 设置要保存的序列化类的文件路径
            filePath = context.getFilesDir().getAbsolutePath() + EMChatManager.getInstance().getCurrentUser() + "ease_ack.dat";
            mFileInputStream = new FileInputStream(new File(filePath));
            mObjInputStream = new ObjectInputStream(mFileInputStream);
            mACKData = (EaseACKData) mObjInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取单例对象
     * 
     * @return
     */
    public EaseACKUtil getInstance(Context context) {
        if (instance == null) {
            instance = new EaseACKUtil(context);
        }
        return instance;
    }

    /**
     * 将不能发送ack的 msgId 加入到当前集合
     * 
     * @param msgId
     */
    public void saveMsgId(String msgId) {
        mACKData.addMsgId(msgId);
        saveDataToDisk();
    }

    /**
     * 将已经发送的ack的 msgId 移除当前集合
     * 
     * @param msgId
     */
    public void deleteMsgId(String msgId) {
        mACKData.removeMsgId(msgId);
        saveDataToDisk();
    }

    /**
     * 将可序列化的类对象保存到本地
     */
    public void saveDataToDisk() {
        try {
            mFileOutputStream = new FileOutputStream(new File(filePath));
            mObjOutputStream = new ObjectOutputStream(mFileOutputStream);
            
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前所有未发送已读回执的msgId
     * 
     * @return
     */
    public List<String> getAllACKMsgId() {
        List<String> list = new ArrayList<String>();

        return list;
    }

    private class EaseACKData implements Serializable {

        // 保存需要发送的ack消息id
        private List<String> mMsgIdList = new ArrayList<String>();

        /**
         * 将不能发送ack的 msgId 加入到当前集合
         * 
         * @param msgId
         */
        public void addMsgId(String msgId) {
            mMsgIdList.add(msgId);
        }

        /**
         * 将已经发送的ack的 msgId 移除当前集合
         * 
         * @param msgId
         */
        public void removeMsgId(String msgId) {
            mMsgIdList.remove(msgId);
        }
    }

}
