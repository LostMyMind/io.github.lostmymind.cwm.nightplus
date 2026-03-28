package io.github.lostmymind.cwm.nightplus;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/**
 * API 101 调色界面
 * 使用RemotePreferences进行配置共享
 */
public class MainActivity extends Activity implements XposedServiceHelper.OnServiceListener {
    
    private static final String PREF_NAME = "color_config";
    
    private static final int[] BG_COLORS = {0xFF000000, 0xFF1A1A1A, 0xFF0D1B2A};
    private static final String[] BG_NAMES = {"黑", "深灰", "深蓝"};
    private static final int[] TEXT_COLORS = {0xFFFFFFFF, 0xFFE0E0E0, 0xFFF5F5DC};
    private static final String[] TEXT_NAMES = {"白", "灰白", "米黄"};
    
    private int bgColor = 0xFF000000;
    private int textColor = 0xFFFFFFFF;
    
    private View bgPreview;
    private View textPreview;
    private EditText bgHexEdit;
    private EditText textHexEdit;
    private SeekBar seekR;
    private SeekBar seekG;
    private SeekBar seekB;
    private boolean editingBg = true;
    private boolean isUpdatingFromCode = false;
    
    private SharedPreferences prefs;
    private LinearLayout rootLayout;
    private XposedService xposedService;
    
    private static final int DARK_BG = 0xFF1A1A1A;
    private static final int DARK_TEXT = 0xFFE0E0E0;
    private static final int DARK_BTN_BG = 0xFF3A3A3A;
    private static final int DARK_BTN_TEXT = 0xFFE0E0E0;
    private static final int LIGHT_BG = 0xFFF5F5F5;
    private static final int LIGHT_TEXT = 0xFF1A1A1A;
    private static final int LIGHT_BTN_BG = 0xFFE0E0E0;
    private static final int LIGHT_BTN_TEXT = 0xFF1A1A1A;
    private static final int LIGHT_EDIT_BG = 0xFFFFFFFF;
    private static final int DARK_EDIT_BG = 0xFF2A2A2A;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        XposedServiceHelper.registerListener(this);
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadConfig();
        setContentView(createUI());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onServiceBind(XposedService service) {
        this.xposedService = service;
        try {
            prefs = service.getRemotePreferences(PREF_NAME);
            loadConfig();
            updateDisplay();
        } catch (Throwable t) {
        }
    }
    
    @Override
    public void onServiceDied(XposedService service) {
        this.xposedService = null;
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    private void loadConfig() {
        if (prefs == null) return;
        String savedBg = prefs.getString("bg_color", "000000");
        String savedText = prefs.getString("text_color", "FFFFFF");
        if (savedBg.matches("[0-9A-Fa-f]{6}")) {
            bgColor = 0xFF000000 | Integer.parseInt(savedBg, 16);
        }
        if (savedText.matches("[0-9A-Fa-f]{6}")) {
            textColor = 0xFF000000 | Integer.parseInt(savedText, 16);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTheme();
    }
    
    private boolean isDarkMode() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        int mode = uiModeManager.getNightMode();
        if (mode == UiModeManager.MODE_NIGHT_YES) return true;
        if (mode == UiModeManager.MODE_NIGHT_NO) return false;
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
    
    private void updateTheme() {
        if (rootLayout == null) return;
        boolean isDark = isDarkMode();
        rootLayout.setBackgroundColor(isDark ? DARK_BG : LIGHT_BG);
        updateChildrenTheme(rootLayout, isDark);
    }
    
    private void updateChildrenTheme(View view, boolean isDark) {
        int textColor = isDark ? DARK_TEXT : LIGHT_TEXT;
        int btnBg = isDark ? DARK_BTN_BG : LIGHT_BTN_BG;
        int btnText = isDark ? DARK_BTN_TEXT : LIGHT_BTN_TEXT;
        int editBg = isDark ? DARK_EDIT_BG : LIGHT_EDIT_BG;
        
        if (view instanceof Button) {
            Button btn = (Button) view;
            btn.setBackgroundColor(btnBg);
            btn.setTextColor(btnText);
        } else if (view instanceof EditText) {
            EditText et = (EditText) view;
            et.setBackgroundColor(editBg);
            et.setTextColor(textColor);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            String text = tv.getText().toString();
            if (!text.startsWith("#") && !text.equals("R ") && !text.equals("G ") && !text.equals("B ")) {
                tv.setTextColor(textColor);
            }
        } else if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;
            for (int i = 0; i < layout.getChildCount(); i++) {
                updateChildrenTheme(layout.getChildAt(i), isDark);
            }
        }
    }
    
    private LinearLayout createUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        rootLayout = root;
        
        TextView title = new TextView(this);
        title.setText("刺猬猫夜间模式调色");
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);
        
        TextView bgTitle = new TextView(this);
        bgTitle.setText("背景颜色");
        bgTitle.setTextSize(16);
        bgTitle.setPadding(0, 16, 0, 8);
        root.addView(bgTitle);
        
        bgPreview = new View(this);
        bgPreview.setBackgroundColor(bgColor);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(200, 60);
        pp.gravity = Gravity.CENTER;
        bgPreview.setLayoutParams(pp);
        root.addView(bgPreview);
        
        bgHexEdit = createHexEditText(hex(bgColor));
        bgHexEdit.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(180, -2);
        ep.gravity = Gravity.CENTER;
        ep.topMargin = 16;
        bgHexEdit.setLayoutParams(ep);
        root.addView(bgHexEdit);
        
        LinearLayout bgPresets = new LinearLayout(this);
        bgPresets.setGravity(Gravity.CENTER);
        for (int i = 0; i < BG_COLORS.length; i++) {
            final int idx = i;
            Button btn = new Button(this);
            btn.setText(BG_NAMES[i]);
            btn.setTextSize(11);
            btn.setOnClickListener(v -> {
                bgColor = BG_COLORS[idx];
                updateDisplay();
            });
            bgPresets.addView(btn);
        }
        root.addView(bgPresets);
        
        addDivider(root);
        
        TextView textTitle = new TextView(this);
        textTitle.setText("文字颜色");
        textTitle.setTextSize(16);
        textTitle.setPadding(0, 16, 0, 8);
        root.addView(textTitle);
        
        textPreview = new View(this);
        textPreview.setBackgroundColor(textColor);
        textPreview.setLayoutParams(pp);
        root.addView(textPreview);
        
        textHexEdit = createHexEditText(hex(textColor));
        textHexEdit.setGravity(Gravity.CENTER);
        textHexEdit.setLayoutParams(ep);
        root.addView(textHexEdit);
        
        LinearLayout textPresets = new LinearLayout(this);
        textPresets.setGravity(Gravity.CENTER);
        for (int i = 0; i < TEXT_COLORS.length; i++) {
            final int idx = i;
            Button btn = new Button(this);
            btn.setText(TEXT_NAMES[i]);
            btn.setTextSize(11);
            btn.setOnClickListener(v -> {
                textColor = TEXT_COLORS[idx];
                updateDisplay();
            });
            textPresets.addView(btn);
        }
        root.addView(textPresets);
        
        addDivider(root);
        
        TextView rgbLabel = new TextView(this);
        rgbLabel.setText("RGB调色 (先点下方选择编辑目标)");
        rgbLabel.setPadding(0, 16, 0, 8);
        root.addView(rgbLabel);
        
        seekR = new SeekBar(this);
        seekG = new SeekBar(this);
        seekB = new SeekBar(this);
        seekR.setMax(255);
        seekG.setMax(255);
        seekB.setMax(255);
        
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, -2, 1);
        
        LinearLayout rRow = new LinearLayout(this);
        TextView rLabel = new TextView(this);
        rLabel.setText("R ");
        rLabel.setTextColor(0xFFFF0000);
        rRow.addView(rLabel);
        seekR.setLayoutParams(sp);
        rRow.addView(seekR);
        root.addView(rRow);
        
        LinearLayout gRow = new LinearLayout(this);
        TextView gLabel = new TextView(this);
        gLabel.setText("G ");
        gLabel.setTextColor(0xFF00AA00);
        gRow.addView(gLabel);
        seekG.setLayoutParams(sp);
        gRow.addView(seekG);
        root.addView(gRow);
        
        LinearLayout bRow = new LinearLayout(this);
        TextView bLabel = new TextView(this);
        bLabel.setText("B ");
        bLabel.setTextColor(0xFF0000FF);
        bRow.addView(bLabel);
        seekB.setLayoutParams(sp);
        bRow.addView(seekB);
        root.addView(bRow);
        
        SeekBar.OnSeekBarChangeListener sl = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int val, boolean user) {
                if (user) {
                    int c = 0xFF000000 | (seekR.getProgress() << 16) | (seekG.getProgress() << 8) | seekB.getProgress();
                    if (editingBg) bgColor = c; else textColor = c;
                    updateDisplay();
                }
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        };
        seekR.setOnSeekBarChangeListener(sl);
        seekG.setOnSeekBarChangeListener(sl);
        seekB.setOnSeekBarChangeListener(sl);
        
        LinearLayout editBtns = new LinearLayout(this);
        editBtns.setGravity(Gravity.CENTER);
        Button editBgBtn = new Button(this);
        editBgBtn.setText("编辑背景");
        editBgBtn.setOnClickListener(v -> {
            editingBg = true;
            setSeekBar(bgColor);
        });
        editBtns.addView(editBgBtn);
        Button editTextBtn = new Button(this);
        editTextBtn.setText("编辑文字");
        editTextBtn.setOnClickListener(v -> {
            editingBg = false;
            setSeekBar(textColor);
        });
        editBtns.addView(editTextBtn);
        root.addView(editBtns);
        
        addDivider(root);
        
        LinearLayout bottomBtns = new LinearLayout(this);
        bottomBtns.setGravity(Gravity.CENTER);
        Button saveBtn = new Button(this);
        saveBtn.setText("保存设置");
        saveBtn.setOnClickListener(v -> saveConfig());
        bottomBtns.addView(saveBtn);
        Button resetBtn = new Button(this);
        resetBtn.setText("恢复默认");
        resetBtn.setOnClickListener(v -> {
            bgColor = 0xFF000000;
            textColor = 0xFFFFFFFF;
            updateDisplay();
        });
        bottomBtns.addView(resetBtn);
        root.addView(bottomBtns);
        
        TextView hint = new TextView(this);
        hint.setText("\n保存后强制停止刺猬猫再打开生效");
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint);
        
        updateTheme();
        
        return root;
    }
    
    private EditText createHexEditText(String initialValue) {
        EditText et = new EditText(this);
        et.setText("#" + initialValue);
        et.setTextSize(16);
        et.setSingleLine(true);
        
        InputFilter hexFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char c = Character.toUpperCase(source.charAt(i));
                    if (c == '#' || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                        sb.append(c);
                    }
                }
                return sb;
            }
        };
        et.setFilters(new InputFilter[]{hexFilter, new InputFilter.LengthFilter(7)});
        
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromCode) return;
                
                String text = s.toString();
                
                if (text.isEmpty()) {
                    isUpdatingFromCode = true;
                    s.append('#');
                    isUpdatingFromCode = false;
                    return;
                }
                
                if (text.charAt(0) != '#') {
                    isUpdatingFromCode = true;
                    s.insert(0, "#");
                    isUpdatingFromCode = false;
                    return;
                }
                
                if (text.length() == 7) {
                    String hex = text.substring(1);
                    if (hex.matches("[0-9A-Fa-f]{6}")) {
                        int color = 0xFF000000 | Integer.parseInt(hex, 16);
                        if (et == bgHexEdit) {
                            bgColor = color;
                            bgPreview.setBackgroundColor(color);
                        } else {
                            textColor = color;
                            textPreview.setBackgroundColor(color);
                        }
                        setSeekBar(color);
                    }
                }
            }
        });
        
        et.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isUpdatingFromCode) {
                String text = et.getText().toString();
                String hex = text.length() > 1 ? text.substring(1) : "";
                while (hex.length() < 6) hex = hex + "0";
                isUpdatingFromCode = true;
                et.setText("#" + hex.toUpperCase());
                isUpdatingFromCode = false;
                
                if (hex.matches("[0-9A-Fa-f]{6}")) {
                    int color = 0xFF000000 | Integer.parseInt(hex, 16);
                    if (et == bgHexEdit) {
                        bgColor = color;
                        bgPreview.setBackgroundColor(color);
                    } else {
                        textColor = color;
                        textPreview.setBackgroundColor(color);
                    }
                }
            }
        });
        
        return et;
    }
    
    private void addDivider(LinearLayout root) {
        View div = new View(this);
        div.setBackgroundColor(isDarkMode() ? 0xFF444444 : 0xFFCCCCCC);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
        lp.setMargins(0, 24, 0, 16);
        div.setLayoutParams(lp);
        root.addView(div);
    }
    
    private void setSeekBar(int color) {
        seekR.setProgress((color >> 16) & 0xFF);
        seekG.setProgress((color >> 8) & 0xFF);
        seekB.setProgress(color & 0xFF);
    }
    
    private void updateDisplay() {
        bgPreview.setBackgroundColor(bgColor);
        textPreview.setBackgroundColor(textColor);
        
        isUpdatingFromCode = true;
        bgHexEdit.setText("#" + hex(bgColor));
        textHexEdit.setText("#" + hex(textColor));
        isUpdatingFromCode = false;
    }
    
    private String hex(int c) {
        return String.format("%02X%02X%02X", (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF);
    }
    
    private void saveConfig() {
        String bg = hex(bgColor);
        String txt = hex(textColor);
        
        prefs.edit()
            .putString("bg_color", bg)
            .putString("text_color", txt)
            .apply();
        
        Toast.makeText(this, "已保存 背景#" + bg + " 文字#" + txt + "\n请重启刺猬猫", Toast.LENGTH_LONG).show();
    }
}