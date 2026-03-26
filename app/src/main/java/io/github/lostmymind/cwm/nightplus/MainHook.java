package io.github.lostmymind.cwm.nightplus;

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
        
        try {
            Method getColorMethod = Resources.class.getDeclaredMethod("getColor", int.class);
            hook(getColorMethod).intercept(new GetColorHooker());
            log(android.util.Log.INFO, "CWMColorHook", "Hook getColor(int) success");
        } catch (Throwable t) {
            log(android.util.Log.ERROR, "CWMColorHook", "Hook failed: " + t.getMessage());
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