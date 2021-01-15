package com.checkmobi.sdk.network.request;

import com.google.gson.annotations.SerializedName;

public class VerificationRequestBody {

    @SerializedName("id")
    private String id;

    @SerializedName("pin")
    private String pin;

    @SerializedName("use_server_hangup")
    private boolean useServerHangup;
    
    public VerificationRequestBody(String id, String pin) {
        this.id = id;
        this.pin = pin;
        this.useServerHangup = true;
    }
    
    public String getId() {
        return id;
    }
    
    public String getPin() {
        return pin;
    }
    
    public boolean isUseServerHangup() {
        return useServerHangup;
    }
}
