package com.easemob.easeui.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EaseACKData implements Serializable {

    // 注意定义此属性
    private static final long serialVersionUID = 521125L;

    private Map<String, String> ackMap;
    
    public EaseACKData(){
        ackMap = new HashMap<String, String>();
    }

    public Map<String, String> getACKMap() {
        return ackMap;
    }
    
    public void setACKMap(Map<String, String> map){
        ackMap = map;
    }

    /**
     * 添加一条需要重发的 ACK 数据
     * 
     * @param msgId
     *            要添加的msgId
     * @param username
     *            要添加的username
     */
    public void addACKData(String msgId, String username) {
        ackMap.put(msgId, username);
    }

    /**
     * 删除一条需要重发的ACK 数据
     * 
     * @param msgId
     *            要删除的数据msgId
     */
    public void removeACKData(String msgId) {
        ackMap.remove(msgId);
    }
}
