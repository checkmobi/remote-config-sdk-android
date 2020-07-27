package com.checkmobi.sdk.storage;

import com.checkmobi.sdk.model.CountryCode;
import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.GsonHolder;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;

import android.content.Context;

public class SharedPreferencesStorage {
    
    private static final String SHARED_PREFS_FILE = "checkmobi_shared_prefs";
    private static final String SETTINGS = "checkmobi_settings";
    private static final String LAST_VALIDATION_REVERSE_CLI = "checkmobi_last_validation_reverse_cli";
    private static final String LAST_VALIDATION_SMS = "checkmobi_last_validation_sms";
    private static final String LAST_VALIDATION_IVR = "checkmobi_last_validation_ivr";
    private static final String LAST_USED_NUMBER = "checkmobi_last_used_number";
    private static final String LAST_USED_FULL_NUMBER = "checkmobi_last_used_full_number";
    private static final String LAST_USED_COUNTRY_CODE = "checkmobi_last_used_country_code";
    private static final String VERIFIED_NUMBER = "checkmobi_verified_number";
    private static final String VERIFIED_NUMBER_SERVER_ID = "checkmobi_verified_number_server_id";
    
    private void saveString(Context context, String key, String value) {
        context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .commit();
    }
    
    private String readString(Context context, String key) {
        return context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
                .getString(key, null);
    }
    
    protected void saveCountryCode(Context context, CountryCode countryCode) {
        saveString(context, LAST_USED_COUNTRY_CODE, GsonHolder.getGson().toJson(countryCode));
    }
    
    protected CountryCode readCountryCode(Context context) {
        String countryCodeAsString = readString(context, LAST_USED_COUNTRY_CODE);
        if (countryCodeAsString != null) {
            return GsonHolder.getGson().fromJson(countryCodeAsString, CountryCode.class);
        } else {
            return null;
        }
    }
    
    protected void updateLastUsedNumber(Context context, String number) {
        saveString(context, LAST_USED_NUMBER, number);
    }
    
    protected String getLastUsedNumber(Context context) {
        return readString(context, LAST_USED_NUMBER);
    }
    
    protected void saveVerifiedNumberServerId(Context context, String verifiedNumberServerId) {
        saveString(context, VERIFIED_NUMBER_SERVER_ID, verifiedNumberServerId);
    }
    
    protected String getVerifiedNumberServerId(Context context) {
        return readString(context, VERIFIED_NUMBER_SERVER_ID);
    }
    
    protected void saveVerifiedNumber(Context context, String verifiedNumber) {
        saveString(context, VERIFIED_NUMBER,verifiedNumber);
    }
    
    protected String getVerifiedNumber(Context context) {
        return readString(context, VERIFIED_NUMBER);
    }
    
    protected void resetVerifiedNumber(Context context) {
        saveString(context, VERIFIED_NUMBER, null);
        saveString(context, VERIFIED_NUMBER_SERVER_ID, null);
    }
    
}
