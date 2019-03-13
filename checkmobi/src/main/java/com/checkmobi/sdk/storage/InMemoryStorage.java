package com.checkmobi.sdk.storage;

import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage {
    
    private CheckNumberResponse mLastCheckNumberResponse;
    private Map<String, LastValidation> mLastValidationMap;
    
    protected InMemoryStorage() {
        reset();
    }
    
    protected void updateLastValidation(String validationType, ValidationResponse validationResponse) {
        LastValidation lastValidation = readLastValidationForType(validationType);
        if (lastValidation == null) {
            lastValidation = new LastValidation(System.currentTimeMillis(), validationType, 1, validationResponse);
        } else {
            lastValidation.setTries(lastValidation.getTries()+1);
            lastValidation.setTimestamp(System.currentTimeMillis());
            lastValidation.setValidationResponse(validationResponse);
        }
        mLastValidationMap.put(validationType, lastValidation);
    }
    
    protected LastValidation readLastValidationForType(String validationType) {
        return mLastValidationMap.get(validationType);
    }
    
    protected LastValidation getLatestLastValidation() {
        LastValidation latestLastValidation = null;
        for (LastValidation lastValidation : mLastValidationMap.values()) {
            if (latestLastValidation == null || lastValidation.getTimestamp() > latestLastValidation.getTimestamp()) {
                latestLastValidation = lastValidation;
            }
        }
        return latestLastValidation;
    }
    
    protected void updateLastUsedFullNumber(CheckNumberResponse lastFullNumber) {
        mLastCheckNumberResponse = lastFullNumber;
    }
    
    protected CheckNumberResponse getLastUsedFullNumber() {
        return mLastCheckNumberResponse;
    }
    
    protected void reset() {
        mLastValidationMap = new HashMap<>();
        mLastCheckNumberResponse = null;
    }
    
}
