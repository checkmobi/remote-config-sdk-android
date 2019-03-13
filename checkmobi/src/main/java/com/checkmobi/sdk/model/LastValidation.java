package com.checkmobi.sdk.model;

import com.checkmobi.sdk.network.response.ValidationResponse;

public class LastValidation {
    
    private long mTimestamp;
    private String mValidationType;
    private int mTries;
    private ValidationResponse mValidationResponse;
    
    public LastValidation(long timestamp, String validationType, int tries, ValidationResponse validationResponse) {
        mTimestamp = timestamp;
        mValidationType = validationType;
        mTries = tries;
        mValidationResponse = validationResponse;
    }
    
    public long getTimestamp() {
        return mTimestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }
    
    public String getValidationType() {
        return mValidationType;
    }
    
    public void setValidationType(String validationType) {
        mValidationType = validationType;
    }
    
    public int getTries() {
        return mTries;
    }
    
    public void setTries(int tries) {
        this.mTries = tries;
    }
    
    public ValidationResponse getValidationResponse() {
        return mValidationResponse;
    }
    
    public void setValidationResponse(ValidationResponse validationResponse) {
        mValidationResponse = validationResponse;
    }
}
