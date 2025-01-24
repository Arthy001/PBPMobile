package com.example.uhf_bt.services;


import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.UserProfile;
import com.example.uhf_bt.request.LoginRequest;
import com.example.uhf_bt.response.LoginResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {


    @POST("api/userprofile/login")
    Call<UserProfile> login(@Body LoginRequest loginRequest);
    @POST("api/mobile/login/") // Replace "login" with your actual login endpoint
    Call<ResponseBody> login2(@Body LoginRequest loginRequest);
    @GET("api/moveorder")
    Call<List<MoveOrder>> getMoveOrders();
    @GET("api/moveorderitem")
    Call<List<MoveOrderItem>> getMoveOrderItems(@Query("moveorderid") String moveOrderId);
    @GET("api/pickingorder")
    Call<List<PickingOrder>> getPickingOrders();

    @GET("api/pickingorderitem")
    Call<List<PickingOrderItem>> getPickingOrderItems(@Query("pickingorderid") String pickingOrderId);


}