package com.example.shovelheroapp.Models.Retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CloudFunctionsService {
    @POST("sendIdForValidation") // Set up the endpoint for email Cloud Function
    Call<Void> sendIdForValidation(@Body GuardianIdInformation data);
}
