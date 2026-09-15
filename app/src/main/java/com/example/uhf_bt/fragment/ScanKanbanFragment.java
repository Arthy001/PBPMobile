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
import com.example.uhf_bt.models.Kanban;
import com.example.uhf_bt.models.PalletStatus;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.TagDto;
import com.example.uhf_bt.models.VehicleDto;
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

public class ScanKanbanFragment extends Fragment {

    private static final String TAG = "ScanKanbanFragment";

    private List<String> pendingKanbanTags = new ArrayList<>();
    private boolean isTargetScanned = false;
    private EditText EtData_Read;
    private EditText tvTargetInfo;
    private Button BtRead, BtRead2;
    private LinearLayout viewAction;
    private TextView tvTotalKanban, tvTotalScan, tvTotalSuccess;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;

    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private ReceivePalletAdapter adapter;
    private List<PickingOrderItem> itemList = new ArrayList<>();
    private List<PickingOrderItem> tempList = new ArrayList<>();
    private List<ProductItem> stockList = new ArrayList<>();
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private List<VehicleDto> stockTagVehicles = new ArrayList<>();
    private String targetType = "VEHICLE"; // Can be "VEHICLE" or "LOCATION"
    private String targetId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Button btScanRFID, btStop, btClear, btnConfirm;

    private Set<String> scannedKanbans = new HashSet<>();
    private Map<String, List<PickingOrderItem>> kanbanItemsMap = new HashMap<>();

    private TextView tvTotalTarget, tvTotalKanbanCount;

    // RFID Variables
    private EditText etTime;
    private TextView tv_count, tv_time, tv_total;
    private int maxRunTime = 99999999;
    private final int FLAG_START = 0;
    private final int FLAG_STOP = 1;
    private final int FLAG_UPDATE_TIME = 2;
    private final int FLAG_UHFINFO = 3;
    private final int FLAG_UHFINFO_LIST = 5;
    private final int FLAG_SUCCESS = 10;
    private final int FLAG_FAIL = 11;
    private final int FLAG_TIME_OVER = 12;
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
                    Log.i(TAG, "FLAG_TIME_OVER =" + (System.currentTimeMillis() - mStrTime));
                    float useTime2 = (System.currentTimeMillis() - mStrTime) / 1000.0F;
                    stop();
                    break;
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        btScanRFID.setEnabled(true);
                        btnConfirm.setEnabled(true);
                    } else {
                        Utils.playSound(2);
                        Toast.makeText(mContext, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FLAG_UHFINFO_LIST:
                    List<UHFTAGInfo> list = (List<UHFTAGInfo>) msg.obj;
                    addEPCToList(list);
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        btScanRFID.setEnabled(false);
                    } else {
                        Utils.playSound(2);
                    }
                    break;
                case FLAG_UPDATE_TIME:
                    if (mContext.isScanning) {
                        float useTime = (System.currentTimeMillis() - mStrTime) / 1000.0F;
                        handler.sendEmptyMessageDelayed(FLAG_UPDATE_TIME, 10);
                    } else {
                        handler.removeMessages(FLAG_UPDATE_TIME);
                    }
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    addEPCToList(info);
                    break;
            }
        }
    };

    private void addEPCToList(List<UHFTAGInfo> list) {
        for (int k = 0; k < list.size(); k++) {
            addEPCToList(list.get(k));
        }
    }

    private void addEPCToList(UHFTAGInfo uhftagInfo) {
        boolean[] exists = new boolean[1];
        int idx = CheckUtils.getInsertIndex(tempDatas, uhftagInfo, exists);
        insertTag(uhftagInfo, idx, exists[0]);
    }

    void insertTag(UHFTAGInfo info, int index, boolean exists) {
        String data = info.getEPC();
        if (!TextUtils.isEmpty(info.getTid())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("EPC:");
            stringBuilder.append(info.getEPC());
            stringBuilder.append("\n");
            stringBuilder.append("TID:");
            stringBuilder.append(info.getTid());
            if (!TextUtils.isEmpty(info.getUser())) {
                stringBuilder.append("\n");
                stringBuilder.append("USER:");
                stringBuilder.append(info.getUser());
            }
            data = stringBuilder.toString();
        }

        HashMap<String, String> tagMap = null;
        if (exists) {
            tagMap = tagList.get(index);
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(Integer.parseInt(tagMap.get(MainActivity.TAG_COUNT), 10) + 1));
        } else {
            tagMap = new HashMap<>();
            tagMap.put(MainActivity.TAG_EPC, info.getEPC());
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(1));
            tempDatas.add(index, info);
            tagList.add(index, tagMap);

            String xx_tag = data;
            processQrCode(xx_tag);
        }

        tagMap.put(MainActivity.TAG_USER, info.getUser());
        tagMap.put(MainActivity.TAG_DATA, data);
        tagMap.put(MainActivity.TAG_TID, info.getTid());
        tagMap.put(MainActivity.TAG_RSSI, info.getRssi() == null ? "" : info.getRssi());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public static ScanKanbanFragment newInstance(String targetType, String targetId) {
        ScanKanbanFragment fragment = new ScanKanbanFragment();
        Bundle args = new Bundle();
        args.putString("targetType", targetType);
        args.putString("targetId", targetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetType = getArguments().getString("targetType", "VEHICLE");
            targetId = getArguments().getString("targetId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();

        pendingKanbanTags = new ArrayList<>();
        isTargetScanned = false;

        loadVehicles();

        Log.d(TAG, "Fragment created for target type: " + targetType + ", ID: " + targetId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_kanban, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();

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

        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize total quantity TextViews
        tvTotalKanban = view.findViewById(R.id.tvTotalKanban);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);

        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_time);
        tv_total = view.findViewById(R.id.tv_total);
        tvTargetInfo = view.findViewById(R.id.tvTargetInfo);

        EtData_Read = view.findViewById(R.id.EtData_Read);
        BtRead = view.findViewById(R.id.btnRead);
        BtRead2 = view.findViewById(R.id.btnRead2);
        btScanRFID = view.findViewById(R.id.btnScanRFID);
        btStop = view.findViewById(R.id.btnStop);
        btClear = view.findViewById(R.id.btnClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTotalTarget = view.findViewById(R.id.tvTotalTarget);
        tvTotalKanbanCount = view.findViewById(R.id.tvTotalKanbanCount);
    }

    private void setupToolbar() {
        toolbar_title.setText("สแกน Kanban - " + targetType);

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
            showTargetStatistics();
            return true;
        });

        // Search view long click for options
        searchView.setOnLongClickListener(v -> {
            String[] searchOptions = {
                    "Filter by Status",
                    "Show Summary",
                    "Show Statistics",
                    "Toggle Grouping Mode"
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("Scan Options")
                    .setItems(searchOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                filterByStatus();
                                break;
                            case 1:
                                showSummary();
                                break;
                            case 2:
                                showStatistics();
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

    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Scan Notifications");

        if (notificationMessages.isEmpty()) {
            builder.setMessage("No notifications available");
        } else {
            String[] numberedNotifications = new String[notificationMessages.size()];
            for (int i = 0; i < notificationMessages.size(); i++) {
                numberedNotifications[i] = (i + 1) + ". " + notificationMessages.get(i);
            }

            builder.setItems(numberedNotifications, (dialog, which) -> {
                Toast.makeText(mContext, "Selected: " + notificationMessages.get(which), Toast.LENGTH_SHORT).show();
            });

            builder.setNeutralButton("Mark All as Read", (dialog, which) -> {
                notificationMessages.clear();
                hasNotifications = false;
                updateNotificationVisibility();
                Toast.makeText(mContext, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            });
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void updateTotalQuantities() {
        double totalKanban = 0;
        double totalScan = 0;
        double totalSuccess = 0;

        List<PickingOrderItem> currentItems = null;
        if (adapter != null) {
            currentItems = adapter.getFilteredItems();
        }

        if (currentItems == null || currentItems.isEmpty()) {
            currentItems = itemList;
        }

        if (currentItems != null) {
            for (PickingOrderItem item : currentItems) {
                totalKanban += item.getQuantity();
                totalScan += item.getQuantityScan();
                totalSuccess += item.getQuantitySuccess();
            }
        }

        // Update UI with formatted values
        if (tvTotalKanban != null && tvTotalScan != null && tvTotalSuccess != null) {
            tvTotalKanban.setText(String.format("%.0f", totalKanban));
            tvTotalScan.setText(String.format("%.0f", totalScan));
            tvTotalSuccess.setText(String.format("%.0f", totalSuccess));

            Log.d(TAG, String.format("Updated totals - Kanban: %.0f, Scan: %.0f, Success: %.0f",
                    totalKanban, totalScan, totalSuccess));
        }
    }

    @SuppressLint("NewApi")
    private void setupButtonListeners() {
        BtRead.setOnClickListener(v -> read());
        BtRead2.setOnClickListener(v -> read2());
        btScanRFID.setOnClickListener(v -> scanRFID());
        btStop.setOnClickListener(v -> stop());
        btClear.setOnClickListener(v -> clearRFID());
        btnConfirm.setOnClickListener(v -> confirmKanbanTransaction());

        // Long Click for scanned kanbans
        btClear.setOnLongClickListener(v -> {
            showScannedKanbans();
            return true;
        });

        // Long Click for Clear All Data
        btnConfirm.setOnLongClickListener(v -> {
            if (!scannedKanbans.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Clear All Data")
                        .setMessage("Do you want to clear all scanned kanbans and data?")
                        .setPositiveButton("Yes", (dialog, which) -> clearAllData())
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });

        // Key event callback for RFID scanner
        // Complete the Key event callback for RFID scanner (from setupButtonListeners)
        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                Log.d(TAG, "keycode =" + keycode + " ,isExit=" + false);
                if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                    if (keycode == 3) {
                        mContext.isKeyDownUP = true;
                        read();
                        //startThread();
                    } else {
                        if (!mContext.isKeyDownUP) {
                            if (keycode == 1) {
                                if (mContext.isScanning) {
                                    stop();
                                } else {
                                    //startThread();
                                    read();
                                }
                            }
                        }
                        if (keycode == 2) {
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
                Log.d(TAG, "keycode =" + keycode);
                if (keycode == 4) {
                    //stop();
                }
            }
        });
    }

    private void toggleGroupingMode() {
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(mContext, "No items to regroup", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Grouping Mode");

        String[] options = {
                "Separate by Status",
                "Combine all Status"
        };

        builder.setItems(options, (dialog, which) -> {
            boolean separateByStatus = (which == 0);
            List<PickingOrderItem> regroupedData = groupItems(itemList, separateByStatus);
            updateUI(regroupedData);

            String message = separateByStatus ?
                    "Grouped separately by Status" :
                    "Combined all Status together";
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
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

        searchView.setQueryHint("Search kanban...");
    }

    private void setupListView() {
        adapter = new ReceivePalletAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrderItem item = adapter.getItem(position);
            handleKanbanItemClick(item);
        });
    }

    private void scanRFID() {
        startThread();
    }

    private void loadAvailableKanbans() {
        showLoading(true);

        // Load available kanbans based on target type
        Call<List<Kanban>> call;
        if ("VEHICLE".equals(targetType)) {
            call = apiService.getAvailableKanbansForVehicle(targetId);
        } else {
            call = apiService.getAvailableKanbansForLocation(targetId);
        }

        call.enqueue(new Callback<List<Kanban>>() {
            @Override
            public void onResponse(Call<List<Kanban>> call, Response<List<Kanban>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Kanban> availableKanbans = response.body();

                    Log.d(TAG, "Available kanbans loaded: " + availableKanbans.size());

                    if (availableKanbans.isEmpty()) {
                        String msg = "No kanbans available for " + targetType + ": " + targetId;
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Convert kanbans to picking order items
                    List<PickingOrderItem> kanbanItems = convertKanbansToPickingItems(availableKanbans);
                    addKanbansToStockList(kanbanItems);

                    isTargetScanned = true;
                    itemList = new ArrayList<>(kanbanItems);

                    List<PickingOrderItem> dataList = groupItems(kanbanItems, true);
                    updateTempListFromAllItems(kanbanItems);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateUI(dataList);
                            updateTotalCounts();
                            updateTotalQuantities();

                            String successMsg = "Loaded " + kanbanItems.size() + " available kanbans";
                            Toast.makeText(mContext, successMsg, Toast.LENGTH_SHORT).show();

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                processPendingKanbanTags();
                            }, 500);
                        });
                    }

                } else {
                    Log.e(TAG, "API Error - Code: " + response.code());
                    showError("Failed to load available kanbans. Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Kanban>> call, Throwable t) {
                Log.e(TAG, "Network error loading available kanbans", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void loadVehicles() {
        showLoading(true);

        Log.d(TAG, "Loading Vehicles");

        apiService.getVehicles().enqueue(new Callback<List<VehicleDto>>() {
            @Override
            public void onResponse(Call<List<VehicleDto>> call, Response<List<VehicleDto>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    stockTagVehicles = response.body();
                    Log.d(TAG, "Successfully loaded " + stockTagVehicles.size() + " vehicles");
                } else {
                    Log.e(TAG, "Error loading vehicles: " + response.code() + " - " + response.message());
                    showError("Failed to load vehicles");
                }
            }

            @Override
            public void onFailure(Call<List<VehicleDto>> call, Throwable t) {
                Log.e(TAG, "Network error loading vehicles", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<PickingOrderItem> convertKanbansToPickingItems(List<Kanban> kanbans) {
        List<PickingOrderItem> items = new ArrayList<>();

        for (Kanban kanban : kanbans) {
            PickingOrderItem item = new PickingOrderItem();

            item.setId((int) kanban.getId());
            item.setPickingNumber(kanban.getKanbanNo());
            item.setProductName(kanban.getKanbanName());
            item.setTagNumber(kanban.getKanbanTagId());
            item.setQuantity(1);
            item.setQuantityScan(0);
            item.setQuantitySuccess(0);
            item.setBoxType("KANBAN");
            item.setProductNumber(kanban.getKanbanNo());
            item.setPalletStatus(kanban.getKanbanStatus());

            items.add(item);
        }

        return items;
    }

    private void addKanbansToStockList(List<PickingOrderItem> kanbanItems) {
        if (stockList == null) {
            stockList = new ArrayList<>();
        }

        for (PickingOrderItem kanbanItem : kanbanItems) {
            ProductItem productItem = new ProductItem();
            productItem.setProductId(kanbanItem.getId());
            productItem.setProductNumber(kanbanItem.getProductNumber());
            productItem.setProductName(kanbanItem.getProductName());
            productItem.setTagNumber(kanbanItem.getTagNumber());
            productItem.setItemType("KANBAN");
            productItem.setQuantityInBox(1);
            productItem.setProductItemStatus(1);

            stockList.add(productItem);
        }

        Log.d(TAG, "Stock list updated with " + stockList.size() + " kanbans");
    }

    private void processPendingKanbanTags() {
        if (pendingKanbanTags.isEmpty()) {
            return;
        }

        Log.d(TAG, "Processing " + pendingKanbanTags.size() + " pending kanban tags");

        List<String> tagsToProcess = new ArrayList<>(pendingKanbanTags);
        pendingKanbanTags.clear();

        for (String pendingTag : tagsToProcess) {
            Log.d(TAG, "Re-processing pending tag: " + pendingTag);
            processKanbanTag(pendingTag);
        }
    }

    private void processQrCode(String qrCode) {
        try {
            if (qrCode == null || qrCode.length() < 11) {
                String msg = qrCode + " Invalid format";
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
                return;
            }

            String chkTagId = qrCode.substring(0, 1);

            if (chkTagId.contains("B")) {
                // Kanban Tag
                processKanbanTag(qrCode);
            } else if (chkTagId.contains("D")) {
                // Vehicle Tag
                processVehicleTag(qrCode);
            } else if (chkTagId.contains("L")) {
                // Location Tag
                processLocationTag(qrCode);
            } else {
                String msg = "Unknown tag type: " + qrCode;
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
            }

        } catch (Exception e) {
            String msg = "Error processing QR code: " + e.getMessage();
            Log.e(TAG, msg);
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    private void processKanbanTag(String qrCode) {
        if (!isTargetScanned || stockList == null || stockList.isEmpty()) {
            Log.d(TAG, "Target not loaded yet, adding to pending list: " + qrCode);
            String msg = "กรุณาโหลดข้อมูลก่อน - จะจำไว้ให้: " + qrCode;
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();

            if (!pendingKanbanTags.contains(qrCode)) {
                pendingKanbanTags.add(qrCode);
            }
            return;
        }

        ProductItem foundItem = findKanbanByTagId(qrCode);

        if (foundItem == null) {
            Log.d(TAG, "Kanban not found: " + qrCode);
            String msg = qrCode + " ไม่มีอยู่ในรายการ Kanban ที่พร้อมสแกน";
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
            if (item != null && item.getTagNumber() != null &&
                    item.getTagNumber().equals(foundItem.getTagNumber())) {
                found = true;
                if (processKanbanScan(item, qrCode, foundItem)) {
                    break;
                }
            }
        }

        if (!found) {
            String msg = qrCode + " ไม่ตรงกับ Kanban ที่ต้องสแกน";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    private void processVehicleTag(String qrCode) {
        VehicleDto foundVehicle = findVehicleByTag(qrCode);
        if (foundVehicle != null) {
            showVehicleInfoDialog(foundVehicle);
        } else {
            Log.d(TAG, "Vehicle not found in stored list: " + qrCode);
            if ("VEHICLE".equals(targetType)) {
                // Original logic as fallback
                targetId = qrCode;
                tvTargetInfo.setText("Vehicle: " + qrCode);
                loadAvailableKanbans();
            } else {
                String msg = "Vehicle tag scanned but expecting " + targetType;
                notificationMessages.add(msg);
                hasNotifications = true;
                updateNotificationVisibility();
            }
        }
    }

    private VehicleDto findVehicleByTag(String tag) {
        if (stockTagVehicles == null || stockTagVehicles.isEmpty() || tag == null) {
            return null;
        }
        for (VehicleDto vehicle : stockTagVehicles) {
            if (tag.equals(vehicle.getVehicleTag())) {
                return vehicle;
            }
        }
        return null;
    }

    private void showVehicleInfoDialog(VehicleDto vehicle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Vehicle Information");

        StringBuilder info = new StringBuilder();
        info.append("License Plate: ").append(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "N/A").append("\n");
        info.append("Brand: ").append(vehicle.getVehicleBrand() != null ? vehicle.getVehicleBrand() : "N/A").append("\n");
        info.append("Driver: ").append(vehicle.getDriver() != null ? vehicle.getDriver() : "N/A").append("\n");
        info.append("Status: ").append(vehicle.getVehicleStatus()).append("\n");
        info.append("Tag: ").append(vehicle.getVehicleTag());

        builder.setMessage(info.toString());

        builder.setPositiveButton("Use as Target", (dialog, which) -> {
            targetId = vehicle.getVehicleTag();
            tvTargetInfo.setText("Vehicle: " + targetId);
            loadAvailableKanbans();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void processLocationTag(String qrCode) {
        if ("LOCATION".equals(targetType)) {
            // Set target location and load its kanbans
            targetId = qrCode;
            tvTargetInfo.setText("Location: " + qrCode);
            loadAvailableKanbans();
        } else {
            String msg = "Location tag scanned but expecting " + targetType;
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }
    }

    private ProductItem findKanbanByTagId(String tagId) {
        if (stockList == null || stockList.isEmpty() || tagId == null) {
            return null;
        }

        for (ProductItem item : stockList) {
            if (item.getTagNumber() != null && item.getTagNumber().equals(tagId)) {
                return item;
            }
        }

        return null;
    }

    private boolean processKanbanScan(PickingOrderItem item, String qrCode, ProductItem foundItem) {
        // Check if already scanned
        boolean tagAlreadyScanned = false;
        for (PickingOrderItem scannedItem : scannedTags) {
            if (scannedItem.getTagNumber().equals(qrCode)) {
                tagAlreadyScanned = true;
                break;
            }
        }

        if (!tagAlreadyScanned) {
            item.setQuantityScan(item.getQuantityScan() + 1);

            PickingOrderItem newScannedItem = new PickingOrderItem();
            newScannedItem.setProductId(item.getProductId());
            newScannedItem.setQuantity(1);
            newScannedItem.setTagNumber(qrCode.trim());
            newScannedItem.setId(item.getId());
            newScannedItem.setItemType("KANBAN");
            scannedTags.add(newScannedItem);

            scannedKanbans.add(qrCode);
            btnConfirm.setEnabled(true);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        updateTotalQuantities();
                    }, 100);
                });
            }

            Utils.playSound(1);

            String successMsg = "Scanned: " + item.getProductName();
            Toast.makeText(mContext, successMsg, Toast.LENGTH_SHORT).show();

        } else {
            String msg = qrCode + " สแกนไปแล้ว";
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();
        }

        return true;
    }

    private List<PickingOrderItem> groupItems(List<PickingOrderItem> allItems, boolean separateByStatus) {
        return groupItemsByStatus(allItems, separateByStatus);
    }

    private List<PickingOrderItem> groupItemsByStatus(List<PickingOrderItem> allItems, boolean separateByStatus) {
        if (!separateByStatus) {
            return new ArrayList<>(allItems);
        }

        List<PickingOrderItem> dataList = new ArrayList<>();

        for (PickingOrderItem trans : allItems) {
            boolean found = false;
            PickingOrderItem existingItem = null;

            for (PickingOrderItem data : dataList) {
                if (data.getPalletStatus() == trans.getPalletStatus() &&
                        data.getBoxType() != null && data.getBoxType().equals(trans.getBoxType())) {
                    found = true;
                    existingItem = data;
                    break;
                }
            }

            if (found && existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + trans.getQuantity());
                existingItem.setQuantityScan(existingItem.getQuantityScan() + trans.getQuantityScan());
                existingItem.setQuantitySuccess(existingItem.getQuantitySuccess() + trans.getQuantitySuccess());
            } else {
                PickingOrderItem newItem = createNewGroupedItem(trans);
                dataList.add(newItem);
            }
        }

        return dataList;
    }

    private PickingOrderItem createNewGroupedItem(PickingOrderItem trans) {
        PickingOrderItem newItem = new PickingOrderItem();

        newItem.setPickingNumber(trans.getPickingNumber());
        newItem.setId(trans.getId());
        newItem.setProductName(trans.getProductName());
        newItem.setTagNumber(trans.getTagNumber());
        newItem.setQuantity(trans.getQuantity());
        newItem.setQuantityScan(trans.getQuantityScan());
        newItem.setQuantitySuccess(trans.getQuantitySuccess());
        newItem.setBoxType(trans.getBoxType());
        newItem.setPalletStatus(trans.getPalletStatus());
        newItem.setProductNumber(trans.getProductNumber());

        return newItem;
    }

    private void updateTempListFromAllItems(List<PickingOrderItem> allItems) {
        tempList.clear();
        tempList.addAll(allItems);
    }

    private void updateTotalCounts() {
        Set<String> uniqueTargets = new HashSet<>();
        if (targetId != null) {
            uniqueTargets.add(targetId);
        }

        if (tvTotalTarget != null) {
            tvTotalTarget.setText(String.valueOf(uniqueTargets.size()));
        }

        if (tvTotalKanbanCount != null) {
            tvTotalKanbanCount.setText(String.valueOf(scannedKanbans.size()));
        }

        Log.d(TAG, "Updated counts - Targets: " + uniqueTargets.size() +
                ", Kanbans: " + scannedKanbans.size());
    }

    private void filterByStatus() {
        Toast.makeText(mContext, "Status filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showSummary() {
        StringBuilder info = new StringBuilder();
        info.append("Scan Summary\n");
        info.append("============\n");
        info.append("Target Type: ").append(targetType).append("\n");
        info.append("Target ID: ").append(targetId != null ? targetId : "Not set").append("\n");
        info.append("Scanned Kanbans: ").append(scannedKanbans.size()).append("\n");
        info.append("Total Items: ").append(itemList.size()).append("\n\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Scan Summary")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showStatistics() {
        Toast.makeText(mContext, "Statistics - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void clearAllData() {
        scannedKanbans.clear();
        kanbanItemsMap.clear();
        itemList.clear();
        tempList.clear();
        scannedTags.clear();

        if (stockList != null) {
            stockList.clear();
        }

        pendingKanbanTags.clear();
        isTargetScanned = false;

        if (adapter != null) {
            adapter.updateItems(new ArrayList<>());
        }

        updateTotalQuantities();
        updateTotalCounts();
        btnConfirm.setEnabled(false);

        Toast.makeText(mContext, "All scan data cleared", Toast.LENGTH_SHORT).show();
    }

    private void showTargetStatistics() {
        if (scannedKanbans.isEmpty()) {
            Toast.makeText(mContext, "No kanbans scanned yet", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== Target Statistics ===\n\n");
        stats.append("Target Type: ").append(targetType).append("\n");
        stats.append("Target ID: ").append(targetId != null ? targetId : "Not set").append("\n");
        stats.append("Scanned Kanbans: ").append(scannedKanbans.size()).append("\n");
        stats.append("Total Items: ").append(itemList.size()).append("\n\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Target Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showScannedKanbans() {
        if (scannedKanbans.isEmpty()) {
            Toast.makeText(mContext, "No kanbans scanned yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Scanned Kanbans (" + scannedKanbans.size() + ")");

        String[] kanbanArray = scannedKanbans.toArray(new String[0]);
        builder.setItems(kanbanArray, (dialog, which) -> {
            String selectedKanban = kanbanArray[which];
            Toast.makeText(mContext, "Selected Kanban: " + selectedKanban, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void handleKanbanItemClick(PickingOrderItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getProductName())
                .setMessage(String.format("Kanban: %s\nStatus: %s\nTag: %s",
                        item.getPickingNumber(),
                        getStatusName(item.getPalletStatus()),
                        item.getTagNumber()))
                .setPositiveButton("OK", null)
                .show();
    }

    private String getStatusName(int status) {
        return PalletStatus.getDescription(status);
    }

    // RFID Methods
    private void inventory() {
        if (mContext.uhf.getConnectStatus() != ConnectionStatus.CONNECTED) {
            Toast.makeText(mContext, "RFID reader is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        mStrTime = System.currentTimeMillis();
        UHFTAGInfo info = mContext.uhf.inventorySingleTag();
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
        if (mContext.isScanning) {
            stopInventory();
        }
        mContext.isScanning = false;
        cancelInventoryTask();
    }

    public void startThread() {
        if (mContext.isScanning) {
            return;
        }

        String time = etTime.getText().toString();
        if (time != null && time.length() > 0 && time.startsWith(".")) {
            etTime.setText("");
            time = "";
        }
        if (time != null && !time.isEmpty()) {
            maxRunTime = (int) (Float.parseFloat(time) * 1000);
            clearData();
        } else {
            maxRunTime = Integer.parseInt(etTime.getHint().toString()) * 1000;
        }

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

    private void stopInventory() {
        ConnectionStatus connectionStatus = mContext.uhf.getConnectStatus();
        if (connectionStatus != ConnectionStatus.CONNECTED) {
            return;
        }
        boolean result = mContext.uhf.stopInventory();
        Message msg = handler.obtainMessage(FLAG_STOP);
        if (!result && connectionStatus == ConnectionStatus.CONNECTED) {
            msg.arg1 = FLAG_FAIL;
        } else {
            msg.arg1 = FLAG_SUCCESS;
        }
        handler.sendMessage(msg);
    }

    private void cancelInventoryTask() {
        if (mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }

    private void clearRFID() {
        clearData();
    }

    private void clearData() {
        Log.d(TAG, "Clearing scan data only");

        if (adapter != null) {
            List<PickingOrderItem> items = adapter.getFilteredItems();
            if (items != null) {
                for (PickingOrderItem item : items) {
                    item.setQuantityScan(0);
                }
            }
            adapter.notifyDataSetChanged();
        }

        scannedTags.clear();

        if (tempDatas != null) {
            tempDatas.clear();
        }

        if (tagList == null) {
            tagList = new ArrayList<>();
        } else {
            tagList.clear();
        }

        btnConfirm.setEnabled(false);
        updateTotalQuantities();

        Log.d(TAG, "Clear scan data completed");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void confirmKanbanTransaction() {
        if (scannedTags.isEmpty()) {
            Toast.makeText(mContext, "No scanned kanbans to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("Confirm kanban assignment:\n\n");
        confirmMsg.append("Target Type: ").append(targetType).append("\n");
        confirmMsg.append("Target ID: ").append(targetId).append("\n");
        confirmMsg.append("Scanned Kanbans: ").append(scannedKanbans.size()).append("\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Kanban Assignment")
                .setMessage(confirmMsg.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    performConfirmTransaction();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performConfirmTransaction() {
        List<String> kanbanList = new ArrayList<>(scannedKanbans);

        Call<List<ResponseDto>> call;
        if ("VEHICLE".equals(targetType)) {
            call = apiService.assignKanbansToVehicle(targetId, kanbanList);
        } else {
            call = apiService.assignKanbansToLocation(targetId, kanbanList);
        }

        call.enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
                if (response.isSuccessful()) {
                    List<ResponseDto> results = response.body();

                    boolean allSuccess = true;
                    StringBuilder errorMessages = new StringBuilder();

                    if (results != null) {
                        for (ResponseDto result : results) {
                            if (!result.getIsSuccess()) {
                                allSuccess = false;
                                errorMessages.append("• ").append(result.getMessage()).append("\n");
                            }
                        }
                    }

                    if (allSuccess) {
                        updateSuccessQuantities();
                        scannedTags.clear();
                        resetScanQuantities();

                        Toast.makeText(mContext, "Kanban assignment completed successfully", Toast.LENGTH_LONG).show();
                        btnConfirm.setEnabled(false);
                    } else {
                        String errorMsg = "Some assignments failed:\n\n" + errorMessages.toString();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Assignment Errors")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();

                        Toast.makeText(mContext, "Confirm assignment failed", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                Toast.makeText(mContext, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSuccessQuantities() {
        List<PickingOrderItem> items = adapter.getFilteredItems();
        if (items != null) {
            for (PickingOrderItem item : items) {
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

    private void read() {
        String ptrStr = "2";
        if (ptrStr.equals("")) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!TextUtils.isDigitsOnly(ptrStr)) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        String cntStr = "6";
        if (cntStr.equals("")) {
            Toast.makeText(mContext, R.string.uhf_msg_len_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!TextUtils.isDigitsOnly(cntStr)) {
            Toast.makeText(mContext, R.string.uhf_msg_len_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        String pwdStr = "00000000";
        if (!TextUtils.isEmpty(pwdStr)) {
            if (pwdStr.length() != 8) {
                Toast.makeText(mContext, R.string.uhf_msg_addr_must_len8, Toast.LENGTH_SHORT).show();
                return;
            } else if (!Utils.vailHexInput(pwdStr)) {
                Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            pwdStr = "00000000";
        }

        String data = "";
        int Bank = 1;
        boolean cb_filter = false;
        if (cb_filter == true) {
            // Filter logic if needed
        } else {
            data = mContext.uhf.readData(pwdStr,
                    Bank,
                    Integer.parseInt(ptrStr),
                    Integer.parseInt(cntStr));
        }

        tvTargetInfo.setText(data);
        if (data != null && data.length() > 0) {
            String chkTagId = data.substring(0, 1);
            if (chkTagId.contains("B") || chkTagId.contains("V") || chkTagId.contains("L")) {
                Toast.makeText(mContext, R.string.rfid_msg_read_succ, Toast.LENGTH_SHORT).show();
                Utils.playSound(1);

                // Process the scanned tag
                processQrCode(data);
            } else {
                Toast.makeText(mContext, "ไม่ใช่ Tag ที่ถูกต้อง!", Toast.LENGTH_SHORT).show();
                Utils.playSound(2);
            }
        } else {
            Toast.makeText(mContext, R.string.rfid_msg_read_fail, Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        }
    }

    private void read2() {
        if (tvTargetInfo.getText() == null) return;
        String data = String.valueOf(tvTargetInfo.getText());
        if (data != null && data.length() > 0) {
            String chkTagId = data.substring(0, 1);
            if (chkTagId.contains("D")) {
                Toast.makeText(mContext, R.string.rfid_msg_read_succ, Toast.LENGTH_SHORT).show();

                // Process the scanned tag
                processQrCode(data);
            } else {
                Toast.makeText(mContext, "ไม่ใช่ Tag ที่ถูกต้อง!", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(mContext, R.string.rfid_msg_read_fail, Toast.LENGTH_SHORT).show();

        }
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
        Log.d(TAG, "updateUI called with " + items.size() + " items");

        try {
            if (adapter == null) {
                adapter = new ReceivePalletAdapter(requireContext(), items);
                listView.setAdapter(adapter);
            } else {
                adapter.updateItems(items);
            }

            updateTotalQuantities();

            Log.d(TAG, "updateUI completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in updateUI: " + e.getMessage(), e);
            showError("Error updating interface: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNotifications();

        if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            btScanRFID.setEnabled(true);
        } else {
            btScanRFID.setEnabled(false);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;

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
        super.onDetach();
    }

    class ConnectStatus implements MainActivity.IConnectStatus {
        @Override
        public void getStatus(ConnectionStatus connectionStatus) {
            Log.i(TAG, "getStatus connectionStatus=" + connectionStatus);
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                if (!mContext.isScanning) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    btScanRFID.setEnabled(true);
                }
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                stop();
                btClear.setEnabled(true);
                btStop.setEnabled(false);
                btScanRFID.setEnabled(false);
                btnConfirm.setEnabled(true);
            }
        }
    }
}



