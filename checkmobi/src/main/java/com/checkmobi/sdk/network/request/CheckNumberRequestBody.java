package com.checkmobi.sdk.network.request;

public class CheckNumberRequestBody {
    
    private static final String ANDROID_PLATFORM = "android";
    
    private String number;
    private String platform = ANDROID_PLATFORM;
    private String language;
    
    public CheckNumberRequestBody(String number, String language) {
        this.number = number;
        this.language = language;
    }
    
}
