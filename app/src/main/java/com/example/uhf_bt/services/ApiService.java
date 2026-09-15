package com.example.uhf_bt.services;


import androidx.annotation.Nullable;

import com.example.uhf_bt.models.CheckStockItem;
import com.example.uhf_bt.models.CheckStockItemDetailDto;
import com.example.uhf_bt.models.Kanban;
import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.models.CheckStock;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.MoveTagDto;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.models.PalletTransaction;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.PickingTagDto;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ProductItemLite;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.TagDto;
import com.example.uhf_bt.models.UserProfile;
import com.example.uhf_bt.models.Vehicle;
import com.example.uhf_bt.models.VehicleDto;
import com.example.uhf_bt.request.LoginRequest;
import com.example.uhf_bt.response.LoginResponse;

import java.util.List;

import jxl.write.DateTime;
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

    @GET("api/mobile/mobiletag/all")
    Call<List<TagDto>> getAllTags(@Query("productids") @Nullable String productIds);

    @GET("api/mobile/mobileproductitem")
    Call<List<ProductItem>> getProductItems();
    @retrofit2.http.Streaming
    @GET("api/mobile/mobileproductitem/lite")
    Call<okhttp3.ResponseBody> getProductItemsLiteStream(@Query("lastSyncTime") String lastSyncTime);
    @GET("api/mobile/mobilemoveorder")
    Call<List<MoveOrder>> getMoveOrders();
    @GET("api/mobile/mobilemoveorder/by-date")
    Call<List<MoveOrder>> getMoveOrdersbyDate(@Query("date") String date);

    @GET("api/mobile/mobilecheckstock")
    Call<List<CheckStock>> getCheckStocks();
    @GET("api/mobile/mobilecheckstock/by-date")
    Call<List<CheckStock>> getCheckStocksByDate(@Query("date") String date);

    @GET("api/mobile/MobileCheckStockItem")
    Call<List<CheckStockItemDetailDto>> getCheckStockItems(@Query("checkStockId") String checkStockId);

    @GET("api/mobile/MobileCheckStockItem/checkstock-tags/{checkStockId}")
    Call<List<TagDto>> getCheckStockTags(@Path("checkStockId") String checkStockId);

    @GET("api/mobile/mobilecheckstocktransaction")
    Call<List<com.example.uhf_bt.models.CheckStockItem>> getCheckStockTransactions(@Query("checkStockId") String checkStockId);

    @POST("api/mobile/mobilecheckstocktransaction")
    Call<List<ResponseDto>> createCheckStockTransaction(
            @Header("ParentId") String parentId,
            @Header("UserId") String userId,
            @Body List<com.example.uhf_bt.models.CheckStockTagDto> items
    );

    @GET("api/mobile/mobilemoveorderitem")
    Call<List<MoveOrderItem>> getMoveOrderItems(@Query("moveorderid") String moveOrderId);
    @GET("api/mobile/mobilemoveordertransaction/by-move-order/{id}")
    Call<List<MoveOrderItem>> getMoveOrderTransactions(@Path("id") String moveOrderId);

    @GET("api/mobile/productitem/by-tag/{tagId}")
    Call<ProductItem> getProductItemByTag(@Path("tagId") String tagId);
    @GET("api/mobile/mobileproductitem/by-status/{status}")
    Call<List<ProductItem>> getProductItemsByStatus(@Path("status") int status);
    @GET("api/mobile/mobileproductitem/by-product-instock/{id}")
    Call<List<ProductItem>> getProductItemsByProductId(@Path("id") String id);

    @GET("api/mobile/mobileproductitem/stock-by-product-number/{Number}")
    Call<List<ProductItem>> getProductItemsByProductNumber(@Path("Number") String ProductNumber);
    @POST("api/mobile/MobileMoveOrderTransactionChild")
    Call<List<ResponseDto>> createMoveOrderTransaction(
            @Header("ParentId") String parentId,
            @Header("UserId") String userId,  // เพิ่มบรรทัดนี้ (ส่ง null ได้)
            @Body List<MoveTagDto> items
    );
    @POST("api/mobile/MobilePickingOrderTransactionChild")
    Call<List<ResponseDto>> createPickingOrderTransaction(
            @Header("ParentId") String parentId,
            @Header("UserId") String userId,
            @Body List<PickingTagDto> items
    );

    @POST("api/mobile/MobileReceivePallet")
    Call<List<ResponseDto>> createReceivePallet(@Body List<PickingOrderItem> items
    );

    @POST("api/mobile/MobilePickingOrderTransactionChild/Renant")
    Call<List<ResponseDto>> createPickingRenant(
            @Header("ParentId") String parentId,
            @Header("TagRemark") String tagRemark,
            @Header("UserId") String userId,
            @Body List<PickingTagDto> items
    );

    ///api/mobile/MobilePickingOrderTransactionChild/Renant
    @GET("api/mobile/mobilepickingordertransaction/by-picking-order/{pickingOrderId}")
    Call<List<PickingOrderTransaction>> getPickingOrderTransactions(@Path("pickingOrderId") String pickingOrderId);

    @GET("api/mobile/mobilepickingordertransaction/pickingrenanttransaction/{pickingOrderId}")
    Call<List<PickingOrderTransaction>> getPickingRenantTransaction(@Path("pickingOrderId") String pickingOrderId);


    @GET("api/mobile/mobilepickingorder")
    Call<List<PickingOrder>> getPickingOrders();
    @GET("api/mobile/mobilepickingorder/by-date")
    Call<List<PickingOrder>> getPickingOrdersbyDate(@Query("date") String date);;

    @GET("api/mobile/mobilepickingorderitem")
    Call<List<PickingOrderItem>> getPickingOrderItems(@Query("pickingorderid") String pickingOrderId);

    @GET("api/mobile/mobilePickingOrderItem/pallet-tagids")
    Call<List<PalletTag>> getPalletTags();

    @GET("api/mobile/mobilePickingOrderItem/pallet-tagids/{pickingOrderId}")
    Call<List<PalletTag>> getPalletTagsByPickingOrder(@Path("pickingOrderId") String pickingOrderId);

    @GET("api/mobile/mobilePickingOrderItem/pallet-tagids/by-date")
    Call<List<PalletTag>> getPalletTagsByDate(@Query("date") String date);

    // Optional: Add a method to get pallet details by tag ID
    @GET("api/mobile/MobilePickingOrderItem/pallet/{palletTagId}")
    Call<PalletTag> getPalletByTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/MobileReceivePallet/pallet/{palletTagNumber}")
    Call<List<PickingOrderItem>> getPickingOrderItemByPalletTagNumber(@Path("palletTagNumber") String palletTagNumber);

    @GET("api/mobile/MobilePickingOrderItem/pallet/{palletTagId}")
    Call<List<PickingOrderItem>> getPalletDetailByTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/MobilePickingOrderTransaction/by-pallet-tag-id/{palletTagId}")
    Call<List<PalletTransaction>> getTransactionsByPalletTagId(@Path("palletTagId") String palletTagId);

    @GET("api/mobile/mobileKanban")
    Call<List<Kanban>> getKanbans();

    @POST("api/mobile/MobileKanban")
    Call<List<ResponseDto>> createKanbanPreparing(
            @Header("ParentId") String kanbanTagId,
            @Body List<PalletTag> items
    );

    @POST("api/mobile/MobileKanban/Confirm")
    Call<List<ResponseDto>> createKanbanConfirm(
            @Header("KanbanId") String kanbanId,
            @Header("KanbanNumber") String kanbanNumber
    );

    @GET("api/mobile/MobileKanban/KanbanDetail/{kanbanId}/{number}")
    Call<List<PickingOrderItem>> getKanbanDetail(@Path("kanbanId") int kanbanId, @Path("number") String number);

    @GET("api/mobile/MobileKanban/KanbanTransaction/{kanbanTagId}")
    Call<List<PickingOrderItem>> getKanbanTransaction(@Path("kanbanTagId") String kanbanTagId);


    @POST("api/mobile/MobileVehicle")
    Call<List<ResponseDto>> createMobileVehicleLoading(
            @Header("ParentId") String vehicleTagId,
            @Body List<Kanban> kanbans
    );

//    @POST("api/mobile/MobileVehicle/Confirm")
//    Call<List<ResponseDto>> createVehicleConfirm(
//            @Header("ParentId") String vehicleId
//    );

    @POST("api/mobile/MobileVehicle/Confirm")
    Call<List<ResponseDto>> createVehicleConfirm(
            @Header("ParentId") String vehicleId,
            @Header("UserId") @Nullable String userId
    );

    @GET("api/mobile/Mobilevehicle") // Replace with your actual API endpoint
    Call<List<VehicleDto>> getVehicles();

    // Add these methods to your existing ApiService interface

    // Vehicle related APIs
    @GET("api/mobile/MobileVehicle/VehicleKanban/{vehicleTagId}")
    Call<List<PickingOrderItem>> getVehicleKanbans(@Path("vehicleTagId") String vehicleTagId,
                                                   @Query("number") @Nullable String number);
    @POST("api/mobile/MobileVehicle/CheckIn/{vehicleTagId}")
    Call<ResponseDto> checkInVehicle(@Path("vehicleTagId") String vehicleTagId);

    // Kanban scanning APIs
    @GET("api/kanbans/available/vehicle/{vehicleId}")
    Call<List<Kanban>> getAvailableKanbansForVehicle(@Path("vehicleId") String vehicleId);

    @GET("api/kanbans/available/location/{locationId}")
    Call<List<Kanban>> getAvailableKanbansForLocation(@Path("locationId") String locationId);

    @POST("api/vehicles/{vehicleId}/assign-kanbans")
    Call<List<ResponseDto>> assignKanbansToVehicle(@Path("vehicleId") String vehicleId, @Body List<String> kanbanIds);

    @POST("api/locations/{locationId}/assign-kanbans")
    Call<List<ResponseDto>> assignKanbansToLocation(@Path("locationId") String locationId, @Body List<String> kanbanIds);

}
