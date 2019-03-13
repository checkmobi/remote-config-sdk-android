package com.checkmobi.sdk.network;

import com.checkmobi.sdk.model.CountryCode;
import com.checkmobi.sdk.network.request.CheckNumberRequestBody;
import com.checkmobi.sdk.network.request.ValidationRequestBody;
import com.checkmobi.sdk.network.request.VerificationRequestBody;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;
import com.checkmobi.sdk.network.response.VerificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CheckMobiServiceInterface {
    
    @GET("v1/countries")
    Call<List<CountryCode>> getCountries();
    
    @POST("v1/validation/remote-config")
    Call<CheckNumberResponse> checkNumber(@Header("Authorization") String authorization, @Body CheckNumberRequestBody checkNumberRequestBody);
    
    @POST("v1/validation/request")
    Call<ValidationResponse> validate(@Header("Authorization") String authorization, @Body ValidationRequestBody validationRequestBody);
    
    @POST("v1/validation/verify")
    Call<VerificationResponse> verify(@Header("Authorization") String authorization, @Body VerificationRequestBody verificationRequestBody);
    
}
