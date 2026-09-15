package com.example.uhf_bt.fragment;

import static com.example.uhf_bt.services.Settings.appMode;

import android.app.AlertDialog;
import android.app.appsearch.ReportSystemUsageRequest;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackAnimationCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.MoveOrderChildAdapter;
import com.example.uhf_bt.adapter.MoveOrderChildTagAdapter;
import com.example.uhf_bt.adapter.MoveOrderChildTagWithStatusAdapter;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.MoveTagDto;
import com.example.uhf_bt.models.PickingComponentItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.TagDto;
import com.example.uhf_bt.models.TagWithStatus;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.example.uhf_bt.tool.CheckUtils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.internal.connection.Exchange;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveOrderChildFragment extends Fragment {

    private static final String TAG = "MoveOrderChildFragment";

    private EditText tvBarcode;
    private Button btnScanBarocde;
    private LinearLayout view_barcode;

    private TextView tvTotalQty, tvTotalScan, tvTotalSuccess;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private List<MoveOrderItem> scannedTags = new ArrayList<>();


    private TextView tvOrderNo,tvSourceWarehouse, tvDestinationWarehouse;
    private ListView listView;

    private ProgressBar progressBar;
    private View emptyView;
    private MoveOrderChildAdapter adapter;
    private List<TagDto> stockTags = new ArrayList<>();
    private List<MoveOrderItem> tempList = new ArrayList<>();
    private List<MoveOrderItem> itemList = new ArrayList<>();
    private String moveOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    private MainActivity mContext;

    private Button btnScanRFID, btStop, btClear, btnConfirm ;


    public static MoveOrderChildFragment newInstance(String moveOrderId) {
        MoveOrderChildFragment fragment = new MoveOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("MoveOrderId", moveOrderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel1));
        }
        if (getArguments() != null) {
            moveOrderId = getArguments().getString("MoveOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_order_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        loadMoveOrderItems();
        setupButtonListeners();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        tvOrderNo = view.findViewById(R.id.tvOrderNo);
        tvSourceWarehouse = view.findViewById(R.id.tvSourceWarehouse);
        tvDestinationWarehouse = view.findViewById(R.id.tvDestinationWarehouse);
        listView = view.findViewById(R.id.listView);

        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_count);
        tv_total = view.findViewById(R.id.tv_total);

        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btClear = view.findViewById(R.id.btnClear);
        btStop = view.findViewById(R.id.btnStop);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        // Initialize total quantity TextViews
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);

        tvBarcode = view.findViewById(R.id.tvBarcode);
        btnScanBarocde = view.findViewById(R.id.btnScanBarcode);

        view_barcode = view.findViewById(R.id.view_barcode);

        if (appMode == "RFID" )
        {
            view_barcode.setVisibility(View.GONE);
        }

    }

    private void setupToolbar() {

        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Add notification button click listener
        ivNotification.setOnClickListener(v -> {
            showNotificationsDialog();
        });
    }

    private void setupButtonListeners() {
        btnScanRFID.setOnClickListener(v -> scanRFID());
        btClear.setOnClickListener(v -> clearRFID());
        btStop.setOnClickListener(v -> stopRFID());
        btnConfirm.setOnClickListener(v -> confirmMoveOrderTransaction());
        //btnQRCode.setOnClickListener(v -> scanQRCode());
        btnScanBarocde.setOnClickListener(v -> scanBarcode());
        tagList = new ArrayList<>();
        //mContext.tagList=tagList;
        //adapter=new UHFReadTagFragment.MyAdapter(mContext);
        //LvTags.setAdapter(adapter);

        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                if (!isExit && mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                    if(keycode==3){
                        mContext.isKeyDownUP=true;
                        startThread();
                    } else{
                        if(!mContext.isKeyDownUP){
                            if(keycode==1) {
                                if (mContext.isScanning) {
                                    stop();
                                } else {
                                    startThread();
                                }
                            }
                        }
                        if(keycode==2) {
                            if (mContext.isScanning) {
                                stop();
                                SystemClock.sleep(100);
                            }
                            //MR20
                            //inventory();
                        }
                    }

                }
            }

            @Override
            public void onKeyUp(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                if(keycode==4) {
                    stop();
                }
            }
        });
    }

    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;

    private void checkNotifications() {

        notificationMessages.clear();
        simulateGetNotifications();

        // Update UI based on notification status
        hasNotifications = !notificationMessages.isEmpty();
        updateNotificationVisibility();
    }

    private void simulateGetNotifications() {

    }

    // Add this method to update notification visibility
    // Update the updateNotificationVisibility method
    private void updateNotificationVisibility() {
        if (ivNotification != null && tvNotificationCount != null) {
            if (hasNotifications) {
                ivNotification.setVisibility(View.VISIBLE);
                tvNotificationCount.setVisibility(View.VISIBLE);
                tvNotificationCount.setText(String.valueOf(notificationMessages.size()));
            } else {
                ivNotification.setVisibility(View.GONE);
                tvNotificationCount.setVisibility(View.GONE);
            }
        }
    }
    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notifications");

        if (notificationMessages.isEmpty()) {
            builder.setMessage("No notifications available");
        } else {
            // Create numbered notifications
            String[] numberedNotifications = new String[notificationMessages.size()];
            for (int i = 0; i < notificationMessages.size(); i++) {
                numberedNotifications[i] = (i + 1) + ". " + notificationMessages.get(i);
            }

            builder.setItems(numberedNotifications, (dialog, which) -> {
                // Handle notification click
                Toast.makeText(mContext, "Selected: " + notificationMessages.get(which), Toast.LENGTH_SHORT).show();

                // You could mark this notification as read here
                // For demo purposes, we'll just show a toast
            });

            // Add option to mark all as read
            builder.setNeutralButton("Mark All as Read", (dialog, which) -> {
                // Clear all notifications
                notificationMessages.clear();
                hasNotifications = false;
                updateNotificationVisibility();
                Toast.makeText(mContext, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            });
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void setupSearchView() {
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnSearchClickListener(v -> {
            ivBack.setVisibility(View.GONE);
            toolbar_title.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            searchView.setLayoutParams(params);
        });

        searchView.setOnCloseListener(() -> {
            ivBack.setVisibility(View.VISIBLE);
            toolbar_title.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            searchView.setLayoutParams(params);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        // Customize SearchView appearance
        EditText searchEditText = searchView.findViewById(com.example.uhf_bt.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.WHITE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) searchIcon.getLayoutParams();
            params.setMarginEnd(0);
            searchIcon.setLayoutParams(params);
        }

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(Color.WHITE);
        }

        searchView.setQueryHint("Search item...");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupListView() {
        adapter = new MoveOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MoveOrderItem item = adapter.getItem(position);
            handleItemClick(item);
        });
    }

    private void updateTotalQuantities() {
        double totalQty = 0;
        double totalScan = 0;
        double totalSuccess = 0;

        for (MoveOrderItem item : itemList) {
            totalQty += item.getQuantity();
            totalScan += item.getQuantityScan();
            totalSuccess += item.getQuantitySuccess();
        }

        // Update UI with formatted values
        tvTotalQty.setText(String.format("%.0f", totalQty));
        tvTotalScan.setText(String.format("%.0f", totalScan));
        tvTotalSuccess.setText(String.format("%.0f", totalSuccess));
    }

    private void loadMoveOrderItems() {
        showLoading(true);

        apiService.getMoveOrderItems(moveOrderId).enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<MoveOrderItem> items = response.body();
                    itemList = response.body();

                    updateUI(items);
                    updateTotalQuantities(); // Add this line
                    if (items.size() > 0) {
                        loadMoveOrderTransactions();


                        // เรียก loadStockTag() ด้วย ProductIds ที่ไม่ซ้ำกัน
                        String uniqueProductIds = getUniqueProductIds();
                        loadStockTag(uniqueProductIds); // ส่ง null ได้
                    }
                    if (items.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading move order items: " + response.code());
                    showError("Failed to load move order items");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading move order items", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void loadStockTag(String productIds) {
        showLoading(true);

        Log.d(TAG, "Loading stock tags with productIds: " + (productIds != null ? productIds : "null (all products)"));

        // เรียก API ด้วย productIds parameter (ส่ง null ได้)
        apiService.getAllTags(productIds).enqueue(new Callback<List<TagDto>>() {
            @Override
            public void onResponse(Call<List<TagDto>> call, Response<List<TagDto>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    stockTags = response.body();

                    if (productIds != null && !productIds.trim().isEmpty()) {
                        Log.d(TAG, "Successfully loaded " + stockTags.size() + " tags for products: " + productIds);
                    } else {
                        Log.d(TAG, "Successfully loaded all " + stockTags.size() + " tags");
                    }

                    // Optional: ทำอะไรกับ stockTags ที่ได้หลังจากโหลดเสร็จ
                    //onStockTagsLoaded();

                } else {
                    Log.e(TAG, "Error loading tags: " + response.code() + " - " + response.message());
                    showError("Failed to load tags");
                }
            }

            @Override
            public void onFailure(Call<List<TagDto>> call, Throwable t) {
                Log.e(TAG, "Network error loading tags", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Method สำหรับดึง ProductId ที่ไม่ซ้ำกันจาก itemList
     * @return String ของ ProductIds ที่คั่นด้วย comma หรือ null ถ้าไม่มีข้อมูล
     */
    private String getUniqueProductIds() {
        if (itemList == null || itemList.isEmpty()) {
            Log.d(TAG, "itemList is null or empty, returning null for productIds");
            return null;
        }

        // ใช้ Set เพื่อเก็บ ProductId ที่ไม่ซ้ำกัน
        Set<Integer> uniqueProductIds = new HashSet<>();

        Log.d(TAG, "Processing " + itemList.size() + " items to extract unique ProductIds");

        for (MoveOrderItem item : itemList) {
            if (item != null && item.getProductId() > 0) { // ตรวจสอบว่า ProductId ถูกต้อง
                uniqueProductIds.add(item.getProductId());
                Log.d(TAG, "Added ProductId: " + item.getProductId());
            } else {
                Log.w(TAG, "Skipped invalid item or ProductId: " +
                        (item != null ? item.getProductId() : "null item"));
            }
        }

        // ตรวจสอบว่ามี ProductId หรือไม่
        if (uniqueProductIds.isEmpty()) {
            Log.w(TAG, "No valid ProductIds found, returning null");
            return null;
        }

        // แปลง Set เป็น List และเรียงลำดับ
        List<Integer> sortedIds = new ArrayList<>(uniqueProductIds);
        Collections.sort(sortedIds); // เรียงลำดับเพื่อความสม่ำเสมอ

        // สร้าง String ที่คั่นด้วย comma
        StringBuilder productIdsBuilder = new StringBuilder();
        for (int i = 0; i < sortedIds.size(); i++) {
            if (i > 0) {
                productIdsBuilder.append(",");
            }
            productIdsBuilder.append(sortedIds.get(i));
        }

        String result = productIdsBuilder.toString();
        Log.d(TAG, "Unique ProductIds generated: " + result + " (Total: " + uniqueProductIds.size() + " unique products)");

        return result;
    }

//    private void loadStockTag() {
//        showLoading(true);
//        apiService.getAllTags().enqueue(new Callback<List<TagDto>>() {
//            @Override
//            public void onResponse(Call<List<TagDto>> call, Response<List<TagDto>> response) {
//                showLoading(false);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    stockTags = response.body();
//                } else {
//                    Log.e(TAG, "Error loading all Tag: " + response.code());
//                    showError("Failed to load all Tag");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<TagDto>> call, Throwable t) {
//                Log.e(TAG, "Network error loading all Tags", t);
//                showLoading(false);
//                showError("Network error: " + t.getMessage());
//            }
//        });
//    }
    private void loadMoveOrderTransactions() {
        showLoading(true);

        apiService.getMoveOrderTransactions(moveOrderId).enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    tempList = response.body();

                    if (!tempList.isEmpty()) {
                        // Iterate through tempList to find matches in itemList
                        for (MoveOrderItem tempItem : tempList) {
                            // Find matching item in itemList
                            for (MoveOrderItem item : itemList) {
                                // Match by productId and lot
                                int xproductId = item.getProductId();
                                int xxproductId = tempItem.getProductId();
                                String xLot = item.getLot();
                                String xxLot = tempItem.getLot();
                                String xxItemType = tempItem.getItemType();
                                if (item.getProductId() == (tempItem.getProductId())
                                        && item.getLot().equals(tempItem.getLot())
                                        && item.getItemType().equals(tempItem.getItemType())) {
                                    // Update quantity success
                                    item.setQuantitySuccess(item.getQuantitySuccess() + 1);
                                    break; // Found match, move to next tempItem
                                }
                            }
                        }

                        // Update adapter with the modified itemList
                        if (adapter != null) {
                            adapter.updateItems(itemList);  // Make sure you have this method in your adapter
                            adapter.notifyDataSetChanged();
                        }

                        // Update total quantities after updating items
                        updateTotalQuantities(); // Add this line

                        // If you're on a background thread, use runOnUiThread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }

                } else {
                    Log.e(TAG, "Error loading move order transactions: " + response.code());
                    showError("Failed to load move order transactions");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading move order transactions", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (show) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(List<MoveOrderItem> items) {
        if (!items.isEmpty()) {
            MoveOrderItem firstItem = items.get(0);
            tvOrderNo.setText("Move Order: " + firstItem.getMoveOrder());
            tvSourceWarehouse.setText("From: " + firstItem.getWarehouseSourceName());
            tvDestinationWarehouse.setText("To: " + firstItem.getWarehouseDestinationName());
        }

        adapter.updateItems(items);
        updateTotalQuantities(); // Add this line

        // Enable/disable buttons based on status
        boolean hasUnfinishedItems = false;
        for (MoveOrderItem item : items) {
            if (item.getMoveTransStatus() != 1) {
                hasUnfinishedItems = true;
                break;
            }
        }
        //btnComplete.setEnabled(!hasUnfinishedItems);
        //btnScanRfid.setEnabled(hasUnfinishedItems);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleItemClick(MoveOrderItem item) {
        Log.d(TAG, "=== handleItemClick Debug ===");
        Log.d(TAG, "Selected item - ProductId: " + item.getProductId() +
                ", Lot: " + item.getLot() +
                ", ItemType: " + item.getItemType() +
                ", ProductNumber: " + item.getProductNumber() +
                ", Product: " + item.getProduct());

        // สร้าง dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_scanned_tags_move_order_child, null);
        builder.setView(dialogView);

        // หา views ใน dialog
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvItemInfo = dialogView.findViewById(R.id.tvItemInfo);
        TextView tvTagCount = dialogView.findViewById(R.id.tvTagCount);
        ListView lvTags = dialogView.findViewById(R.id.lvTags);
        TextView tvNoTags = dialogView.findViewById(R.id.tvNoTags);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // กำหนดชื่อ Dialog
        String productNumber = item.getProductNumber() != null ? item.getProductNumber() : "Unknown Product";
        tvDialogTitle.setText("All Tags for " + productNumber);

        // แสดงข้อมูลสินค้า
        String itemType = item.getItemType() != null ? item.getItemType() : "Unknown";
        String product = item.getProduct() != null ? item.getProduct() : "N/A";
        String barcode = item.getBarcode() != null ? item.getBarcode() : "N/A";
        String lot = item.getLot() != null ? item.getLot() : "N/A";

        String itemInfo = String.format("Product: %s\nBarcode: %s\nLot: %s\nType: %s",
                product, barcode, lot, itemType);
        tvItemInfo.setText(itemInfo);

        // รวมข้อมูลจาก 2 sources ใช้ Wrapper
        List<TagWithStatus> allTagsWithStatus = new ArrayList<>();

        Log.d(TAG, "=== BEFORE FILTERING ===");
        Log.d(TAG, "tempList total size: " + (tempList != null ? tempList.size() : 0));
        Log.d(TAG, "scannedTags total size: " + (scannedTags != null ? scannedTags.size() : 0));

        // 1. เพิ่มข้อมูลจาก tempList (ที่ Success แล้ว)
        int successCount = 0;
        if (tempList != null) {
            Log.d(TAG, "tempList size: " + tempList.size());
            for (int i = 0; i < tempList.size(); i++) {
                MoveOrderItem tempTag = tempList.get(i);
                Log.d(TAG, "tempList[" + i + "] - ProductId: " + tempTag.getProductId() +
                        ", Lot: '" + tempTag.getLot() + "'" +
                        ", ItemType: '" + tempTag.getItemType() + "'" +
                        ", TagNumber: '" + tempTag.getTagNumber() + "'");

                // ผ่อนเงื่อนไข - ตรวจสอบแค่ ProductId อย่างเดียวก่อน
                boolean productIdMatch = (tempTag.getProductId() == item.getProductId());

                // ตรวจสอบ Lot (แบบ flexible)
                boolean lotMatch = true; // เริ่มด้วย true
                if (tempTag.getLot() != null && item.getLot() != null) {
                    lotMatch = tempTag.getLot().trim().equalsIgnoreCase(item.getLot().trim());
                }

                // ตรวจสอบ ItemType (แบบ flexible)
                boolean itemTypeMatch = true; // เริ่มด้วย true
                if (tempTag.getItemType() != null && item.getItemType() != null) {
                    itemTypeMatch = tempTag.getItemType().trim().equalsIgnoreCase(item.getItemType().trim());
                }

                Log.d(TAG, "tempList[" + i + "] Match check - ProductId: " + productIdMatch +
                        ", Lot: " + lotMatch + ", ItemType: " + itemTypeMatch);

                // เงื่อนไขใหม่ - ต้องตรง ProductId และ (Lot หรือ ItemType)
                if (productIdMatch && (lotMatch || itemTypeMatch)) {
                    // ใช้ Wrapper Class
                    TagWithStatus successTag = new TagWithStatus(tempTag, "SUCCESS");
                    allTagsWithStatus.add(successTag);
                    successCount++;

                    Log.d(TAG, "Added SUCCESS tag: '" + tempTag.getTagNumber() + "'");
                } else {
                    Log.d(TAG, "tempList[" + i + "] - NOT MATCHED");
                }
            }
        } else {
            Log.d(TAG, "tempList is NULL");
        }

        // 2. เพิ่มข้อมูลจาก scannedTags (ที่กำลังสแกน)
        int scanningCount = 0;
        if (scannedTags != null) {
            Log.d(TAG, "scannedTags size: " + scannedTags.size());
            for (int i = 0; i < scannedTags.size(); i++) {
                MoveOrderItem scannedTag = scannedTags.get(i);
                Log.d(TAG, "scannedTags[" + i + "] - ProductId: " + scannedTag.getProductId() +
                        ", Lot: '" + scannedTag.getLot() + "'" +
                        ", ItemType: '" + scannedTag.getItemType() + "'" +
                        ", TagNumber: '" + scannedTag.getTagNumber() + "'");

                // ผ่อนเงื่อนไข - ตรวจสอบแค่ ProductId อย่างเดียวก่อน
                boolean productIdMatch = (scannedTag.getProductId() == item.getProductId());

                // ตรวจสอบ Lot (แบบ flexible)
                boolean lotMatch = true; // เริ่มด้วย true
                if (scannedTag.getLot() != null && item.getLot() != null) {
                    lotMatch = scannedTag.getLot().trim().equalsIgnoreCase(item.getLot().trim());
                }

                // ตรวจสอบ ItemType (แบบ flexible)
                boolean itemTypeMatch = true; // เริ่มด้วย true
                if (scannedTag.getItemType() != null && item.getItemType() != null) {
                    itemTypeMatch = scannedTag.getItemType().trim().equalsIgnoreCase(item.getItemType().trim());
                }

                Log.d(TAG, "scannedTags[" + i + "] Match check - ProductId: " + productIdMatch +
                        ", Lot: " + lotMatch + ", ItemType: " + itemTypeMatch);

                // เงื่อนไขใหม่ - ต้องตรง ProductId และ (Lot หรือ ItemType)
                if (productIdMatch && (lotMatch || itemTypeMatch)) {
                    // ใช้ Wrapper Class
                    TagWithStatus scanningTag = new TagWithStatus(scannedTag, "SCANNING");
                    allTagsWithStatus.add(scanningTag);
                    scanningCount++;

                    Log.d(TAG, "Added SCANNING tag: '" + scannedTag.getTagNumber() + "'");
                } else {
                    Log.d(TAG, "scannedTags[" + i + "] - NOT MATCHED");
                }
            }
        } else {
            Log.d(TAG, "scannedTags is NULL");
        }

        Log.d(TAG, "=== FILTERING RESULTS ===");
        Log.d(TAG, "Total tags found: " + allTagsWithStatus.size() + " (Success: " + successCount + ", Scanning: " + scanningCount + ")");

        // เรียงลำดับตาม TagNumber
        allTagsWithStatus.sort((tag1, tag2) -> {
            String tagNumber1 = tag1.getTagNumber() != null ? tag1.getTagNumber() : "";
            String tagNumber2 = tag2.getTagNumber() != null ? tag2.getTagNumber() : "";
            return tagNumber1.compareToIgnoreCase(tagNumber2);
        });

        Log.d(TAG, "Tags sorted by TagNumber");

        // แสดงจำนวนแท็กแบบแยกประเภท
        String tagCountText = String.format("Total Tags: %d (Success: %d, Scanning: %d)",
                allTagsWithStatus.size(), successCount, scanningCount);
        tvTagCount.setText(tagCountText);

        // แสดงรายการแท็กหรือข้อความว่าไม่มีแท็ก
        if (allTagsWithStatus.isEmpty()) {
            lvTags.setVisibility(View.GONE);
            tvNoTags.setVisibility(View.VISIBLE);
            tvNoTags.setText("No tags found for this item with type: " + itemType +
                    "\n\nDebugging info:" +
                    "\n- tempList size: " + (tempList != null ? tempList.size() : 0) +
                    "\n- scannedTags size: " + (scannedTags != null ? scannedTags.size() : 0));
            Log.d(TAG, "No tags found - showing empty message");
        } else {
            lvTags.setVisibility(View.VISIBLE);
            tvNoTags.setVisibility(View.GONE);

            Log.d(TAG, "=== CREATING ADAPTER ===");
            Log.d(TAG, "About to create adapter with " + allTagsWithStatus.size() + " items");

            // Debug: แสดงข้อมูลที่จะส่งให้ adapter
            for (int i = 0; i < allTagsWithStatus.size(); i++) {
                TagWithStatus tagWithStatus = allTagsWithStatus.get(i);
                Log.d(TAG, "allTagsWithStatus[" + i + "] - TagNumber: '" + tagWithStatus.getTagNumber() +
                        "', Status: '" + tagWithStatus.getStatus() + "'");
            }

            // ตั้งค่า adapter กับข้อมูลรวม (ใช้ Wrapper)
            MoveOrderChildTagWithStatusAdapter adapter = new MoveOrderChildTagWithStatusAdapter(requireContext(), allTagsWithStatus);
            lvTags.setAdapter(adapter);

            // ปรับขนาดความสูงของ ListView
            int maxHeight = getResources().getDisplayMetrics().heightPixels * 2 / 3;
            int totalHeight = allTagsWithStatus.size() * (int) (56 * getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams params = lvTags.getLayoutParams();
            params.height = Math.min(totalHeight, maxHeight);
            lvTags.setLayoutParams(params);

            Log.d(TAG, "Showing " + allTagsWithStatus.size() + " tags in ListView");
            Log.d(TAG, "ListView height set to: " + params.height);
        }

        // สร้างและแสดง dialog
        AlertDialog dialog = builder.create();

        // ตั้งค่าขนาด dialog
        dialog.setOnShowListener(dialogInterface -> {
            // ปรับขนาด dialog ให้เหมาะสม
            if (dialog.getWindow() != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
                dialog.getWindow().setLayout(width, height);
                Log.d(TAG, "Dialog size set to: " + width + "x" + height);
            }
        });

        dialog.show();
        Log.d(TAG, "Dialog shown");

        // ตั้งค่าปุ่มปิด
        btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close button clicked");
            dialog.dismiss();
        });

        Log.d(TAG, "=== handleItemClick Complete ===");
    }

//    private void handleItemClick(MoveOrderItem item) {
//        Log.d(TAG, "=== handleItemClick Debug ===");
//        Log.d(TAG, "Selected item - ProductId: " + item.getProductId() +
//                ", Lot: " + item.getLot() +
//                ", ItemType: " + item.getItemType() +
//                ", ProductNumber: " + item.getProductNumber() +
//                ", Product: " + item.getProduct());
//
//        // สร้าง dialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        View dialogView = getLayoutInflater().inflate(R.layout.layout_scanned_tags_move_order_child, null);
//        builder.setView(dialogView);
//
//        // หา views ใน dialog
//        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
//        TextView tvItemInfo = dialogView.findViewById(R.id.tvItemInfo);
//        TextView tvTagCount = dialogView.findViewById(R.id.tvTagCount);
//        ListView lvTags = dialogView.findViewById(R.id.lvTags);
//        TextView tvNoTags = dialogView.findViewById(R.id.tvNoTags);
//        Button btnClose = dialogView.findViewById(R.id.btnClose);
//
//        // กำหนดชื่อ Dialog
//        String productNumber = item.getProductNumber() != null ? item.getProductNumber() : "Unknown Product";
//        tvDialogTitle.setText("All Tags for " + productNumber);
//
//        // แสดงข้อมูลสินค้า
//        String itemType = item.getItemType() != null ? item.getItemType() : "Unknown";
//        String product = item.getProduct() != null ? item.getProduct() : "N/A";
//        String barcode = item.getBarcode() != null ? item.getBarcode() : "N/A";
//        String lot = item.getLot() != null ? item.getLot() : "N/A";
//
//        String itemInfo = String.format("Product: %s\nBarcode: %s\nLot: %s\nType: %s",
//                product, barcode, lot, itemType);
//        tvItemInfo.setText(itemInfo);
//
//        // รวมข้อมูลจาก 2 sources ใช้ Wrapper
//        List<TagWithStatus> allTagsWithStatus = new ArrayList<>();
//
//        Log.d(TAG, "=== BEFORE FILTERING ===");
//        Log.d(TAG, "tempList total size: " + (tempList != null ? tempList.size() : 0));
//        Log.d(TAG, "scannedTags total size: " + (scannedTags != null ? scannedTags.size() : 0));
//
//        // 1. เพิ่มข้อมูลจาก tempList (ที่ Success แล้ว)
//        int successCount = 0;
//        if (tempList != null) {
//            Log.d(TAG, "tempList size: " + tempList.size());
//            for (int i = 0; i < tempList.size(); i++) {
//                MoveOrderItem tempTag = tempList.get(i);
//                Log.d(TAG, "tempList[" + i + "] - ProductId: " + tempTag.getProductId() +
//                        ", Lot: '" + tempTag.getLot() + "'" +
//                        ", ItemType: '" + tempTag.getItemType() + "'" +
//                        ", TagNumber: '" + tempTag.getTagNumber() + "'");
//
//                // ผ่อนเงื่อนไข - ตรวจสอบแค่ ProductId อย่างเดียวก่อน
//                boolean productIdMatch = (tempTag.getProductId() == item.getProductId());
//
//                // ตรวจสอบ Lot (แบบ flexible)
//                boolean lotMatch = true; // เริ่มด้วย true
//                if (tempTag.getLot() != null && item.getLot() != null) {
//                    lotMatch = tempTag.getLot().trim().equalsIgnoreCase(item.getLot().trim());
//                }
//
//                // ตรวจสอบ ItemType (แบบ flexible)
//                boolean itemTypeMatch = true; // เริ่มด้วย true
//                if (tempTag.getItemType() != null && item.getItemType() != null) {
//                    itemTypeMatch = tempTag.getItemType().trim().equalsIgnoreCase(item.getItemType().trim());
//                }
//
//                Log.d(TAG, "tempList[" + i + "] Match check - ProductId: " + productIdMatch +
//                        ", Lot: " + lotMatch + ", ItemType: " + itemTypeMatch);
//
//                // เงื่อนไขใหม่ - ต้องตรง ProductId และ (Lot หรือ ItemType)
//                if (productIdMatch && (lotMatch || itemTypeMatch)) {
//                    // ใช้ Wrapper Class
//                    TagWithStatus successTag = new TagWithStatus(tempTag, "SUCCESS");
//                    allTagsWithStatus.add(successTag);
//                    successCount++;
//
//                    Log.d(TAG, "Added SUCCESS tag: '" + tempTag.getTagNumber() + "'");
//                } else {
//                    Log.d(TAG, "tempList[" + i + "] - NOT MATCHED");
//                }
//            }
//        } else {
//            Log.d(TAG, "tempList is NULL");
//        }
//
//        // 2. เพิ่มข้อมูลจาก scannedTags (ที่กำลังสแกน)
//        int scanningCount = 0;
//        if (scannedTags != null) {
//            Log.d(TAG, "scannedTags size: " + scannedTags.size());
//            for (int i = 0; i < scannedTags.size(); i++) {
//                MoveOrderItem scannedTag = scannedTags.get(i);
//                Log.d(TAG, "scannedTags[" + i + "] - ProductId: " + scannedTag.getProductId() +
//                        ", Lot: '" + scannedTag.getLot() + "'" +
//                        ", ItemType: '" + scannedTag.getItemType() + "'" +
//                        ", TagNumber: '" + scannedTag.getTagNumber() + "'");
//
//                // ผ่อนเงื่อนไข - ตรวจสอบแค่ ProductId อย่างเดียวก่อน
//                boolean productIdMatch = (scannedTag.getProductId() == item.getProductId());
//
//                // ตรวจสอบ Lot (แบบ flexible)
//                boolean lotMatch = true; // เริ่มด้วย true
//                if (scannedTag.getLot() != null && item.getLot() != null) {
//                    lotMatch = scannedTag.getLot().trim().equalsIgnoreCase(item.getLot().trim());
//                }
//
//                // ตรวจสอบ ItemType (แบบ flexible)
//                boolean itemTypeMatch = true; // เริ่มด้วย true
//                if (scannedTag.getItemType() != null && item.getItemType() != null) {
//                    itemTypeMatch = scannedTag.getItemType().trim().equalsIgnoreCase(item.getItemType().trim());
//                }
//
//                Log.d(TAG, "scannedTags[" + i + "] Match check - ProductId: " + productIdMatch +
//                        ", Lot: " + lotMatch + ", ItemType: " + itemTypeMatch);
//
//                // เงื่อนไขใหม่ - ต้องตรง ProductId และ (Lot หรือ ItemType)
//                if (productIdMatch && (lotMatch || itemTypeMatch)) {
//                    // ใช้ Wrapper Class
//                    TagWithStatus scanningTag = new TagWithStatus(scannedTag, "SCANNING");
//                    allTagsWithStatus.add(scanningTag);
//                    scanningCount++;
//
//                    Log.d(TAG, "Added SCANNING tag: '" + scannedTag.getTagNumber() + "'");
//                } else {
//                    Log.d(TAG, "scannedTags[" + i + "] - NOT MATCHED");
//                }
//            }
//        } else {
//            Log.d(TAG, "scannedTags is NULL");
//        }
//
//        Log.d(TAG, "=== FILTERING RESULTS ===");
//        Log.d(TAG, "Total tags found: " + allTagsWithStatus.size() + " (Success: " + successCount + ", Scanning: " + scanningCount + ")");
//
//        // แสดงจำนวนแท็กแบบแยกประเภท
//        String tagCountText = String.format("Total Tags: %d (Success: %d, Scanning: %d)",
//                allTagsWithStatus.size(), successCount, scanningCount);
//        tvTagCount.setText(tagCountText);
//
//        // แสดงรายการแท็กหรือข้อความว่าไม่มีแท็ก
//        if (allTagsWithStatus.isEmpty()) {
//            lvTags.setVisibility(View.GONE);
//            tvNoTags.setVisibility(View.VISIBLE);
//            tvNoTags.setText("No tags found for this item with type: " + itemType +
//                    "\n\nDebugging info:" +
//                    "\n- tempList size: " + (tempList != null ? tempList.size() : 0) +
//                    "\n- scannedTags size: " + (scannedTags != null ? scannedTags.size() : 0));
//            Log.d(TAG, "No tags found - showing empty message");
//        } else {
//            lvTags.setVisibility(View.VISIBLE);
//            tvNoTags.setVisibility(View.GONE);
//
//            Log.d(TAG, "=== CREATING ADAPTER ===");
//            Log.d(TAG, "About to create adapter with " + allTagsWithStatus.size() + " items");
//
//            // Debug: แสดงข้อมูลที่จะส่งให้ adapter
//            for (int i = 0; i < allTagsWithStatus.size(); i++) {
//                TagWithStatus tagWithStatus = allTagsWithStatus.get(i);
//                Log.d(TAG, "allTagsWithStatus[" + i + "] - TagNumber: '" + tagWithStatus.getTagNumber() +
//                        "', Status: '" + tagWithStatus.getStatus() + "'");
//            }
//
//            // ตั้งค่า adapter กับข้อมูลรวม (ใช้ Wrapper)
//            MoveOrderChildTagWithStatusAdapter adapter = new MoveOrderChildTagWithStatusAdapter(requireContext(), allTagsWithStatus);
//            lvTags.setAdapter(adapter);
//
//            // ปรับขนาดความสูงของ ListView
//            int maxHeight = getResources().getDisplayMetrics().heightPixels * 2 / 3;
//            int totalHeight = allTagsWithStatus.size() * (int) (56 * getResources().getDisplayMetrics().density);
//            ViewGroup.LayoutParams params = lvTags.getLayoutParams();
//            params.height = Math.min(totalHeight, maxHeight);
//            lvTags.setLayoutParams(params);
//
//            Log.d(TAG, "Showing " + allTagsWithStatus.size() + " tags in ListView");
//            Log.d(TAG, "ListView height set to: " + params.height);
//        }
//
//        // สร้างและแสดง dialog
//        AlertDialog dialog = builder.create();
//
//        // ตั้งค่าขนาด dialog
//        dialog.setOnShowListener(dialogInterface -> {
//            // ปรับขนาด dialog ให้เหมาะสม
//            if (dialog.getWindow() != null) {
//                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
//                dialog.getWindow().setLayout(width, height);
//                Log.d(TAG, "Dialog size set to: " + width + "x" + height);
//            }
//        });
//
//        dialog.show();
//        Log.d(TAG, "Dialog shown");
//
//        // ตั้งค่าปุ่มปิด
//        btnClose.setOnClickListener(v -> {
//            Log.d(TAG, "Close button clicked");
//            dialog.dismiss();
//        });
//
//        Log.d(TAG, "=== handleItemClick Complete ===");
//    }





    private void startScanningForItem(MoveOrderItem item) {
        // TODO: Implement scanning functionality
        Toast.makeText(requireContext(), "Scanning not implemented yet", Toast.LENGTH_SHORT).show();
    }

    //--------------------------------------获取 解析数据-------------------------------------------------
    private static final int REQUEST_CODE_ITEM_DETAIL = 1001;

    private RFIDWithUHFBLE uhf;
    static boolean isExit_=false;
    private boolean isExit = false;
    private EditText etTime;
    private TextView tv_count, tv_time, tv_total;
    private ConnectStatus mConnectStatus = new ConnectStatus();
    private int maxRunTime = 99999999;

    private final int FLAG_START = 0;//开始
    private final int FLAG_STOP = 1;//停止
    private final int FLAG_UPDATE_TIME = 2; // 更新时间
    private final int FLAG_UHFINFO = 3;
    private final int FLAG_UHFINFO_LIST = 5;
    private final int FLAG_SUCCESS = 10;//成功
    private final int FLAG_FAIL = 11;//失败
    private final int FLAG_TIME_OVER= 12;//
    private long mStrTime;
    private Timer mTimer = new Timer();
    private TimerTask mInventoryPerMinuteTask;
    private List<UHFTAGInfo> tempDatas = new ArrayList<>();
    private ArrayList<HashMap<String, String>> tagList;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_TIME_OVER:
                    Log.i(TAG, "FLAG_TIME_OVER ="+(System.currentTimeMillis() -mStrTime));
                    float useTime2 = (System.currentTimeMillis() - mStrTime) / 1000.0F;
                    //tv_time.setText(NumberTool.getPointDouble(1, useTime2) + "s");
                    stop();
                    break;
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //停止成功
                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        btnScanRFID.setEnabled(true);
                        //btInventory.setEnabled(true);
                        //btInventoryPerMinute.setEnabled(true);
                    } else {
                        //停止失败
                        Utils.playSound(2);
                        Toast.makeText(mContext, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
                    }

                    break;
                case FLAG_UHFINFO_LIST:
                    List<UHFTAGInfo> list = ( List<UHFTAGInfo>) msg.obj;
                    addEPCToList(list);
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //开始读取标签成功
                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        btnScanRFID.setEnabled(false);
                        //btInventory.setEnabled(false);
                        //sbtInventoryPerMinute.setEnabled(false);
                    } else {
                        //开始读取标签失败
                        Utils.playSound(2);
                    }
                    break;
                case FLAG_UPDATE_TIME:
                    if(mContext.isScanning){
                        float useTime = (System.currentTimeMillis() - mStrTime) / 1000.0F;
                        //tv_time.setText(NumberTool.getPointDouble(1, useTime) + "s");
                        handler.sendEmptyMessageDelayed(FLAG_UPDATE_TIME,10);
                    }else {
                        handler.removeMessages(FLAG_UPDATE_TIME);
                    }
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    addEPCToList(info);
                    //Utils.playSound(1);
                    break;
            }
        }
    };

    private void addEPCToList(List<UHFTAGInfo> list) {
        for(int k=0;k<list.size();k++){
            addEPCToList(list.get(k));
        }
    }
    private void addEPCToList(UHFTAGInfo uhftagInfo) {
        boolean[] exists=new boolean[1];
        int idx= CheckUtils.getInsertIndex(tempDatas,uhftagInfo,exists);
        insertTag(uhftagInfo,idx,exists[0]);
    }

    int temp_zz = 0;
    void insertTag(UHFTAGInfo info, int index,boolean exists){

        String data=info.getEPC();
        if(!TextUtils.isEmpty(info.getTid())){
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("EPC:");
            stringBuilder.append(info.getEPC());
            stringBuilder.append("\n");
            stringBuilder.append("TID:");
            stringBuilder.append(info.getTid());
            if(!TextUtils.isEmpty(info.getUser())){
                stringBuilder.append("\n");
                stringBuilder.append("USER:");
                stringBuilder.append(info.getUser());
            }
            data=stringBuilder.toString();
        }
        HashMap<String,String> tagMap=null;
        if(exists){
            tagMap = tagList.get(index);
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(Integer.parseInt(tagMap.get(MainActivity.TAG_COUNT), 10) + 1));
        }else {
            tagMap = new HashMap<>();
            tagMap.put(MainActivity.TAG_EPC, info.getEPC());
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(1));
            tempDatas.add(index,info);
            tagList.add(index, tagMap);
            //tv_count.setText(String.valueOf(tagList.size()));

            String xx_tag = data;
            processQrCode(xx_tag);

        }
        tagMap.put(MainActivity.TAG_USER, info.getUser());
        tagMap.put(MainActivity.TAG_DATA, data);
        tagMap.put(MainActivity.TAG_TID, info.getTid());
        tagMap.put(MainActivity.TAG_RSSI, info.getRssi()==null?"":info.getRssi());
        //tv_total.setText(String.valueOf(++total));
        //Toast.makeText(mContext, "EPC: " + data, Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
            uhf = mContext.uhf;

            // Initialize variables
            if (tagList == null) {
                tagList = new ArrayList<>();
            }

            if (tempDatas == null) {
                tempDatas = new ArrayList<>();
            }


            // Register connect status listener
            mContext.addConnectStatusNotice(mConnectStatus);

            // Try to set TagFocus
//            if (uhf != null && uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
//                try {
//                    uhf.setTagFocus(true);
//                } catch (Exception e) {
//                    Log.e(TAG, "Error setting TagFocus in onAttach: " + e.getMessage());
//                }
//            }
        }
    }


    private void handleStart() {
        //btnScanRfid.setEnabled(false);
        //btnComplete.setEnabled(true);
        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                Log.d("DeviceAPI_setKeyEvent","  keycode ="+keycode +"   ,isExit_="+isExit_);
                if(!isExit_ && mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED)
                    inventory();
            }

            @Override
            public void onKeyUp(int i) {

            }
        });
        // TODO: Implement start logic
    }

    private void handleComplete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Complete Order")
                .setMessage("Are you sure you want to complete this order?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // TODO: Implement completion logic
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void clearSearch() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
    }

    public void refreshData() {
        clearSearch();
        loadMoveOrderItems();

    }


    private void processUHFInfo(UHFTAGInfo info) {
        if (info != null) {
            String epc = info.getEPC();
            Toast.makeText(mContext, "Scanned EPC: " + epc, Toast.LENGTH_SHORT).show();
            processQrCode(epc);
        }
    }

    private MoveOrderItem findItemInTempListByTagId(String tagNumber) {
        if (tempList == null || tempList.isEmpty() || tagNumber == null) {
            return null;
        }

        for (MoveOrderItem item : tempList) {
            if (item.getTagNumber() != null && item.getTagNumber().equals(tagNumber)) {
                return item;
            }
        }

        return null;
    }

    private void scanBarcode()
    {
        String barcode= String.valueOf(tvBarcode.getText());
        if (barcode != null) processQrCode(barcode);
    }

    private void processQrCode(String qrCode) {
        try {
            // Basic input validation
            if (qrCode == null || qrCode.length() < 11) {
                String msg = qrCode + " Invalid format";
                notificationMessages.add(msg);
                //showToastOnUiThread(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            String barcode = qrCode.substring(0, 6);
            String lot = qrCode.substring(6, 14);
            String mc = qrCode.substring(12, 14);
            String box_type = qrCode.substring(14, 15);
            String box_size = qrCode.substring(15, 19);
            String running = qrCode.substring(19, 24);
            String itemType = "Full Box";
            if (box_type.equals("C")) {
                itemType = "Renant";
            } else if (box_type.equals("F")) {
                itemType = "Full Box";
            }

            boolean found = false;

            // **เพิ่มการตรวจสอบ: ตรวจสอบว่า tag นี้เคย confirm ไปแล้วหรือยัง (ใน tempList)**
            String xxTagID = qrCode;

            boolean isStock = isTagInStock(xxTagID);
            if (isStock) {
                String msg = qrCode + " มีในระบบแล้ว!";
                notificationMessages.add(msg);
                //showToastOnUiThread(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            MoveOrderItem foundItem = findItemInTempListByTagId(xxTagID);
            if (foundItem != null) {
                String msg = qrCode + " has already been confirmed in previous transaction!";
                notificationMessages.add(msg);
                //showToastOnUiThread(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            // Get all items from adapter
            List<MoveOrderItem> items = adapter.getFilteredItems();

            if (items == null) {
                String msg = "No items available";
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            // Search for matching item
            for (MoveOrderItem item : items) {
                String tempbarcode = item.getBarcode();
                String templot = item.getLot();
                String tempitemtype = item.getItemType();
                double allocatedQty = 0;
                if (itemType.equals("Full Box")) {
                    allocatedQty = item.getQuantityPerBox();
                } else if (itemType.equals("Renant")) {
                    allocatedQty = item.getAllocatedQty();
                }

                if (item != null &&
                        item.getBarcode() != null &&
                        item.getLot() != null &&
                        item.getBarcode().equals(barcode) &&
                        item.getLot().equals(lot) &&
                        item.getItemType().equals(itemType)) {

                    found = true;

                    // **ตรวจสอบว่าแท็กนี้สแกนในรอบปัจจุบันแล้วหรือยัง (ใน scannedTags)**
                    boolean isAlreadyScannedInCurrentSession = false;
                    for (MoveOrderItem scannedTag : scannedTags) {
                        if (scannedTag.getTagNumber() != null &&
                                scannedTag.getTagNumber().equals(qrCode.trim())) {
                            isAlreadyScannedInCurrentSession = true;
                            break;
                        }
                    }

                    if (!isAlreadyScannedInCurrentSession) {
                        // **เพิ่มการตรวจสอบปริมาณ: qty success + qty scan + 1 ต้องไม่เกิน qty**
                        double currentTotal = item.getQuantitySuccess() + item.getQuantityScan();
                        double newTotal = currentTotal + 1; // +1 สำหรับแท็กที่กำลังจะสแกน
                        double maxQty = item.getQuantity();

                        if (newTotal > maxQty) {
                            String msg = String.format("Item: %s, Lot: %s - Cannot scan more! Would exceed limit (Success: %.0f + Scan: %.0f + 1 = %.0f > %.0f)",
                                    barcode, lot,
                                    item.getQuantitySuccess(),
                                    item.getQuantityScan(),
                                    newTotal,
                                    maxQty);
                            if (!notificationMessages.contains(msg)) {
                                notificationMessages.add(msg);
                                hasNotifications = true;
                                updateNotificationVisibility();
                            }
                            // ไม่ return แล้ว ยอมให้สแกนเกินได้
                        }

                        // Add tag to scanned list
                        MoveOrderItem xitem = new MoveOrderItem();
                        xitem.setId(item.getId());
                        xitem.setProductId(item.getProductId());
                        xitem.setQuantity(1);
                        xitem.setTagNumber(qrCode.trim());
                        xitem.setLotNumber(item.getLotNumber());
                        xitem.setLot(lot);
                        xitem.setItemType(itemType);
                        xitem.setAllocatedQty(allocatedQty);
                        scannedTags.add(xitem);

                        item.setQuantityScan(item.getQuantityScan() + 1);

                        // Show success message with remaining quantity
                        double remaining = maxQty - newTotal;
                        String successMsg = String.format("Scanned successfully! Remaining: %.0f/%.0f", remaining, maxQty);
                        //showToastOnUiThread(successMsg);

                        // Update UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                updateTotalQuantities();
                            });
                        }
                    } else {
                        String msg = qrCode + " has already been scanned in current session!";
                        if (!notificationMessages.contains(msg)) {
                            notificationMessages.add(msg);
                            hasNotifications = true;
                            updateNotificationVisibility();
                        }
                    }
                    break;
                }
            }

            if (!found) {
                String msg = "Item: " + barcode + " , Lot: " + lot + " No matching in Order ";
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
            }

        } catch (Exception e) {
            Log.e("MoveOrderChildFragment", "Error processing QR code: " + e.getMessage());
            String msg = "Error processing QR code: " + e.getMessage();
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    private boolean isTagInStock(String tagNumber) {
        if (stockTags == null || stockTags.isEmpty() || tagNumber == null) {
            Log.d(TAG, "stockTags is null/empty or tagNumber is null");
            return false;
        }

        Log.d(TAG, "Checking if tagNumber '" + tagNumber + "' exists in " + stockTags.size() + " stockTags");

        for (TagDto stockTag : stockTags) {
            if (stockTag != null && stockTag.getTagNumber() != null) {
                // ตรวจสอบแบบ exact match
                if (stockTag.getTagNumber().equals(tagNumber)) {
                    Log.d(TAG, "Found exact match for tagNumber: " + tagNumber);
                    return true;
                }

                // ตรวจสอบแบบ case-insensitive (optional)
                if (stockTag.getTagNumber().equalsIgnoreCase(tagNumber.trim())) {
                    Log.d(TAG, "Found case-insensitive match for tagNumber: " + tagNumber);
                    return true;
                }
            }
        }

        Log.d(TAG, "TagNumber '" + tagNumber + "' not found in stockTags");
        return false;
    }

    // Helper method to show Toast on UI thread
    private void showToastOnUiThread(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void inventory() {
        if (uhf.getConnectStatus() != ConnectionStatus.CONNECTED) {
            Toast.makeText(mContext, "RFID reader is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        mStrTime = System.currentTimeMillis();
        UHFTAGInfo info = uhf.inventorySingleTag();
        if (info != null) {
            Message msg = handler.obtainMessage(FLAG_UHFINFO);
            msg.obj = info;
            handler.sendMessage(msg);
        } else {
            Toast.makeText(mContext, "No tag detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void stop() {

        Log.i(TAG, "stop mContext.isScanning=false");
        handler.removeMessages(FLAG_TIME_OVER);
        if(mContext.isScanning) {

            stopInventory();
        }
        mContext.isScanning=false;
        cancelInventoryTask();
    }

    public void startThread() {
        if (mContext.isScanning) {
            return;
        }

//        try {
//            mContext.uhf.setTagFocus(true);
//        } catch (Exception ex) {
//            Log.e(TAG, "Error setting TagFocus: " + ex.getMessage());
//        }

        //--

        String time = etTime.getText().toString();
        if(time!=null && time.length()>0 && time.startsWith(".")){
            etTime.setText("");
            time="";
        }
        if (time != null && !time.isEmpty()) {
            maxRunTime = (int)(Float.parseFloat(time) * 1000);
            clearData();
        } else {
            maxRunTime=Integer.parseInt(etTime.getHint().toString())* 1000;
        }

        //--
        mContext.uhf.setInventoryCallback(new IUHFInventoryCallback() {
            @Override
            public void callback(UHFTAGInfo uhftagInfo) {
                handler.sendMessage(handler.obtainMessage(FLAG_UHFINFO, uhftagInfo));
            }
        });
        mContext.isScanning = true;
        //        cbFilter.setChecked(true);
        //new TagThread().start();

        Message msg = handler.obtainMessage(FLAG_START);
        Log.i(TAG, "startInventoryTag() 1");
        if (mContext.uhf.startInventoryTag()) {
            mStrTime = System.currentTimeMillis();
            msg.arg1 = FLAG_SUCCESS;
            handler.sendEmptyMessage(FLAG_UPDATE_TIME);
            handler.removeMessages(FLAG_TIME_OVER);
            handler.sendEmptyMessageDelayed(FLAG_TIME_OVER,maxRunTime);
        } else {
            msg.arg1 = FLAG_FAIL;
            mContext.isScanning = false;
        }
        handler.sendMessage(msg);


    }

    private void stopInventory(){
        //Log.i(TAG, "stopInventory() 2");

        ConnectionStatus connectionStatus = mContext.uhf.getConnectStatus();
        if(connectionStatus != ConnectionStatus.CONNECTED){
            return;
        }
        boolean result=false;
        result = mContext.uhf.stopInventory();
        Message msg = handler.obtainMessage(FLAG_STOP);
        if (!result && connectionStatus == ConnectionStatus.CONNECTED) {
            msg.arg1 = FLAG_FAIL;
        } else {
            msg.arg1 = FLAG_SUCCESS;
        }
        handler.sendMessage(msg);
    }

    private void cancelInventoryTask() {
        if(mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }
    private int  selectIndex=-1;
    private void clearData() {
        // Reset selection
        mContext.selectEPC = null;
        selectIndex = -1;

        // ล้าง scannedTags
        if (scannedTags != null) {
            scannedTags.clear();
        }

        // Reset quantityScan ของทุกรายการเป็น 0 (แต่เก็บ quantitySuccess ไว้)
        if (itemList != null) {
            for (MoveOrderItem item : itemList) {
                item.setQuantityScan(0);
            }
        }

        // ล้าง tagList
        if (tagList == null) {
            tagList = new ArrayList<>();
        } else {
            tagList.clear();
        }

        // ล้าง tempDatas
        if (tempDatas != null) {
            tempDatas.clear();
        }

        // อัพเดท UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateTotalQuantities();

        // **ไม่เรียก loadMoveOrderItems() เพื่อไม่ให้ข้อมูル quantitySuccess หายไป**
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check notifications when fragment resumes
        checkNotifications();

        if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            btnScanRFID.setEnabled(true);
            //btInventory.setEnabled(true);
            //btInventoryPerMinute.setEnabled(true);

            //cbFilter.setEnabled(true);
        } else {
            btnScanRFID.setEnabled(false);
            //btInventory.setEnabled(false);
            //btInventoryPerMinute.setEnabled(false);

            //cbFilter.setChecked(false);
            //cbFilter.setEnabled(false);
        }
    }


    private void scanRFID() {
        // TODO: Implement RFID scanning logic
        //  Toast.makeText(mContext, "RFID scanning not implemented yet", Toast.LENGTH_SHORT).show();
        startThread();
    }

    private void stopRFID() {
        // TODO: Implement RFID scanning logic
        //  Toast.makeText(mContext, "RFID scanning not implemented yet", Toast.LENGTH_SHORT).show();
        btnConfirm.setEnabled(true);
        stop();
    }

    private void clearRFID() {
        // TODO: Implement RFID scanning logic
        //  Toast.makeText(mContext, "RFID scanning not implemented yet", Toast.LENGTH_SHORT).show();
        String userId = MainActivity.globalUserId;
        clearData();
        //fetchCountingDetails(journal);
    }

    private void confirmMoveOrderTransaction() {
        // ตรวจสอบว่ามี scannedTags หรือไม่ก่อนเริ่ม loading
        if (scannedTags == null || scannedTags.isEmpty()) {
            Toast.makeText(mContext, "No scanned tags to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        // ตรวจสอบว่ามียอด scan เกิน quantity หรือไม่
        if (itemList != null) {
            for (MoveOrderItem item : itemList) {
                if (item.getQuantitySuccess() + item.getQuantityScan() > item.getQuantity()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("แจ้งเตือน")
                            .setMessage("ไม่สามารถบันทึกได้: รหัสสินค้า " + item.getProductNumber() + " มียอดสแกนเกินกำหนด")
                            .setPositiveButton("ตกลง", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
            }
        }

        // แสดง loading indicator
        showLoading(true);

        // Create Retrofit service or similar HTTP client
        List<MoveTagDto> sendTags = new ArrayList<>();

        // **แก้ไข: สร้าง MoveTagDto object ใหม่ในแต่ละรอบของลูป**
        for (MoveOrderItem scannedTag : scannedTags) {
            MoveTagDto item = new MoveTagDto(); // สร้างใหม่ในทุกรอบ
            item.setMoveOrderItemId(scannedTag.getId());
            item.setTagNumber(scannedTag.getTagNumber());
            sendTags.add(item);
        }

        Log.d(TAG, "Confirming " + sendTags.size() + " scanned tags...");

        String userId = MainActivity.globalUserId;

        Call<List<ResponseDto>> call = apiService.createMoveOrderTransaction(
                moveOrderId,
                userId,
                sendTags
        );

        call.enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
                showLoading(false);

                if (response.isSuccessful()) {

                    List<ResponseDto> results = response.body();

                    // เช็คว่าทุกรายการ success หรือไม่
                    boolean allSuccess = true;
                    StringBuilder errorMessages = new StringBuilder();

                    for (ResponseDto result : results) {
                        if (!result.getIsSuccess()) {
                            allSuccess = false;
                            errorMessages.append("• ").append(result.getMessage()).append("\n");
                        }
                    }

                    if (allSuccess) {


                        // **แก้ไข: ล้างข้อมูลสแกนและ reset quantityScan**
                        clearScanDataAfterConfirm();

                        checkNotifications();
                        // โหลดข้อมูลใหม่
                        loadMoveOrderItems();

                        Toast.makeText(mContext, "จัดเก็บสินค้าเสร็จเรียบร้อย", Toast.LENGTH_LONG).show();

                    } else {
                        // มีรายการที่ไม่สำเร็จ
                        String errorMsg = "Some transactions failed:\n\n" + errorMessages.toString();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Move Errors")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();

                        Toast.makeText(mContext, "Confirm Move failed ", Toast.LENGTH_SHORT).show();
                    }





                } else {
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                showLoading(false);

                String networkError = "Network error: " + t.getMessage();
                Toast.makeText(mContext, networkError, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network failure: ", t);
                //Toast.makeText(mContext, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // เพิ่มเมธอดใหม่สำหรับล้างข้อมูลสแกนหลัง confirm
    private void clearScanDataAfterConfirm() {
        // ล้าง scannedTags
        if (scannedTags != null) {
            scannedTags.clear();
        }

        // Reset quantityScan ของทุกรายการเป็น 0
        if (itemList != null) {
            for (MoveOrderItem item : itemList) {
                item.setQuantityScan(0);
            }
        }

        // ล้าง tagList สำหรับ RFID
        if (tagList != null) {
            tagList.clear();
        }

        // ล้าง tempDatas
        if (tempDatas != null) {
            tempDatas.clear();
        }

        // Reset selection
        mContext.selectEPC = null;

        // อัพเดท UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateTotalQuantities();
    }



    class ConnectStatus implements MainActivity.IConnectStatus {
        @Override
        public void getStatus(ConnectionStatus connectionStatus) {
            Log.i(TAG, "getStatus connectionStatus="+connectionStatus);
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                if (!mContext.isScanning) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    btnScanRFID.setEnabled(true);
                    //btInventory.setEnabled(true);
                    //btInventoryPerMinute.setEnabled(true);
                }

                //cbFilter.setEnabled(true);
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                stop();
                btClear.setEnabled(true);
                btStop.setEnabled(false);
                btnScanRFID.setEnabled(false);

            }
        }
    }

}
