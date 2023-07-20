package com.hyphenate.easeui.manager;

import android.text.TextUtils;

import com.hyphenate.easeui.interfaces.IUIKitInterface;
import com.hyphenate.util.EMLog;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an internal management class, which is used to manage some interfaces inside uikit,
 * which is convenient for interface setting and transfer.
 * Do not call it externally.
 */
public class EaseChatInterfaceManager {
    private static final String TAG = EaseChatInterfaceManager.class.getSimpleName();
    private static EaseChatInterfaceManager mInstance;
    private Map<String, IUIKitInterface> interfaceMap;

    private EaseChatInterfaceManager(){
        interfaceMap = new HashMap<>();
    }

    public static EaseChatInterfaceManager getInstance() {
        if(mInstance == null) {
            synchronized (EaseChatInterfaceManager.class) {
                if(mInstance == null) {
                    mInstance = new EaseChatInterfaceManager();
                }
            }
        }
        return mInstance;
    }

    public void setInterface(String tag, IUIKitInterface iuiKitInterface) {
        if(TextUtils.isEmpty(tag)) {
            EMLog.e(TAG, "tag should not be null");
            return;
        }
        interfaceMap.put(tag, iuiKitInterface);
    }

    public IUIKitInterface getInterface(String tag) {
        if(!interfaceMap.containsKey(tag)) {
            EMLog.e(TAG, "Do not have interface with tag: "+tag);
            return null;
        }
        return interfaceMap.get(tag);
    }

    public boolean removeInterface(String tag) {
        if(TextUtils.isEmpty(tag)) {
            return false;
        }
        return interfaceMap.remove(tag) != null;
    }

    public void clear() {
        interfaceMap.clear();
    }
}
