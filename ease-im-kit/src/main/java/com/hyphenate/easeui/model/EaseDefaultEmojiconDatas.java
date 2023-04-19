package com.hyphenate.easeui.model;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.utils.EaseSmileUtils;

public class EaseDefaultEmojiconDatas {
    
    private static String[] emojis = new String[]{
        EaseSmileUtils.ee_1,
        EaseSmileUtils.ee_2,
        EaseSmileUtils.ee_3,
        EaseSmileUtils.ee_4,
        EaseSmileUtils.ee_5,
        EaseSmileUtils.ee_6,
        EaseSmileUtils.ee_7,
        EaseSmileUtils.ee_8,
        EaseSmileUtils.ee_9,
        EaseSmileUtils.ee_10,
        EaseSmileUtils.ee_11,
        EaseSmileUtils.ee_12,
        EaseSmileUtils.ee_13,
        EaseSmileUtils.ee_14,
        EaseSmileUtils.ee_15,
        EaseSmileUtils.ee_16,
        EaseSmileUtils.ee_17,
        EaseSmileUtils.ee_18,
        EaseSmileUtils.ee_19,
        EaseSmileUtils.ee_20,
        EaseSmileUtils.ee_21,
        EaseSmileUtils.ee_22,
        EaseSmileUtils.ee_23,
        EaseSmileUtils.ee_24,
        EaseSmileUtils.ee_25,
        EaseSmileUtils.ee_26,
        EaseSmileUtils.ee_27,
        EaseSmileUtils.ee_28,
        EaseSmileUtils.ee_29,
        EaseSmileUtils.ee_30,
        EaseSmileUtils.ee_31,
        EaseSmileUtils.ee_32,
        EaseSmileUtils.ee_33,
        EaseSmileUtils.ee_34,
        EaseSmileUtils.ee_35,
       
    };

    private static String[] systemEmojis = new String[]{
            EaseSmileUtils.e_1,
            EaseSmileUtils.e_2,
            EaseSmileUtils.e_3,
            EaseSmileUtils.e_4,
            EaseSmileUtils.e_5,
            EaseSmileUtils.e_6,
            EaseSmileUtils.e_7,
            EaseSmileUtils.e_8,
            EaseSmileUtils.e_9,
            EaseSmileUtils.e_10,
            EaseSmileUtils.e_11,
            EaseSmileUtils.e_12,
            EaseSmileUtils.e_13,
            EaseSmileUtils.e_14,
            EaseSmileUtils.e_15,
            EaseSmileUtils.e_16,
            EaseSmileUtils.e_17,
            EaseSmileUtils.e_18,
            EaseSmileUtils.e_19,
            EaseSmileUtils.e_20,
            EaseSmileUtils.e_21,
            EaseSmileUtils.e_22,
            EaseSmileUtils.e_23,
            EaseSmileUtils.e_24,
            EaseSmileUtils.e_25,
            EaseSmileUtils.e_26,
            EaseSmileUtils.e_27,
            EaseSmileUtils.e_28,
            EaseSmileUtils.e_29,
            EaseSmileUtils.e_30,
            EaseSmileUtils.e_31,
            EaseSmileUtils.e_32,
            EaseSmileUtils.e_33,
            EaseSmileUtils.e_34,
            EaseSmileUtils.e_35,

    };
    
    private static int[] icons = new int[]{
        R.drawable.ee_1,  
        R.drawable.ee_2,  
        R.drawable.ee_3,  
        R.drawable.ee_4,  
        R.drawable.ee_5,  
        R.drawable.ee_6,  
        R.drawable.ee_7,  
        R.drawable.ee_8,  
        R.drawable.ee_9,  
        R.drawable.ee_10,  
        R.drawable.ee_11,  
        R.drawable.ee_12,  
        R.drawable.ee_13,  
        R.drawable.ee_14,  
        R.drawable.ee_15,  
        R.drawable.ee_16,  
        R.drawable.ee_17,  
        R.drawable.ee_18,  
        R.drawable.ee_19,  
        R.drawable.ee_20,  
        R.drawable.ee_21,  
        R.drawable.ee_22,  
        R.drawable.ee_23,  
        R.drawable.ee_24,  
        R.drawable.ee_25,  
        R.drawable.ee_26,  
        R.drawable.ee_27,  
        R.drawable.ee_28,  
        R.drawable.ee_29,  
        R.drawable.ee_30,  
        R.drawable.ee_31,  
        R.drawable.ee_32,  
        R.drawable.ee_33,  
        R.drawable.ee_34,  
        R.drawable.ee_35,  
    };
    
    
    private static final EaseEmojicon[] DATA = createData();
    
    private static EaseEmojicon[] createData(){
        EaseEmojicon[] datas = new EaseEmojicon[icons.length * 2];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EaseEmojicon(icons[i], emojis[i], Type.NORMAL);
        }
        for (int i = 0; i < icons.length; i++) {
            datas[i + icons.length] = new EaseEmojicon(icons[i], systemEmojis[i], Type.NORMAL);
        }
        return datas;
    }
    
    public static EaseEmojicon[] getData(){
        return DATA;
    }
}
