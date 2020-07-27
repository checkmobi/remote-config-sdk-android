package com.checkmobi.sdk;

import com.checkmobi.sdk.storage.StorageController;
import com.checkmobi.sdk.ui.VerificationIntentBuilder;

import android.content.Context;

public class CheckmobiSdk {
    
    private static CheckmobiSdk instance = new CheckmobiSdk();
    
    private String apiKey;
    
    private CheckmobiSdk() {
    }
    
    public static CheckmobiSdk getInstance() {
        return instance;
    }
    
    public VerificationIntentBuilder createVerificationIntentBuilder() {
        return new VerificationIntentBuilder();
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getVerifiedNumber(Context context) {
        return StorageController.getInstance().getVerifiedNumber(context);
    }
    
    public String getVerifiedNumberServerId(Context context) {
        return StorageController.getInstance().getVerifiedNumberServerId(context);
    }
    
    public void resetVerifiedNumber(Context context) {
        StorageController.getInstance().resetVerifiedNumber(context);
    }
}
