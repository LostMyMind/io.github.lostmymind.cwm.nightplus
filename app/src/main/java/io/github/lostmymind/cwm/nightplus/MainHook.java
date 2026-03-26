package io.github.lostmymind.cwm.nightplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;

import java.io.File;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * API 101 MainHook
 * Hook Resources.getColor() to replace night mode colors
 */
public class MainHook extends XposedModule {

    private static final String TARGET_PACKAGE = "com.kuangxiangciweimao.novel";
    private static final String MODULE_PACKAGE = "io.github.lostmymind.cwm.nightplus";
    private static final String PREF_NAME = "color_config";
    
    private static int bgColor = Color.BLACK;
    private static int textColor = Color.WHITE;
    
    private static final String[] BG_COLOR_NAMES = {
        "color_2c2c2c",
        "color_bg_1_night"
    };
    private static final String[] TEXT_COLOR_NAMES = {
        "readpageText_night",
        "color_949494"
    };

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        if (!TARGET_PACKAGE.equals(param.getPackageName())) {
            return;
        }

        log(android.util.Log.INFO, "CWMColorHook", "onPackageLoaded: " + param.getPackageName());
        
        // 读取配置
        loadConfig();


        try {
            Method getColorMethod = Resources.class.getDeclaredMethod("getColor", int.class);
            hook(getColorMethod).intercept(new GetColorHooker());
            log(android.util.Log.INFO, "CWMColorHook", "Hook getColor(int) success");
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "Hook failed: " + t.getMessage());
        }
    }

    private void loadConfig() {
        try {
            // 首先尝试使用 getRemotePreferences
            SharedPreferences prefs = getRemotePreferences(PREF_NAME);
            String bgHex = prefs.getString("bg_color", null);
            String textHex = prefs.getString("text_color", null);

            if (bgHex != null && bgHex.matches("[0-9A-Fa-f]{6}")) {
                bgColor = Color.parseColor("#" + bgHex);
            }
            if (textHex != null && textHex.matches("[0-9A-Fa-f]{6}")) {
                textColor = Color.parseColor("#" + textHex);
            }

            log(android.util.Log.INFO, "CWMColorHook", "RemotePreferences: bg=#" + bgHex + ", text=#" + textHex);
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "Load config failed: " + t.getMessage());

            // 尝试直接读取文件作为备选
            tryLoadFromFile();
        }
    }
    
    private void tryLoadFromFile() {
        try {
            // 尝试读取模块的SharedPreferences文件
            String prefPath = "/data/data/" + MODULE_PACKAGE + "/shared_prefs/" + PREF_NAME + ".xml";
            File prefFile = new File(prefPath);
            
            if (prefFile.exists() && prefFile.canRead()) {
                log(android.util.Log.INFO, "CWMColorHook", "Found pref file: " + prefPath);
                // 由于安全限制，直接读取可能失败
            }
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "tryLoadFromFile failed: " + t.getMessage());
        }
    }
    
    private static boolean isTargetColor(Resources res, int id, String[] colorNames) {
        try {
            String name = res.getResourceName(id);
            for (String target : colorNames) {
                if (name != null && name.contains(target)) {
                    return true;
                }
            }
        } catch (Throwable t) {
            // Resource not found
        }
        return false;
    }
    
    public static class GetColorHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            Resources res = (Resources) chain.getThisObject();
            int id = (int) chain.getArg(0);
            
            if (isTargetColor(res, id, BG_COLOR_NAMES)) {
                return bgColor;
            }
            if (isTargetColor(res, id, TEXT_COLOR_NAMES)) {
                return textColor;
            }
            
            return chain.proceed();
        }
    }
}