package com.hyphenate.easeui.utils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.content.Context;

/**
 * 保存阅后即焚阅读后但是发送ack失败的消息id
 *
 * Created by lzan13 on 2017/3/21.
 *
 * @author lzan13
 */
public class EaseACKUtil {

    private String filePath = "";

    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private ObjectInputStream mObjInputStream;
    private ObjectOutputStream mObjOutputStream;

    private static EaseACKUtil instance;
    private EaseACKData ackData;

    /**
     * 私有的构造方法，这里来获取保存msgId的序列化类对象
     */
    private EaseACKUtil(Context context) {
        try {
            // 设置要保存的序列化类的文件路径
            filePath = context.getFilesDir().getAbsolutePath() + "/" + EMClient.getInstance()
                    .getCurrentUser() + "/ease_ack.dat";
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            // 将文件读入到文件输入流
            mFileInputStream = new FileInputStream(file);
            mObjInputStream = new ObjectInputStream(mFileInputStream);
            // 从Object输入流读取对象
            ackData = (EaseACKData) mObjInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFileInputStream != null) {
                    mFileInputStream.close();
                }
                if (mObjInputStream != null) {
                    mObjInputStream.close();
                }
                if (ackData == null) {
                    ackData = new EaseACKData();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取单例对象
     */
    public static EaseACKUtil getInstance(Context context) {
        if (instance == null) {
            instance = new EaseACKUtil(context);
        }
        return instance;
    }

    /**
     * 将不能发送ack的 msgId 加入到当前集合
     */
    public void saveACKDataId(String msgId, String username) {
        ackData.addACKData(msgId, username);
        saveDataToDisk();
    }

    /**
     * 将已经发送的ack的 msgId 移除当前集合
     */
    public void deleteACKData(String msgId) {
        ackData.removeACKData(msgId);
        saveDataToDisk();
    }

    /**
     * 将可序列化的类对象保存到本地
     */
    public void saveDataToDisk() {
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            mFileOutputStream = new FileOutputStream(file);
            mObjOutputStream = new ObjectOutputStream(mFileOutputStream);
            // 将对象写入到输出流中
            mObjOutputStream.writeObject(ackData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFileOutputStream != null) {
                    mFileOutputStream.close();
                }
                if (mObjOutputStream != null) {
                    mObjOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前所有未发送已读回执的集合
     *
     * @return 返回消息回执未发送成功的集合
     */
    public Map<String, String> getACKMap() {
        return ackData.getACKMap();
    }

    /**
     * 当连接到服务器之后，这里开始检查是否有没有发送的ack回执消息，
     */
    public void checkACKData() {
        Map<String, String> ackMap = getACKMap();
        Set set = ackMap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> mapEntry = (Entry<String, String>) iterator.next();
            //EMClient.getInstance()
            //        .chatManager()
            //        .ackMessageRead(mapEntry.getValue(), mapEntry.getKey());
            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);
            EMCmdMessageBody body = new EMCmdMessageBody(EaseConstant.MESSAGE_ATTR_BURN_ACTION);
            message.addBody(body);
            message.setTo(mapEntry.getValue());
            message.setAttribute(EaseConstant.MESSAGE_ATTR_BURN_MSG_ID, mapEntry.getKey());
            EMClient.getInstance().chatManager().sendMessage(message);
            iterator.remove();
        }
        ackData.setACKMap(ackMap);
        saveDataToDisk();
    }
}