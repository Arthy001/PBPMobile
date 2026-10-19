package com.example.uhf_bt.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.PickPackAdapter;
import com.example.uhf_bt.models.PickAndPackTagDto;
import com.example.uhf_bt.models.PickPackItem;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.button.MaterialButton;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickPackFragment extends Fragment {
    private static final String TAG = "PickPackFragment";

    // Message handler flags
    private static final int MSG_TAG_READ = 1;
    private static final int MSG_UPDATE_COUNTS = 2;

    // Context & API
    private MainActivity mContext;
    private ApiService apiService;

    // Views
    private ImageView ivBack;
    private TextView tvToolbarTitle;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private EditText etPalletTagId;
    private Button btnLoadPallet;
    private ImageButton btnScanBarcode;
    private MaterialButton btnModeBox, btnModePiece;
    private TextView tvPalletsCount, tvPackedCount, tvBoxesCount, tvScannedCount, tvInStockCount;
    private MaterialButton btnScanRFID, btnStopRFID, btnClearRFID, btnConfirmSave;
    private ListView listViewItems;
    private View emptyView;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    // Isolated State Data (No shared globals)
    private final List<PickPackItem> itemList = new ArrayList<>();
    private final List<ProductItem> stockList = new ArrayList<>();
    private final Map<Integer, Set<String>> scannedTagsByItemId = new HashMap<>();
    private final Set<String> allScannedTags = new HashSet<>();
    private final Set<String> notifiedTags = new HashSet<>();
    private final List<String> notificationMessages = new ArrayList<>();

    private PickPackAdapter adapter;
    private String currentScanMode = "Box"; // "Box" or "Piece"
    private String currentPalletTagId = "";

    // Handler for UI updates from background threads / callbacks
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_TAG_READ) {
                String epc = (String) msg.obj;
                if (!TextUtils.isEmpty(epc)) {
                    processScannedTag(epc.trim());
                }
            } else if (msg.what == MSG_UPDATE_COUNTS) {
                updateDashboardCounts();
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_and_pack, container, false);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews(view);
        setupListeners();
        updateScanModeUI();

        return view;
    }

    private void initViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        tvToolbarTitle = view.findViewById(R.id.toolbar_title);
        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        etPalletTagId = view.findViewById(R.id.etPalletTagId);
        btnLoadPallet = view.findViewById(R.id.btnLoadPallet);
        btnScanBarcode = view.findViewById(R.id.btnScanBarcode);

        btnModeBox = view.findViewById(R.id.btnModeBox);
        btnModePiece = view.findViewById(R.id.btnModePiece);

        tvPalletsCount = view.findViewById(R.id.tvPalletsCount);
        tvPackedCount = view.findViewById(R.id.tvPackedCount);
        tvBoxesCount = view.findViewById(R.id.tvBoxesCount);
        tvScannedCount = view.findViewById(R.id.tvScannedCount);
        tvInStockCount = view.findViewById(R.id.tvInStockCount);

        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btnStopRFID = view.findViewById(R.id.btnStopRFID);
        btnClearRFID = view.findViewById(R.id.btnClearRFID);
        btnConfirmSave = view.findViewById(R.id.btnConfirmSave);

        listViewItems = view.findViewById(R.id.listViewItems);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);

        listViewItems.setEmptyView(emptyView);
        adapter = new PickPackAdapter(requireContext(), itemList);
        listViewItems.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            stopScan();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Search action on soft keyboard
        etPalletTagId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String tag = etPalletTagId.getText().toString().trim();
                if (!TextUtils.isEmpty(tag)) {
                    loadPalletData(tag);
                }
                return true;
            }
            return false;
        });

        btnLoadPallet.setOnClickListener(v -> {
            String tag = etPalletTagId.getText().toString().trim();
            if (TextUtils.isEmpty(tag)) {
                Toast.makeText(getContext(), "Please enter Pallet Tag ID", Toast.LENGTH_SHORT).show();
                return;
            }
            loadPalletData(tag);
        });

        // Scan barcode button for Pallet ID
        btnScanBarcode.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ready to scan Pallet Barcode/QR", Toast.LENGTH_SHORT).show();
            etPalletTagId.requestFocus();
        });

        // Mode switches
        btnModeBox.setOnClickListener(v -> {
            currentScanMode = "Box";
            updateScanModeUI();
        });

        btnModePiece.setOnClickListener(v -> {
            currentScanMode = "Piece";
            updateScanModeUI();
        });

        // RFID Action buttons
        btnScanRFID.setOnClickListener(v -> startScan());
        btnStopRFID.setOnClickListener(v -> stopScan());
        btnClearRFID.setOnClickListener(v -> clearScannedData());
        btnConfirmSave.setOnClickListener(v -> confirmSave());

        // Notification popup
        ivNotification.setOnClickListener(v -> showNotificationDialog());
    }

    private void updateScanModeUI() {
        if ("Box".equalsIgnoreCase(currentScanMode)) {
            btnModeBox.setBackgroundColor(0xFFFF9800); // Orange
            btnModeBox.setTextColor(0xFFFFFFFF);
            btnModePiece.setBackgroundColor(0xFFE0E0E0); // Light Gray
            btnModePiece.setTextColor(0xFF424242);
        } else {
            btnModePiece.setBackgroundColor(0xFFFF9800); // Orange
            btnModePiece.setTextColor(0xFFFFFFFF);
            btnModeBox.setBackgroundColor(0xFFE0E0E0); // Light Gray
            btnModeBox.setTextColor(0xFF424242);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUHFCallbacks();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    private void setupUHFCallbacks() {
        if (mContext == null || mContext.uhf == null) return;

        // Hardware trigger key callback (handles pistol grip trigger)
        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                if (mContext != null && !mContext.isScanning) {
                    startScan();
                }
            }

            @Override
            public void onKeyUp(int keycode) {
                if (mContext != null && mContext.isScanning) {
                    stopScan();
                }
            }
        });

        // Inventory tag stream callback
        mContext.uhf.setInventoryCallback(new IUHFInventoryCallback() {
            @Override
            public void callback(UHFTAGInfo uhftagInfo) {
                if (uhftagInfo != null && !TextUtils.isEmpty(uhftagInfo.getEPC())) {
                    Message msg = mHandler.obtainMessage(MSG_TAG_READ, uhftagInfo.getEPC());
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void startScan() {
        if (mContext == null || mContext.uhf == null) {
            Toast.makeText(getContext(), "UHF device not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mContext.uhf.getConnectStatus() != ConnectionStatus.CONNECTED) {
            Toast.makeText(getContext(), "UHF reader not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (itemList.isEmpty()) {
            Toast.makeText(getContext(), "Please load Picking Order items first", Toast.LENGTH_SHORT).show();
            return;
        }

        setupUHFCallbacks();

        if (mContext.uhf.startInventoryTag()) {
            mContext.isScanning = true;
            btnScanRFID.setEnabled(false);
            btnStopRFID.setEnabled(true);
            btnConfirmSave.setEnabled(false);
        } else {
            Toast.makeText(getContext(), "Failed to start inventory", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        if (mContext != null && mContext.uhf != null) {
            if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                mContext.uhf.stopInventory();
            }
            mContext.isScanning = false;
        }

        if (btnScanRFID != null) btnScanRFID.setEnabled(true);
        if (btnStopRFID != null) btnStopRFID.setEnabled(false);
        if (btnConfirmSave != null) {
            btnConfirmSave.setEnabled(!allScannedTags.isEmpty());
        }
    }

    /**
     * Core matching logic ported from Gate's ProcessTagBatch:
     * Matches scanned EPC against loaded order items based on Barcode / Partial RFID / Tag Number.
     */
    private void processScannedTag(String tagEpc) {
        if (allScannedTags.contains(tagEpc)) {
            return; // Already matched and counted
        }

        String extractedBarcode = tagEpc.length() >= 6 ? tagEpc.substring(0, 6) : "";

        // Find matching items from itemList
        List<PickPackItem> matchedItems = new ArrayList<>();
        for (PickPackItem item : itemList) {
            boolean isBarcodeMatch = !TextUtils.isEmpty(item.getBarcode()) && item.getBarcode().equals(extractedBarcode);
            boolean isTagMatch = tagEpc.equals(item.getPalletTagNumber());
            boolean isPartialMatch = tagEpc.startsWith("C") && !TextUtils.isEmpty(item.getGenRfidPartial())
                    && item.getGenRfidPartial().equalsIgnoreCase(tagEpc);

            if (isBarcodeMatch || isTagMatch || isPartialMatch) {
                matchedItems.add(item);
            }
        }

        // Check product stock existence
        boolean foundInStock = false;
        if (!tagEpc.startsWith("C")) {
            for (ProductItem pi : stockList) {
                if (tagEpc.equalsIgnoreCase(pi.getTagNumber())) {
                    foundInStock = true;
                    break;
                }
            }
            if (!foundInStock && !notifiedTags.contains(tagEpc)) {
                notifiedTags.add(tagEpc);
                addNotification("Tag Not In Stock", "Tag [" + tagEpc + "] was not found in stock.");
            }
        }

        // Priority: Find first item that is not fully scanned
        PickPackItem targetItem = null;
        for (PickPackItem item : matchedItems) {
            if (item.getScannedCount() < item.getTotalTags()) {
                targetItem = item;
                break;
            }
        }

        if (targetItem != null) {
            int itemId = targetItem.getPickingOrderItemId();
            Set<String> set = scannedTagsByItemId.computeIfAbsent(itemId, k -> new HashSet<>());

            if (!set.contains(tagEpc)) {
                set.add(tagEpc);
                allScannedTags.add(tagEpc);
                targetItem.setScannedCount(set.size());
                targetItem.setHighlighted(true);

                Utils.playSound(1); // Success beep

                adapter.notifyDataSetChanged();
                updateDashboardCounts();
            }
        } else if (!matchedItems.isEmpty()) {
            // All matched items are already full
            if (!notifiedTags.contains(tagEpc)) {
                notifiedTags.add(tagEpc);
                addNotification("Over Scan", "Tag [" + tagEpc + "] exceeds required quantity for matched item.");
            }
            Utils.playSound(2); // Warning beep
        } else {
            // Tag does not match any item in the order
            if (!notifiedTags.contains(tagEpc)) {
                notifiedTags.add(tagEpc);
                addNotification("Unmatched Tag", "Tag [" + tagEpc + "] does not match any item in this pallet.");
            }
            Utils.playSound(2);
        }
    }

    private void addNotification(String title, String message) {
        notificationMessages.add(title + ": " + message);
        if (tvNotificationCount != null) {
            tvNotificationCount.setText(String.valueOf(notificationMessages.size()));
            tvNotificationCount.setVisibility(View.VISIBLE);
        }
    }

    private void showNotificationDialog() {
        if (notificationMessages.isEmpty()) {
            Toast.makeText(getContext(), "No notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] items = notificationMessages.toArray(new CharSequence[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("แจ้งเตือน (" + notificationMessages.size() + ")")
                .setItems(items, null)
                .setPositiveButton("Clear All", (dialog, which) -> {
                    notificationMessages.clear();
                    tvNotificationCount.setVisibility(View.GONE);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void updateDashboardCounts() {
        int pallets = TextUtils.isEmpty(currentPalletTagId) ? 0 : 1;
        int totalBoxes = 0;
        int packedCount = 0;

        for (PickPackItem item : itemList) {
            totalBoxes += item.getTotalTags();
            if (item.getScannedCount() >= item.getTotalTags() && item.getTotalTags() > 0) {
                packedCount++;
            }
        }

        tvPalletsCount.setText(String.valueOf(pallets));
        tvBoxesCount.setText(String.valueOf(totalBoxes));
        tvPackedCount.setText(String.valueOf(packedCount));
        tvScannedCount.setText(String.valueOf(allScannedTags.size()));
        tvInStockCount.setText(String.valueOf(stockList.size()));

        if (btnConfirmSave != null && !mContext.isScanning) {
            btnConfirmSave.setEnabled(!allScannedTags.isEmpty());
        }
    }

    private void clearScannedData() {
        allScannedTags.clear();
        scannedTagsByItemId.clear();
        notifiedTags.clear();
        notificationMessages.clear();
        tvNotificationCount.setVisibility(View.GONE);

        for (PickPackItem item : itemList) {
            item.setScannedCount(0);
            item.setHighlighted(false);
        }

        adapter.notifyDataSetChanged();
        updateDashboardCounts();
        Toast.makeText(getContext(), "Scanned data cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Load Picking Order items for the given Pallet Tag ID
     */
    private void loadPalletData(String palletTagId) {
        stopScan();
        progressBar.setVisibility(View.VISIBLE);

        apiService.getPickPackOrderByPalletTagId(palletTagId).enqueue(new Callback<List<PickPackItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<PickPackItem>> call,
                                   @NonNull Response<List<PickPackItem>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickPackItem> items = response.body();
                    if (!items.isEmpty()) {
                        currentPalletTagId = palletTagId;
                        itemList.clear();
                        allScannedTags.clear();
                        scannedTagsByItemId.clear();

                        for (int i = 0; i < items.size(); i++) {
                            PickPackItem item = items.get(i);
                            item.setEvenRow(i % 2 == 0);
                            itemList.add(item);
                        }

                        adapter.notifyDataSetChanged();
                        loadProductInStock();
                        updateDashboardCounts();
                        Toast.makeText(getContext(), "Loaded " + items.size() + " items successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No items found for Pallet Tag ID: " + palletTagId, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load order: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PickPackItem>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Preload product items in stock from server for tag validation
     */
    private void loadProductInStock() {
        apiService.getPickPackProductInStock().enqueue(new Callback<List<ProductItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductItem>> call,
                                   @NonNull Response<List<ProductItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stockList.clear();
                    stockList.addAll(response.body());
                    tvInStockCount.setText(String.valueOf(stockList.size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load in-stock products: " + t.getMessage());
            }
        });
    }

    /**
     * Saves the scanned tags by posting to /MobileGate/pick-and-pack
     */
    private void confirmSave() {
        if (allScannedTags.isEmpty()) {
            Toast.makeText(getContext(), "No scanned tags to save", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("ยืนยันการบันทึก Pick & Pack")
                .setMessage("ต้องการบันทึกแท็กทั้งหมดจำนวน " + allScannedTags.size() + " แท็ก ใช่หรือไม่?")
                .setPositiveButton("ยืนยัน", (dialog, which) -> executeSave())
                .setNegativeButton("ยกเลิก", null)
                .show();
    }

    private void executeSave() {
        stopScan();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("กำลังบันทึกข้อมูล Pick & Pack...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Build List<PickAndPackTagDto> payload matching gate's payload
        List<PickAndPackTagDto> tagsToSave = new ArrayList<>();

        for (Map.Entry<Integer, Set<String>> entry : scannedTagsByItemId.entrySet()) {
            int orderItemId = entry.getKey();
            Set<String> tags = entry.getValue();

            PickPackItem matchedItem = null;
            for (PickPackItem it : itemList) {
                if (it.getPickingOrderItemId() == orderItemId) {
                    matchedItem = it;
                    break;
                }
            }

            if (matchedItem == null) continue;

            for (String tagEpc : tags) {
                // Find matching productItem in stock for tagId / productItemId
                ProductItem matchedProduct = null;
                for (ProductItem pi : stockList) {
                    if (tagEpc.equalsIgnoreCase(pi.getTagNumber())) {
                        matchedProduct = pi;
                        break;
                    }
                }

                PickAndPackTagDto dto = new PickAndPackTagDto();
                dto.setPickingOrderId(matchedItem.getPickingOrderId());
                dto.setPickingOrderItemId(matchedItem.getPickingOrderItemId());
                dto.setTagNumber(tagEpc);
                dto.setPalletTagNumber(currentPalletTagId);
                dto.setAllocatedQty(1.0);
                dto.setRemark(matchedItem.getRemark());

                if (matchedProduct != null) {
                    dto.setProductItemId(matchedProduct.getProductItemId());
                    if (matchedProduct.getId() != null) {
                        dto.setTagId(matchedProduct.getId().intValue());
                    }
                }

                tagsToSave.add(dto);
            }
        }

        apiService.savePickAndPack(tagsToSave).enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ResponseDto>> call,
                                   @NonNull Response<List<ResponseDto>> response) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<ResponseDto> results = response.body();
                    boolean allSuccess = true;
                    StringBuilder errorMsg = new StringBuilder();

                    for (ResponseDto r : results) {
                        if (!r.isSuccess()) {
                            allSuccess = false;
                            if (!TextUtils.isEmpty(r.getMessage())) {
                                errorMsg.append(r.getMessage()).append("\n");
                            }
                        }
                    }

                    if (allSuccess) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("บันทึกสำเร็จ")
                                .setMessage("บันทึกข้อมูล Pick & Pack เรียบร้อยแล้ว (" + tagsToSave.size() + " tags)")
                                .setPositiveButton("ตกลง", (d, w) -> {
                                    clearScannedData();
                                    loadPalletData(currentPalletTagId);
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("พบข้อผิดพลาด")
                                .setMessage(errorMsg.length() > 0 ? errorMsg.toString() : "บางรายการบันทึกไม่สำเร็จ")
                                .setPositiveButton("ตกลง", null)
                                .show();
                    }
                } else {
                    Toast.makeText(getContext(), "Server error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ResponseDto>> call, @NonNull Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
