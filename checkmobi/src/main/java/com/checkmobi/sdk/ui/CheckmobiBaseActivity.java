package com.checkmobi.sdk.ui;

import com.checkmobi.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class CheckmobiBaseActivity extends AppCompatActivity {
    
    public static final String EXTRA_CUSTOM_THEME = "checkmobi_extra_custom_theme";
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        int theme = R.style.CheckmobiUI;
        if (extras != null) {
            int customTheme = extras.getInt(EXTRA_CUSTOM_THEME, -1);
            if (customTheme != -1) {
                theme = customTheme;
            }
        }
        setTheme(theme);
    }
    
    protected void startActivityForResultWithExtras(Intent intent, int requestCode) {
        startActivityForResult(addExtras(intent), requestCode);
    }
    
    protected void startActivityWithExtras(Intent intent) {
        startActivity(addExtras(intent));
    }
    
    private Intent addExtras(Intent intent) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }
}
