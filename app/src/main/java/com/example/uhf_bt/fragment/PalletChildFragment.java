package com.example.uhf_bt.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.PickingOrderChildAdapter;
import com.example.uhf_bt.adapter.TagStatusAdapter;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.PickingTagDto;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.TagDto;
import com.example.uhf_bt.models.TagStatusItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.example.uhf_bt.tool.CheckUtils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class PalletChildFragment extends Fragment {

    private static final String TAG = "PalletChildFragment";

    LinearLayout viewAction ;
    private TextView tvTotalQty, tvTotalScan, tvTotalSuccess;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;


    // Add these variables to your PickingOrderChildFragment class
    private ArrayAdapter<String> palletAdapter;
    private boolean isPalletLocked = false;

    private TextView tvOrderNo;
    private TextView tvCustomer;
    private TextView tvOrderDate;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PickingOrderChildAdapter adapter;
    private List<PickingOrderItem> itemList = new ArrayList<>();
    private List<PickingOrderItem> tempList = new ArrayList<>();

    private List<ProductItem> stockList = new ArrayList<>();
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private String pickingOrderId;
    private String palletTagId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Button btnScanRFID, btStop, btClear, btnConfirm ;

    public PalletChildFragment() {
        // Required empty public constructor
    }

    public static PalletChildFragment newInstance(String palletTagId, String pickingOrder, String pickingOrderId) {
        PalletChildFragment fragment = new PalletChildFragment();
        Bundle args = new Bundle();
        args.putString("PalletTagId", palletTagId);
        args.putString("PickingOrder", pickingOrder);
        args.putString("pickingOrderId", pickingOrderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            palletTagId = getArguments().getString("PalletTagId");
            pickingOrderId = getArguments().getString("pickingOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pallet_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();
        loadPalletDetail();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        viewAction = view.findViewById(R.id.viewAction);
        // Initialize notification views
        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        tvOrderNo = view.findViewById(R.id.tvOrderNo);
        tvCustomer = view.findViewById(R.id.tvCustomer);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_count);
        tv_total = view.findViewById(R.id.tv_total);

        // Initialize total quantity TextViews
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);

        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btClear = view.findViewById(R.id.btnClear);
        btStop = view.findViewById(R.id.btnStop);
        btnConfirm = view.findViewById(R.id.btnConfirm);

    }

    // 3. Add this method to calculate and update total quantities
    private void updateTotalQuantities() {
        double totalQty = 0;
        double totalScan = 0;
        double totalSuccess = 0;

        for (PickingOrderItem item : itemList) {
            totalQty += item.getQuantity();
            totalScan += item.getQuantityScan();
            totalSuccess += item.getQuantitySuccess();
        }

        // Update UI with formatted values
        if (tvTotalQty != null && tvTotalScan != null && tvTotalSuccess != null) {
            tvTotalQty.setText(String.format("%.0f", totalQty));
            tvTotalScan.setText(String.format("%.0f", totalSuccess));
            tvTotalSuccess.setText(String.format("%.0f", totalSuccess));
        }
    }
    private void loadPickingOrderTransactions() {
        showLoading(true);

        apiService.getPickingOrderTransactions(pickingOrderId).enqueue(new Callback<List<PickingOrderTransaction>>() {
            @Override
            public void onResponse(Call<List<PickingOrderTransaction>> call, Response<List<PickingOrderTransaction>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderTransaction> transactions = response.body();

                    if (!transactions.isEmpty()) {
                        // Reset all QuantitySuccess first to 0
                        for (PickingOrderItem item : itemList) {
                            item.setQuantitySuccess(0);
                        }

                        // Create a map to count transactions per picking order item with TagStatus >= 5
                        Map<Integer, Integer> transactionCounts = new HashMap<>();

                        // Count transactions per picking order item with TagStatus >= 5 (ShippingReceive or higher)
                        for (PickingOrderTransaction transaction : transactions) {
                            if (transaction.getTagStatus() >= 5) {
                                int pickingOrderItemId = transaction.getPickingOrderItemId();
                                Integer currentCount = transactionCounts.get(pickingOrderItemId);
                                if (currentCount == null) {
                                    transactionCounts.put(pickingOrderItemId, 1);
                                } else {
                                    transactionCounts.put(pickingOrderItemId, currentCount + 1);
                                }
                            }
                        }

                        // Update quantities in itemList
                        for (PickingOrderItem item : itemList) {
                            int itemId = item.getId();
                            if (transactionCounts.containsKey(itemId)) {
                                item.setQuantitySuccess(transactionCounts.get(itemId));
                            }
                        }

                        // Update UI on the main thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (adapter != null) {
                                    adapter.updateItems(itemList);
                                    adapter.notifyDataSetChanged();
                                }
                                updateTotalQuantities();
                            });
                        }

                        // Save transactions for reference
                        tempList = new ArrayList<>();
                        for (PickingOrderTransaction trans : transactions) {
                            PickingOrderItem item = new PickingOrderItem();
                            item.setId(trans.getPickingOrderItemId());
                            item.setPickingOrderItemId(trans.getPickingOrderItemId());
                            item.setProductId(trans.getProductId());
                            item.setQuantity(trans.getQuantity());
                            item.setTagNumber(trans.getTagNumber());
                            item.setLotNumber(trans.getLotNumber());
                            item.setLot(trans.getLot());
                            item.setTagStatus(trans.getTagStatus());
                            tempList.add(item);
                        }
                    }
                } else {
                    Log.e(TAG, "Error loading picking order transactions: " + response.code());
                    showError("Failed to load picking order transactions");
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderTransaction>> call, Throwable t) {
                Log.e(TAG, "Network error loading picking order transactions", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void setupToolbar() {

        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Setup notification button click listener
        ivNotification.setOnClickListener(v -> {
            showNotificationsDialog();
        });
    }

    // 5. Add these methods for notification handling
    private void checkNotifications() {
        // This method will be called to check for notifications
        // Don't clear existing notifications if they're still relevant

        hasNotifications = !notificationMessages.isEmpty();
        updateNotificationVisibility();
    }

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

    private void addNotification(String message) {
        notificationMessages.add(message);
        hasNotifications = true;
        updateNotificationVisibility();
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


    private void setupButtonListeners() {
        btnScanRFID.setOnClickListener(v -> scanRFID());
        btClear.setOnClickListener(v -> clearRFID());
        btStop.setOnClickListener(v -> stopRFID());
        btnConfirm.setOnClickListener(v -> confirmPickingOrderTransaction());
        //btnQRCode.setOnClickListener(v -> scanQRCode());

        tagList = new ArrayList<>();
        //mContext.tagList=tagList;
        //adapter=new UHFReadTagFragment.MyAdapter(mContext);
        //LvTags.setAdapter(adapter);

        if (mContext != null && mContext.uhf != null) {
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

    private void setupListView() {
        adapter = new PickingOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrderItem item = adapter.getItem(position);
            handleItemClick(item);
        });
    }

    private void loadPalletDetail() {
        showLoading(true);

        if (pickingOrderId != null && !pickingOrderId.isEmpty()) {
            apiService.getPickingOrderItemsByPickingOrderId(pickingOrderId).enqueue(new Callback<List<PickingOrderItem>>() {
                @Override
                public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<PickingOrderItem> items = response.body();
                        if (!items.isEmpty()) {
                            itemList = items;
                            loadPickingOrderTransactions();
                            updateUI(itemList);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "Error loading picking order items: " + response.code());
                        showError("Failed to load picking order items");
                    }
                }

                @Override
                public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                    Log.e(TAG, "Network error loading picking order items", t);
                    showLoading(false);
                    showError("Network error: " + t.getMessage());
                }
            });
        } else {
            apiService.getPalletDetailByTagId(palletTagId).enqueue(new Callback<List<PickingOrderItem>>() {
                @Override
                public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<PickingOrderItem> items = response.body();
                        if (!items.isEmpty()) {
                            itemList = items;
                            loadPickingOrderTransactions();
                            updateUI(itemList);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "Error loading pallet detail: " + response.code());
                        showError("Failed to load pallet detail");
                    }
                }

                @Override
                public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                    Log.e(TAG, "Network error loading pallet detail", t);
                    showLoading(false);
                    showError("Network error: " + t.getMessage());
                }
            });
        }
    }

//    private void loadPickingOrderItems() {
//        showLoading(true);
//
//        apiService.getPickingOrderItems(pickingOrderId).enqueue(new Callback<List<PickingOrderItem>>() {
//            @Override
//            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
//                showLoading(false);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    //List<PickingOrderItem> items = response.body();
//                    itemList = response.body();
//                    updateUI(itemList);
////                    if (items.isEmpty()) {
////                        showEmptyState();
////                    }
//                } else {
//                    Log.e(TAG, "Error loading picking order items: " + response.code());
//                    showError("Failed to load picking order items");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
//                Log.e(TAG, "Network error loading picking order items", t);
//                showLoading(false);
//                showError("Network error: " + t.getMessage());
//            }
//        });
//    }

    private void loadProductItemStock() {
        showLoading(true);
        apiService.getProductItemsByStatus(1).enqueue(new Callback<List<ProductItem>>() {
            @Override
            public void onResponse(Call<List<ProductItem>> call, Response<List<ProductItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    stockList = response.body();

                    if (!stockList.isEmpty()) {
                    }
                } else {
                    Log.e(TAG, "Error loading product item stock: " + response.code());
                    showError("Failed to load product item stock");
                }
            }

            @Override
            public void onFailure(Call<List<ProductItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading product item stock", t);
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

    private void updateUI(List<PickingOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        PickingOrderItem firstItem = items.get(0);
        if (firstItem != null) {
            tvOrderNo.setText("Order No: " + (firstItem.getPickingNumber() != null ? firstItem.getPickingNumber() : ""));
            tvCustomer.setText("Customer: " + (firstItem.getCustomer() != null ? firstItem.getCustomer() : ""));
            tvOrderDate.setText("Order Date: " + (firstItem.getOrderDate() != null ? firstItem.getOrderDate() : ""));
        }

        if(firstItem.getPalletStatus() == 3)
        {
            viewAction.setVisibility(View.VISIBLE);
        }
        else
        {
            viewAction.setVisibility(View.INVISIBLE);
        }

        if (adapter != null) {
            adapter.updateItems(items);
        }
        // Update total quantities
        updateTotalQuantities();
        // ตรวจสอบว่า palletAdapter ไม่เป็น null ก่อนใช้งาน
        if (palletAdapter != null) {
            // Get unique pallets
            Set<String> uniquePallets = new HashSet<>();
            for (PickingOrderItem item : items) {
                if (item.getPalletName() != null && !item.getPalletName().isEmpty()) {
                    uniquePallets.add(item.getPalletName());
                }
            }

            // Update spinner
            palletAdapter.clear();
            palletAdapter.addAll(new ArrayList<>(uniquePallets));
            palletAdapter.notifyDataSetChanged();
        }
    }

    private void handleItemClick(PickingOrderItem item) {
        if (item == null || getContext() == null) return;
        showTagStatusDialog(item);
    }

    private void showTagStatusDialog(PickingOrderItem item) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tag_status, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogSubtitle = dialogView.findViewById(R.id.tvDialogSubtitle);
        TextView tvDialogSummary = dialogView.findViewById(R.id.tvDialogSummary);
        ListView lvDialogTags = dialogView.findViewById(R.id.lvDialogTags);
        Button btnDialogClose = dialogView.findViewById(R.id.btnDialogClose);

        tvDialogTitle.setText(item.getProductName() != null ? item.getProductName() : "รายละเอียดสินค้า");
        tvDialogSubtitle.setText("รหัสสินค้า: " + (item.getProductNumber() != null ? item.getProductNumber() : "-"));

        Set<String> confirmedTags = new HashSet<>();
        Map<String, Integer> confirmedTagStatusMap = new HashMap<>();

        // 1. รวบรวมแท็กที่สแกนรับเข้าแล้วจาก tempList / transactions ของไอเทมชิ้นนี้ใน Pallet
        if (tempList != null) {
            for (PickingOrderItem transItem : tempList) {
                if (transItem.getPickingOrderItemId() == item.getId() ||
                        (transItem.getProductId() == item.getProductId() && transItem.getPickingOrderItemId() == 0)) {
                    if (transItem.getTagNumber() != null && !transItem.getTagNumber().isEmpty()) {
                        String tNum = transItem.getTagNumber().trim();
                        confirmedTags.add(tNum);
                        confirmedTagStatusMap.put(tNum, transItem.getTagStatus());
                    }
                }
            }
        }

        int targetQuantity = (int) item.getQuantity();
        if (targetQuantity <= 0) {
            targetQuantity = Math.max(1, (int) item.getQuantitySuccess());
        }

        List<String> orderTags = new ArrayList<>();

        // 2. ดึงแท็กทั้งหมดของไอเทมนี้ที่ส่งมาจาก API (getPalletDetailByTagId)
        if (itemList != null) {
            for (PickingOrderItem listItem : itemList) {
                if (listItem.getId() == item.getId() || listItem.getProductId() == item.getProductId()) {
                    if (listItem.getTagNumber() != null && !listItem.getTagNumber().isEmpty()) {
                        String[] tagsArr = listItem.getTagNumber().split(",");
                        for (String tag : tagsArr) {
                            String trimmed = tag.trim();
                            if (!trimmed.isEmpty() && !orderTags.contains(trimmed)) {
                                orderTags.add(trimmed);
                            }
                        }
                    }
                }
            }
        }

        List<String> finalTagNumbers = new ArrayList<>();

        // ลำดับที่ 1: ใส่แท็กที่สแกนสำเร็จแล้วก่อน
        for (String tag : confirmedTags) {
            if (!finalTagNumbers.contains(tag) && finalTagNumbers.size() < targetQuantity) {
                finalTagNumbers.add(tag);
            }
        }

        // ลำดับที่ 2: ใส่แท็กที่ระบุมาจาก API ออเดอร์
        for (String tag : orderTags) {
            if (!finalTagNumbers.contains(tag) && finalTagNumbers.size() < targetQuantity) {
                finalTagNumbers.add(tag);
            }
        }

        List<TagStatusItem> tagStatusList = new ArrayList<>();
        int receivedCount = 0;

        for (String tagNum : finalTagNumbers) {
            boolean isReceived = false;
            if (confirmedTagStatusMap.containsKey(tagNum)) {
                int status = confirmedTagStatusMap.get(tagNum);
                // เช็คว่า TagStatus >= 5 (ShippingReceive = 5) หรือไม่
                if (status >= 5) {
                    isReceived = true;
                } else if (status == 0) { // กรณี API ยังไม่ได้ระบุค่า status ให้ fallback เช็คว่าอยู่ใน confirmedTags
                    isReceived = true;
                }
            }

            if (isReceived) {
                receivedCount++;
            }
            tagStatusList.add(new TagStatusItem(tagNum, isReceived));
        }

        // ลำดับที่ 3: หาก API ไม่ได้ส่ง TagNumber ตั้งต้นมา ให้ใส่ Placeholder แท็กเท่าจำนวนที่ยังขาดอยู่
        if (tagStatusList.size() < targetQuantity) {
            int missingCount = targetQuantity - tagStatusList.size();
            int currentSuccess = (int) item.getQuantitySuccess();
            for (int i = 1; i <= missingCount; i++) {
                int indexNumber = tagStatusList.size() + 1;
                boolean isReceived = indexNumber <= currentSuccess;
                if (isReceived) {
                    receivedCount++;
                }
                tagStatusList.add(new TagStatusItem("Tag #" + indexNumber, isReceived));
            }
        }

        tvDialogSummary.setText(String.format("รับแล้ว %d / %d แท็ก", receivedCount, tagStatusList.size()));

        TagStatusAdapter tagStatusAdapter = new TagStatusAdapter(requireContext(), tagStatusList);
        lvDialogTags.setAdapter(tagStatusAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnDialogClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.98);
            int height = (int) (displayMetrics.heightPixels * 0.98);
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void startScanningForItem(PickingOrderItem item) {
        // TODO: Implement scanning functionality
        Toast.makeText(requireContext(), "Scanning not implemented yet", Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------获取 解析数据-------------------------------------------------
    private static final int REQUEST_CODE_ITEM_DETAIL = 1001;

    private RFIDWithUHFBLE uhf;
    static boolean isExit_=false;
    private boolean isExit = false;
    //private EditText etTime;
    //private TextView tv_count, tv_time, tv_total;
    private PalletChildFragment.ConnectStatus mConnectStatus = new PalletChildFragment.ConnectStatus();
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

    private EditText etTime;
    private TextView tv_count, tv_time, tv_total;

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

    // Improved onAttach method with proper initialization and TagFocus handling
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
            uhf = mContext.uhf;

            // Initialize lists
            if (tagList == null) {
                tagList = new ArrayList<>();
            }

            if (tempDatas == null) {
                tempDatas = new ArrayList<>();
            }

            // Register connect status listener
            mContext.addConnectStatusNotice(mConnectStatus);

            // Try to set TagFocus immediately if connected
//            if (uhf != null && uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
//                try {
//                    uhf.setTagFocus(true);
//                } catch (Exception e) {
//                    Log.e(TAG, "Error setting TagFocus in onAttach: " + e.getMessage());
//                }
//            }
        }
    }

    @Override
    public void onDetach() {
        // Remove connect status listener to prevent memory leaks
        if (mContext != null) {
            mContext.removeConnectStatusNotice(mConnectStatus);
        }
        super.onDetach();
    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof MainActivity) {
//            mContext = (MainActivity) context;
//            uhf = mContext.uhf;
//        }
//    }


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
        //loadMoveOrderItems();

    }


    private void processUHFInfo(UHFTAGInfo info) {
        if (info != null) {
            String epc = info.getEPC();
            Toast.makeText(mContext, "Scanned EPC: " + epc, Toast.LENGTH_SHORT).show();
            processQrCode(epc);
        }
    }

    private PickingOrderItem findItemInTempListByTagId(String tagNumber) {
        if (tempList == null || tempList.isEmpty() || tagNumber == null) {
            return null;
        }

        for (PickingOrderItem item : tempList) {
            if (item.getTagNumber() != null && item.getTagNumber().equals(tagNumber)) {
                return item;
            }
        }

        return null;
    }


    private ProductItem findItemStockByTagId(String tagNumber) {
        if (stockList == null || stockList.isEmpty() || tagNumber == null) {
            return null;
        }

        for (ProductItem item : stockList) {
            if (item.getTagNumber() != null && item.getTagNumber().equals(tagNumber)) {
                return item;
            }
        }

        return null;
    }

    // Update processQrCode method to handle quantity tracking
    private void processQrCode(String qrCode) {
        try {
            // Basic input validation
            if (qrCode == null || qrCode.length() < 11) {
                //Toast.makeText(mContext, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                String msg = qrCode +  " Invalid format";
                showToastOnUiThread(msg);
                notificationMessages.add(msg);
                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน

                return;
            }

            String chkTagId = qrCode.substring(0,4);
            if (chkTagId.contains("FFFF")) {
                //Toast.makeText(mContext, qrCode + " ไม่ใช่แท็กสินค้า!" , Toast.LENGTH_SHORT).show();
                //return;
            }
            else if (chkTagId.contains("CCCC"))
            {
                String xxTagID = qrCode;
                //ProductItem foundItem = findItemStockByTagId(xxTagID);

//                if (foundItem == null) {
//                    Log.d(TAG, "No item stock found with tagId: " + xxTagID);
//                    return;
//                }

                // Get all items from adapter
                List<PickingOrderItem> items = adapter.getFilteredItems();

                if (items == null) {
                    //Toast.makeText(mContext, "No items available", Toast.LENGTH_SHORT).show();
                    String msg = "No items available";
                    showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน

                    return;
                }

                boolean found = false;

                int productId = 3;

                while (productId ==3 || productId == 4)
                {
                    for (PickingOrderItem item : items) {
                        if (item != null && item.getProductId() == 3 || item.getProductId() == 4) {
                            found = true;

                            int pickingItemId = item.getId();

                            if (pickingItemId < 3) continue;
                            // Check if we've already reached the required quantity
                            if (item.getQuantitySuccess() + item.getQuantityScan() >= item.getQuantity()) {
                                //showToastOnUiThread("Required quantity already reached for this item!");
                                String msg = item.getProductNumber() + " สินค้านี้สแกนครบแล้ว" ;
                                showToastOnUiThread(msg);
                                notificationMessages.add(msg);
                                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน

                                return;
                            }

                            // Check if this tag was already scanned
                            boolean tagAlreadyScanned = false;
                            for (PickingOrderItem scannedItem : scannedTags) {
                                if (scannedItem.getTagNumber().equals(qrCode)) {
                                    tagAlreadyScanned = true;
                                    break;
                                }
                            }

                            if (!tagAlreadyScanned) {
                                // Update quantity scanned
                                item.setQuantityScan(item.getQuantityScan() + 0.5);

                                int temp_pickorderitemid = item.getPickingOrderItemId();
                                // Add tag to scanned list
                                PickingOrderItem newScannedItem = new PickingOrderItem();
                                newScannedItem.setPickingOrderItemId(item.getPickingOrderItemId());
                                newScannedItem.setProductId(item.getProductId());
                                newScannedItem.setQuantity(0.5);
                                newScannedItem.setTagNumber(qrCode.trim());
                                newScannedItem.setLotNumber("20241031PD4-L08");
                                newScannedItem.setLot("241031");
                                //newScannedItem.setId(pickingItemId);
                                newScannedItem.setPickingOrderItemId(4);
                                scannedTags.add(newScannedItem);

                                // Show success message
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        String message = String.format("Found Item: %s, Lot: %s\nScanned: %.0f + Success: %.0f / Required: %.0f",
                                                item.getProductName(),
                                                item.getLot(),
                                                item.getQuantityScan(),
                                                item.getQuantitySuccess(),
                                                item.getQuantity());
                                        //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                        adapter.notifyDataSetChanged();
                                    });
                                }
                            } else {
                                //showToastOnUiThread("This tag has already been scanned!");
                                String msg = qrCode  + " สแกนไปแล้ว" ;
                                showToastOnUiThread(msg);
                                notificationMessages.add(msg);
                                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน

                            }
                            //break;
                        }
                    }
                }
                // Search for matching item


                productId++;
                if(productId >= 5) return;

                if (!found) {
                    //showToastOnUiThread(String.format("No matching item found for scanned tag: %s", qrCode));
                    String msg = qrCode  + " ไม่พบข้อมูลสนค้า" ;
                    showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                }

            }
            else
            {

                String xxTagID = qrCode;
                ProductItem foundItem = findItemStockByTagId(xxTagID);

                if (foundItem == null) {
                    //Log.d(TAG, "No item stock found with tagId: " + xxTagID);
                    String msg = qrCode  + " ไม่มีสินค้าในสต็อก" ;
                    showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                // Get all items from adapter
                List<PickingOrderItem> items = adapter.getFilteredItems();

                if (items == null) {
                    //Toast.makeText(mContext, "No items available", Toast.LENGTH_SHORT).show();
                    String msg =  "No items available" ;
                    showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                boolean found = false;

                // Search for matching item
                for (PickingOrderItem item : items) {
                    if (item != null && item.getProductId() == foundItem.getProductId()) {
                        found = true;

                        int pickingItemId = item.getId();
                        // Check if we've already reached the required quantity
                        if (item.getQuantitySuccess() + item.getQuantityScan() >= item.getQuantity()) {
                            //showToastOnUiThread("Required quantity already reached for this item!");
                            String msg = item.getProductNumber() + " สินค้านี้สแกนครบแล้ว" ;
                            showToastOnUiThread(msg);
                            notificationMessages.add(msg);
                            hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                            updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                            return;
                        }

                        // Check if this tag was already scanned
                        boolean tagAlreadyScanned = false;
                        for (PickingOrderItem scannedItem : scannedTags) {
                            if (scannedItem.getTagNumber().equals(qrCode)) {
                                tagAlreadyScanned = true;
                                break;
                            }
                        }

                        if (!tagAlreadyScanned) {
                            // Update quantity scanned
                            item.setQuantityScan(item.getQuantityScan() + 1);
                            int temp_pickorderitemid = item.getPickingOrderItemId();
                            // Add tag to scanned list
                            PickingOrderItem newScannedItem = new PickingOrderItem();
                            newScannedItem.setProductId(item.getProductId());
                            newScannedItem.setQuantity(1);
                            newScannedItem.setTagNumber(qrCode.trim());
                            newScannedItem.setLotNumber(foundItem.getLotNumber());
                            newScannedItem.setLot(foundItem.getLot());
                            newScannedItem.setId(item.getPickingOrderItemId());
                            newScannedItem.setPickingOrderItemId(pickingItemId);
                            scannedTags.add(newScannedItem);

                            // Show success message
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    String message = String.format("Found Item: %s, Lot: %s\nScanned: %.0f + Success: %.0f / Required: %.0f",
                                            item.getProductName(),
                                            item.getLot(),
                                            item.getQuantityScan(),
                                            item.getQuantitySuccess(),
                                            item.getQuantity());
                                    //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                    adapter.notifyDataSetChanged();
                                    updateTotalQuantities();
                                });
                            }
                        } else {
                            //showToastOnUiThread("This tag has already been scanned!");
                            String msg = qrCode +  " สแกนไปแล้ว" ;
                            showToastOnUiThread(msg);
                            notificationMessages.add(msg);
                            hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                            updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                        }
                        break;
                    }
                }

                if (!found) {
                    //showToastOnUiThread(String.format("No matching item found for scanned tag: %s", qrCode));
                    String msg = qrCode +  " ไม่ตรงกับรายการที่ต้องจัด" ;
                    showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                }

            }



        } catch (Exception e) {
            Log.e(TAG, "Error processing QR code: " + e.getMessage());

            String msg = "Error processing QR code: " + e.getMessage();
            showToastOnUiThread(msg);
            notificationMessages.add(msg);
            hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
            updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
        }
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

    // Improved startThread method with proper error handling and TagFocus setting
    public void startThread() {
        if (mContext.isScanning) {
            return;
        }

        // Ensure lists are initialized
        if (tempDatas == null) {
            tempDatas = new ArrayList<>();
        }

        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        // Set TagFocus to true before starting inventory with try-catch
//        try {
//            mContext.uhf.setTagFocus(true);
//        } catch (Exception ex) {
//            Log.e(TAG, "Error setting TagFocus: " + ex.getMessage());
//        }

        // Time handling
        String time = etTime.getText().toString();
        if (time != null && time.length() > 0 && time.startsWith(".")) {
            etTime.setText("");
            time = "";
        }
        if (time != null && !time.isEmpty()) {
            maxRunTime = (int)(Float.parseFloat(time) * 1000);
            // Don't call clearData() here as it might be causing issues
        } else {
            maxRunTime = Integer.parseInt(etTime.getHint().toString()) * 1000;
        }

        // Set inventory callback
        mContext.uhf.setInventoryCallback(new IUHFInventoryCallback() {
            @Override
            public void callback(UHFTAGInfo uhftagInfo) {
                handler.sendMessage(handler.obtainMessage(FLAG_UHFINFO, uhftagInfo));
            }
        });
        mContext.isScanning = true;

        Message msg = handler.obtainMessage(FLAG_START);
        Log.i(TAG, "startInventoryTag() 1");
        if (mContext.uhf.startInventoryTag()) {
            mStrTime = System.currentTimeMillis();
            msg.arg1 = FLAG_SUCCESS;
            handler.sendEmptyMessage(FLAG_UPDATE_TIME);
            handler.removeMessages(FLAG_TIME_OVER);
            handler.sendEmptyMessageDelayed(FLAG_TIME_OVER, maxRunTime);
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

    // Fixed clearData method to properly handle lists and prevent crashes
    private void clearData() {
        // Reset scan tracking variables
        mContext.selectEPC = null;
        selectIndex = -1;

        // Clear scanned tags list safely
        if (scannedTags != null) {
            scannedTags.clear();
        }

        // Reset scan quantities for all items
        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                for (PickingOrderItem item : items) {
                    item.setQuantityScan(0);
                }
            }
            adapter.notifyDataSetChanged();

        }

        // Clear the tempDatas list
        if (tempDatas != null) {
            tempDatas.clear();
        }

        // Initialize tagList if null, otherwise clear it
        if (tagList == null) {
            tagList = new ArrayList<>();
        } else {
            tagList.clear();
        }

        updateTotalQuantities();
        // Reload pallet detail
        loadPalletDetail();

    }

//    private void clearData() {
//        mContext.selectEPC = null;
//        selectIndex = -1;
//
//        if (scannedTags != null) {
//            scannedTags.clear();
//        }
//
//        // Reset scan quantities for all items
//        if (adapter != null) {
//            List<PickingOrderItem> items = adapter.getFilteredItems();
//            if (items != null) {
//                for (PickingOrderItem item : items) {
//                    item.setQuantityScan(0);
//                }
//            }
//            adapter.notifyDataSetChanged();
//        }
//
//        //if (tagList == null) {
//        tagList = new ArrayList<>();
//        //}
//
//        //loadPickingOrderItems();
//        loadPalletDetail();
//    }


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
        clearData();
        //fetchCountingDetails(journal);
    }

    private void confirmPickingOrderTransaction() {

        List<PickingTagDto> sendTags = new ArrayList<>();

        if (scannedTags != null && !scannedTags.isEmpty()) {
            // **แก้ไข: สร้าง MoveTagDto object ใหม่ในแต่ละรอบของลูป**
            for (PickingOrderItem scannedTag : scannedTags) {
                PickingTagDto item = new PickingTagDto(); // สร้างใหม่ในทุกรอบ
                item.setPickingOrderItemId(scannedTag.getId());
                item.setTagNumber(scannedTag.getTagNumber());
                sendTags.add(item);
            }
        }

        if (sendTags.isEmpty()) {
            Toast.makeText(mContext, "No scanned tags to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = MainActivity.globalUserId;
        // Create Retrofit service or similar HTTP client
        Call<List<ResponseDto>> call = apiService.createPickingOrderTransaction(
                pickingOrderId,
                userId,
                sendTags
        );

        call.enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
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
                        // Handle success
                        Toast.makeText(mContext, "Data successfully", Toast.LENGTH_LONG).show();
                        btnConfirm.setEnabled(false);
                    } else {
                        // มีรายการที่ไม่สำเร็จ
                        String errorMsg = "Error:\n\n" + errorMessages.toString();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Pick Errors")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();

                        Toast.makeText(mContext, "Confirm Kaban failed ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                // Handle failure
                Toast.makeText(mContext, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
