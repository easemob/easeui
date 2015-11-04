package com.easemob.easeui.model;

import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.utils.EaseSmileUtils;

public class EaseDefaultEmojiconDatas2 {
    
    private static int[] icons = new int[]{
        R.drawable.icon_002_cover,  
        R.drawable.icon_007_cover,  
        R.drawable.icon_010_cover,  
        R.drawable.icon_012_cover,  
        R.drawable.icon_013_cover,  
        R.drawable.icon_018_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
    };
    
    
    private static final EaseEmojicon[] DATA = createData();
    
    private static EaseEmojicon[] createData(){
        EaseEmojicon[] datas = new EaseEmojicon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EaseEmojicon(icons[i], null);
        }
        return datas;
    }
    
    public static EaseEmojicon[] getData(){
        return DATA;
    }
}
