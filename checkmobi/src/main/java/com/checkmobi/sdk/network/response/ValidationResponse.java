package com.checkmobi.sdk.network.response;

import com.google.gson.annotations.SerializedName;

public class ValidationResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("cli_prefix")
    private String cli_prefix;
    
    public String getId() {
        return id;
    }
    
    public String getCli_prefix() {
        return cli_prefix;
    }
}
