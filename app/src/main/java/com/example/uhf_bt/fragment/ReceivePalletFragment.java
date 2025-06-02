package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.example.uhf_bt.adapter.ReceivePalletAdapter;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.example.uhf_bt.tool.CheckUtils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.ArrayList;
import java.util.Arrays;
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

public class ReceivePalletFragment extends Fragment {
    private static final String TAG = "ReceivePalletFragment";

    private List<String> pendingProductTags = new ArrayList<>();
    private boolean isPalletScanned = false;
    private EditText EtData_Read;
    //private Button BtRead;
    private LinearLayout viewAction ;
    private TextView tvTotalQty, tvTotalScan, tvTotalSuccess;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;

    //private Spinner spinnerPallet;
    //private Button btnLockPallet;
    private ArrayAdapter<String> palletAdapter;

    //private TextView tvOrderNo;
    //private TextView tvCustomer;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private ReceivePalletAdapter adapter;
    private List<PickingOrderItem> itemList = new ArrayList<>();
    private List<PickingOrderItem> tempList = new ArrayList<>();
    private List<ProductItem> stockList = new ArrayList<>();
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private String pickingOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Button btScanRFID, btStop, btClear, btnConfirm ;

    private Set<String> scannedPallets = new HashSet<>();
    private Map<String, List<PickingOrderItem>> palletItemsMap = new HashMap<>();

    private TextView tvTotalPickingOrder, tvTotalPallet;

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
                        btScanRFID.setEnabled(true);
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
                        btScanRFID.setEnabled(false);
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

    public static ReceivePalletFragment newInstance(String pickingOrderId) {
        ReceivePalletFragment fragment = new ReceivePalletFragment();
        Bundle args = new Bundle();
        //args.putString("PickingOrderId", pickingOrderId);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //pickingOrderId = getArguments().getString("PickingOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();

        // *** Initialize new variables ***
        pendingProductTags = new ArrayList<>();
        isPalletScanned = false;

        Log.d(TAG, "Fragment created with pending tags support");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive_pallet, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();
        //loadPickingOrderItems();
        //loadProductItemStock();

       // loadPickingOrderTransactions(); // Add this line
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

        //tvOrderNo = view.findViewById(R.id.tvOrderNo);
        //tvCustomer = view.findViewById(R.id.tvCustomer);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize total quantity TextViews
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);


        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_count);
        tv_total = view.findViewById(R.id.tv_total);


        EtData_Read = view.findViewById(R.id.EtData_Read);
        //EtData_QTY = view.findViewById(R.id.EtData_QTY);
        btScanRFID = view.findViewById(R.id.btnScanRFID);
        btStop = view.findViewById(R.id.btnStop);
        btClear = view.findViewById(R.id.btnClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTotalPickingOrder = view.findViewById(R.id.tvTotalPickingOrder);
        tvTotalPallet = view.findViewById(R.id.tvTotalPallet);
    }

    // Add this method to filter items by pallet
    private void filterItemsByPallet(String pallet) {
        if (adapter != null) {
            adapter.setPalletLocked(true, pallet);
        }
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

        // Long click on title to show statistics
        toolbar_title.setOnLongClickListener(v -> {
            showPalletStatistics();
            return true;
        });

        // *** เพิ่ม: Click บน search view เพื่อแสดง DO options ***
        searchView.setOnLongClickListener(v -> {
            String[] searchOptions = {
                    "Filter by DO",
                    "Show DO Summary",
                    "Show DO Statistics",
                    "Toggle Grouping Mode"
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("DO Options")
                    .setItems(searchOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                filterByDO();
                                break;
                            case 1:
                                showDOSummary();
                                break;
                            case 2:
                                showDOStatistics();
                                break;
                            case 3:
                                toggleGroupingMode();
                                break;
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void checkNotifications() {
        // สำหรับการทดสอบ อาจเพิ่มการแจ้งเตือนเริ่มต้น (ถ้ายังไม่มีการแจ้งเตือนใดๆ)
        if (notificationMessages.isEmpty()) {
            // เพิ่มการแจ้งเตือนเริ่มต้นเพื่อการทดสอบ
            // notificationMessages.add("ทดสอบการแจ้งเตือน");
        }
        // อัพเดทสถานะการมีการแจ้งเตือน
        hasNotifications = !notificationMessages.isEmpty();

        // อัพเดทการแสดงผลปุ่มแจ้งเตือน
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

    private void handleItemClick(PickingOrderItem item) {
        if (item.getRemark() != null && !item.getRemark().isEmpty()) {
            // Create ComponentDetailsFragment
            ComponentDetailsFragment detailFragment = new ComponentDetailsFragment();
            Bundle args = new Bundle();
            args.putString("pickingOrderId", pickingOrderId);
            args.putString("productName", item.getProduct());
            args.putString("remarkString", item.getRemark());
            args.putString("pickingOrderItemId", String.valueOf(item.getPickingOrderItemId()));
            detailFragment.setArguments(args);

            // Navigate to the fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            // Use the existing dialog for items without remarks
            new AlertDialog.Builder(requireContext())
                    .setTitle(item.getProduct())
                    .setMessage(String.format("Quantity: %d\nUnit Price: ฿%.2f\nTotal: ฿%.2f",
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotal()))
                    .setPositiveButton("Scan", (dialog, which) -> {
                        startScanningForItem(item);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }


    private void updateTotalQuantities() {
        double totalQty = 0;
        double totalScan = 0;
        double totalSuccess = 0;

        // *** เปลี่ยนจาก itemList เป็น adapter.getFilteredItems() ***
        List<PickingOrderItem> currentItems = null;
        if (adapter != null) {
            currentItems = adapter.getFilteredItems();
        }

        // ถ้าไม่มีข้อมูลจาก adapter ให้ใช้ itemList เป็น fallback
        if (currentItems == null || currentItems.isEmpty()) {
            currentItems = itemList;
        }

        if (currentItems != null) {
            for (PickingOrderItem item : currentItems) {
                totalQty += item.getQuantity();
                totalScan += item.getQuantityScan();
                totalSuccess += item.getQuantitySuccess();
            }
        }

        // Update UI with formatted values
        if (tvTotalQty != null && tvTotalScan != null && tvTotalSuccess != null) {
            tvTotalQty.setText(String.format("%.0f", totalQty));
            tvTotalScan.setText(String.format("%.0f", totalScan));
            tvTotalSuccess.setText(String.format("%.0f", totalSuccess));

            // *** เพิ่ม Log สำหรับ Debug ***
            Log.d(TAG, String.format("Updated totals - Qty: %.0f, Scan: %.0f, Success: %.0f",
                    totalQty, totalScan, totalSuccess));
        }
    }
    // *** อัพเดท setupButtonListeners เพื่อเพิ่ม debug totals ***
    @SuppressLint("NewApi")
    private void setupButtonListeners() {
        btScanRFID.setOnClickListener(v -> scanRFID());
        btStop.setOnClickListener(v -> stop());
        btClear.setOnClickListener(v -> clearRFID());
        btnConfirm.setOnClickListener(v -> confirmPickingOrderTransaction());

        // เพิ่ม Long Click สำหรับดูรายการ Pallet ที่สแกนแล้ว
        btClear.setOnLongClickListener(v -> {
            showScannedPallets();
            return true;
        });

        // เพิ่ม Long Click สำหรับ Clear All Data
        btnConfirm.setOnLongClickListener(v -> {
            if (!scannedPallets.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Clear All Data")
                        .setMessage("Do you want to clear all scanned pallets and data?")
                        .setPositiveButton("Yes", (dialog, which) -> clearAllData())
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });

        // เพิ่ม: Long Click บน Stop button เพื่อดู debug options
        btStop.setOnLongClickListener(v -> {
            // Show debug menu
            String[] debugOptions = {
                    "Show Stock List Info",
                    "Debug Totals",
                    "Manual Refresh Totals",
                    "Show Detailed Stock List",
                    "Show Pending Tags",
                    "Process Pending Tags Now",
                    "Show Grouping Info" // *** เพิ่มตัวเลือกใหม่ ***
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("Debug Options")
                    .setItems(debugOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showStockListInfo();
                                break;
                            case 1:
                                debugTotals();
                                break;
                            case 2:
                                manualRefreshTotals();
                                Toast.makeText(mContext, "Totals refreshed", Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                showDetailedStockListInfo();
                                break;
                            case 4:
                                showPendingTags();
                                break;
                            case 5:
                                processPendingProductTags();
                                Toast.makeText(mContext, "Processed pending tags", Toast.LENGTH_SHORT).show();
                                break;
                            case 6:
                                showGroupingInfo(); // *** เพิ่มการเรียกใช้ ***
                                break;
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

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

    // *** เพิ่ม method สำหรับสลับระหว่าง grouping modes ***
    private void toggleGroupingMode() {
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(mContext, "No items to regroup", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Grouping Mode");

        String[] options = {
                "Separate by DO (แยกตาม DO)",
                "Combine all DO (รวม DO)"
        };

        builder.setItems(options, (dialog, which) -> {
            boolean separateByDO = (which == 0);
            List<PickingOrderItem> regroupedData = groupItems(itemList, separateByDO);
            updateUI(regroupedData);

            String message = separateByDO ?
                    "Grouped separately by DO" :
                    "Combined all DO together";
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // *** เพิ่ม method สำหรับ manual test pending tags ***
    private void testPendingTagsFlow() {
        // Clear everything first
        clearAllData();

        // Simulate scanning product tags before pallet
        String[] testProductTags = {"C123456789123", "C234567890234", "C345678901345"};

        Toast.makeText(mContext, "Testing: Scanning product tags before pallet...", Toast.LENGTH_LONG).show();

        for (String tag : testProductTags) {
            processQrCode(tag);
        }

        // Show current pending status
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            showPendingTags();
        }, 1000);
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
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
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
        adapter = new ReceivePalletAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrderItem item = adapter.getItem(position);
            String temp_remark = item.getRemark();
            if (temp_remark != null  )
            {
                if (temp_remark.trim().length() > 0)  handleItemClick(item);
            }

        });
    }

    private void scanRFID() {
        // TODO: Implement RFID scanning logic
        //  Toast.makeText(mContext, "RFID scanning not implemented yet", Toast.LENGTH_SHORT).show();
        startThread();
    }

    // 3. ปรับปรุง loadPickingOrderItems ให้รองรับหลาย Pallet
    private void loadPickingOrderItems(String palletTagId) {
        // ตรวจสอบว่า Pallet นี้เคยสแกนแล้วหรือไม่
        if (scannedPallets.contains(palletTagId)) {
            String msg = "Pallet " + palletTagId + " already scanned!";
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return;
        }

        // *** เพิ่ม: Backup scan quantities ก่อนที่จะ regroup ***
        Log.d(TAG, "Backing up scan quantities before adding new pallet");
        backupScanQuantities();

        showLoading(true);

        apiService.getPickingOrderItemByPalletTagId(palletTagId).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderItem> newPalletItems = response.body();

                    if (newPalletItems.isEmpty()) {
                        String msg = "No items found for Pallet: " + palletTagId;
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // เพิ่มส่วนนี้: สร้าง ProductItem จาก PickingOrderItem และ add ใส่ stockList
                    addPickingOrderItemsToStockList(newPalletItems);

                    // เพิ่ม Pallet ใหม่เข้าไปในรายการ
                    scannedPallets.add(palletTagId);
                    palletItemsMap.put(palletTagId, new ArrayList<>(newPalletItems));

                    // Set pallet scanned flag
                    isPalletScanned = true;

                    // รวม items จากทุก Pallet ที่สแกนแล้ว
                    List<PickingOrderItem> allItems = new ArrayList<>();
                    for (List<PickingOrderItem> palletItems : palletItemsMap.values()) {
                        allItems.addAll(palletItems);
                    }

                    // อัพเดท itemList และ tempList
                    itemList = new ArrayList<>(allItems);

                    // *** ใช้ grouping แบบแยก DO พร้อม restore scan quantities ***
                    Log.d(TAG, "Regrouping items with scan quantities preservation");
                    List<PickingOrderItem> dataList = groupItems(allItems, true); // true = แยกตาม DO

                    // Update tempList for transactions
                    updateTempListFromAllItems(allItems);

                    // Update UI
                    updateUI(dataList);

                    // แสดงข้อความสำเร็จ
                    String successMsg = "Added Pallet: " + palletTagId + " (" + newPalletItems.size() + " items)";
                    Toast.makeText(mContext, successMsg, Toast.LENGTH_SHORT).show();

                    // อัพเดท Total counts
                    updateTotalCounts();

                    // Log สำหรับ Debug
                    Log.d(TAG, "Stock List updated. Total items in stockList: " + stockList.size());

                    // *** เพิ่ม: ตรวจสอบ total scan หลัง regroup ***
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Force update totals to ensure they're correct
                            updateTotalQuantities();

                            // Debug log
                            debugTotalScanAfterRegroup();

                            // Wait a bit for UI to update then process pending tags
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                processPendingProductTags();
                            }, 500);
                        });
                    }

                } else {
                    Log.e(TAG, "Error loading picking order items: " + response.code());
                    showError("Failed to load picking order items for Pallet: " + palletTagId);
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading picking order items", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // *** เพิ่ม method สำหรับ debug total scan หลัง regroup ***
    private void debugTotalScanAfterRegroup() {
        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                double totalScan = 0;
                double totalSuccess = 0;

                for (PickingOrderItem item : items) {
                    totalScan += item.getQuantityScan();
                    totalSuccess += item.getQuantitySuccess();

                    if (item.getQuantityScan() > 0 || item.getQuantitySuccess() > 0) {
                        Log.d(TAG, "Item with scan data - " + item.getProductNumber() +
                                ", DO: " + item.getPickingOrder() +
                                ", Scan: " + item.getQuantityScan() +
                                ", Success: " + item.getQuantitySuccess());
                    }
                }

                Log.d(TAG, "Total after regroup - Scan: " + totalScan + ", Success: " + totalSuccess);
                Log.d(TAG, "Scanned tags count: " + scannedTags.size());
                Log.d(TAG, "Scan backup size: " + scanBackup.size());
            }
        }
    }


    // *** เพิ่ม method สำหรับดูข้อมูล grouping (debug) ***
    @SuppressLint("NewApi")
    private void showGroupingInfo() {
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(mContext, "No items to show grouping info", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group data ใหม่เพื่อแสดงข้อมูล
        List<PickingOrderItem> groupedByDO = groupItems(itemList, true);
        List<PickingOrderItem> groupedWithoutDO = groupItems(itemList, false);

        StringBuilder info = new StringBuilder();
        info.append("Grouping Information\n");
        info.append("===================\n");
        info.append("Original items: ").append(itemList.size()).append("\n");
        info.append("Grouped by DO+Product+BoxType: ").append(groupedByDO.size()).append("\n");
        info.append("Grouped by Product+BoxType only: ").append(groupedWithoutDO.size()).append("\n\n");

        // แสดงรายละเอียด DO
        Map<String, Integer> doCount = new HashMap<>();
        for (PickingOrderItem item : itemList) {
            String doNumber = item.getPickingOrder();
            if (doNumber != null) {
                doCount.put(doNumber, doCount.getOrDefault(doNumber, 0) + 1);
            }
        }

        info.append("Delivery Orders:\n");
        for (Map.Entry<String, Integer> entry : doCount.entrySet()) {
            info.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" items\n");
        }

        info.append("\nGrouped Items (by DO):\n");
        String currentDO = "";
        for (PickingOrderItem item : groupedByDO) {
            if (!item.getPickingOrder().equals(currentDO)) {
                currentDO = item.getPickingOrder();
                info.append("\n--- DO: ").append(currentDO).append(" ---\n");
            }
            info.append("  ").append(item.getProductNumber())
                    .append(" (").append(item.getBoxType()).append(")")
                    .append(" - Qty: ").append(item.getQuantity()).append("\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Grouping Information")
                .setMessage(info.toString())
                .setPositiveButton("Switch to Combined View", (dialog, which) -> {
                    // ใช้ grouping แบบไม่แยก DO
                    List<PickingOrderItem> combinedData = groupItems(itemList, false);
                    updateUI(combinedData);
                    Toast.makeText(mContext, "Switched to combined grouping", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Switch to Separate View", (dialog, which) -> {
                    // ใช้ grouping แบบแยก DO
                    List<PickingOrderItem> separateData = groupItems(itemList, true);
                    updateUI(separateData);
                    Toast.makeText(mContext, "Switched to separate DO grouping", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }



    // *** ปรับปรุง addPickingOrderItemsToStockList เพื่อรองรับ TagId หลายตัว ***
    private void addPickingOrderItemsToStockList(List<PickingOrderItem> pickingOrderItems) {
        if (pickingOrderItems == null || pickingOrderItems.isEmpty()) {
            return;
        }

        // Initialize stockList if null
        if (stockList == null) {
            stockList = new ArrayList<>();
        }

        for (PickingOrderItem pickingItem : pickingOrderItems) {
            // จัดการ TagId ที่อาจมีหลายค่า (comma-separated)
            String tagIds = pickingItem.getTagId();
            if (tagIds != null && !tagIds.isEmpty()) {
                // Split TagId by comma and process each one
                String[] tagArray = tagIds.split(",");

                for (String tagId : tagArray) {
                    String trimmedTagId = tagId.trim();
                    if (!trimmedTagId.isEmpty()) {
                        processIndividualTagId(trimmedTagId, pickingItem);
                    }
                }
            }
        }

        Log.d(TAG, "Stock list updated. Total unique tags: " + stockList.size());
    }

    // *** เพิ่ม method สำหรับแสดง DO summary ***
    private void showDOSummary() {
        if (adapter == null) {
            Toast.makeText(mContext, "No data available", Toast.LENGTH_SHORT).show();
            return;
        }

        int uniqueDOCount = adapter.getUniqueDOCount();
        String doSummary = adapter.getDOSummary();

        StringBuilder info = new StringBuilder();
        info.append("Delivery Order Summary\n");
        info.append("=====================\n");
        info.append("Total DOs: ").append(uniqueDOCount).append("\n");
        info.append("Total Pallets: ").append(scannedPallets.size()).append("\n");
        info.append("Total Items: ").append(adapter.getCount()).append("\n\n");

        info.append("DO Details:\n");
        info.append(doSummary);

        new AlertDialog.Builder(requireContext())
                .setTitle("DO Summary")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }


    // *** เพิ่ม method สำหรับจัดการ TagId แต่ละตัว ***
    private void processIndividualTagId(String tagId, PickingOrderItem pickingItem) {
        // ตรวจสอบว่า TagId มีอยู่แล้วใน stockList หรือไม่
        boolean tagExists = false;
        ProductItem existingItem = null;

        for (ProductItem item : stockList) {
            if (tagId.equals(item.getTagId())) {
                tagExists = true;
                existingItem = item;
                break;
            }
        }

        if (tagExists && existingItem != null) {
            // อัพเดทข้อมูลถ้าจำเป็น
            updateExistingProductItem(existingItem, pickingItem);
            Log.d(TAG, "Updated existing item in stockList - TagId: " + tagId);
        } else {
            // สร้าง ProductItem ใหม่สำหรับ TagId นี้
            ProductItem newProductItem = createProductItemFromPickingOrderItem(pickingItem);
            newProductItem.setTagId(tagId); // Set specific TagId
            stockList.add(newProductItem);

            Log.d(TAG, "Added to stockList - TagId: " + tagId +
                    ", Product: " + pickingItem.getProductNumber());
        }
    }

    // *** เพิ่ม method สำหรับสร้าง ProductItem จาก PickingOrderItem ***
    // *** ปรับปรุง createProductItemFromPickingOrderItem เพื่อรองรับการเปลี่ยน TagId ***
    private ProductItem createProductItemFromPickingOrderItem(PickingOrderItem pickingItem) {
        ProductItem productItem = new ProductItem();

        // Map ข้อมูลจาก PickingOrderItem ไป ProductItem
        // หมายเหตุ: TagId จะถูก set แยกใน processIndividualTagId

        productItem.setProductId(pickingItem.getProductId());
        productItem.setProductNumber(pickingItem.getProductNumber());
        productItem.setProductName(pickingItem.getProduct());
        productItem.setBarcode(pickingItem.getBarcode());

        // Set ข้อมูลเพิ่มเติม
        productItem.setLotNumber(pickingItem.getLotNumber());
        productItem.setLot(pickingItem.getLot());
        productItem.setItemType(pickingItem.getBoxType()); // Map BoxType -> ItemType
        productItem.setQuantityInBox(pickingItem.getAllocatedQty()); // Map AllocatedQty -> QuantityInBox

        // Set สถานะเป็น Active (1) เพื่อให้ใช้ในการสแกนได้
        productItem.setItemStatus(1);

        // Set ข้อมูลอื่นๆ ถ้ามี
//        if (pickingItem.getLocator() != null) {
//            productItem.setLocation(pickingItem.getLocator());
//        }

        // เพิ่มข้อมูล Pallet สำหรับการอ้างอิง
//        if (pickingItem.getPalletTagId() != null) {
//            productItem.setPalletTagId(pickingItem.getPalletTagId());
//        }

        // เพิ่มข้อมูล Picking Order สำหรับการอ้างอิง
//        if (pickingItem.getPickingOrder() != null) {
//            productItem.setPickingOrder(pickingItem.getPickingOrder());
//        }

        return productItem;
    }



    // *** เพิ่ม method สำหรับอัพเดท ProductItem ที่มีอยู่แล้ว ***
    private void updateExistingProductItem(ProductItem existingItem, PickingOrderItem pickingItem) {
        // อัพเดทข้อมูลที่อาจเปลี่ยนแปลง
        if (pickingItem.getLotNumber() != null) {
            existingItem.setLotNumber(pickingItem.getLotNumber());
        }

        if (pickingItem.getLot() != null) {
            existingItem.setLot(pickingItem.getLot());
        }

        if (pickingItem.getBoxType() != null) {
            existingItem.setItemType(pickingItem.getBoxType());
        }

        // อัพเดท quantity in box
        if (pickingItem.getAllocatedQty() > 0) {
            existingItem.setQuantityInBox(pickingItem.getAllocatedQty());
        }


        Log.d(TAG, "Updated existing item in stockList - TagId: " + pickingItem.getTagId());
    }

    // 4. เพิ่ม method สำหรับ group items
    @SuppressLint("NewApi")
    private List<PickingOrderItem> groupItemsByProductAndBoxType(List<PickingOrderItem> allItems) {
        List<PickingOrderItem> dataList = new ArrayList<>();

        for (PickingOrderItem trans : allItems) {
            boolean found = false;
            PickingOrderItem existingItem = null;

            for (PickingOrderItem data : dataList) {
                // เพิ่ม condition สำหรับ PickingOrder (DO)
                if (data.getProductId() == trans.getProductId() &&
                        data.getBoxType() != null && data.getBoxType().equals(trans.getBoxType()) &&
                        data.getPickingOrder() != null && data.getPickingOrder().equals(trans.getPickingOrder())) {
                    found = true;
                    existingItem = data;
                    break;
                }
            }

            if (found && existingItem != null) {
                // Update existing item
                existingItem.setQuantity(existingItem.getQuantity() + trans.getQuantity());
                existingItem.setAllocatedQty(existingItem.getAllocatedQty() + trans.getAllocatedQty());

                // Combine tag IDs
                String existingTagIds = existingItem.getTagId();
                String newTagId = trans.getTagId();
                if (existingTagIds != null && !existingTagIds.isEmpty()) {
                    if (newTagId != null && !newTagId.isEmpty()) {
                        existingItem.setTagId(existingTagIds + "," + newTagId);
                    }
                } else {
                    existingItem.setTagId(newTagId);
                }

                // Combine other fields as needed
                combineItemFields(existingItem, trans);

                // *** เพิ่ม: รักษา scan quantities จาก original item ***
                if (trans.getQuantityScan() > 0) {
                    existingItem.setQuantityScan(existingItem.getQuantityScan() + trans.getQuantityScan());
                }
                if (trans.getQuantitySuccess() > 0) {
                    existingItem.setQuantitySuccess(existingItem.getQuantitySuccess() + trans.getQuantitySuccess());
                }

                Log.d(TAG, "Grouped item - DO: " + trans.getPickingOrder() +
                        ", Product: " + trans.getProductNumber() +
                        ", BoxType: " + trans.getBoxType() +
                        ", New Qty: " + existingItem.getQuantity() +
                        ", Scan: " + existingItem.getQuantityScan() +
                        ", Success: " + existingItem.getQuantitySuccess());

            } else {
                // Create new grouped item
                PickingOrderItem newItem = createNewGroupedItem(trans);

                // *** เพิ่ม: คัดลอก scan quantities ***
                newItem.setQuantityScan(trans.getQuantityScan());
                newItem.setQuantitySuccess(trans.getQuantitySuccess());

                dataList.add(newItem);

                Log.d(TAG, "New grouped item - DO: " + trans.getPickingOrder() +
                        ", Product: " + trans.getProductNumber() +
                        ", BoxType: " + trans.getBoxType() +
                        ", Qty: " + newItem.getQuantity() +
                        ", Scan: " + newItem.getQuantityScan() +
                        ", Success: " + newItem.getQuantitySuccess());
            }
        }

        // *** เพิ่ม: Restore scan quantities from backup ***
        restoreScanQuantities(dataList);

        // Sort items by DO, then by Product
        dataList.sort((item1, item2) -> {
            // First sort by PickingOrder (DO)
            int doComparison = 0;
            if (item1.getPickingOrder() != null && item2.getPickingOrder() != null) {
                doComparison = item1.getPickingOrder().compareTo(item2.getPickingOrder());
            } else if (item1.getPickingOrder() != null) {
                doComparison = -1;
            } else if (item2.getPickingOrder() != null) {
                doComparison = 1;
            }

            if (doComparison != 0) {
                return doComparison;
            }

            // Then sort by ProductNumber
            if (item1.getProductNumber() != null && item2.getProductNumber() != null) {
                return item1.getProductNumber().compareTo(item2.getProductNumber());
            } else if (item1.getProductNumber() != null) {
                return -1;
            } else if (item2.getProductNumber() != null) {
                return 1;
            }

            return 0;
        });

        Log.d(TAG, "Grouped items by DO+Product+BoxType. Total groups: " + dataList.size());
        return dataList;
    }

    // *** เพิ่ม method สำหรับ group เฉพาะ Product และ BoxType (ไม่แยก DO) ***
    private List<PickingOrderItem> groupItemsByProductAndBoxTypeOnly(List<PickingOrderItem> allItems) {
        List<PickingOrderItem> dataList = new ArrayList<>();

        for (PickingOrderItem trans : allItems) {
            boolean found = false;
            PickingOrderItem existingItem = null;

            for (PickingOrderItem data : dataList) {
                // Group เฉพาะ ProductId และ BoxType (ไม่สนใจ DO)
                if (data.getProductId() == trans.getProductId() &&
                        data.getBoxType() != null && data.getBoxType().equals(trans.getBoxType())) {
                    found = true;
                    existingItem = data;
                    break;
                }
            }

            if (found && existingItem != null) {
                // Update existing item
                existingItem.setQuantity(existingItem.getQuantity() + trans.getQuantity());
                existingItem.setAllocatedQty(existingItem.getAllocatedQty() + trans.getAllocatedQty());

                // Combine tag IDs
                String existingTagIds = existingItem.getTagId();
                String newTagId = trans.getTagId();
                if (existingTagIds != null && !existingTagIds.isEmpty()) {
                    if (newTagId != null && !newTagId.isEmpty()) {
                        existingItem.setTagId(existingTagIds + "," + newTagId);
                    }
                } else {
                    existingItem.setTagId(newTagId);
                }

                // Combine picking orders
                String existingDO = existingItem.getPickingOrder();
                String newDO = trans.getPickingOrder();
                if (newDO != null && !newDO.isEmpty()) {
                    if (existingDO != null && !existingDO.isEmpty()) {
                        if (!existingDO.contains(newDO)) {
                            existingItem.setPickingOrder(existingDO + ", " + newDO);
                        }
                    } else {
                        existingItem.setPickingOrder(newDO);
                    }
                }

                // Combine other fields
                combineItemFields(existingItem, trans);

            } else {
                // Create new grouped item
                PickingOrderItem newItem = createNewGroupedItem(trans);
                dataList.add(newItem);
            }
        }

        Log.d(TAG, "Grouped items by Product+BoxType only. Total groups: " + dataList.size());
        return dataList;
    }

    // *** เพิ่ม method สำหรับเลือกวิธี grouping ***
    private List<PickingOrderItem> groupItems(List<PickingOrderItem> allItems, boolean separateByDO) {
        if (separateByDO) {
            return groupItemsByProductAndBoxType(allItems);
        } else {
            return groupItemsByProductAndBoxTypeOnly(allItems);
        }
    }

    // 5. เพิ่ม method สำหรับ combine fields
    private void combineItemFields(PickingOrderItem existingItem, PickingOrderItem newItem) {
        // Combine locators
        String existingLocator = existingItem.getLocator();
        String newLocator = newItem.getLocator();
        if (newLocator != null && !newLocator.isEmpty()) {
            if (existingLocator != null && !existingLocator.isEmpty()) {
                if (!existingLocator.contains(newLocator)) {
                    existingItem.setLocator(existingLocator + "; " + newLocator);
                }
            } else {
                existingItem.setLocator(newLocator);
            }
        }

        // Combine lot suggestions
        String existingLotSuggestion = existingItem.getLotSuggestion();
        String newLotSuggestion = newItem.getLotSuggestion();
        if (newLotSuggestion != null && !newLotSuggestion.isEmpty()) {
            if (existingLotSuggestion != null && !existingLotSuggestion.isEmpty()) {
                if (!existingLotSuggestion.contains(newLotSuggestion)) {
                    existingItem.setLotSuggestion(existingLotSuggestion + "; " + newLotSuggestion);
                }
            } else {
                existingItem.setLotSuggestion(newLotSuggestion);
            }
        }

        // Combine remarks
        String existingRemark = existingItem.getRemark();
        String newRemark = newItem.getRemark();
        if (newRemark != null && !newRemark.isEmpty()) {
            if (existingRemark != null && !existingRemark.isEmpty()) {
                if (!existingRemark.contains(newRemark)) {
                    existingItem.setRemark(existingRemark + "; " + newRemark);
                }
            } else {
                existingItem.setRemark(newRemark);
            }
        }

        // Update QtyBox
        String existingQtyBox = existingItem.getQtyBox();
        String newQtyBox = newItem.getQtyBox();
        if (newQtyBox != null && !newQtyBox.isEmpty()) {
            if (existingQtyBox != null && !existingQtyBox.isEmpty()) {
                if (!existingQtyBox.contains(newQtyBox)) {
                    existingItem.setQtyBox(existingQtyBox + "+" + newQtyBox);
                }
            } else {
                existingItem.setQtyBox(newQtyBox);
            }
        }
    }

    // 6. เพิ่ม method สำหรับอัพเดท tempList
    private void updateTempListFromAllItems(List<PickingOrderItem> allItems) {
        tempList.clear();

        for (PickingOrderItem trans : allItems) {
            PickingOrderItem tempItem = new PickingOrderItem();
            tempItem.setProductId(trans.getProductId());
            tempItem.setQuantity(trans.getQuantity());
            tempItem.setTagId(trans.getTagId());
            tempItem.setLotNumber(trans.getLotNumber());
            tempItem.setLot(trans.getLot());
            tempList.add(tempItem);
        }
    }

    // 7. เพิ่ม method สำหรับอัพเดท Total counts
    private void updateTotalCounts() {
        // Count unique picking orders
        Set<String> uniquePickingOrders = new HashSet<>();
        for (PickingOrderItem item : itemList) {
            if (item.getPickingOrder() != null) {
                // Handle multiple DOs in single item
                String[] doArray = item.getPickingOrder().split(",");
                for (String doNumber : doArray) {
                    uniquePickingOrders.add(doNumber.trim());
                }
            }
        }

        // Update UI
        if (tvTotalPickingOrder != null) {
            tvTotalPickingOrder.setText(String.valueOf(uniquePickingOrders.size()));
        }

        if (tvTotalPallet != null) {
            tvTotalPallet.setText(String.valueOf(scannedPallets.size()));
        }

        // *** Log สำหรับ debug ***
        Log.d(TAG, "Updated counts - DOs: " + uniquePickingOrders.size() +
                ", Pallets: " + scannedPallets.size());
    }


    // *** เพิ่ม method สำหรับค้นหา items ตาม DO ***
    private List<PickingOrderItem> getItemsByDO(String doNumber) {
        List<PickingOrderItem> result = new ArrayList<>();

        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                for (PickingOrderItem item : items) {
                    if (item.getPickingOrder() != null &&
                            item.getPickingOrder().contains(doNumber)) {
                        result.add(item);
                    }
                }
            }
        }

        return result;
    }

    // *** เพิ่ม method สำหรับ filter ตาม DO ***
    private void filterByDO() {
        if (adapter == null || adapter.getCount() == 0) {
            Toast.makeText(mContext, "No data to filter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get unique DOs
        Set<String> uniqueDOs = new HashSet<>();
        List<PickingOrderItem> items = adapter.getFilteredItems();
        if (items != null) {
            for (PickingOrderItem item : items) {
                if (item.getPickingOrder() != null) {
                    String[] doArray = item.getPickingOrder().split(",");
                    for (String doNumber : doArray) {
                        uniqueDOs.add(doNumber.trim());
                    }
                }
            }
        }

        if (uniqueDOs.isEmpty()) {
            Toast.makeText(mContext, "No DOs found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create DO selection dialog
        String[] doArray = uniqueDOs.toArray(new String[0]);
        Arrays.sort(doArray); // Sort DOs

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by DO");

        builder.setItems(doArray, (dialog, which) -> {
            String selectedDO = doArray[which];

            // Filter items by selected DO
            List<PickingOrderItem> filteredItems = getItemsByDO(selectedDO);

            if (filteredItems.isEmpty()) {
                Toast.makeText(mContext, "No items found for DO: " + selectedDO, Toast.LENGTH_SHORT).show();
            } else {
                // Update adapter with filtered items
                adapter.updateItems(filteredItems);
                Toast.makeText(mContext, "Showing " + filteredItems.size() +
                        " items for DO: " + selectedDO, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNeutralButton("Show All", (dialog, which) -> {
            // Reset filter to show all items
            List<PickingOrderItem> groupedData = groupItems(itemList, true);
            updateUI(groupedData);
            Toast.makeText(mContext, "Showing all items", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // *** เพิ่ม method สำหรับแสดงสถิติแต่ละ DO ***
    private void showDOStatistics() {
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(mContext, "No data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group statistics by DO
        Map<String, DOStatistics> doStats = new HashMap<>();

        for (PickingOrderItem item : itemList) {
            if (item.getPickingOrder() != null) {
                String[] doArray = item.getPickingOrder().split(",");
                for (String doNumber : doArray) {
                    String trimmedDO = doNumber.trim();
                    DOStatistics stats = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        stats = doStats.computeIfAbsent(trimmedDO, k -> new DOStatistics());
                    }

                    stats.itemCount++;
                    stats.totalQty += item.getQuantity();
                    stats.totalScan += item.getQuantityScan();
                    stats.totalSuccess += item.getQuantitySuccess();

                    // Track pallets
                    if (item.getPallet() != null) {
                        stats.pallets.add(item.getPallet());
                    }

                    // Track customers
                    if (item.getCustomer() != null) {
                        stats.customers.add(item.getCustomer());
                    }
                }
            }
        }

        StringBuilder info = new StringBuilder();
        info.append("DO Statistics\n");
        info.append("=============\n\n");

        for (Map.Entry<String, DOStatistics> entry : doStats.entrySet()) {
            String doNumber = entry.getKey();
            DOStatistics stats = entry.getValue();

            info.append("DO: ").append(doNumber).append("\n");
            info.append("  Items: ").append(stats.itemCount).append("\n");
            info.append("  Pallets: ").append(stats.pallets.size()).append("\n");
            info.append("  Customers: ").append(stats.customers.size()).append("\n");
            info.append("  Qty: ").append(String.format("%.0f", stats.totalQty)).append("\n");
            info.append("  Scanned: ").append(String.format("%.0f", stats.totalScan)).append("\n");
            info.append("  Success: ").append(String.format("%.0f", stats.totalSuccess)).append("\n");

            // Calculate completion percentage
            double completionRate = stats.totalQty > 0 ?
                    (stats.totalSuccess / stats.totalQty) * 100 : 0;
            info.append("  Completion: ").append(String.format("%.1f%%", completionRate)).append("\n\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("DO Statistics")
                .setMessage(info.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    // *** ปรับปรุง method clearAllData เพื่อเคลียร์ stockList ด้วย ***
    private void clearAllData() {
        // Clear existing data
        scannedPallets.clear();
        palletItemsMap.clear();
        itemList.clear();
        tempList.clear();
        scannedTags.clear();

        // เพิ่ม: เคลียร์ stockList ด้วย
        if (stockList != null) {
            stockList.clear();
            Log.d(TAG, "Stock list cleared");
        }

        // เคลียร์ pending tags และ flags
        pendingProductTags.clear();
        isPalletScanned = false;

        // *** เพิ่ม: เคลียร์ scan backup ***
        scanBackup.clear();
        Log.d(TAG, "Scan backup cleared");

        // Clear UI
        if (adapter != null) {
            adapter.updateItems(new ArrayList<>());
        }

        // Reset totals
        updateTotalQuantities();
        updateTotalCounts();

        // Reset buttons
        btnConfirm.setEnabled(false);

        Toast.makeText(mContext, "All data cleared including scan backup", Toast.LENGTH_SHORT).show();
    }

    // *** เพิ่ม method สำหรับแสดงข้อมูล stockList (สำหรับ Debug) ***
    private void showStockListInfo() {
        if (stockList == null || stockList.isEmpty()) {
            Toast.makeText(mContext, "Stock list is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Stock List Info\n");
        info.append("===============\n");
        info.append("Total Items: ").append(stockList.size()).append("\n\n");

        // แสดง 10 รายการแรก
        int displayCount = Math.min(10, stockList.size());
        for (int i = 0; i < displayCount; i++) {
            ProductItem item = stockList.get(i);
            info.append("Item ").append(i + 1).append(":\n");
            info.append("  TagId: ").append(item.getTagId()).append("\n");
            info.append("  Product: ").append(item.getProductNumber()).append("\n");
            info.append("  Type: ").append(item.getItemType()).append("\n");
            info.append("  Qty: ").append(item.getQuantityInBox()).append("\n\n");
        }

        if (stockList.size() > 10) {
            info.append("... and ").append(stockList.size() - 10).append(" more items");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Stock List Information")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // Helper method to create a new grouped item
    private PickingOrderItem createNewGroupedItem(PickingOrderItem trans) {
        PickingOrderItem newItem = new PickingOrderItem();

        newItem.setPickingOrder(trans.getPickingOrder());
        newItem.setId(trans.getId());
        newItem.setPickingOrderItemId(trans.getPickingOrderItemId());
        newItem.setRowGuid(trans.getRowGuid());
        newItem.setCreatedAtUtc(trans.getCreatedAtUtc());
        newItem.setPickingOrder(trans.getPickingOrder());
        newItem.setCustomer(trans.getCustomer());
        newItem.setOrderDate(trans.getOrderDate());

        newItem.setPalletTagId(trans.getPalletTagId());
        newItem.setPallet(trans.getPallet());
        newItem.setPalletNo(trans.getPalletNo());

        newItem.setProductId(trans.getProductId());
        newItem.setProductNumber(trans.getProductNumber());
        newItem.setProduct(trans.getProduct());
        newItem.setBarcode(trans.getBarcode());
        newItem.setSummary(trans.getSummary());
        newItem.setUnitPrice(trans.getUnitPrice());
        newItem.setTotal(trans.getTotal());

        newItem.setQuantity(1); // Start with 1
        newItem.setQtyBox(trans.getQtyBox());
        newItem.setAllocatedQty(trans.getAllocatedQty());
        newItem.setTagId(trans.getTagId());
        newItem.setLocator(trans.getLocator());
        newItem.setLotSuggestion(trans.getLotSuggestion());
        newItem.setBoxType(trans.getBoxType());
        newItem.setSequenceNumber(trans.getSequenceNumber());
        newItem.setRemark(trans.getRemark());

        // Initialize scan quantities
        newItem.setQuantityScan(0);
        newItem.setQuantitySuccess(0);

        return newItem;
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

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(List<PickingOrderItem> items) {
        if (!items.isEmpty()) {
            //PickingOrderItem firstItem = items.get(0);
            //tvOrderNo.setText("DO: " + firstItem.getPickingOrder());
            //tvCustomer.setText("ลูกค้า: " + firstItem.getCustomer());
            //tvOrderDate.setText("Order Date: " + firstItem.getOrderDate());
        }

        adapter.updateItems(items);



        // Enable/disable buttons based on total amount
        double totalAmount = 0;
        for (PickingOrderItem item : items) {
            totalAmount += item.getTotal();
        }

        // Update total quantities
        updateTotalQuantities();

        // Get unique pallets
        Set<String> uniquePallets = new HashSet<>();
        for (PickingOrderItem item : items) {
            if (item.getPallet() != null && !item.getPallet().isEmpty()) {
                uniquePallets.add(item.getPallet());
            }
        }

        // Update spinner
        //palletAdapter.clear();
        //palletAdapter.addAll(new ArrayList<>(uniquePallets));
        //palletAdapter.notifyDataSetChanged();

    }

//    private void handleItemClick(PickingOrderItem item) {
//        new AlertDialog.Builder(requireContext())
//                .setTitle(item.getProduct())
//                .setMessage(String.format("Quantity: %d\nUnit Price: ฿%.2f\nTotal: ฿%.2f",
//                        item.getQuantity(),
//                        item.getUnitPrice(),
//                        item.getTotal()))
//                .setPositiveButton("Scan", (dialog, which) -> {
//                    startScanningForItem(item);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }

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

        try {
            mContext.uhf.setTagFocus(true);
        } catch (Exception ex) {
            Log.e(TAG, "Error setting TagFocus: " + ex.getMessage());
        }

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


    @Override
    public void onResume() {
        super.onResume();

        // Check notifications when fragment resumes
        checkNotifications();

        if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            btScanRFID.setEnabled(true);
            //btInventory.setEnabled(true);
            //btInventoryPerMinute.setEnabled(true);

            //cbFilter.setEnabled(true);
        } else {
            btScanRFID.setEnabled(false);
            //btInventory.setEnabled(false);
            //btInventoryPerMinute.setEnabled(false);

            //cbFilter.setChecked(false);
            //cbFilter.setEnabled(false);
        }
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

    private void startScanningForItem(PickingOrderItem item) {
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
    int temp_zz = 0;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;


            // Initialize variables
            if (tagList == null) {
                tagList = new ArrayList<>();
            }

            if (tempDatas == null) {
                tempDatas = new ArrayList<>();
            }


        }
    }

    @Override
    public void onDetach() {
        // Remove connect status listener to prevent memory leaks
        if (mContext != null) {

        }
        super.onDetach();
    }

    public void clearSearch() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
    }
    // *** ปรับปรุง findItemStockByTagId เพื่อให้แน่ใจว่าใช้ stockList ที่อัพเดทแล้ว ***
    private ProductItem findItemStockByTagId(String tagId) {
        if (stockList == null || stockList.isEmpty() || tagId == null) {
            Log.d(TAG, "Stock list is empty or tagId is null");
            return null;
        }

        for (ProductItem item : stockList) {
            if (item.getTagId() != null && item.getTagId().equals(tagId)) {
                Log.d(TAG, "Found item in stock list - TagId: " + tagId + ", Product: " + item.getProductNumber());
                return item;
            }
        }

        Log.d(TAG, "Item not found in stock list - TagId: " + tagId);
        return null;
    }

    // Update processQrCode method to handle quantity tracking
    private void processQrCode(String qrCode) {
        try {
            // Basic input validation
            if (qrCode == null || qrCode.length() < 11) {
                String msg = qrCode + " Invalid format";
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            String chkTagId = qrCode.substring(0, 1);

            if (chkTagId.contains("F")) {
                // Pallet Tag - เพิ่มเข้าไปในรายการ Pallet ที่สแกนแล้ว
                loadPickingOrderItems(qrCode);
                isPalletScanned = true;

                // *** Process pending product tags after pallet is loaded ***
                processPendingProductTags();
                return;

            } else if (chkTagId.contains("C")) {
                // Component Tag
                processComponentTag(qrCode);
            } else {
                // Product Tag
                processProductTag(qrCode);
            }

        } catch (Exception e) {
            String msg = "Error processing QR code: " + e.getMessage();
            Log.e(TAG, msg);
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    private void processPendingProductTags() {
        if (pendingProductTags.isEmpty()) {
            return;
        }

        Log.d(TAG, "Processing " + pendingProductTags.size() + " pending product tags");

        List<String> tagsToProcess = new ArrayList<>(pendingProductTags);
        pendingProductTags.clear();

        for (String pendingTag : tagsToProcess) {
            Log.d(TAG, "Re-processing pending tag: " + pendingTag);

            // Re-process the tag now that stockList is available
            String chkTagId = pendingTag.substring(0, 1);
            if (chkTagId.contains("C")) {
                processComponentTag(pendingTag);
            } else {
                processProductTag(pendingTag);
            }
        }
    }

    private void processComponentTag(String qrCode) {
        // *** ตรวจสอบว่ามี Pallet แล้วหรือไม่ ***
        if (!isPalletScanned || stockList == null || stockList.isEmpty()) {
            Log.d(TAG, "No pallet scanned yet, adding to pending list: " + qrCode);
            String msg = "กรุณาสแกนพาเลทก่อน - จะจำไว้ให้: " + qrCode;
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();

            // Add to pending list if not already exists
            if (!pendingProductTags.contains(qrCode)) {
                pendingProductTags.add(qrCode);
            }
            return;
        }

        String xxTagID = qrCode;
        ProductItem foundItem = findItemStockByTagId(xxTagID);

        if (foundItem == null) {
            Log.d(TAG, "No item stock found with tagId: " + xxTagID);
            String msg = qrCode + " ไม่มีอยู่ในสต็อก";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return;
        }

        // Get all items from adapter
        List<PickingOrderItem> items = adapter.getFilteredItems();

        if (items == null || items.isEmpty()) {
            String msg = "No items available";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return;
        }

        boolean found = false;

        // Search for matching item
        for (PickingOrderItem item : items) {
            String temp_box_type = item.getBoxType();
            String temp_box_type2 = foundItem.getItemType();

            if (item != null && item.getTagId() != null && item.getTagId().contains(foundItem.getTagId())) {
                found = true;

                if (processItemScan(item, qrCode, foundItem, temp_box_type2)) {
                    break;
                }
            }
        }

        if (!found) {
            String msg = qrCode + " ไม่ตรงกับสินค้าที่ต้องจัด";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    // *** ปรับปรุง processProductTag เพื่อใช้ stockList ที่อัพเดทแล้ว ***
    private void processProductTag(String qrCode) {
        // *** ตรวจสอบว่ามี Pallet แล้วหรือไม่ ***
        if (!isPalletScanned || stockList == null || stockList.isEmpty()) {
            Log.d(TAG, "No pallet scanned yet, adding to pending list: " + qrCode);
            String msg = "กรุณาสแกนพาเลทก่อน - จะจำไว้ให้: " + qrCode;
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();

            // Add to pending list if not already exists
            if (!pendingProductTags.contains(qrCode)) {
                pendingProductTags.add(qrCode);
            }
            return;
        }

        String xxTagID = qrCode;
        ProductItem foundItem = findItemStockByTagId(xxTagID);

        if (foundItem == null) {
            Log.d(TAG, "No item stock found with tagId: " + xxTagID);
            String msg = qrCode + " ไม่มีอยู่ในสต็อก";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return;
        }

        List<PickingOrderItem> items = adapter.getFilteredItems();

        if (items == null || items.isEmpty()) {
            String msg = "No items available";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return;
        }

        boolean found = false;

        for (PickingOrderItem item : items) {
            String temp_box_type = item.getBoxType();
            String temp_box_type2 = foundItem.getItemType();

            if (item != null && item.getProductId() == foundItem.getProductId()) {
                if (temp_box_type != null && temp_box_type.equals(temp_box_type2)) {
                    found = true;
                    if (processItemScan(item, qrCode, foundItem, temp_box_type2)) {
                        break;
                    }
                } else {
                    String msg = qrCode + " สถานะเป็นกล่องเศษ! ";
                    notificationMessages.add(msg);
                    hasNotifications = true;
                    updateNotificationVisibility();
                    break;
                }
            }
        }

        if (!found) {
            String msg = qrCode + " ไม่ตรงกับสินค้าที่ต้องจัด";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    // *** เพิ่ม method สำหรับดู stockList แบบ detailed ***
    @SuppressLint("NewApi")
    private void showDetailedStockListInfo() {
        if (stockList == null || stockList.isEmpty()) {
            Toast.makeText(mContext, "Stock list is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group by ProductId
        Map<Integer, List<ProductItem>> groupedByProduct = new HashMap<>();
        for (ProductItem item : stockList) {
            groupedByProduct.computeIfAbsent(item.getProductId(), k -> new ArrayList<>()).add(item);
        }

        StringBuilder info = new StringBuilder();
        info.append("Detailed Stock List\n");
        info.append("==================\n");
        info.append("Total Unique Tags: ").append(stockList.size()).append("\n");
        info.append("Total Products: ").append(groupedByProduct.size()).append("\n\n");

        for (Map.Entry<Integer, List<ProductItem>> entry : groupedByProduct.entrySet()) {
            List<ProductItem> productItems = entry.getValue();
            if (!productItems.isEmpty()) {
                ProductItem firstItem = productItems.get(0);
                info.append("Product: ").append(firstItem.getProductNumber()).append("\n");
                info.append("  Tags: ").append(productItems.size()).append(" pcs\n");

                // Show first few TagIds
                int showCount = Math.min(3, productItems.size());
                for (int i = 0; i < showCount; i++) {
                    info.append("    - ").append(productItems.get(i).getTagId()).append("\n");
                }
                if (productItems.size() > 3) {
                    info.append("    ... and ").append(productItems.size() - 3).append(" more\n");
                }
                info.append("\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Detailed Stock Information")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean processItemScan(PickingOrderItem item, String qrCode, ProductItem foundItem, String itemType) {
        int pickingItemId = item.getId();

        // Check if we've already reached the required quantity
        if (item.getQuantitySuccess() + item.getQuantityScan() >= item.getQuantity()) {
            String msg = item.getProductNumber() + " สแกนสินค้าครบแล้ว";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
            return true;
        }

        // Check if this tag was already scanned
        boolean tagAlreadyScanned = false;
        for (PickingOrderItem scannedItem : scannedTags) {
            if (scannedItem.getTagId().equals(qrCode)) {
                tagAlreadyScanned = true;
                break;
            }
        }

        if (!tagAlreadyScanned) {
            // *** เพิ่ม Log เพื่อ Debug ***
            Log.d(TAG, "Processing item scan - Product: " + item.getProductNumber() +
                    ", Current Scan: " + item.getQuantityScan());

            // Update quantity scanned
            item.setQuantityScan(item.getQuantityScan() + 1);

            // *** เพิ่ม Log หลังอัพเดท ***
            Log.d(TAG, "Updated scan quantity - Product: " + item.getProductNumber() +
                    ", New Scan: " + item.getQuantityScan());

            // Add tag to scanned list
            PickingOrderItem newScannedItem = new PickingOrderItem();
            newScannedItem.setProductId(item.getProductId());
            newScannedItem.setQuantity(1);
            newScannedItem.setTagId(qrCode.trim());
            newScannedItem.setLotNumber(foundItem.getLotNumber());
            newScannedItem.setLot(foundItem.getLot());
            newScannedItem.setId(item.getPickingOrderItemId());
            newScannedItem.setPickingOrderItemId(pickingItemId);
            newScannedItem.setItemType(itemType);
            newScannedItem.setAllocatedQty(foundItem.getQuantityInBox());
            newScannedItem.setPalletTagId(item.getPalletTagId());
            scannedTags.add(newScannedItem);

            btnConfirm.setEnabled(true);

            // *** ปรับปรุง UI Update ให้ทำงานทันที ***
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Notify adapter ก่อน
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    // รอสักครู่แล้วค่อย update totals
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        updateTotalQuantities();
                    }, 100); // รอ 100ms เพื่อให้ adapter update เสร็จก่อน
                });
            } else {
                // ถ้าไม่มี Activity ให้ update ทันที
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                updateTotalQuantities();
            }

            // Show success sound/notification
            Utils.playSound(1);

            // *** เพิ่มการแสดงข้อความสำเร็จ ***
            String successMsg = "Scanned: " + item.getProductNumber() + " (" + item.getQuantityScan() + "/" + item.getQuantity() + ")";
            //showToastOnUiThread(successMsg);

        } else {
            String msg = qrCode + " สแกนไปแล้ว";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }

        return true;
    }

    private void manualRefreshTotals() {
        Log.d(TAG, "Manual refresh totals called");

        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                Log.d(TAG, "Current items count: " + items.size());

                double totalScan = 0;
                for (PickingOrderItem item : items) {
                    Log.d(TAG, "Item: " + item.getProductNumber() + ", Scan: " + item.getQuantityScan());
                    totalScan += item.getQuantityScan();
                }

                Log.d(TAG, "Calculated total scan: " + totalScan);
            } else {
                Log.d(TAG, "No items from adapter");
            }
        } else {
            Log.d(TAG, "Adapter is null");
        }

        updateTotalQuantities();
    }

    // *** เพิ่ม method สำหรับ validate ข้อมูลก่อนเพิ่มเข้า stockList ***
    private boolean validatePickingOrderItem(PickingOrderItem item) {
        if (item == null) {
            Log.w(TAG, "PickingOrderItem is null");
            return false;
        }

        if (item.getTagId() == null || item.getTagId().trim().isEmpty()) {
            Log.w(TAG, "TagId is null or empty for product: " + item.getProductNumber());
            return false;
        }

        if (item.getProductId() <= 0) {
            Log.w(TAG, "Invalid ProductId: " + item.getProductId());
            return false;
        }

        if (item.getProductNumber() == null || item.getProductNumber().trim().isEmpty()) {
            Log.w(TAG, "ProductNumber is null or empty for TagId: " + item.getTagId());
            return false;
        }

        return true;
    }

    private void showPalletStatistics() {
        if (scannedPallets.isEmpty()) {
            Toast.makeText(mContext, "No pallets scanned yet", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== Pallet Statistics ===\n\n");

        int totalItems = 0;
        int totalScanned = 0;
        int totalSuccess = 0;
        Set<String> allDOs = new HashSet<>();

        for (String palletId : scannedPallets) {
            List<PickingOrderItem> palletItems = palletItemsMap.get(palletId);
            if (palletItems != null) {
                int palletItemCount = palletItems.size();
                totalItems += palletItemCount;

                stats.append("Pallet: ").append(palletId).append("\n");
                stats.append("Items: ").append(palletItemCount).append("\n");

                // Get picking orders for this pallet
                Set<String> palletOrders = new HashSet<>();
                for (PickingOrderItem item : palletItems) {
                    if (item.getPickingOrder() != null) {
                        String[] doArray = item.getPickingOrder().split(",");
                        for (String doNumber : doArray) {
                            String trimmedDO = doNumber.trim();
                            palletOrders.add(trimmedDO);
                            allDOs.add(trimmedDO);
                        }
                    }
                }
                stats.append("DOs: ").append(String.join(", ", palletOrders)).append("\n");
                stats.append("DO Count: ").append(palletOrders.size()).append("\n\n");
            }
        }

        // Calculate totals from current adapter items
        List<PickingOrderItem> allItems = adapter.getFilteredItems();
        if (allItems != null) {
            for (PickingOrderItem item : allItems) {
                totalScanned += item.getQuantityScan();
                totalSuccess += item.getQuantitySuccess();
            }
        }

        stats.append("=== Totals ===\n");
        stats.append("Total Pallets: ").append(scannedPallets.size()).append("\n");
        stats.append("Total DOs: ").append(allDOs.size()).append("\n");
        stats.append("Total Items: ").append(totalItems).append("\n");
        stats.append("Total Scanned: ").append(totalScanned).append("\n");
        stats.append("Total Success: ").append(totalSuccess).append("\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Pallet Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("Show DO Details", (dialog, which) -> {
                    showDOStatistics();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // Helper method to show Toast on UI thread
    private void showToastOnUiThread(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    // *** เพิ่ม method สำหรับแสดง pending tags (สำหรับ debug) ***
    private void showPendingTags() {
        if (pendingProductTags.isEmpty()) {
            Toast.makeText(mContext, "No pending tags", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Pending Product Tags\n");
        info.append("===================\n");
        info.append("Total: ").append(pendingProductTags.size()).append("\n\n");

        for (int i = 0; i < pendingProductTags.size(); i++) {
            info.append((i + 1)).append(". ").append(pendingProductTags.get(i)).append("\n");
        }

        info.append("\nThese tags will be processed automatically when a pallet is scanned.");

        new AlertDialog.Builder(requireContext())
                .setTitle("Pending Tags")
                .setMessage(info.toString())
                .setPositiveButton("Clear Pending", (dialog, which) -> {
                    pendingProductTags.clear();
                    Toast.makeText(mContext, "Pending tags cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void clearData() {
        Log.d(TAG, "Clearing scan data only");

        // Reset scan quantities for all items
        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                for (PickingOrderItem item : items) {
                    Log.d(TAG, "Clearing scan for: " + item.getProductNumber() +
                            " (was: " + item.getQuantityScan() + ")");
                    item.setQuantityScan(0);
                }
            }
            adapter.notifyDataSetChanged();
        }

        // Clear scanned tags list
        scannedTags.clear();

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

        btnConfirm.setEnabled(false);

        // Force update totals
        updateTotalQuantities();

        Log.d(TAG, "Clear scan data completed (kept pallet data and pending tags)");
    }

    // *** เพิ่ม method สำหรับ debug totals (เรียกใช้ใน long click) ***
    private void debugTotals() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== Debug Totals ===\n\n");

        // Check itemList
        debug.append("ItemList count: ").append(itemList != null ? itemList.size() : 0).append("\n");
        if (itemList != null) {
            double itemListScan = 0;
            for (PickingOrderItem item : itemList) {
                itemListScan += item.getQuantityScan();
            }
            debug.append("ItemList total scan: ").append(itemListScan).append("\n\n");
        }

        // Check adapter items
        if (adapter != null) {
            List<PickingOrderItem> adapterItems = adapter.getFilteredItems();
            debug.append("Adapter items count: ").append(adapterItems != null ? adapterItems.size() : 0).append("\n");

            if (adapterItems != null) {
                double adapterScan = 0;
                for (PickingOrderItem item : adapterItems) {
                    adapterScan += item.getQuantityScan();
                    debug.append("  - ").append(item.getProductNumber())
                            .append(": scan=").append(item.getQuantityScan())
                            .append(", qty=").append(item.getQuantity()).append("\n");
                }
                debug.append("Adapter total scan: ").append(adapterScan).append("\n\n");
            }
        } else {
            debug.append("Adapter is null\n\n");
        }

        // Check scanned tags
        debug.append("Scanned tags count: ").append(scannedTags.size()).append("\n");
        for (PickingOrderItem tag : scannedTags) {
            debug.append("  - Tag: ").append(tag.getTagId()).append(", Product: ").append(tag.getProductId()).append("\n");
        }

        // Current totals display
        debug.append("\nCurrent UI values:\n");
        debug.append("Total Scan: ").append(tvTotalScan != null ? tvTotalScan.getText() : "null").append("\n");
        debug.append("Total Qty: ").append(tvTotalQty != null ? tvTotalQty.getText() : "null").append("\n");
        debug.append("Total Success: ").append(tvTotalSuccess != null ? tvTotalSuccess.getText() : "null").append("\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Debug Totals")
                .setMessage(debug.toString())
                .setPositiveButton("Refresh Now", (dialog, which) -> {
                    manualRefreshTotals();
                })
                .setNegativeButton("Close", null)
                .show();
    }


    private void showScannedPallets() {
        if (scannedPallets.isEmpty()) {
            Toast.makeText(mContext, "No pallets scanned yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Scanned Pallets (" + scannedPallets.size() + ")");

        String[] palletArray = scannedPallets.toArray(new String[0]);
        builder.setItems(palletArray, (dialog, which) -> {
            String selectedPallet = palletArray[which];
            List<PickingOrderItem> palletItems = palletItemsMap.get(selectedPallet);
            if (palletItems != null) {
                String msg = "Pallet: " + selectedPallet + "\nItems: " + palletItems.size();
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Close", null);
        builder.setNeutralButton("Clear All", (dialog, which) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Clear")
                    .setMessage("Are you sure you want to clear all scanned pallets?")
                    .setPositiveButton("Yes", (d, w) -> clearAllData())
                    .setNegativeButton("No", null)
                    .show();
        });

        builder.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void confirmPickingOrderTransaction() {
        if (scannedTags.isEmpty()) {
            Toast.makeText(mContext, "No scanned items to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group scanned tags by pallet
        Map<String, List<PickingOrderItem>> palletTransactions = new HashMap<>();
        for (PickingOrderItem tag : scannedTags) {
            String palletId = tag.getPalletTagId();
            if (palletId != null) {
                palletTransactions.computeIfAbsent(palletId, k -> new ArrayList<>()).add(tag);
            }
        }

        // Show confirmation dialog with details
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("Confirm transactions for:\n\n");

        for (Map.Entry<String, List<PickingOrderItem>> entry : palletTransactions.entrySet()) {
            confirmMsg.append("Pallet: ").append(entry.getKey()).append("\n");
            confirmMsg.append("Items: ").append(entry.getValue().size()).append("\n\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Transactions")
                .setMessage(confirmMsg.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    performConfirmTransaction();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 9. แยก method สำหรับ perform transaction
    private void performConfirmTransaction() {
        // You may need to adjust this based on your API requirements
        // If API supports batch transactions for multiple pallets

        Call<List<PickingOrderItem>> call = apiService.createReceivePallet(scannedTags);

        call.enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                if (response.isSuccessful()) {
                    // Update success quantities
                    updateSuccessQuantities();

                    // Clear scanned tags
                    scannedTags.clear();

                    // Reset scan quantities
                    resetScanQuantities();

                    Toast.makeText(mContext, "Transactions confirmed successfully", Toast.LENGTH_LONG).show();
                    btnConfirm.setEnabled(false);

                } else {
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                Toast.makeText(mContext, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 10. Helper methods
    private void updateSuccessQuantities() {
        List<PickingOrderItem> items = adapter.getFilteredItems();
        if (items != null) {
            for (PickingOrderItem item : items) {
                // Move scanned quantity to success quantity
                item.setQuantitySuccess(item.getQuantitySuccess() + item.getQuantityScan());
                item.setQuantityScan(0);
            }
            adapter.notifyDataSetChanged();
            updateTotalQuantities();
        }
    }

    private void resetScanQuantities() {
        List<PickingOrderItem> items = adapter.getFilteredItems();
        if (items != null) {
            for (PickingOrderItem item : items) {
                item.setQuantityScan(0);
            }
            adapter.notifyDataSetChanged();
            updateTotalQuantities();
        }
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
                    btScanRFID.setEnabled(true);
                    //btInventory.setEnabled(true);
                    //btInventoryPerMinute.setEnabled(true);
                }

                //cbFilter.setEnabled(true);
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                stop();
                btClear.setEnabled(true);
                btStop.setEnabled(false);
                btScanRFID.setEnabled(false);

            }
        }
    }

    // *** เพิ่ม helper class สำหรับสถิติ DO ***
    private static class DOStatistics {
        int itemCount = 0;
        double totalQty = 0;
        double totalScan = 0;
        double totalSuccess = 0;
        Set<String> pallets = new HashSet<>();
        Set<String> customers = new HashSet<>();
    }

    private Map<String, ScanData> scanBackup = new HashMap<>();

    private static class ScanData {
        double quantityScan = 0;
        double quantitySuccess = 0;

        ScanData(double scan, double success) {
            this.quantityScan = scan;
            this.quantitySuccess = success;
        }
    }

    // *** เพิ่ม method สำหรับสร้าง unique key สำหรับ item ***
    private String createItemKey(PickingOrderItem item) {
        return String.format("%s_%d_%s",
                item.getPickingOrder() != null ? item.getPickingOrder() : "NULL",
                item.getProductId(),
                item.getBoxType() != null ? item.getBoxType() : "NULL");
    }

    // *** เพิ่ม method สำหรับ backup scan quantities ***
    private void backupScanQuantities() {
        if (adapter == null) return;

        List<PickingOrderItem> currentItems = adapter.getFilteredItems();
        if (currentItems != null) {
            for (PickingOrderItem item : currentItems) {
                if (item.getQuantityScan() > 0 || item.getQuantitySuccess() > 0) {
                    String key = createItemKey(item);
                    scanBackup.put(key, new ScanData(item.getQuantityScan(), item.getQuantitySuccess()));
                    Log.d(TAG, "Backed up scan data for key: " + key +
                            ", Scan: " + item.getQuantityScan() +
                            ", Success: " + item.getQuantitySuccess());
                }
            }
        }
    }

    // *** เพิ่ม method สำหรับ restore scan quantities ***
    private void restoreScanQuantities(List<PickingOrderItem> items) {
        if (items == null || scanBackup.isEmpty()) return;

        for (PickingOrderItem item : items) {
            String key = createItemKey(item);
            ScanData scanData = scanBackup.get(key);

            if (scanData != null) {
                item.setQuantityScan(scanData.quantityScan);
                item.setQuantitySuccess(scanData.quantitySuccess);
                Log.d(TAG, "Restored scan data for key: " + key +
                        ", Scan: " + scanData.quantityScan +
                        ", Success: " + scanData.quantitySuccess);
            }
        }
    }

}