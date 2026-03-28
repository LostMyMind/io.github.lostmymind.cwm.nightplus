package io.github.lostmymind.cwm.nightplus;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.SparseIntArray;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * API 101 MainHook - Optimized
 */
public class MainHook extends XposedModule {

    private static final String TARGET = "com.kuangxiangciweimao.novel";
    private static final String PREF = "color_config";
    private static int bgColor = Color.BLACK;
    private static int bgColorBright = Color.BLACK;
    private static int textColor = Color.WHITE;
    
    private static final Set<Integer> bgIds = new HashSet<>();
    private static final Set<Integer> bgBrightIds = new HashSet<>();
    private static final Set<Integer> textIds = new HashSet<>();
    private static final SparseIntArray cache = new SparseIntArray(32);
    private static boolean built = false;
    
    private static final String[] BG_NAMES = {"color_2c2c2c", "color_bg_1_night"};
    private static final String[] BG_BRIGHT_NAMES = {
        "color_bg_catalog_night", "color_bg_main_night", "deepDark",
        "edit_text_default_color", "color_title_bg1_night", "daibi_night",
        "color_bg_2_night", "bg_msg_night", "text_btn_item_night",
        "ksad_text_black_222", "ksad_splash_endcard_name_color", "text_222222"
    };
    private static final String[] TEXT_NAMES = {"readpageText_night", "color_949494"};

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        if (!TARGET.equals(param.getPackageName())) return;
        loadConfig();
        try {
            Method m1 = Resources.class.getDeclaredMethod("getColor", int.class);
            hook(m1).intercept(Hooker.INSTANCE);
            Method m2 = Resources.class.getDeclaredMethod("getColor", int.class, Resources.Theme.class);
            hook(m2).intercept(Hooker.INSTANCE);
        } catch (Throwable ignored) {}
    }
    
    private void loadConfig() {
        try {
            SharedPreferences p = getRemotePreferences(PREF);
            String bg = p.getString("bg_color", null);
            String tx = p.getString("text_color", null);
            if (bg != null && bg.matches("[0-9A-Fa-f]{6}")) {
                bgColor = Color.parseColor("#" + bg);
                int r = Math.min(255, ((bgColor >> 16) & 0xFF) + 10);
                int g = Math.min(255, ((bgColor >> 8) & 0xFF) + 10);
                int b = Math.min(255, (bgColor & 0xFF) + 10);
                bgColorBright = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            if (tx != null && tx.matches("[0-9A-Fa-f]{6}")) textColor = Color.parseColor("#" + tx);
            cache.clear();
        } catch (Throwable ignored) {}
    }
    
    private static void buildIds(Resources res) {
        if (built) return;
        try {
            for (String n : BG_NAMES) { int id = res.getIdentifier(n, "color", TARGET); if (id != 0) bgIds.add(id); }
            for (String n : BG_BRIGHT_NAMES) { int id = res.getIdentifier(n, "color", TARGET); if (id != 0) bgBrightIds.add(id); }
            for (String n : TEXT_NAMES) { int id = res.getIdentifier(n, "color", TARGET); if (id != 0) textIds.add(id); }
            built = true;
        } catch (Throwable ignored) {}
    }
    
    public static class Hooker implements XposedInterface.Hooker {
        static final Hooker INSTANCE = new Hooker();
        
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            Resources res = (Resources) chain.getThisObject();
            int id = (int) chain.getArg(0);
            
            buildIds(res);
            
            int c = cache.get(id, -1);
            if (c != -1) return c;
            
            if (bgIds.contains(id)) c = bgColor;
            else if (bgBrightIds.contains(id)) c = bgColorBright;
            else if (textIds.contains(id)) c = textColor;
            else return chain.proceed();
            
            cache.put(id, c);
            return c;
        }
    }
}