package com.checkmobi.sdk.network.request;

public class ValidationRequestBody {
    
    private String type;
    private String number;
    private String platform;
    private String language;
    
    public ValidationRequestBody(String type, String number, String language) {
        this.type = type;
        this.number = number;
        this.language = language;
        this.platform = "android";
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
}
