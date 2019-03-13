package com.checkmobi.sdk.ui;

import android.content.Context;
import android.content.Intent;

public class VerificationIntentBuilder {
    
    private int mTheme = -1;
    
    public VerificationIntentBuilder setTheme(int theme) {
        mTheme = theme;
        return this;
    }
    
    public Intent build(Context context) {
        Intent intent = new Intent(context, NumberInputActivity.class);
        if (mTheme != -1) {
            intent.putExtra(CheckmobiBaseActivity.EXTRA_CUSTOM_THEME, mTheme);
        }
        return intent;
    }
    
}
