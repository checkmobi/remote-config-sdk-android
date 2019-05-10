package com.checkmobi.sdk.network.response;

import com.google.gson.annotations.SerializedName;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.IVR;
import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.REVERSE_CLI;
import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.SMS;

public class CheckNumberResponse {
    
    public static final String DEFAULT_SMS_TEMPLATE = "Your validation code is: %code%";
    
    @SerializedName("settings")
    private List<ValidationMethod> validationMethods;
    
    @SerializedName("country_code")
    private int countryCode;
    
    @SerializedName("country_iso_code")
    private String countryIsoCode;
    
    private String carrier;
    
    @SerializedName("is_mobile")
    private boolean mobile;
    
    @SerializedName("e164_format")
    private String e164Format;
    
    private String formatting;
    
    public List<ValidationMethod> getValidationMethods() {
        return validationMethods;
    }
    
    public int getCountryCode() {
        return countryCode;
    }
    
    public String getCountryIsoCode() {
        return countryIsoCode;
    }
    
    public String getCarrier() {
        return carrier;
    }
    
    public boolean isMobile() {
        return mobile;
    }
    
    public String getE164Format() {
        return e164Format;
    }
    
    public String getFormatting() {
        return formatting;
    }
    
    public boolean isValidationTypeAvailable(String validationType) {
        for (ValidationMethod validationMethod : validationMethods) {
            if (validationMethod.type.equals(validationType)) {
                return true;
            }
        }
        return false;
    }
    
    public static CheckNumberResponse createResponseFromE164Number(String e164Number) {
        CheckNumberResponse response = new CheckNumberResponse();
        response.e164Format = e164Number;
        
        response.validationMethods = getDefaultVerificationMethods();
        return response;
    }
    
    @NonNull
    public static List<ValidationMethod> getDefaultVerificationMethods() {
        List<ValidationMethod> validationMethods = new ArrayList<>();
        ValidationMethod smsVerification = new ValidationMethod(SMS, 3, 30);
        smsVerification.smsTemplate = DEFAULT_SMS_TEMPLATE;
        validationMethods.add(smsVerification);
        ValidationMethod ivrVerification = new ValidationMethod(IVR, 3, 30);
        validationMethods.add(ivrVerification);
        ValidationMethod missedCallVerification = new ValidationMethod(REVERSE_CLI, 3, 30);
        validationMethods.add(missedCallVerification);
        return validationMethods;
    }
    
    static public class ValidationMethod {
    
        public static final String SMS = "sms";
        public static final String IVR = "ivr";
        public static final String REVERSE_CLI = "reverse_cli";
        
        private String type;
        
        @SerializedName("max_attempts")
        private int maxAttempts;
        
        private int delay;
        
        @SerializedName("sms_template")
        private String smsTemplate;
    
        @SerializedName("android_app_hash")
        private String androidAppHash;
    
        public ValidationMethod(String type, int maxAttempts, int delay) {
            this.type = type;
            this.maxAttempts = maxAttempts;
            this.delay = delay;
        }
    
        public String getType() {
            return type;
        }
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public int getDelay() {
            return delay;
        }
    
        public long getTimeUntilAvailableInMillis() {
            return delay * 1000;
        }
        
        public String getSmsTemplate() {
            return smsTemplate;
        }
    
        public String getAndroidAppHash() {
            return androidAppHash;
        }
    }
    
}