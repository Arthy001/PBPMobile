package com.example.uhf_bt.services;


import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.UserProfile;
import com.example.uhf_bt.request.LoginRequest;
import com.example.uhf_bt.response.LoginResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @GET("api/moveordertransaction/by-move-order/{id}")
    Call<List<MoveOrderItem>> getMoveOrderTransactions(@Path("id") String moveOrderId);

    @GET("api/productitem/by-tag/{tagId}")
    Call<ProductItem> getProductItemByTag(@Path("tagId") String tagId);
    @GET("api/productitem/by-status/{status}")
    Call<List<ProductItem>> getProductItemsByStatus(@Path("status") int status);
    @POST("api/MoveOrderTransactionChild")
    Call<List<MoveOrderItem>> createMoveOrderTransaction(
            @Header("ParentId") String parentId,
            @Body List<MoveOrderItem> items
    );

    @POST("api/PickingOrderTransactionChild")
    Call<List<PickingOrderItem>> createPickingOrderTransaction(
            @Header("ParentId") String parentId,
            @Body List<PickingOrderItem> items
    );



    @GET("api/pickingorder")
    Call<List<PickingOrder>> getPickingOrders();

    @GET("api/pickingorderitem")
    Call<List<PickingOrderItem>> getPickingOrderItems(@Query("pickingorderid") String pickingOrderId);


}