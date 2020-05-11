package com.hyphenate.easeui.utils;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;

public class EaseVersionUtils {

    /**
     * 判断当前SDK版本是否是Q版本以上
     * @return
     */
    public static boolean isTargetQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * 检查app的运行模式
     * @return true 为作用域模式；false为兼容模式
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isExternalStorageLegacy() {
        return Environment.isExternalStorageLegacy();
    }
}
