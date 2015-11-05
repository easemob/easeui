package com.easemob.easeui.model;

import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.domain.EaseEmojicon.Type;
import com.easemob.easeui.utils.EaseSmileUtils;

public class EaseDefaultEmojiconDatas2 {
    
    private static int[] icons = new int[]{
        R.drawable.icon_002_cover,  
        R.drawable.icon_007_cover,  
        R.drawable.icon_010_cover,  
        R.drawable.icon_012_cover,  
        R.drawable.icon_013_cover,  
        R.drawable.icon_018_cover,  
        R.drawable.icon_019_cover,  
        R.drawable.icon_020_cover,  
        R.drawable.icon_021_cover,  
        R.drawable.icon_022_cover,  
        R.drawable.icon_024_cover,  
        R.drawable.icon_027_cover,  
        R.drawable.icon_029_cover,  
        R.drawable.icon_030_cover,  
        R.drawable.icon_035_cover,  
        R.drawable.icon_040_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
        R.drawable.icon_002_cover,  
    };
    
    private static int[] bigIcons = new int[]{
        R.drawable.icon_002,  
        R.drawable.icon_007,  
        R.drawable.icon_010,  
        R.drawable.icon_012,  
        R.drawable.icon_013,  
        R.drawable.icon_018,  
        R.drawable.icon_019,  
        R.drawable.icon_020,  
        R.drawable.icon_021,  
        R.drawable.icon_022,  
        R.drawable.icon_024,  
        R.drawable.icon_027,  
        R.drawable.icon_029,  
        R.drawable.icon_030,  
        R.drawable.icon_035,  
        R.drawable.icon_040,  
        R.drawable.icon_002,  
        R.drawable.icon_002,  
        R.drawable.icon_002,  
        R.drawable.icon_002,  
        R.drawable.icon_002,  
    };
    
    
    private static final EaseEmojicon[] DATA = createData();
    
    private static EaseEmojicon[] createData(){
        EaseEmojicon[] datas = new EaseEmojicon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EaseEmojicon(icons[i], null, Type.BIG_EXPRESSION);
            datas[i].setBigIcon(bigIcons[i]);
            datas[i].setName("大哭");
            datas[i].setIdentityCode("em"+ (1000+i));
        }
        return datas;
    }
    
    public static EaseEmojicon[] getData(){
        return DATA;
    }
}
