package io.github.lostmymind.cwm.nightplus;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;

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
    private static final String PREF_NAME = "color_config";
    
    private static int bgColor = Color.BLACK;
    private static int bgColorBright = Color.BLACK;
    private static int textColor = Color.WHITE;
    
    private static final String[] BG_COLOR_NAMES = {
        "color_2c2c2c",
        "color_bg_1_night"
    };
    
    private static final String[] BG_COLOR_BRIGHT_NAMES = {
        "color_bg_catalog_night",
        "color_bg_main_night",
        "deepDark",
        "edit_text_default_color",
        "color_title_bg1_night",
        "daibi_night",
        "color_bg_2_night",
        "bg_msg_night",
        "text_btn_item_night",
        "ksad_text_black_222",
        "ksad_splash_endcard_name_color",
        "text_222222"
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
        
        loadConfig();
        
        try {
            Method getColor1 = Resources.class.getDeclaredMethod("getColor", int.class);
            hook(getColor1).intercept(new GetColorHooker());
            
            Method getColor2 = Resources.class.getDeclaredMethod("getColor", int.class, Resources.Theme.class);
            hook(getColor2).intercept(new GetColorThemeHooker());
            
            log(android.util.Log.INFO, "CWMColorHook", "Hook getColor success");
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "Hook failed: " + t.getMessage());
        }
    }
    
    private void loadConfig() {
        try {
            SharedPreferences prefs = getRemotePreferences(PREF_NAME);
            String bgHex = prefs.getString("bg_color", null);
            String textHex = prefs.getString("text_color", null);
            
            if (bgHex != null && bgHex.matches("[0-9A-Fa-f]{6}")) {
                bgColor = Color.parseColor("#" + bgHex);
                int r = Math.min(255, ((bgColor >> 16) & 0xFF) + 0x0A);
                int g = Math.min(255, ((bgColor >> 8) & 0xFF) + 0x0A);
                int b = Math.min(255, (bgColor & 0xFF) + 0x0A);
                bgColorBright = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            if (textHex != null && textHex.matches("[0-9A-Fa-f]{6}")) {
                textColor = Color.parseColor("#" + textHex);
            }
            
            log(android.util.Log.INFO, "CWMColorHook", "Config loaded: bg=#" + bgHex + ", bgBright=#" + Integer.toHexString(bgColorBright) + ", text=#" + textHex);
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "Load config failed: " + t.getMessage());
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
        }
        return false;
    }
    
    private static void logColorMatch(Resources res, int id, String matchedName, int color) {
        try {
            String name = res.getResourceName(id);
            android.util.Log.i("CWMColorHook", "Match: " + name + " -> " + matchedName + " = #" + Integer.toHexString(color));
        } catch (Throwable t) {
        }
    }
    
    public static class GetColorHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            Resources res = (Resources) chain.getThisObject();
            int id = (int) chain.getArg(0);
            
            if (isTargetColor(res, id, BG_COLOR_NAMES)) {
                logColorMatch(res, id, "BG", bgColor);
                return bgColor;
            }
            if (isTargetColor(res, id, BG_COLOR_BRIGHT_NAMES)) {
                logColorMatch(res, id, "BG_BRIGHT", bgColorBright);
                return bgColorBright;
            }
            if (isTargetColor(res, id, TEXT_COLOR_NAMES)) {
                logColorMatch(res, id, "TEXT", textColor);
                return textColor;
            }
            
            return chain.proceed();
        }
    }
    
    public static class GetColorThemeHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            Resources res = (Resources) chain.getThisObject();
            int id = (int) chain.getArg(0);
            
            if (isTargetColor(res, id, BG_COLOR_NAMES)) {
                logColorMatch(res, id, "BG", bgColor);
                return bgColor;
            }
            if (isTargetColor(res, id, BG_COLOR_BRIGHT_NAMES)) {
                logColorMatch(res, id, "BG_BRIGHT", bgColorBright);
                return bgColorBright;
            }
            if (isTargetColor(res, id, TEXT_COLOR_NAMES)) {
                logColorMatch(res, id, "TEXT", textColor);
                return textColor;
            }
            
            return chain.proceed();
        }
    }
}