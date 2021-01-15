package com.checkmobi.sdk.network.request;

import com.google.gson.annotations.SerializedName;

public class CheckNumberRequestBody {
    
    private static final String ANDROID_PLATFORM = "android";

    @SerializedName("number")
    private String number;

    @SerializedName("platform")
    private String platform = ANDROID_PLATFORM;

    @SerializedName("language")
    private String language;
    
    public CheckNumberRequestBody(String number, String language) {
        this.number = number;
        this.language = language;
    }
    
}
