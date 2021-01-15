package com.checkmobi.sdk.network.request;

import com.google.gson.annotations.SerializedName;

public class ValidationRequestBody {

    @SerializedName("type")
    private String type;

    @SerializedName("number")
    private String number;

    @SerializedName("platform")
    private String platform;

    @SerializedName("language")
    private String language;
    
    @SerializedName("android_app_hash")
    private String androidAppHash;
    
    public ValidationRequestBody(String type, String number, String language, String androidAppHash) {
        this.type = type;
        this.number = number;
        this.language = language;
        this.platform = "android";
        this.androidAppHash = androidAppHash;
    }
    
    public String getType() {
        return type;
    }
    
    public String getNumber() {
        return number;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getAndroidAppHash() {
        return androidAppHash;
    }
}
