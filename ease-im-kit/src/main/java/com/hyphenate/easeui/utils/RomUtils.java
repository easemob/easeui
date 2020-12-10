package com.hyphenate.easeui.utils;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.StringDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

public class RomUtils {
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_QIKU = "QIKU";
    public static final String ROM_LETV = "LETV";
    public static final String ROM_LENOVO = "LENOVO";
    public static final String ROM_NUBIA = "NUBIA";
    public static final String ROM_ZTE = "ZTE";
    public static final String ROM_COOLPAD = "COOLPAD";
    public static final String ROM_UNKNOWN = "UNKNOWN";

    @StringDef({
            ROM_MIUI, ROM_EMUI, ROM_VIVO, ROM_OPPO, ROM_FLYME,
            ROM_SMARTISAN, ROM_QIKU, ROM_LETV, ROM_LENOVO, ROM_ZTE,
            ROM_COOLPAD, ROM_UNKNOWN
    })
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RomName {
    }

    private static final String SYSTEM_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String SYSTEM_VERSION_EMUI = "ro.build.version.emui";
    private static final String SYSTEM_VERSION_VIVO = "ro.vivo.os.version";
    private static final String SYSTEM_VERSION_OPPO = "ro.build.version.opporom";
    private static final String SYSTEM_VERSION_FLYME = "ro.build.display.id";
    private static final String SYSTEM_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String SYSTEM_VERSION_LETV = "ro.letv.eui";
    private static final String SYSTEM_VERSION_LENOVO = "ro.lenovo.lvp.version";

    class AvailableRomType {
        public static final int MIUI = 1;
        public static final int FLYME = 2;
        public static final int ANDROID_NATIVE = 3;
        public static final int NA = 4;
    }

    public static int getLightStatusBarAvailableRomType() {
        //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错
        if (isMiUIV7OrAbove()) {
            return AvailableRomType.ANDROID_NATIVE;
        }

        if (isMiUIV6OrAbove()) {
            return AvailableRomType.MIUI;
        }

        if (isFlymeV4OrAbove()) {
            return AvailableRomType.FLYME;
        }

        if (isAndroidMOrAbove()) {
            return AvailableRomType.ANDROID_NATIVE;
        }

        return AvailableRomType.NA;
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private static boolean isFlymeV4OrAbove() {
        String displayId = Build.DISPLAY;
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            String[] displayIdArray = displayId.split(" ");
            for (String temp : displayIdArray) {
                //版本号4以上，形如4.x.
                if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*")) {
                    return true;
                }
            }
        }
        return false;
    }

    //Android Api 23以上
    private static boolean isAndroidMOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";

    private static boolean isMiUIV6OrAbove() {
        FileInputStream stream = null;
        try {
            final Properties properties = new Properties();
            stream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            properties.load(stream);
            String uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null);
            if (uiCode != null) {
                int code = Integer.parseInt(uiCode);
                return code >= 4;
            } else {
                return false;
            }

        } catch (final Exception e) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    static boolean isMiUIV7OrAbove() {
        FileInputStream stream = null;
        try {
            final Properties properties = new Properties();
            stream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            properties.load(stream);
            String uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null);
            if (uiCode != null) {
                int code = Integer.parseInt(uiCode);
                return code >= 5;
            } else {
                return false;
            }

        } catch (final Exception e) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static String getSystemProperty(String propName) {
        return "";
        //return SystemProperties.get(propName, null);
    }

    @RomName
    public static String getRomName() {
        if (isMiuiRom()) {
            return ROM_MIUI;
        }
        if (isHuaweiRom()) {
            return ROM_EMUI;
        }
        if (isVivoRom()) {
            return ROM_VIVO;
        }
        if (isOppoRom()) {
            return ROM_OPPO;
        }
        if (isMeizuRom()) {
            return ROM_FLYME;
        }
        if (isSmartisanRom()) {
            return ROM_SMARTISAN;
        }
        if (is360Rom()) {
            return ROM_QIKU;
        }
        if (isLetvRom()) {
            return ROM_LETV;
        }
        if (isLenovoRom()) {
            return ROM_LENOVO;
        }
        if (isZTERom()) {
            return ROM_ZTE;
        }
        if (isCoolPadRom()) {
            return ROM_COOLPAD;
        }
        return ROM_UNKNOWN;
    }

    public static String getDeviceManufacture() {
        if (isMiuiRom()) {
            return "小米";
        }
        if (isHuaweiRom()) {
            return "华为";
        }
        if (isVivoRom()) {
            return ROM_VIVO;
        }
        if (isOppoRom()) {
            return ROM_OPPO;
        }
        if (isMeizuRom()) {
            return "魅族";
        }
        if (isSmartisanRom()) {
            return "锤子";
        }
        if (is360Rom()) {
            return "奇酷";
        }
        if (isLetvRom()) {
            return "乐视";
        }
        if (isLenovoRom()) {
            return "联想";
        }
        if (isZTERom()) {
            return "中兴";
        }
        if (isCoolPadRom()) {
            return "酷派";
        }
        return Build.MANUFACTURER;
    }

    public static boolean isMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_MIUI));
    }

    public static boolean isHuaweiRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_EMUI));
    }

    public static boolean isVivoRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_VIVO));
    }

    public static boolean isOppoRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_OPPO));
    }

    public static boolean isMeizuRom() {
        String meizuFlymeOSFlag = getSystemProperty(SYSTEM_VERSION_FLYME);
        return !TextUtils.isEmpty(meizuFlymeOSFlag) && meizuFlymeOSFlag.toUpperCase().contains(ROM_FLYME);
    }

    public static boolean isSmartisanRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_SMARTISAN));
    }

    public static boolean is360Rom() {
        String manufacturer = Build.MANUFACTURER;
        return !TextUtils.isEmpty(manufacturer) && manufacturer.toUpperCase().contains(ROM_QIKU);
    }

    public static boolean isLetvRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LETV));
    }

    public static boolean isLenovoRom() {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LENOVO));
    }

    public static boolean isCoolPadRom() {
        String model = Build.MODEL;
        String fingerPrint = Build.FINGERPRINT;
        return (!TextUtils.isEmpty(model) && model.toLowerCase().contains(ROM_COOLPAD))
                || (!TextUtils.isEmpty(fingerPrint) && fingerPrint.toLowerCase().contains(ROM_COOLPAD));
    }

    public static boolean isZTERom() {
        String manufacturer = Build.MANUFACTURER;
        String fingerPrint = Build.FINGERPRINT;
        return (!TextUtils.isEmpty(manufacturer) && (fingerPrint.toLowerCase().contains(ROM_NUBIA)
                || fingerPrint.toLowerCase().contains(ROM_ZTE)))
                || (!TextUtils.isEmpty(fingerPrint) && (fingerPrint.toLowerCase().contains(ROM_NUBIA)
                || fingerPrint.toLowerCase().contains(ROM_ZTE)));
    }

    public static boolean isDomesticSpecialRom() {
        return RomUtils.isMiuiRom()
                || RomUtils.isHuaweiRom()
                || RomUtils.isMeizuRom()
                || RomUtils.is360Rom()
                || RomUtils.isOppoRom()
                || RomUtils.isVivoRom()
                || RomUtils.isLetvRom()
                || RomUtils.isZTERom()
                || RomUtils.isLenovoRom()
                || RomUtils.isCoolPadRom();
    }

}