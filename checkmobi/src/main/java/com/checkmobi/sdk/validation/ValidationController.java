package com.checkmobi.sdk.validation;

import com.checkmobi.sdk.CheckmobiSdk;
import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.RetrofitController;
import com.checkmobi.sdk.network.request.ValidationRequestBody;
import com.checkmobi.sdk.network.request.VerificationRequestBody;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;
import com.checkmobi.sdk.network.response.VerificationResponse;
import com.checkmobi.sdk.storage.StorageController;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValidationController {
    
    public static final String PIN_EXTRA = "ValidationController.PIN_EXTRA";
    public static final String ID_EXTRA = "ValidationController.ID_EXTRA";
    
    public static CheckNumberResponse.ValidationMethod getFirstAvailableValidationMethod(@NonNull Context context, CheckNumberResponse checkNumberResponse) {
        List<CheckNumberResponse.ValidationMethod> validationMethods = StorageController.getInstance().getLastUsedValidationMethods();
        for (CheckNumberResponse.ValidationMethod validationMethod : validationMethods) {
            String validationType = validationMethod.getType();
            if (getRetriesLeftForValidationMethod(context, validationType) > 0) {
                if (checkNumberResponse != null && !checkNumberResponse.isValidationTypeAvailable(validationType)) {
                    return null;
                } else {
                    return validationMethod;
                }
            }
        }
        return null;
    }
    
    public static boolean isValidationMethodValid(@NonNull Context context, @NonNull String validationType, @NonNull CheckNumberResponse checkNumberResponse) {
        if (checkNumberResponse != null && !checkNumberResponse.isValidationTypeAvailable(validationType)) {
            return false;
        } else {
            for (CheckNumberResponse.ValidationMethod validationMethod : checkNumberResponse.getValidationMethods()) {
                if (validationMethod.getType().equals(validationType) &&
                        getRetriesLeftForValidationMethod(context, validationType) > 0) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static void validate(@NonNull final Context context, @NonNull final String validationType, @NonNull final String phoneNumber, @NonNull final Callback<ValidationResponse> callback) {
        String language = Locale.getDefault().getLanguage();
        if (validationType.equals(CheckNumberResponse.ValidationMethod.IVR)) {
            language = Locale.getDefault().toString().replace('_', '-');
            if (!LanguageConstants.IVR_SUPPORTED_LANGUAGES_SET.contains(language)) {
                language = null;
            }
        }
        Call<ValidationResponse> repos = RetrofitController.getInstance().getService()
                .validate(CheckmobiSdk.getInstance().getApiKey(), new ValidationRequestBody(validationType, phoneNumber, language));
        repos.enqueue(new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                if (response.isSuccessful()) {
                    if (getRetriesLeftForValidationMethod(context, validationType) > 0) {
                        StorageController.getInstance().updateLastValidation(validationType, response.body());
                    }
                }
                callback.onResponse(call, response);
            }
        
            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
    
    public static long getRetriesLeftForValidationMethod(@NonNull Context context, @NonNull String validationType) {
        List<CheckNumberResponse.ValidationMethod> validationMethods = StorageController.getInstance().getLastUsedValidationMethods();
        LastValidation lastValidation = StorageController.getInstance().readLastValidationForType(validationType);
        int validationsUsedCount = 0;
        if (lastValidation != null) {
            validationsUsedCount = lastValidation.getTries();
        }
        for (CheckNumberResponse.ValidationMethod method : validationMethods) {
            if (method.getType().equals(validationType)) {
                return method.getMaxAttempts() - validationsUsedCount;
            }
        }
        return 0;
    }
    
    public static long getTimeUntilValidationMethodsAreAvailable() {
        List<CheckNumberResponse.ValidationMethod> validationMethods = StorageController.getInstance().getLastUsedValidationMethods();
        LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
        if (lastValidation != null) {
            for (CheckNumberResponse.ValidationMethod method : validationMethods) {
                if (method.getType().equals(lastValidation.getValidationType())) {
                    long timeUntilAvailable = method.getTimeUntilAvailableInMillis();
                    long timePassedSinceLastValidation = System.currentTimeMillis() - lastValidation.getTimestamp();
                    if (timePassedSinceLastValidation > timeUntilAvailable) {
                        return 0;
                    } else {
                        return timeUntilAvailable - timePassedSinceLastValidation;
                    }
                }
            }
        }
        return 0;
    }
    
    public static void verify(String id, String pin, Callback<VerificationResponse> callback) {
        Call<VerificationResponse> repos = RetrofitController.getInstance().getService()
                .verify(CheckmobiSdk.getInstance().getApiKey(), new VerificationRequestBody(id, pin));
        repos.enqueue(callback);
    }
    
    public static boolean isValidationMethodTurnedOn(@NonNull String validationType) {
        List<CheckNumberResponse.ValidationMethod> validationMethods = StorageController.getInstance().getLastUsedValidationMethods();
        for (CheckNumberResponse.ValidationMethod method : validationMethods) {
            if (method.getType().equals(validationType)) {
                return true;
            }
        }
        return false;
    }
    
    
}
