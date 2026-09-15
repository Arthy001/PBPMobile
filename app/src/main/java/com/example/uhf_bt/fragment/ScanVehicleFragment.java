package com.example.uhf_bt.fragment;

import static com.example.uhf_bt.services.Settings.appMode;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.ReceivePalletAdapter;
import com.example.uhf_bt.models.Kanban;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.VehicleDto;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.example.uhf_bt.tool.CheckUtils;
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

public class ScanVehicleFragment extends Fragment {

    private static final String TAG = "ScanVehicleFragment";

    private List<String> pendingKanbanTags = new ArrayList<>();
    private boolean isVehicleScanned = false;
    private EditText EtData_Read, tvBarcode;
    private TextView tvTargetInfo;
    private Button BtRead, BtRead2, btnScanBarcode;
    private LinearLayout view_barcode;
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
    private List<PickingOrderItem> stockList = new ArrayList<>();
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private List<VehicleDto> vehiclesList = new ArrayList<>();
    
    private String selectedVehicleId;
    private String selectedVehicleLicense;
    private String selectedVehicleTag;
    private String tagString = "";
    private TextView tvTotalDO, tvTotalPallet, tvTotalQty, tvTotalSuccessSummary, tvTotalScanSummary, tvTotalKanbanCountSummary;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Button btScanRFID, btStop, btClear, btnConfirm;

    private Set<String> scannedKanbans = new HashSet<>();
    private TextView tvTotalKanbanCount;

    // RFID Variables
    private EditText etTime;
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
    private List<UHFTAGInfo> tempDatas = new ArrayList<>();
    private ArrayList<HashMap<String, String>> tagList;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_TIME_OVER:
                    stop();
                    break;
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        btScanRFID.setEnabled(true);
                        btnConfirm.setEnabled(isVehicleScanned && !scannedKanbans.isEmpty());
                    }
                    break;
                case FLAG_UHFINFO_LIST:
                    List<UHFTAGInfo> list = (List<UHFTAGInfo>) msg.obj;
                    for (UHFTAGInfo info : list) addEPC(info);
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        btScanRFID.setEnabled(false);
                    }
                    break;
                case FLAG_UHFINFO:
                    addEPC((UHFTAGInfo) msg.obj);
                    break;
            }
        }
    };

    private void addEPC(UHFTAGInfo info) {
        boolean[] exists = new boolean[1];
        int idx = CheckUtils.getInsertIndex(tempDatas, info, exists);
        if (!exists[0]) {
            tempDatas.add(idx, info);
            HashMap<String, String> tagMap = new HashMap<>();
            tagMap.put(MainActivity.TAG_EPC, info.getEPC());
            if (tagList == null) tagList = new ArrayList<>();
            tagList.add(idx, tagMap);
            
            processScannedTag(info.getEPC());
        }
    }

    public static ScanVehicleFragment newInstance() {
        return new ScanVehicleFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        loadVehicles();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_vehicle, container, false);
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
        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalSuccessSummary = view.findViewById(R.id.tvTotalSuccessSummary);
        tvTotalScanSummary = view.findViewById(R.id.tvTotalScanSummary);
        tvTotalDO = view.findViewById(R.id.tvTotalDO);
        tvTotalPallet = view.findViewById(R.id.tvTotalPallet);
        tvTotalKanbanCountSummary = view.findViewById(R.id.tvTotalKanbanCountSummary);
        
        tvTargetInfo = view.findViewById(R.id.tvTargetInfo);
        tvTotalKanbanCount = view.findViewById(R.id.tvTotalKanbanCount);
        EtData_Read = view.findViewById(R.id.EtData_Read);
        BtRead = view.findViewById(R.id.btnRead);
        BtRead2 = view.findViewById(R.id.btnRead2);
        btScanRFID = view.findViewById(R.id.btnScanRFID);
        btStop = view.findViewById(R.id.btnStop);
        btClear = view.findViewById(R.id.btnClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        etTime = view.findViewById(R.id.etTime);

        tvBarcode = view.findViewById(R.id.tvBarcode);
        btnScanBarcode = view.findViewById(R.id.btnScanBarcode);
        view_barcode = view.findViewById(R.id.view_barcode);

        if ("RFID".equals(appMode)) {
            view_barcode.setVisibility(View.GONE);
            BtRead2.setVisibility(View.GONE);
        }


    }

    private void setupToolbar() {
        toolbar_title.setText("จัดการรถ (RFID)");
        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        ivNotification.setOnClickListener(v -> showNotificationsDialog());
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupListView() {
        adapter = new ReceivePalletAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        btScanRFID.setOnClickListener(v -> startThread());
        btStop.setOnClickListener(v -> stop());
        btClear.setOnClickListener(v -> clearData());
        btnConfirm.setOnClickListener(v -> confirmTemp());
        btnScanBarcode.setOnClickListener(v -> scanBarcode());
        
        // BtRead ทำหน้าที่สั่งยิงสแกนจากหัวอ่าน hardware
        BtRead.setOnClickListener(v -> readTagFromHardware());
        
        // BtRead2 ทำหน้าที่ประมวลผลค่าที่อยู่ในช่องกรอก (Manual Input)
        BtRead2.setOnClickListener(v -> {
            String manualData = EtData_Read.getText().toString().trim();
            if (!manualData.isEmpty()) {
                processScannedTag(manualData);
            } else {
                Toast.makeText(mContext, "กรุณากรอกข้อมูลแท็ก", Toast.LENGTH_SHORT).show();
            }
        });

        if (mContext != null && mContext.uhf != null) {
            mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
                @Override
                public void onKeyDown(int keycode) {
                    if (keycode == 1 || keycode == 3) {
                        if (mContext.isScanning) stop(); else startThread();
                    }
                }
                @Override
                public void onKeyUp(int keycode) {}
            });
        }
    }

    private void readTagFromHardware() {
        if (mContext == null || mContext.uhf == null) return;
        String data = mContext.uhf.readData("00000000", 1, 2, 6);
        if (data != null && data.length() > 0) {
            data = data.trim();
            EtData_Read.setText(data);
            processScannedTag(data);
        } else {
            Toast.makeText(mContext, "สแกนไม่ติด หรือไม่มีแท็กในระยะ", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadVehicles() {
        apiService.getVehicles().enqueue(new Callback<List<VehicleDto>>() {
            @Override
            public void onResponse(Call<List<VehicleDto>> call, Response<List<VehicleDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vehiclesList = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<VehicleDto>> call, Throwable t) {
                showError("โหลดข้อมูลรถล้มเหลว: " + t.getMessage());
            }
        });
    }

    private void processScannedTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return;
        
        tag = tag.trim();
        String type = tag.substring(0, 1).toUpperCase();
        
        if (type.equals("D")) {
            processVehicleTag(tag);
        } else if (type.equals("B")) {
            processKanbanTag(tag);
        } else {
            addNotification("Unknown Tag: " + tag);
        }
    }

    private void processVehicleTag(String tag) {
        if (tag == null) return;
        tag = tag.trim();
        VehicleDto vehicle = null;
        for (VehicleDto v : vehiclesList) {
            if (v.getVehicleTag() != null && tag.equalsIgnoreCase(v.getVehicleTag().trim())) {
                vehicle = v;
                break;
            }
        }

        if (vehicle != null) {
            final VehicleDto finalVehicle = vehicle;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    selectedVehicleId = String.valueOf(finalVehicle.getId());
                    selectedVehicleLicense = finalVehicle.getLicensePlate();
                    selectedVehicleTag = finalVehicle.getVehicleTag();
                    tvTargetInfo.setText(finalVehicle.getLicensePlate() + " (" + finalVehicle.getDriver() + ")");
                    tvTargetInfo.setTextColor(Color.parseColor("#01579B"));
                    isVehicleScanned = true;
                    loadAvailableKanbans();
                    Utils.playSound(1);
                });
            }
        } else {
            addNotification("ไม่พบรถที่มี Tag: " + tag);
        }
    }

    private void loadAvailableKanbans() {
        showLoading(true);
        apiService.getAvailableKanbansForVehicle(selectedVehicleId).enqueue(new Callback<List<Kanban>>() {
            @Override
            public void onResponse(Call<List<Kanban>> call, Response<List<Kanban>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderItem> items = convertToPickingItems(response.body());
                    itemList = items;
                    stockList = new ArrayList<>(items);
                    updateUI(items);
                    processPendingTags();
                }
            }
            @Override
            public void onFailure(Call<List<Kanban>> call, Throwable t) {
                showLoading(false);
                showError("โหลด Kanban ล้มเหลว");
            }
        });
    }

    private List<PickingOrderItem> convertToPickingItems(List<Kanban> kanbans) {
        List<PickingOrderItem> items = new ArrayList<>();
        for (Kanban k : kanbans) {
            PickingOrderItem item = new PickingOrderItem();
            item.setId((int) k.getId());
            item.setPickingNumber(k.getKanbanNo());
            item.setProductName(k.getKanbanName());
            item.setTagNumber(k.getKanbanTagId());
            item.setQuantity(1);
            item.setQuantityScan(0);
            item.setQuantitySuccess(0);
            item.setBoxType("KANBAN");
            item.setPalletStatus(k.getKanbanStatus());
            items.add(item);
        }
        return items;
    }

    private void processKanbanTag(String tag) {
        if (!isVehicleScanned) {
            pendingKanbanTags.add(tag);
            addNotification("สแกนรถก่อนจัดการ Kanban");
            return;
        }

        // ตรวจสอบว่าเป็น Barcode แบบที่มี "B" นำหน้าหรือไม่
        if (tag.toUpperCase().startsWith("B")) {
            try {
                String kanbanNumber = tag;
                loadKanbanDetail(kanbanNumber);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing Kanban barcode: " + tag);
            }
        }

        // สำหรับ RFID หรือกรณีอื่นๆ ส่ง tag ไปตรงๆ
        loadKanbanDetail(tag);
    }

    private void loadKanbanDetail(String kanbanTagId) {
        showLoading(true);
        apiService.getKanbanTransaction(kanbanTagId).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderItem> details = response.body();
                    if (details.isEmpty()) {
                        addNotification("ไม่พบข้อมูลใน Kanban: " + kanbanTagId);
                        return;
                    }

                    // ดึงข้อมูล ID จริงจากรายการที่ส่งกลับมา
                    int actualKanbanId = details.get(0).getKanbanId();
                    String actualNumber = details.get(0).getNumber();

                    // เก็บข้อมูลว่า Kanban นี้สแกนแล้วเพื่อใช้ตอนยืนยัน
                    scannedKanbans.add(kanbanTagId); 

                    // เพิ่มรายการสินค้าจริงเข้าไปใน itemList (ถ้ายังไม่มี)
                    for (PickingOrderItem detail : details) {
                        // กรองรายการ: ต้องมี qtyBox และ Quantity > 0
                        if (detail.getQtyBox() == null || detail.getQtyBox().trim().isEmpty() || detail.getQuantity() <= 0) {
                            continue; // ข้ามรายการที่ไม่เข้าเงื่อนไข
                        }

                        boolean itemExists = false;
                        for (PickingOrderItem existing : itemList) {
                            if (existing.getId() == detail.getId() && !"KANBAN".equals(existing.getBoxType())) {
                                // ถ้ามีอยู่แล้ว ให้อัปเดตยอดให้เป็นสำเร็จ
                                existing.setQuantityScan(existing.getQuantity());
                                existing.setQuantitySuccess(existing.getQuantity());
                                itemExists = true;
                                break;
                            }
                        }
                        if (!itemExists) {
                            // ถ้าเป็นรายการใหม่ ให้เซ็ตยอด Scan และ Success ให้เท่ากับ Qty
                            detail.setQuantityScan(detail.getQuantity());
                            detail.setQuantitySuccess(detail.getQuantity());
                            itemList.add(detail);
                        }
                    }

                    // เก็บข้อมูลว่า Kanban นี้สแกนแล้ว
                    scannedKanbans.add(kanbanTagId); 

                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        updateUI(itemList); // ใช้ updateUI เพื่อให้ Adapter รับ List ใหม่
                        btnConfirm.setEnabled(true);
                    });
                    Utils.playSound(1);
                } else {
                    // ดึงข้อความ Error จาก Server มาแสดง (Alert)
                    String errorMsg = "โหลดรายละเอียด Kanban ล้มเหลว";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            // แบบง่าย: ถ้ามี "message" ใน json
                            if (errorJson.contains("message")) {
                                errorMsg = errorJson.split("\"message\":")[1].split(",")[0].replace("\"", "").replace("}", "");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                showLoading(false);
                showError("ข้อผิดพลาดเครือข่าย: " + t.getMessage());
            }
        });
    }

    private void processPendingTags() {
        List<String> tags = new ArrayList<>(pendingKanbanTags);
        pendingKanbanTags.clear();
        for (String t : tags) processKanbanTag(t);
    }

    private void updateTotals() {
        double totalQty = 0;
        double totalScan = 0;
        double totalSuccess = 0;

        Set<String> uniqueDOs = new HashSet<>();
        Set<String> uniquePallets = new HashSet<>();
        int kanbanCount = scannedKanbans.size();

        for (PickingOrderItem item : itemList) {
            // ไม่ต้องเช็ค BoxType เป็น KANBAN แล้วเพราะเราเอาออกไปแล้ว
            totalQty += item.getQuantity();
            totalScan += item.getQuantityScan();
            totalSuccess += item.getQuantitySuccess();

            if (item.getPickingNumber() != null && !item.getPickingNumber().isEmpty()) {
                uniqueDOs.add(item.getPickingNumber());
            }
            if (item.getPalletNumber() != null && !item.getPalletNumber().isEmpty()) {
                uniquePallets.add(item.getPalletNumber());
            }
        }

        if (getActivity() != null) {
            final double finalTotalQty = totalQty;
            final double finalTotalScan = totalScan;
            final double finalTotalSuccess = totalSuccess;
            final int finalKanbanCount = kanbanCount;
            final int doCount = uniqueDOs.size();
            final int palletCount = uniquePallets.size();

            getActivity().runOnUiThread(() -> {
                if (tvTotalQty != null) tvTotalQty.setText(String.format("%.0f", finalTotalQty));
                if (tvTotalScanSummary != null) tvTotalScanSummary.setText(String.format("%.0f", finalTotalScan));
                if (tvTotalSuccessSummary != null) tvTotalSuccessSummary.setText(String.format("%.0f", finalTotalSuccess));

                if (tvTotalDO != null) tvTotalDO.setText(String.valueOf(doCount));
                if (tvTotalPallet != null) tvTotalPallet.setText(String.valueOf(palletCount));
                if (tvTotalKanbanCountSummary != null) tvTotalKanbanCountSummary.setText(String.valueOf(finalKanbanCount));
                
                // TextView ตัวเดิมที่แสดงในสีฟ้าข้างรถ
                if (tvTotalKanbanCount != null) tvTotalKanbanCount.setText(String.valueOf(finalKanbanCount));
            });
        }
    }

    private void updateUI(List<PickingOrderItem> items) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                adapter.updateItems(items);
                updateTotals();
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void confirmTemp(){
        confirmTransaction();
    }

    private void confirmTransaction() {
        if (scannedKanbans.isEmpty()) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("ยืนยันการบันทึก")
            .setMessage("ต้องการบันทึกการนำ Kanban เข้า " + selectedVehicleLicense + " ใช่หรือไม่?")
            .setPositiveButton("ยืนยัน", (d, w) -> performConfirm())
            .setNegativeButton("ยกเลิก", null)
            .show();
    }

    private void performConfirm() {
        if (selectedVehicleTag == null || scannedKanbans.isEmpty()) {
            Toast.makeText(mContext, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Kanban> kanbanList = new ArrayList<>();
        for (String tagId : scannedKanbans) {
            Kanban k = new Kanban();
            k.setKanbanTagId(tagId);
            kanbanList.add(k);
        }

        apiService.createMobileVehicleLoading(selectedVehicleTag, kanbanList).enqueue(new Callback<List<ResponseDto>>() {
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean allSuccess = true;
                    StringBuilder sb = new StringBuilder();
                    for (ResponseDto r : response.body()) {
                        if (!r.isSuccess()) {
                            allSuccess = false;
                            sb.append(r.getMessage()).append("\n");
                        }
                    }

                    if (allSuccess) {
                        Toast.makeText(mContext, "บันทึกสำเร็จ", Toast.LENGTH_SHORT).show();
                        clearData();
                    } else {
                        showError("บางรายการล้มเหลว: " + sb.toString());
                    }
                } else {
                    showError("บันทึกล้มเหลว: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                showError("บันทึกล้มเหลว: " + t.getMessage());
            }
        });
    }

    private void clearData() {
        scannedKanbans.clear();
        scannedTags.clear();
        isVehicleScanned = false;
        selectedVehicleId = null;
        selectedVehicleLicense = null;
        selectedVehicleTag = null;
        if (getActivity() != null) getActivity().runOnUiThread(() -> {
            tvTargetInfo.setText("กรุณาสแกน Tag รถ");
            tvTargetInfo.setTextColor(Color.GRAY);
            updateUI(new ArrayList<>());
            btnConfirm.setEnabled(false);
        });
    }

    private void startThread() {
        if (mContext == null || mContext.uhf == null) return;
        mContext.uhf.setInventoryCallback(uhftagInfo -> handler.sendMessage(handler.obtainMessage(FLAG_UHFINFO, uhftagInfo)));
        mContext.isScanning = true;
        if (mContext.uhf.startInventoryTag()) {
            mStrTime = System.currentTimeMillis();
            handler.sendEmptyMessage(FLAG_START);
        } else {
            mContext.isScanning = false;
        }
    }

    private void stop() {
        if (mContext != null && mContext.uhf != null) mContext.uhf.stopInventory();
        mContext.isScanning = false;
        handler.sendEmptyMessage(FLAG_STOP);
    }

    private void readTag() {
        if (mContext == null || mContext.uhf == null) return;
        String data = mContext.uhf.readData("00000000", 1, 2, 6);
        if (data != null && data.length() > 0) {
            EtData_Read.setText(data);
            processScannedTag(data);
        } else {
            Toast.makeText(mContext, "สแกนไม่ติด", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNotification(String msg) {
        notificationMessages.add(msg);
        hasNotifications = true;
        updateNotificationVisibility();
    }

    private void updateNotificationVisibility() {
        if (ivNotification != null) {
            ivNotification.setVisibility(hasNotifications ? View.VISIBLE : View.GONE);
            tvNotificationCount.setVisibility(hasNotifications ? View.VISIBLE : View.GONE);
            tvNotificationCount.setText(String.valueOf(notificationMessages.size()));
        }
    }

    private void showNotificationsDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("แจ้งเตือน")
            .setItems(notificationMessages.toArray(new String[0]), null)
            .setPositiveButton("ปิด", (d, w) -> {
                notificationMessages.clear();
                hasNotifications = false;
                updateNotificationVisibility();
            })
            .show();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void scanBarcode() {
        if (tvBarcode == null) return;
        String barcode = tvBarcode.getText().toString().trim();
        if (!barcode.isEmpty()) {
            processScannedTag(barcode);
            tvBarcode.setText(""); // ล้างค่าหลังสแกน
        } else {
            Toast.makeText(mContext, "กรุณากรอกหรือสแกน Barcode", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) mContext = (MainActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext != null && mContext.uhf != null) {
            btScanRFID.setEnabled(mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED);
        }
    }
}
