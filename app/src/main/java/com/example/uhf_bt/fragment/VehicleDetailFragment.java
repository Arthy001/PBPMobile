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
import androidx.core.content.ContextCompat;
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
import com.example.uhf_bt.models.PalletStatus;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.Vehicle;
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

public class VehicleDetailFragment extends Fragment {

    private static final String TAG = "VehicleDetailFragment";

    private List<String> pendingKanbanTags = new ArrayList<>();
    private boolean isVehicleScanned = false;
    private TextView tvTotalKanban, tvTotalScan, tvTotalSuccess, tvTotalPickingOrder, tvTotalPallet, tvTotalQty;
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
    private String vehicleId, licensePlate, driver, vehicleTag, number, vehicleStatus;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Button btClear, btnConfirm;

    private Set<String> scannedKanbans = new HashSet<>();
    private Map<String, List<PickingOrderItem>> kanbanItemsMap = new HashMap<>();

    private TextView tvTotalVehicle, tvTotalKanbanCount;

    public static VehicleDetailFragment newInstance(String vehicleId,
        String licensePlate, String driver, String vehicleTag, String number,
                                                    String vehicleStatus) {
        VehicleDetailFragment fragment = new VehicleDetailFragment();
        Bundle args = new Bundle();
        args.putString("vehicleId", vehicleId);
        args.putString("licensePlate", licensePlate);
        args.putString("driver", driver);
        args.putString("vehicleTag", vehicleTag);
        args.putString("number", number);
        args.putString("vehicleStatus", vehicleStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel5));
        }

        if (getArguments() != null) {
            vehicleId = getArguments().getString("vehicleId");
            licensePlate = getArguments().getString("licensePlate");
            driver = getArguments().getString("driver");
            vehicleTag = getArguments().getString("vehicleTag");
            number = getArguments().getString("number");
            vehicleStatus = getArguments().getString("vehicleStatus");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();

        // Initialize new variables
        pendingKanbanTags = new ArrayList<>();
        isVehicleScanned = false;

        Log.d(TAG, "Fragment created for vehicle: " + licensePlate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle_detail, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();
        loadVehicleKanbans(vehicleTag);
        if (vehicleStatus.equals("12"))
        {
            btnConfirm.setEnabled(true);
        }
        else
        {
            btnConfirm.setEnabled(false);
        }
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        // Initialize notification views
        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize total quantity TextViews


        btClear = view.findViewById(R.id.btnClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);


        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);
        tvTotalPickingOrder = view.findViewById(R.id.tvTotalPickingOrder);
        tvTotalPallet = view.findViewById(R.id.tvTotalPallet);
        tvTotalKanban = view.findViewById(R.id.tvTotalKanban); // เพิ่มบรรทัดนี้
    }

    private void setupToolbar() {
        // Set vehicle info in title
        toolbar_title.setText(licensePlate + " - " + driver);

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
            showVehicleStatistics();
            return true;
        });

        // Search view long click for vehicle options
        searchView.setOnLongClickListener(v -> {
            String[] searchOptions = {
                    "Filter by Kanban Status",
                    "Show Vehicle Summary",
                    "Show Kanban Statistics",
                    "Toggle Grouping Mode"
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("Vehicle Options")
                    .setItems(searchOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                filterByKanbanStatus();
                                break;
                            case 1:
                                showVehicleSummary();
                                break;
                            case 2:
                                showKanbanStatistics();
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
        builder.setTitle("Vehicle Notifications");

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
        double totalQty = 0;
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
                totalQty += item.getQuantity();
                totalScan += item.getQuantityScan();
                totalSuccess += item.getQuantitySuccess();
            }
        }

        // Update UI with formatted values
        if (tvTotalQty != null) {
            tvTotalQty.setText(String.format("%.0f", totalQty));
        }
        if (tvTotalScan != null) {
            tvTotalScan.setText(String.format("%.0f", totalScan));
        }
        if (tvTotalSuccess != null) {
            tvTotalSuccess.setText(String.format("%.0f", totalSuccess));
        }

        Log.d(TAG, String.format("Updated totals - Qty: %.0f, Scan: %.0f, Success: %.0f",
                totalQty, totalScan, totalSuccess));
    }

    private void setupButtonListeners() {
        btClear.setOnClickListener(v -> checkInVehicle());
        btnConfirm.setOnClickListener(v -> confirmVehicleTransaction());

        // Long Click for scanned kanbans
        btClear.setOnLongClickListener(v -> {
            showScannedKanbans();
            return true;
        });

        // Long Click for Clear All Data
        btnConfirm.setOnLongClickListener(v -> {
            if (!scannedKanbans.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Clear All Vehicle Data")
                        .setMessage("Do you want to clear all scanned kanbans and data?")
                        .setPositiveButton("Yes", (dialog, which) -> clearAllVehicleData())
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
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

    private void loadVehicleKanbans(String vehicleTag) {
        Log.d(TAG, "Loading kanbans for vehicle tag: " + vehicleTag);

        if (vehicleTag == null || vehicleTag.trim().isEmpty()) {
            Log.e(TAG, "VehicleTag is null or empty");
            showError("Invalid Vehicle Tag");
            return;
        }
        if(number == null || number.trim().length() == 0)
        {
            return;
        }
        showLoading(true);

        apiService.getVehicleKanbans(vehicleTag, number).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderItem> kanbanItems = response.body();

                    Log.d(TAG, "API Response successful. Items received: " + kanbanItems.size());

                    if (kanbanItems.isEmpty()) {
                        String msg = "No items found for Vehicle: " + licensePlate;
                        Log.w(TAG, msg);
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Set all items to success automatically
                    for (PickingOrderItem item : kanbanItems) {
                        item.setQuantitySuccess(item.getQuantity());
                        item.setQuantityScan(0);
                    }
                    Log.d(TAG, "Auto set all items to success");

                    // Collect unique kanban IDs from the richer response
                    Set<String> uniqueKanbanIds = new HashSet<>();
                    for (PickingOrderItem item : kanbanItems) {
                        String kanbanId = String.valueOf(item.getKanbanId());
                        uniqueKanbanIds.add(kanbanId);
                        Log.d(TAG, "Found kanban ID: " + kanbanId + " PalletNo: " + item.getPalletNo());
                    }

                    Log.d(TAG, "Total unique kanban IDs found: " + uniqueKanbanIds.size());

                    scannedKanbans.addAll(uniqueKanbanIds);
                    kanbanItemsMap.put(vehicleId, new ArrayList<>(kanbanItems));

                    isVehicleScanned = true;
                    itemList = new ArrayList<>(kanbanItems);

                    List<PickingOrderItem> dataList = groupItems(kanbanItems, true);
                    updateTempListFromAllItems(kanbanItems);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                updateUI(dataList);
                                updateTotalCounts();
                                updateTotalQuantities();

                                //btnConfirm.setEnabled(true);

                                String successMsg = "Loaded " + kanbanItems.size() + " items for " + licensePlate;
                                Toast.makeText(mContext, successMsg, Toast.LENGTH_SHORT).show();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    processPendingKanbanTags();
                                }, 500);

                            } catch (Exception e) {
                                Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                                showError("Error updating UI: " + e.getMessage());
                            }
                        });
                    }

                } else {
                    Log.e(TAG, "API Error - Code: " + response.code() + ", Message: " + response.message());
                    showError("Failed to load data. Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading vehicle kanbans", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
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

    private void processKanbanTag(String kanbanTag) {
        // Process kanban tag scanning logic here
        Log.d(TAG, "Processing kanban tag: " + kanbanTag);

        // Add to pending if vehicle not loaded yet
        if (!isVehicleScanned || itemList == null || itemList.isEmpty()) {
            Log.d(TAG, "Vehicle not loaded yet, adding to pending list: " + kanbanTag);
            String msg = "กรุณาโหลดรถก่อน - จะจำไว้ให้: " + kanbanTag;
            notificationMessages.add(msg);
            hasNotifications = true;
            updateNotificationVisibility();

            if (!pendingKanbanTags.contains(kanbanTag)) {
                pendingKanbanTags.add(kanbanTag);
            }
            return;
        }

        // Process kanban tag logic here
        // Similar to product tag processing in KanbanDetailFragment
    }

    private List<PickingOrderItem> groupItems(List<PickingOrderItem> allItems, boolean separateByStatus) {
        // Implement grouping logic similar to KanbanDetailFragment
        // Group by kanban status if separateByStatus is true
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
                existingItem.setQuantitySuccess(existingItem.getQuantitySuccess() + trans.getQuantitySuccess());
                existingItem.setQuantityScan(existingItem.getQuantityScan() + trans.getQuantityScan());
            } else {
                PickingOrderItem newItem = createNewGroupedItem(trans);
                dataList.add(newItem);
            }
        }

        return dataList;
    }

    private PickingOrderItem createNewGroupedItem(PickingOrderItem trans) {
        PickingOrderItem newItem = new PickingOrderItem();

        // Base fields
        newItem.setId(trans.getId());
        newItem.setPickingOrderItemId(trans.getPickingOrderItemId());
        newItem.setRowGuid(trans.getRowGuid());
        newItem.setCreatedAtUtc(trans.getCreatedAtUtc());

        // Picking Order info
        newItem.setPickingNumber(trans.getPickingNumber());
        newItem.setPickingOrderId(trans.getPickingOrderId());
        newItem.setOrderStatus(trans.getOrderStatus());
        newItem.setCustomer(trans.getCustomer());
        newItem.setOrderDate(trans.getOrderDate());

        // Product info
        newItem.setProductId(trans.getProductId());
        newItem.setProductNumber(trans.getProductNumber());
        newItem.setProductName(trans.getProductName());
        newItem.setBarcode(trans.getBarcode());
        newItem.setSummary(trans.getSummary());

        // Pallet & Kanban info
        newItem.setPalletId(trans.getPalletId());
        newItem.setPalletNo(trans.getPalletNo());
        newItem.setPalletTagNumber(trans.getPalletTagNumber());
        newItem.setPalletName(trans.getPalletName());
        newItem.setPalletNumber(trans.getPalletNumber());
        newItem.setPalletStatus(trans.getPalletStatus());
        newItem.setKanbanId(trans.getKanbanId());
        newItem.setNumber(trans.getNumber());

        // Quantities and pricing
        newItem.setQuantity(trans.getQuantity());
        newItem.setQuantityScan(trans.getQuantityScan());
        newItem.setQuantitySuccess(trans.getQuantitySuccess());
        newItem.setUnitPrice(trans.getUnitPrice());
        newItem.setTotal(trans.getTotal());
        newItem.setQtyBox(trans.getQtyBox());
        newItem.setAllocatedQty(trans.getAllocatedQty());

        // Tag & Location info
        newItem.setTagId(trans.getTagId());
        newItem.setTagNumber(trans.getTagNumber());
        newItem.setLocator(trans.getLocator());
        newItem.setLotSuggestion(trans.getLotSuggestion());
        newItem.setBoxType(trans.getBoxType());
        newItem.setSequenceNumber(trans.getSequenceNumber());
        newItem.setRemark(trans.getRemark());

        return newItem;
    }

    private void updateTempListFromAllItems(List<PickingOrderItem> allItems) {
        tempList.clear();
        tempList.addAll(allItems);
    }

    private void updateTotalCounts() {
        // Count unique vehicles, kanbans, picking orders, and pallets
        Set<String> uniqueVehicles = new HashSet<>();
        uniqueVehicles.add(vehicleId);

        Set<String> uniquePickingOrders = new HashSet<>();
        Set<String> uniquePallets = new HashSet<>();

        if (itemList != null) {
            for (PickingOrderItem item : itemList) {
                if (item.getPickingNumber() != null) {
                    String[] doArray = item.getPickingNumber().split(",");
                    for (String doNumber : doArray) {
                        uniquePickingOrders.add(doNumber.trim());
                    }
                }
                if (item.getPalletNo() != null) {
                    uniquePallets.add(item.getPalletNo());
                } else if (item.getPalletTagNumber() != null) {
                    uniquePallets.add(item.getPalletTagNumber());
                }
            }
        }

        if (tvTotalVehicle != null) {
            tvTotalVehicle.setText(String.valueOf(uniqueVehicles.size()));
        }

        if (tvTotalKanbanCount != null) {
            tvTotalKanbanCount.setText(String.valueOf(scannedKanbans.size()));
        }

        if (tvTotalKanban != null) {
            tvTotalKanban.setText(String.valueOf(scannedKanbans.size()));
        }

        if (tvTotalPickingOrder != null) {
            tvTotalPickingOrder.setText(String.valueOf(uniquePickingOrders.size()));
        }

        if (tvTotalPallet != null) {
            tvTotalPallet.setText(String.valueOf(uniquePallets.size()));
        }

        Log.d(TAG, "Updated counts - Vehicles: " + uniqueVehicles.size() +
                ", Kanbans: " + scannedKanbans.size() +
                ", DOs: " + uniquePickingOrders.size() +
                ", Pallets: " + uniquePallets.size());
    }

    private void filterByKanbanStatus() {
        // Implement kanban status filtering
        Toast.makeText(mContext, "Kanban status filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showVehicleSummary() {
        StringBuilder info = new StringBuilder();
        info.append("Vehicle Summary\n");
        info.append("===============\n");
        info.append("License Plate: ").append(licensePlate).append("\n");
        info.append("Driver: ").append(driver).append("\n");
        info.append("Total Kanbans: ").append(scannedKanbans.size()).append("\n");
        info.append("Total Items: ").append(itemList.size()).append("\n\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Vehicle Summary")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showKanbanStatistics() {
        // Implement kanban statistics
        Toast.makeText(mContext, "Kanban statistics - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void clearAllVehicleData() {
        scannedKanbans.clear();
        kanbanItemsMap.clear();
        itemList.clear();
        tempList.clear();
        scannedTags.clear();

        if (stockList != null) {
            stockList.clear();
        }

        pendingKanbanTags.clear();
        isVehicleScanned = false;

        if (adapter != null) {
            adapter.updateItems(new ArrayList<>());
        }

        updateTotalQuantities();
        updateTotalCounts();
        //btnConfirm.setEnabled(false);

        Toast.makeText(mContext, "All vehicle data cleared", Toast.LENGTH_SHORT).show();
    }

    private void showVehicleStatistics() {
        if (scannedKanbans.isEmpty()) {
            Toast.makeText(mContext, "No kanbans loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== Vehicle Statistics ===\n\n");
        stats.append("Vehicle: ").append(licensePlate).append("\n");
        stats.append("Driver: ").append(driver).append("\n");
        stats.append("Total Kanbans: ").append(scannedKanbans.size()).append("\n");
        stats.append("Total Items: ").append(itemList.size()).append("\n\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Vehicle Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("Show Details", (dialog, which) -> {
                    showKanbanStatistics();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showScannedKanbans() {
        if (scannedKanbans.isEmpty()) {
            Toast.makeText(mContext, "No kanbans loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Loaded Kanbans (" + scannedKanbans.size() + ")");

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

    // Missing methods for VehicleDetailFragment.java
    // Add these methods to complete the file

    private void checkInVehicle() {
        if (vehicleTag == null || vehicleTag.isEmpty()) {
            Toast.makeText(mContext, "Vehicle Tag is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        apiService.checkInVehicle(vehicleTag).enqueue(new Callback<ResponseDto>() {
            @Override
            public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(mContext, "Vehicle Checked In Successfully", Toast.LENGTH_SHORT).show();
                        vehicleStatus = "11"; // Update local status to CheckIn
                        btnConfirm.setEnabled(true);
                        Log.d(TAG, "Check-In successful for tag: " + vehicleTag);
                    } else {
                        Toast.makeText(mContext, "Check-In failed: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "Check-In error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDto> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mContext, "Network error during Check-In", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Check-In failure", t);
            }
        });
    }

    private void confirmVehicleTransaction() {
        String total = String.valueOf(tvTotalQty.getText());
        String success = String.valueOf(tvTotalSuccess.getText());
        if (!total.contains(success)) {
            Toast.makeText(mContext, "No scanned items to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("Confirm vehicle loading for:\n\n");
        confirmMsg.append("Vehicle: ").append(licensePlate).append("\n");
        confirmMsg.append("Driver: ").append(driver).append("\n");
        confirmMsg.append("Kanbans: ").append(scannedKanbans.size()).append("\n");

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Vehicle Loading")
                .setMessage(confirmMsg.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    performVehicleConfirmTransaction();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    String userID = MainActivity.globalUserId;
    private void performVehicleConfirmTransaction() {
        showLoading(true);
        Call<List<ResponseDto>> call = apiService.createVehicleConfirm(vehicleTag, userID);

        call.enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
                showLoading(false);
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

                        Toast.makeText(mContext, "Vehicle shipment confirmed successfully", Toast.LENGTH_LONG).show();
                        btnConfirm.setEnabled(false);
                    } else {
                        String errorMsg = "Some transactions failed:\n\n" + errorMessages.toString();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Vehicle Confirm Errors")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();

                        Toast.makeText(mContext, "Confirm vehicle shipment failed", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                showLoading(false);
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

