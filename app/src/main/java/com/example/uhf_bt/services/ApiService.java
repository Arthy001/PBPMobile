package com.example.uhf_bt.services;


import com.example.uhf_bt.models.Kanban;
import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.models.PalletTransaction;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
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
import retrofit2.http.Tag;

public interface ApiService {


    @POST("api/mobile/mobileuserprofile/login")
    Call<UserProfile> login(@Body LoginRequest loginRequest);
    @POST("api/mobile/login/") // Replace "login" with your actual login endpoint
    Call<ResponseBody> login2(@Body LoginRequest loginRequest);
    @GET("api/mobile/mobilemoveorder")
    Call<List<MoveOrder>> getMoveOrders();
    @GET("api/mobile/mobilemoveorderitem")
    Call<List<MoveOrderItem>> getMoveOrderItems(@Query("moveorderid") String moveOrderId);
    @GET("api/mobile/mobilemoveordertransaction/by-move-order/{id}")
    Call<List<MoveOrderItem>> getMoveOrderTransactions(@Path("id") String moveOrderId);

    @GET("api/mobile/productitem/by-tag/{tagId}")
    Call<ProductItem> getProductItemByTag(@Path("tagId") String tagId);
    @GET("api/mobile/mobileproductitem/by-status/{status}")
    Call<List<ProductItem>> getProductItemsByStatus(@Path("status") int status);
    @POST("api/mobile/MobileMoveOrderTransactionChild")
    Call<List<MoveOrderItem>> createMoveOrderTransaction(
            @Header("ParentId") String parentId,
            @Body List<MoveOrderItem> items
    );
    @POST("api/mobile/MobilePickingOrderTransactionChild")
    Call<List<PickingOrderItem>> createPickingOrderTransaction(
            @Header("ParentId") String parentId,
            @Body List<PickingOrderItem> items
    );

    @POST("api/mobile/MobileReceivePallet")
    Call<List<PickingOrderItem>> createReceivePallet(@Body List<PickingOrderItem> items
    );

    @POST("api/mobile/MobilePickingOrderTransactionChild/Renant")
    Call<List<PickingOrderItem>> createPickingRenant(
            @Header("ParentId") String parentId,
            @Header("TagRemark") String tagRemark,
            @Body List<PickingOrderItem> items
    );

    ///api/mobile/MobilePickingOrderTransactionChild/Renant
    @GET("api/mobile/mobilepickingordertransaction/by-picking-order/{pickingOrderId}")
    Call<List<PickingOrderTransaction>> getPickingOrderTransactions(@Path("pickingOrderId") String pickingOrderId);

    @GET("api/mobile/mobilepickingordertransaction/pickingrenanttransaction/{pickingOrderId}")
    Call<List<PickingOrderTransaction>> getPickingRenantTransaction(@Path("pickingOrderId") String pickingOrderId);


    @GET("api/mobile/mobilepickingorder")
    Call<List<PickingOrder>> getPickingOrders();

    @GET("api/mobile/mobilepickingorderitem")
    Call<List<PickingOrderItem>> getPickingOrderItems(@Query("pickingorderid") String pickingOrderId);

    @GET("api/mobile/mobilePickingOrderItem/pallet-tagids")
    Call<List<PalletTag>> getPalletTags();

    @GET("api/mobile/mobilePickingOrderItem/pallet-tagids/{pickingOrderId}")
    Call<List<PalletTag>> getPalletTagsByPickingOrder(@Path("pickingOrderId") String pickingOrderId);

    // Optional: Add a method to get pallet details by tag ID
    @GET("api/mobile/MobilePickingOrderItem/pallet/{palletTagId}")
    Call<PalletTag> getPalletByTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/MobileReceivePallet/pallet/{palletTagId}")
    Call<List<PickingOrderItem>> getPickingOrderItemByPalletTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/MobilePickingOrderItem/pallet/{palletTagId}")
    Call<List<PickingOrderItem>> getPalletDetailByTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/MobilePickingOrderTransaction/by-pallet-tag-id/{palletTagId}")
    Call<List<PalletTransaction>> getTransactionsByPalletTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/mobileKanban")
    Call<List<Kanban>> getKanbans();

    @POST("api/mobile/MobileKanban")
    Call<List<PalletTag>> createKanbanPreparing(
            @Header("ParentId") String kanbanTagId,
            @Body List<PalletTag> items
    );



}