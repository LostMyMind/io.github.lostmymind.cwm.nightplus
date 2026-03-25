package com.example.ciweimaocolorhook;

import android.graphics.Color;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

/**
 * 刺猬猫夜间模式颜色Hook
 * 使用LSPosed XSharedPreferences读取配置
 */
public class ColorHook implements IXposedHookInitPackageResources {
    
    private static final String TARGET_PACKAGE = "com.kuangxiangciweimao.novel";
    private static final String MODULE_PACKAGE = "com.example.ciweimaocolorhook";
    private static final String PREF_NAME = "color_config";
    
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        if (!TARGET_PACKAGE.equals(resparam.packageName)) {
            return;
        }
        
        android.util.Log.i("LSPosedFramework", "[刺猬猫颜色Hook] 开始Hook");
        
        // 读取配置
        int bg = Color.BLACK;
        int text = Color.WHITE;
        
        XSharedPreferences pref = new XSharedPreferences(MODULE_PACKAGE, PREF_NAME);
        if (pref.getFile().canRead()) {
            String bgHex = pref.getString("bg_color", "000000");
            String textHex = pref.getString("text_color", "FFFFFF");
            
            android.util.Log.i("LSPosedFramework", "[刺猬猫颜色Hook] 读取配置: bg=" + bgHex + ", text=" + textHex);
            
            if (bgHex.matches("[0-9A-Fa-f]{6}")) {
                bg = Color.parseColor("#" + bgHex);
            }
            if (textHex.matches("[0-9A-Fa-f]{6}")) {
                text = Color.parseColor("#" + textHex);
            }
        } else {
            android.util.Log.i("LSPosedFramework", "[刺猬猫颜色Hook] 配置不可读，使用默认值");
        }
        
        // Hook颜色资源
        try {
            resparam.res.setReplacement(TARGET_PACKAGE, "color", "color_2c2c2c", bg);
            resparam.res.setReplacement(TARGET_PACKAGE, "color", "readpageText_night", text);
            resparam.res.setReplacement(TARGET_PACKAGE, "color", "color_949494", text);
            resparam.res.setReplacement(TARGET_PACKAGE, "color", "color_bg_1_night", bg);
            
            android.util.Log.i("LSPosedFramework", "[刺猬猫颜色Hook] Hook完成: bg=#" + Integer.toHexString(bg) + ", text=#" + Integer.toHexString(text));
        } catch (Exception e) {
            android.util.Log.e("LSPosedFramework", "[刺猬猫颜色Hook] Hook失败: " + e.getMessage());
        }
    }
}