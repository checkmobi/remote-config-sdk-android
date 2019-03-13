package com.checkmobi.sdk.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitController {
    
    private Retrofit retrofit;
    private CheckMobiServiceInterface service;
    
    private static final RetrofitController instance = new RetrofitController();
    
    private RetrofitController() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.checkmobi.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    
        service = retrofit.create(CheckMobiServiceInterface.class);
    };
    
    public static RetrofitController getInstance() {
        return instance;
    }
    
    public CheckMobiServiceInterface getService() {
        return service;
    }
    
}
