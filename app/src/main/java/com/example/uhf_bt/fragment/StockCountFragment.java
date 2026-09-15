package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Build;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.ProductItemAdapter;
import com.example.uhf_bt.models.ProductItemLite;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import android.os.SystemClock;

import com.example.uhf_bt.database.AppDatabase;
import com.example.uhf_bt.database.ProductItemDao;
import com.example.uhf_bt.models.ProductItemEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;
import java.io.IOException;

public class StockCountFragment extends Fragment {

    private static final String TAG = "StockCountFragment";
    
    // UI Elements - Summary Cards
    private TextView tvSummaryTotal, tvSummaryScanned, tvSummaryStock;
    private TextView tvSummaryPick, tvSummaryPack, tvSummaryUnknown, tvSummaryOthers, tvSummaryRemaining;
    private View cardTotal, cardScanned, cardStock, cardPick, cardPack, cardUnknown, cardOthers, cardRemaining;
    private TextView tvListTitle;

    // Standard UI Elements
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private ProductItemAdapter adapter;
    private SearchView searchView;
    private Toolbar toolbar;
    private EditText tvBarcode;
    private Button btnScanBarcode;
    
    // Data Structures
    private List<ProductItemLite> displayList = new ArrayList<>(); 
    private java.util.Set<String> unknownTags = new java.util.HashSet<>();
    private ProductItemDao productItemDao;
    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    
    private ActivityResultLauncher<Intent> exportCsvLauncher;
    private ActivityResultLauncher<Intent> exportTxtLauncher;
    
    // Filter State
    private String currentFilter = "SCANNED";
    
    // Filter UI and selections
    private ImageView ivFilter;
    private List<String> selectedProducts = new ArrayList<>();
    private List<String> selectedProductGroups = new ArrayList<>();
    private List<String> selectedWarehouses = new ArrayList<>();
    private Integer selectedStatus = null;
    
    // Available options for filter dropdowns
    private List<String> availableProducts = new ArrayList<>();
    private List<String> availableProductGroups = new ArrayList<>();
    private List<String> availableWarehouses = new ArrayList<>();

    // RFID & Context
    private MainActivity mContext;
    private RFIDWithUHFBLE uhf;
    private ApiService apiService;
    private Button btnScanRFID, btnStop, btnClear, btnExport;
    
    private final int FLAG_START = 0;
    private final int FLAG_STOP = 1;
    private final int FLAG_UPDATE_TIME = 2;
    private final int FLAG_UHFINFO = 3;
    private final int FLAG_UHFINFO_LIST = 5;
    private final int FLAG_SUCCESS = 10;
    private final int FLAG_FAIL = 11;
    private final int FLAG_TIME_OVER = 12;
    
    private long mStrTime;
    private int maxRunTime = 99999999;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
            uhf = mContext.uhf;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel7));
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
        productItemDao = AppDatabase.getInstance(getContext()).productItemDao();

        exportCsvLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                writeCsvToUri(uri);
            }
        });

        exportTxtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                writeTxtToUri(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_count, container, false);
        initViews(view);
        setupDrillDownListeners();
        setupRFIDButtons();
        loadProductItems();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        searchView = view.findViewById(R.id.searchView);
        
        // Summary Cards
        tvSummaryTotal = view.findViewById(R.id.tvSummaryTotal);
        tvSummaryScanned = view.findViewById(R.id.tvSummaryScanned);
        tvSummaryStock = view.findViewById(R.id.tvSummaryStock);
        tvSummaryPick = view.findViewById(R.id.tvSummaryPick);
        tvSummaryPack = view.findViewById(R.id.tvSummaryPack);
        tvSummaryUnknown = view.findViewById(R.id.tvSummaryUnknown);
        tvSummaryOthers = view.findViewById(R.id.tvSummaryOthers);
        tvSummaryRemaining = view.findViewById(R.id.tvSummaryRemaining);
        
        cardTotal = view.findViewById(R.id.cardTotal);
        cardScanned = view.findViewById(R.id.cardScanned);
        cardStock = view.findViewById(R.id.cardStock);
        cardPick = view.findViewById(R.id.cardPick);
        cardPack = view.findViewById(R.id.cardPack);
        cardUnknown = view.findViewById(R.id.cardUnknown);
        cardOthers = view.findViewById(R.id.cardOthers);
        cardRemaining = view.findViewById(R.id.cardRemaining);
        
        tvListTitle = view.findViewById(R.id.tvListTitle);
        
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        
        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btnStop = view.findViewById(R.id.btnStop);
        btnClear = view.findViewById(R.id.btnClear);
        btnExport = view.findViewById(R.id.btnExport);
        tvBarcode = view.findViewById(R.id.tvBarcode);
        btnScanBarcode = view.findViewById(R.id.btnScanBarcode);

        adapter = new ProductItemAdapter(mContext, new ArrayList<>());
        listView.setAdapter(adapter);
        
        // Filter button
        ivFilter = view.findViewById(R.id.ivFilter);
        ivFilter.setOnClickListener(v -> showFilterDialog());

        // SearchView expand/collapse listeners
        TextView toolbarTitle = view.findViewById(R.id.toolbar_title);
        searchView.setOnSearchClickListener(v -> {
            // Hide title and filter button when search expands
            toolbarTitle.setVisibility(View.GONE);
            ivFilter.setVisibility(View.GONE);
        });
        
        searchView.setOnCloseListener(() -> {
            // Show title and filter button when search closes
            toolbarTitle.setVisibility(View.VISIBLE);
            ivFilter.setVisibility(View.VISIBLE);
            return false; // Return false to allow default close behavior
        });

        view.findViewById(R.id.ivBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void setupDrillDownListeners() {
        cardTotal.setOnClickListener(v -> updateDisplayList("ALL"));
        cardScanned.setOnClickListener(v -> updateDisplayList("SCANNED"));
        cardStock.setOnClickListener(v -> updateDisplayList("STOCK"));
        cardPick.setOnClickListener(v -> updateDisplayList("PICK"));
        cardPack.setOnClickListener(v -> updateDisplayList("PACK"));
        cardUnknown.setOnClickListener(v -> updateDisplayList("UNKNOWN"));
        cardOthers.setOnClickListener(v -> updateDisplayList("OTHERS"));
        cardRemaining.setOnClickListener(v -> updateDisplayList("REMAINING"));
    }

    private void updateDisplayList(String filter) {
        currentFilter = filter;
        
        dbExecutor.execute(() -> {
            String title = "Details: ";
            List<ProductItemEntity> entities = new ArrayList<>(); // Initialize to prevent uninitialized error
            
            // Check if there are active filters
            boolean hasActiveFilters = !selectedProducts.isEmpty() || 
                                      !selectedProductGroups.isEmpty() || 
                                      !selectedWarehouses.isEmpty();
            
            if (hasActiveFilters) {
                // Apply filters first, then filter by card type
                List<String> products = selectedProducts.isEmpty() ? new ArrayList<>() : selectedProducts;
                List<String> groups = selectedProductGroups.isEmpty() ? new ArrayList<>() : selectedProductGroups;
                List<String> warehouses = selectedWarehouses.isEmpty() ? new ArrayList<>() : selectedWarehouses;
                
                // Get filtered items based on card selection
                Integer statusFilter = null;
                switch (filter) {
                    case "STOCK":
                        statusFilter = 1;
                        title += "Stock Items (Filtered)";
                        break;
                    case "PICK":
                        statusFilter = 2;
                        title += "Pick Items (Filtered)";
                        break;
                    case "PACK":
                        statusFilter = 3;
                        title += "Pack Items (Filtered)";
                        break;
                    case "SHIPPED":
                        statusFilter = 7;
                        title += "Shipped Items (Filtered)";
                        break;
                    case "ALL":
                        // Get ALL items regardless of status (when filters are active)
                        entities = productItemDao.getFilteredItems(
                            products, products.size(),
                            groups, groups.size(),
                            warehouses, warehouses.size(),
                            null // No status filter - get all
                        );
                        title += "All Items (Filtered)";
                        break;
                    case "SCANNED":
                        // Get all filtered items, then filter scanned
                        entities = productItemDao.getFilteredItems(
                            products, products.size(),
                            groups, groups.size(),
                            warehouses, warehouses.size(),
                            selectedStatus
                        );
                        // Filter only scanned items
                        List<ProductItemEntity> scannedOnly = new ArrayList<>();
                        for (ProductItemEntity e : entities) {
                            if (e.isScannedStockCount()) {
                                scannedOnly.add(e);
                            }
                        }
                        entities = scannedOnly;
                        title += "All Scanned (Filtered)";
                        break;
                    case "REMAINING":
                        // Get all filtered items, then filter remaining
                        entities = productItemDao.getFilteredItems(
                            products, products.size(),
                            groups, groups.size(),
                            warehouses, warehouses.size(),
                            selectedStatus
                        );
                        // Filter only remaining items
                        List<ProductItemEntity> remainingOnly = new ArrayList<>();
                        for (ProductItemEntity e : entities) {
                            if (!e.isScannedStockCount()) {
                                remainingOnly.add(e);
                            }
                        }
                        entities = remainingOnly;
                        title += "Remaining to Scan (Filtered)";
                        break;
                    case "OTHERS":
                        // Get filtered items with other statuses
                        entities = productItemDao.getFilteredItems(
                            products, products.size(),
                            groups, groups.size(),
                            warehouses, warehouses.size(),
                            null
                        );
                        // Filter only other status items
                        List<ProductItemEntity> othersOnly = new ArrayList<>();
                        for (ProductItemEntity e : entities) {
                            int status = e.getProductItemStatus();
                            if (status != 1 && status != 2 && status != 3 && status != 7) {
                                othersOnly.add(e);
                            }
                        }
                        entities = othersOnly;
                        title += "Other Status Items (Filtered)";
                        break;
                    case "UNKNOWN":
                        entities = new ArrayList<>();
                        title += "Unknown Tags (Not in System)";
                        break;
                    default:
                        entities = new ArrayList<>();
                }
                
                // For status-based filters (STOCK, PICK, PACK, SHIPPED)
                if (statusFilter != null && !filter.equals("SCANNED") && !filter.equals("REMAINING")) {
                    entities = productItemDao.getFilteredItems(
                        products, products.size(),
                        groups, groups.size(),
                        warehouses, warehouses.size(),
                        statusFilter
                    );
                }
            } else {
                // No active filters - use original logic
                switch (filter) {
                    case "ALL":
                        entities = productItemDao.getAll(); // Get ALL items
                        title += "All Items";
                        break;
                    case "SCANNED":
                        entities = productItemDao.getScannedStockItems();
                        title += "All Scanned";
                        break;
                    case "STOCK":
                        entities = productItemDao.getItemsByStatus(1);
                        title += "Stock Items";
                        break;
                    case "PICK":
                        entities = productItemDao.getItemsByStatus(2);
                        title += "Pick Items";
                        break;
                    case "PACK":
                        entities = productItemDao.getItemsByStatus(3);
                        title += "Pack Items";
                        break;
                    case "SHIPPED":
                        entities = productItemDao.getItemsByStatus(7);
                        title += "Shipped Items";
                        break;
                    case "UNKNOWN":
                        entities = new ArrayList<>();
                        title += "Unknown Tags (Not in System)";
                        break;
                    case "OTHERS":
                        entities = productItemDao.getOtherStatusItems();
                        title += "Other Status Items";
                        break;
                    case "REMAINING":
                        entities = productItemDao.getRemainingStockItems();
                        title += "Remaining to Scan";
                        break;
                    default:
                        entities = new ArrayList<>();
                }
            }

            List<ProductItemLite> list = new ArrayList<>();
            for (ProductItemEntity e : entities) {
                list.add(convertToLite(e));
            }

            final String finalTitle = title + " (" + list.size() + ")";
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    displayList.clear();
                    if (filter.equals("UNKNOWN")) {
                        for (String tag : unknownTags) {
                            ProductItemLite lite = new ProductItemLite();
                            lite.setTagNumber(tag);
                            lite.setProductName("UNKNOWN TAG");
                            displayList.add(lite);
                        }
                    } else {
                        displayList.addAll(list);
                    }
                    tvListTitle.setText(finalTitle.replace("(" + list.size() + ")", "(" + displayList.size() + ")"));
                    adapter.updateItems(displayList);
                    emptyView.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
        });
    }

    private void updateTotalCounts() {
        dbExecutor.execute(() -> {
            int totalStockTarget = productItemDao.getStockTargetCount();
            int totalScanned = productItemDao.getScannedStockCount();
            int scannedStock = productItemDao.getScannedStockCountByStatus(1);
            
            int totalPick = productItemDao.getCountByStatus(2);
            int scannedPick = productItemDao.getScannedStockCountByStatus(2);
            
            int totalPack = productItemDao.getCountByStatus(3);
            int scannedPack = productItemDao.getScannedStockCountByStatus(3);
            
            int totalOthers = productItemDao.getOtherStatusCount();
            int scannedOthers = productItemDao.getScannedStockOtherStatusCount();
            
            // Calculate TOTAL as sum of all statuses
            int grandTotal = totalStockTarget + totalPick + totalPack + totalOthers;
            
            // Debug log
            Log.d(TAG, "Total Calculation: Stock=" + totalStockTarget + ", Pick=" + totalPick + 
                  ", Pack=" + totalPack + ", Others=" + totalOthers + ", GrandTotal=" + grandTotal);
            
            int remainingCount = totalStockTarget - scannedStock;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvSummaryTotal.setText(String.valueOf(grandTotal)); // Show all items
                    tvSummaryScanned.setText(String.valueOf(totalScanned));
                    tvSummaryRemaining.setText(String.valueOf(remainingCount < 0 ? 0 : remainingCount));

                    tvSummaryStock.setText(scannedStock + "/" + totalStockTarget);
                    tvSummaryPick.setText(scannedPick + "/" + totalPick);
                    tvSummaryPack.setText(scannedPack + "/" + totalPack);
                    tvSummaryUnknown.setText(String.valueOf(unknownTags.size()));
                    tvSummaryOthers.setText(scannedOthers + "/" + totalOthers);
                    
                    updateDisplayList(currentFilter);
                });
            }
        });
    }

    // Helper method to check available memory
    private boolean hasEnoughMemory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long availableMemory = maxMemory - usedMemory;
        
        // Require at least 50MB free memory
        long requiredMemory = 50 * 1024 * 1024;
        boolean hasEnough = availableMemory > requiredMemory;
        
        Log.d(TAG, String.format("Memory check: Available=%dMB, Required=%dMB, HasEnough=%b",
                availableMemory / 1024 / 1024,
                requiredMemory / 1024 / 1024,
                hasEnough));
        
        return hasEnough;
    }

    private void loadProductItems() {
        if (apiService == null) return;
        
        // Check available memory before sync
        if (!hasEnoughMemory()) {
            Toast.makeText(getContext(), "Low memory. Please close other apps and try again.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Skipping sync due to low memory");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("PBPPrefs", Context.MODE_PRIVATE);
        String lastSyncTime = prefs.getString("Shared_ProductItem_SyncTime", null);

        apiService.getProductItemsLiteStream(lastSyncTime).enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbExecutor.execute(() -> {
                        try (okhttp3.ResponseBody body = response.body();
                             java.io.Reader reader = body.charStream();
                             com.google.gson.stream.JsonReader jsonReader = new com.google.gson.stream.JsonReader(reader)) {
                            
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            List<ProductItemEntity> batch = new ArrayList<>();
                            String maxDate = lastSyncTime;
                            int count = 0;
                            
                            // Adaptive batch size based on available memory
                            int batchSize = hasEnoughMemory() ? 500 : 250;

                            jsonReader.beginArray();
                            while (jsonReader.hasNext()) {
                                ProductItemLite lite = gson.fromJson(jsonReader, ProductItemLite.class);
                                batch.add(ProductItemEntity.fromLite(lite));
                                
                                if (lite.getLastUpdateDate() != null) {
                                    if (maxDate == null || lite.getLastUpdateDate().compareTo(maxDate) > 0) {
                                        maxDate = lite.getLastUpdateDate();
                                    }
                                }

                                // Reduced batch size to prevent memory spikes
                                if (batch.size() >= batchSize) {
                                    productItemDao.upsertAll(batch);
                                    count += batch.size();
                                    batch.clear();
                                    
                                    // Suggest GC to free memory between batches
                                    System.gc();
                                    
                                    final int currentCount = count;
                                    // Log progress every 5000 items
                                    if (count % 5000 == 0) {
                                        Log.d(TAG, "Synced " + count + " items...");
                                    }
                                    
                                    // Small delay to allow GC
                                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                                    
                                    getActivity().runOnUiThread(() -> {
                                        // Optional: update progress UI
                                    });
                                }
                            }
                            jsonReader.endArray();

                            if (!batch.isEmpty()) {
                                productItemDao.upsertAll(batch);
                                count += batch.size();
                            }
                            
                            productItemDao.deleteMarkedItems();
                            
                            if (maxDate != null) {
                                prefs.edit().putString("Shared_ProductItem_SyncTime", maxDate).apply();
                            }

                            final int finalCount = count;
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                updateTotalCounts();
                                Toast.makeText(getContext(), "Synced " + finalCount + " items", Toast.LENGTH_SHORT).show();
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Streaming error", e);
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // RFID & Barcode Implementation
    private void setupRFIDButtons() {
        btnScanRFID.setOnClickListener(v -> startThread());
        btnStop.setOnClickListener(v -> stop());
        btnClear.setOnClickListener(v -> clearData());
        btnExport.setOnClickListener(v -> showExportDialog());
        
        btnScanBarcode.setOnClickListener(v -> scanBarcode());
        
        tvBarcode.setOnEditorActionListener((v, actionId, event) -> {
            scanBarcode();
            return true;
        });

        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                    if (keycode == 3) {
                        mContext.isKeyDownUP = true;
                        startThread();
                    } else {
                        if (!mContext.isKeyDownUP) {
                            if (keycode == 1) {
                                if (mContext.isScanning) {
                                    stop();
                                } else {
                                    startThread();
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
                if (keycode == 4) {
                    stop();
                }
            }
        });
    }

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Export Scanned Items");
        builder.setMessage("Choose export format:");

        builder.setPositiveButton("Export CSV", (dialog, which) -> {
            exportScannedItemsAsCSV();
        });

        builder.setNegativeButton("Export TXT", (dialog, which) -> {
            exportScannedItemsAsText();
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void exportScannedItemsAsCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String filename = "stock_count_" + sdf.format(new Date()) + ".csv";
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        exportCsvLauncher.launch(intent);
    }

    private void exportScannedItemsAsText() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String filename = "stock_count_" + sdf.format(new Date()) + ".txt";
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        exportTxtLauncher.launch(intent);
    }

    private void scanBarcode() {
        String barcode = tvBarcode.getText().toString().trim();
        if (!TextUtils.isEmpty(barcode)) {
            processTag(barcode);
            tvBarcode.setText("");
        }
    }

    // Filter Dialog Methods
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_stock_filter, null);
        builder.setView(dialogView);
        builder.setTitle("กรองสต็อก");
        
        // Load available options from database
        dbExecutor.execute(() -> {
            availableProducts = productItemDao.getAllProductNames();
            availableProductGroups = productItemDao.getAllProductGroupNames();
            availableWarehouses = productItemDao.getAllWarehouseNames();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setupFilterDialog(dialogView, builder);
                });
            }
        });
    }

    private void setupFilterDialog(View dialogView, AlertDialog.Builder builder) {
        Button btnSelectProducts = dialogView.findViewById(R.id.btnSelectProducts);
        Button btnSelectProductGroups = dialogView.findViewById(R.id.btnSelectProductGroups);
        Button btnSelectWarehouses = dialogView.findViewById(R.id.btnSelectWarehouses);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        
        // Setup Status Spinner
        String[] statusOptions = {"All Status", "Stock (1)", "Pick (2)", "Pack (3)", "Others"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        
        // Update button texts if there are existing selections
        if (!selectedProducts.isEmpty()) {
            btnSelectProducts.setText("เลือกแล้ว (" + selectedProducts.size() + ")");
        }
        if (!selectedProductGroups.isEmpty()) {
            btnSelectProductGroups.setText("เลือกแล้ว (" + selectedProductGroups.size() + ")");
        }
        if (!selectedWarehouses.isEmpty()) {
            btnSelectWarehouses.setText("เลือกแล้ว (" + selectedWarehouses.size() + ")");
        }
        if (selectedStatus != null) {
            spinnerStatus.setSelection(selectedStatus);
        }
        
        // Product multi-select
        btnSelectProducts.setOnClickListener(v -> {
            showMultiSelectDialog("เลือกสินค้า", availableProducts, selectedProducts, 
                selected -> {
                    selectedProducts = new ArrayList<>(selected);
                    btnSelectProducts.setText(selected.isEmpty() ? "เลือกสินค้า... (All)" : 
                        "เลือกแล้ว (" + selected.size() + ")");
                });
        });
        
        // Product Group multi-select
        btnSelectProductGroups.setOnClickListener(v -> {
            showMultiSelectDialog("เลือกกลุ่มสินค้า", availableProductGroups, selectedProductGroups,
                selected -> {
                    selectedProductGroups = new ArrayList<>(selected);
                    btnSelectProductGroups.setText(selected.isEmpty() ? "เลือกกลุ่มสินค้า... (All)" : 
                        "เลือกแล้ว (" + selected.size() + ")");
                });
        });
        
        // Warehouse multi-select
        btnSelectWarehouses.setOnClickListener(v -> {
            showMultiSelectDialog("เลือกคลังสินค้า", availableWarehouses, selectedWarehouses,
                selected -> {
                    selectedWarehouses = new ArrayList<>(selected);
                    btnSelectWarehouses.setText(selected.isEmpty() ? "เลือกคลังสินค้า... (All)" : 
                        "เลือกแล้ว (" + selected.size() + ")");
                });
        });
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        // Apply Filter
        dialogView.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            int statusPos = spinnerStatus.getSelectedItemPosition();
            selectedStatus = (statusPos == 0) ? null : statusPos; // 0 = All
            
            applyFilter();
            dialog.dismiss();
        });
        
        // Clear Filter
        dialogView.findViewById(R.id.btnClearFilter).setOnClickListener(v -> {
            selectedProducts.clear();
            selectedProductGroups.clear();
            selectedWarehouses.clear();
            selectedStatus = null;
            
            applyFilter();
            dialog.dismiss();
        });
        
        // Cancel
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void showMultiSelectDialog(String title, List<String> items, List<String> selectedItems,
                                       OnSelectionChanged callback) {
        if (items == null || items.isEmpty()) {
            Toast.makeText(getContext(), "ไม่มีข้อมูลให้เลือก", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean[] checkedItems = new boolean[items.size()];
        List<String> tempSelected = new ArrayList<>(selectedItems);
        
        // Mark currently selected items
        for (int i = 0; i < items.size(); i++) {
            checkedItems[i] = tempSelected.contains(items.get(i));
        }
        
        new AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMultiChoiceItems(items.toArray(new String[0]), checkedItems,
                (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!tempSelected.contains(items.get(which))) {
                            tempSelected.add(items.get(which));
                        }
                    } else {
                        tempSelected.remove(items.get(which));
                    }
                })
            .setPositiveButton("OK", (dialog, which) -> callback.onSelectionChanged(tempSelected))
            .setNegativeButton("Cancel", null)
            .show();
    }

    interface OnSelectionChanged {
        void onSelectionChanged(List<String> selected);
    }

    private void applyFilter() {
        dbExecutor.execute(() -> {
            List<ProductItemEntity> filteredItems;
            
            // ถ้าไม่มีการเลือก filter ใดๆ ให้แสดงทั้งหมด
            if (selectedProducts.isEmpty() && selectedProductGroups.isEmpty() && 
                selectedWarehouses.isEmpty() && selectedStatus == null) {
                filteredItems = productItemDao.getItemsByStatus(1); // Default: Stock items
            } else {
                // ใช้ query ที่รองรับหลายเงื่อนไข
                // ส่ง empty list แทน null เพื่อป้องกัน NullPointerException
                List<String> products = selectedProducts.isEmpty() ? new ArrayList<>() : selectedProducts;
                List<String> groups = selectedProductGroups.isEmpty() ? new ArrayList<>() : selectedProductGroups;
                List<String> warehouses = selectedWarehouses.isEmpty() ? new ArrayList<>() : selectedWarehouses;
                
                filteredItems = productItemDao.getFilteredItems(
                    products,
                    products.size(),
                    groups,
                    groups.size(),
                    warehouses,
                    warehouses.size(),
                    selectedStatus
                );
            }
            
            List<ProductItemLite> liteList = new ArrayList<>();
            for (ProductItemEntity e : filteredItems) {
                liteList.add(convertToLite(e));
            }
            
            // Calculate summary statistics for filtered items
            int totalCount = filteredItems.size();
            int scannedCount = 0;
            int stockCount = 0;
            int pickCount = 0;
            int packCount = 0;
            int othersCount = 0;
            int scannedStockCount = 0;
            int scannedPickCount = 0;
            int scannedPackCount = 0;
            int scannedOthersCount = 0;
            
            for (ProductItemEntity item : filteredItems) {
                if (item.isScannedStockCount()) {
                    scannedCount++;
                }
                
                int status = item.getProductItemStatus();
                if (status == 1) {
                    stockCount++;
                    if (item.isScannedStockCount()) scannedStockCount++;
                } else if (status == 2) {
                    pickCount++;
                    if (item.isScannedStockCount()) scannedPickCount++;
                } else if (status == 3) {
                    packCount++;
                    if (item.isScannedStockCount()) scannedPackCount++;
                } else {
                    othersCount++;
                    if (item.isScannedStockCount()) scannedOthersCount++;
                }
            }
            
            int remainingCount = totalCount - scannedCount;
            
            // Final values for UI update
            final int finalTotal = totalCount;
            final int finalScanned = scannedCount;
            final int finalStock = stockCount;
            final int finalPick = pickCount;
            final int finalPack = packCount;
            final int finalOthers = othersCount;
            final int finalRemaining = remainingCount;
            final int finalScannedStock = scannedStockCount;
            final int finalScannedPick = scannedPickCount;
            final int finalScannedPack = scannedPackCount;
            final int finalScannedOthers = scannedOthersCount;
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    displayList.clear();
                    displayList.addAll(liteList);
                    adapter.updateItems(displayList);
                    
                    // Update summary cards with filtered data
                    tvSummaryTotal.setText(String.valueOf(finalTotal));
                    tvSummaryScanned.setText(String.valueOf(finalScanned));
                    tvSummaryStock.setText(String.valueOf(finalStock));
                    tvSummaryPick.setText(finalScannedPick + "/" + finalPick);
                    tvSummaryPack.setText(finalScannedPack + "/" + finalPack);
                    tvSummaryOthers.setText(finalScannedOthers + "/" + finalOthers);
                    tvSummaryRemaining.setText(String.valueOf(finalRemaining));
                    tvSummaryUnknown.setText(String.valueOf(unknownTags.size())); // Unknown tags don't change with filter
                    
                    String filterInfo = buildFilterInfo();
                    tvListTitle.setText("Filtered Items: " + displayList.size() + filterInfo);
                    
                    emptyView.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(getContext(), "แสดง " + displayList.size() + " รายการ", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String buildFilterInfo() {
        StringBuilder info = new StringBuilder();
        if (!selectedProducts.isEmpty()) {
            info.append(" | Products: ").append(selectedProducts.size());
        }
        if (!selectedProductGroups.isEmpty()) {
            info.append(" | Groups: ").append(selectedProductGroups.size());
        }
        if (!selectedWarehouses.isEmpty()) {
            info.append(" | Warehouses: ").append(selectedWarehouses.size());
        }
        if (selectedStatus != null) {
            info.append(" | Status: ").append(getStatusText(selectedStatus));
        }
        return info.toString();
    }

    private void startThread() {
        if (mContext.isScanning) return;
        
        mContext.uhf.setInventoryCallback(uhftagInfo -> {
            handler.sendMessage(handler.obtainMessage(FLAG_UHFINFO, uhftagInfo));
        });
        
        mContext.isScanning = true;
        Message msg = handler.obtainMessage(FLAG_START);
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

    private void stop() {
        if (!mContext.isScanning) return;
        
        boolean result = mContext.uhf.stopInventory();
        mContext.isScanning = false;
        handler.removeMessages(FLAG_TIME_OVER);
        
        Message msg = handler.obtainMessage(FLAG_STOP);
        if (result || mContext.uhf.getConnectStatus() != ConnectionStatus.CONNECTED) {
            msg.arg1 = FLAG_SUCCESS;
        } else {
            msg.arg1 = FLAG_FAIL;
        }
        handler.sendMessage(msg);
    }

    private void clearData() {
        dbExecutor.execute(() -> {
            productItemDao.clearScannedStockStatus();
            unknownTags.clear();
            getActivity().runOnUiThread(() -> {
                updateTotalCounts();
                Utils.showToast(getContext(), "Scanned data cleared");
            });
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_TIME_OVER:
                    Log.i(TAG, "FLAG_TIME_OVER =" + (System.currentTimeMillis() - mStrTime));
                    stop();
                    break;
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btnClear.setEnabled(true);
                        btnStop.setEnabled(false);
                        btnScanRFID.setEnabled(true);
                    } else {
                        Utils.showToast(getContext(), "Stop failed");
                    }
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btnClear.setEnabled(false);
                        btnStop.setEnabled(true);
                        btnScanRFID.setEnabled(false);
                    } else {
                        // Fail to start
                    }
                    break;
                case FLAG_UPDATE_TIME:
                    if (mContext.isScanning) {
                        handler.sendEmptyMessageDelayed(FLAG_UPDATE_TIME, 10);
                    } else {
                        handler.removeMessages(FLAG_UPDATE_TIME);
                    }
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    processTag(info.getEPC());
                    break;
                case FLAG_UHFINFO_LIST:
                    List<UHFTAGInfo> list = (List<UHFTAGInfo>) msg.obj;
                    for (UHFTAGInfo tagInfo : list) {
                        processTag(tagInfo.getEPC());
                    }
                    break;
            }
        }
    };

    private void processTag(String tag) {
        if (TextUtils.isEmpty(tag)) return;
        
        dbExecutor.execute(() -> {
            ProductItemEntity entity = productItemDao.findByTag(tag);
            if (entity != null) {
                if (!entity.isScannedStockCount()) {
                    entity.setScannedStockCount(true);
                    productItemDao.update(entity);
                    getActivity().runOnUiThread(() -> {
                        updateTotalCounts();
                        updateDisplayList(currentFilter);
                        Utils.showToast(getContext(), "Scanned: " + entity.getProductName());
                    });
                }
            } else {
                if (!unknownTags.contains(tag)) {
                    unknownTags.add(tag);
                    getActivity().runOnUiThread(() -> {
                        updateTotalCounts();
                        updateDisplayList(currentFilter);
                        Utils.showToast(getContext(), "Unknown Tag: " + tag);
                    });
                }
            }
        });
    }

    private ProductItemLite convertToLite(ProductItemEntity entity) {
        ProductItemLite lite = new ProductItemLite();
        lite.setTagNumber(entity.getTagNumber());
        lite.setProductName(entity.getProductName());
        lite.setProductNumber(entity.getProductNumber());
        lite.setProductGroupName(entity.getProductGroupName());
        lite.setLotNumber(entity.getLotNumber());
        lite.setWarehouseName(entity.getWarehouseName());
        lite.setProductItemStatus(entity.getProductItemStatus());
        lite.setScanned(entity.isScanned());
        lite.setLastUpdateDate(entity.getLastUpdateDate());
        lite.setNotDeleted(entity.isNotDeleted());
        return lite;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbExecutor != null) {
            dbExecutor.shutdown();
        }
    }

    private void writeCsvToUri(Uri uri) {
        dbExecutor.execute(() -> {
            List<ProductItemEntity> scannedItems = productItemDao.getScannedStockItems();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {

                    // Add UTF-8 BOM for Excel visibility
                    writer.write('\ufeff');
                    writer.write("No,Product Number,Product Name,Product Group,Tag Number,Lot Number,Warehouse,Status,Scan Time\n");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String currentTime = dateFormat.format(new Date());

                    for (int i = 0; i < scannedItems.size(); i++) {
                        ProductItemEntity item = scannedItems.get(i);
                        writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                i + 1,
                                escapeCSVField(item.getProductNumber()),
                                escapeCSVField(item.getProductName()),
                                escapeCSVField(item.getProductGroupName()),
                                escapeCSVField(item.getTagNumber()),
                                escapeCSVField(item.getLotNumber()),
                                escapeCSVField(item.getWarehouseName()),
                                escapeCSVField(getStatusText(item.getProductItemStatus())),
                                currentTime));
                    }

                    // Add Unknown Tags at the end of CSV
                    for (String unknown : unknownTags) {
                        writer.write(String.format("N/A,N/A,UNKNOWN TAG,N/A,%s,N/A,N/A,N/A,%s\n",
                                escapeCSVField(unknown),
                                currentTime));
                    }

                    writer.flush();
                    Toast.makeText(getContext(), "CSV Exported successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void writeTxtToUri(Uri uri) {
        dbExecutor.execute(() -> {
            List<ProductItemEntity> scannedItems = productItemDao.getScannedStockItems();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {

                    // Add UTF-8 BOM for Thai character visibility
                    writer.write('\ufeff');
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    writer.write("=== STOCK COUNT EXPORT ===\n");
                    writer.write("Export Date: " + dateFormat.format(new Date()) + "\n");
                    writer.write("Total Scanned: " + scannedItems.size() + "\n\n");

                    for (int i = 0; i < scannedItems.size(); i++) {
                        ProductItemEntity item = scannedItems.get(i);
                        writer.write("ITEM #" + (i + 1) + "\n");
                        writer.write("├─ Product Number: " + item.getProductNumber() + "\n");
                        writer.write("├─ Product Name: " + item.getProductName() + "\n");
                        writer.write("└─ Tag Number: " + item.getTagNumber() + "\n\n");
                    }

                    // Add Unknown Tags at the end of TXT
                    if (!unknownTags.isEmpty()) {
                        writer.write("=== UNKNOWN TAGS (" + unknownTags.size() + ") ===\n");
                        int startIdx = scannedItems.size() + 1;
                        for (String unknown : unknownTags) {
                            writer.write("ITEM #" + (startIdx++) + " (UNKNOWN)\n");
                            writer.write("└─ Tag Number: " + unknown + "\n\n");
                        }
                    }

                    writer.flush();
                    Toast.makeText(getContext(), "Text Exported successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String escapeCSVField(String field) {
        if (field == null) return "N/A";
        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "N/A";
        switch (status) {
            case -1: return "Cancel";
            case -2: return "Out of Stock";
            case 0: return "NEW";
            case 1: return "STOCK";
            case 2: return "PICK";
            case 3: return "PACK";
            case 7: return "SHIPPED";
            default: return "Unknown (" + status + ")";
        }
    }
}
