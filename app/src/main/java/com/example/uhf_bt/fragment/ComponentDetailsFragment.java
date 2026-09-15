package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.PickingComponentAdapter;
import com.example.uhf_bt.adapter.ScannedTagAdapter;
import com.example.uhf_bt.models.PickingComponentItem;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.PickingTagDto;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.ScannedTagItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComponentDetailsFragment extends Fragment {

    private static final String TAG = "ComponentDetailsFragment";
    private Toolbar toolbar;
    private TextView tvTitle;
    private ImageView ivBack;
    private ListView componentListView;
    private TextView tvComponentSummary;
    //private Button btnSaveComponents;
    private Button btnCloseComponents;
    private List<PickingOrderItem> itemList
    = new ArrayList<>();
    private List<PickingComponentItem> componentsList = new ArrayList<>();
    private PickingComponentAdapter componentAdapter;
    private int currentComponentPosition = -1;

    // For scanning
    private String currentScanningComponentCode = null;
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private List<ProductItem> stockList;
    private List<ProductItem> itemstock;
    private String pickingOrderId;
    private String productName;
    private String remarkString;
    private String barcode = "";
    private MainActivity mContext;
    private ApiService apiService;
    private List<PickingOrderTransaction> allTransactions = new ArrayList<>();

    // View Read section
    private LinearLayout viewRead;

    //private EditText EtData_QTY;
    private TextView tvSelectedItem;
    private TextView tvSelectedQty;
    private TextView tvPickedQty;
    private TextView tvScannedQty;
    private EditText EtData_Read;
    private Button BtRead, BtRead2;
    private Button BtSubmit;
    private ProgressBar progressBar;

    // เพิ่มตัวแปรสำหรับ viewRead UI
    private TextView tvReadTitle;
    private Button btnCloseViewRead;

    // เพิ่มตัวแปรสำหรับ ListView และ adapter ใหม่
    private ListView tagListView;
    private List<ScannedTagItem> scannedTagItems = new ArrayList<>();
    private ScannedTagAdapter scannedTagAdapter;


    public interface ComponentDetailsListener {
        void onComponentsSaved(List<PickingOrderItem> scannedComponents);
        void onComponentDetailsClosed();
        void onComponentDetailsClosed(boolean needRefresh); // เพิ่ม method ใหม่
        List<ProductItem> getStockList();
    }

    private ComponentDetailsListener listener;

    public static ComponentDetailsFragment newInstance(String pickingOrderId, String productName, String remarkString, String pickingOrderItemId) {
        ComponentDetailsFragment fragment = new ComponentDetailsFragment();
        Bundle args = new Bundle();
        args.putString("pickingOrderId", pickingOrderId);
        args.putString("productName", productName);
        args.putString("remarkString", remarkString);
        args.putString("pickingOrderItemId", pickingOrderItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
        }

        // Try to get the parent fragment as listener
        try {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ComponentDetailsListener) {
                listener = (ComponentDetailsListener) parentFragment;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent fragment must implement ComponentDetailsListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel2));
        }
        if (getArguments() != null) {
            pickingOrderId = getArguments().getString("pickingOrderId");
            productName = getArguments().getString("productName");
            remarkString = getArguments().getString("remarkString");
            //barcode = getArguments().getString("barcode");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        scannedTags = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_component_details, container, false);
        initViews(view);

        // Get stock list from parent fragment
        if (listener != null) {
            stockList = listener.getStockList();
        } else {
            stockList = new ArrayList<>();
        }

        // Parse components from rema.rk
        parseComponents(remarkString);


        loadPickingOrderTransactions();
        return view;
    }

    @SuppressLint("LongLogTag")
    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvTitle = view.findViewById(R.id.toolbar_title);
        tvTitle.setText(productName + "");
        ivBack = view.findViewById(R.id.ivBack);

        componentListView = view.findViewById(R.id.componentListView);
        tvComponentSummary = view.findViewById(R.id.tvComponentSummary);
        //btnSaveComponents = view.findViewById(R.id.btnSaveComponents);
        btnCloseComponents = view.findViewById(R.id.btnCloseComponents);

        // เพิ่มการผูกปุ่ม Close และ title
        btnCloseViewRead = view.findViewById(R.id.btnCloseViewRead);
        tvReadTitle = view.findViewById(R.id.tvReadTitle);

        // Initialize viewRead section
        viewRead = view.findViewById(R.id.viewRead);
        EtData_Read = view.findViewById(R.id.EtData_Read);
        //EtData_QTY = view.findViewById(R.id.EtData_QTY);
        BtRead = view.findViewById(R.id.BtRead);
        BtRead2 = view.findViewById(R.id.BtRead2);
        BtSubmit = view.findViewById(R.id.BtSubmit);

        // Add TextViews for Item, QTY, QTY Scan
        // We'll add these programmatically to the first LinearLayout in viewRead
        LinearLayout firstLayout = (LinearLayout) viewRead.getChildAt(0);

        // Create and add tvSelectedItem before existing EditText
        tvSelectedItem = view.findViewById(R.id.tvSelectedItem);

        // Add QTY and QTY Scan information to the second LinearLayout
        LinearLayout secondLayout = (LinearLayout) viewRead.getChildAt(1);

        // Create and add tvSelectedQty
        tvSelectedQty = view.findViewById(R.id.tvSelectedQty);
        
        tvPickedQty = view.findViewById(R.id.tvPickedQty);

        // Create and add tvScannedQty
        tvScannedQty = view.findViewById(R.id.tvScannedQty);

        progressBar = view.findViewById(R.id.progressBar);

        // Initially hide viewRead section
        viewRead.setVisibility(View.GONE);

        // Set up component list
        componentsList = new ArrayList<>();
        componentAdapter = new PickingComponentAdapter(requireContext(), componentsList, null);
        componentListView.setAdapter(componentAdapter);

        // Set item click listener for ListView
        componentListView.setOnItemClickListener((parent, view1, position, id) -> {
            showComponentScanningView(position);
        });

        // ใน method initViews ที่ตั้งค่า event listener สำหรับปุ่ม
        btnCloseComponents.setOnClickListener(v -> {
            // เพิ่มการ Log เพื่อตรวจสอบว่าถูกกดหรือไม่
            Log.d(TAG, "Close button clicked");

            // เพิ่มการตรวจสอบ listener ว่าเป็น null หรือไม่
            if (listener != null) {
                Log.d(TAG, "Listener found, calling onComponentDetailsClosed");
                listener.onComponentDetailsClosed();
            } else {
                Log.e(TAG, "Listener is null, cannot close fragment");
                // ถ้า listener เป็น null ให้พยายาม back กลับด้วยวิธีอื่น
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                } else {
                    // ถ้าไม่มี activity ให้แสดงข้อความ
                    Toast.makeText(requireContext(), "Cannot close, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        BtRead.setOnClickListener(v -> read());
        BtRead2.setOnClickListener(v -> read2());
        BtSubmit.setOnClickListener(v -> submitScan());

        // เพิ่ม listener สำหรับปุ่ม Close viewRead
        btnCloseViewRead.setOnClickListener(v -> hideViewRead());
        ivBack.setOnClickListener(v -> hideViewRead());


        // Set back button in toolbar
        //

        // เพิ่ม ListView สำหรับแสดง tag ที่สแกนได้
        tagListView = view.findViewById(R.id.tagListView);
        scannedTagItems = new ArrayList<>();
        scannedTagAdapter = new ScannedTagAdapter(requireContext(), scannedTagItems);
        tagListView.setAdapter(scannedTagAdapter);

        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                read();
            }

            @Override
            public void onKeyUp(int i) {

            }

        });
    }

    private ProgressDialog progressDialog;

    private void showLoading(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("กำลังโหลดข้อมูล...");
                progressDialog.setCancelable(false);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    String temp_product_number = "";
    private void showComponentScanningView(int position) {
        temp_product_number = "";

        if (position >= 0 && position < componentsList.size()) {
            currentComponentPosition = position;
            PickingComponentItem component = componentsList.get(position);

            // Update selected item information in viewRead section
            temp_product_number = component.getComponentCode();
            tvSelectedItem.setText("Item: " + component.getComponentCode());
            tvSelectedQty.setText("Quantity: " + component.getQuantityRequired());
            tvPickedQty.setText("Picked: " + component.getQuantityPicked());
            tvScannedQty.setText("Scan: 0.0");
            // เพิ่มการตั้งค่าชื่อ component ใน title
            tvReadTitle.setText("Item: " + component.getComponentCode());
            barcode = component.getBarcode();


            // Reset input fields
            EtData_Read.setText("");
            //EtData_QTY.setText("1");

            // ล้างข้อมูลใน ListView
            scannedTagItems.clear();

            if (allTransactions != null) {
                for (PickingOrderTransaction t : allTransactions) {
                    if (t.getTagRemark() != null && t.getTagRemark().equals(component.getComponentCode())) {
                        ScannedTagItem historyTag = new ScannedTagItem(
                                t.getProductId(),
                                t.getTagNumber(),
                                t.getLot(),
                                t.getLotNumber(),
                                t.getQuantity(),
                                t.getAllocatedQty(),
                                0.0,
                                ""
                        );
                        historyTag.setHistory(true);
                        scannedTagItems.add(historyTag);
                    }
                }
            }

            scannedTagAdapter.notifyDataSetChanged();
            // แสดง viewRead เพื่อทับหน้าหลัก
            viewRead.setVisibility(View.VISIBLE);


        }
    }

    // เพิ่มฟังก์ชันซ่อน viewRead
    private void hideViewRead() {
        viewRead.setVisibility(View.GONE);
        currentComponentPosition = -1;
        scannedTagItems.clear();
        scannedTagAdapter.notifyDataSetChanged();
    }

    private void loadPickingOrderTransactions() {
        showLoading(true);

        apiService.getPickingRenantTransaction(pickingOrderId).enqueue(new Callback<List<PickingOrderTransaction>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<List<PickingOrderTransaction>> call, Response<List<PickingOrderTransaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderTransaction> transactions = response.body();
                    allTransactions = transactions;

                    if (!transactions.isEmpty()) {
                        // สร้าง map เพื่อสะสม allocatedQty ตาม TagRemark (ComponentCode)
                        Map<String, Double> allocatedQtyByComponent = new HashMap<>();

                        // จัดกลุ่มและสะสม allocatedQty ตาม TagRemark (ComponentCode)
                        for (PickingOrderTransaction transaction : transactions) {
                            String componentCode = transaction.getTagRemark(); // ฟิลด์ที่เก็บ component code

                            if (componentCode != null && !componentCode.isEmpty()) {
                                // สะสม allocatedQty สำหรับ component นี้
                                double currentAllocatedQty = allocatedQtyByComponent.getOrDefault(componentCode, 0.0);
                                double transactionAllocatedQty = transaction.getAllocatedQty(); // ใช้ allocatedQty จาก API
                                allocatedQtyByComponent.put(componentCode, currentAllocatedQty + transactionAllocatedQty);
                            }
                        }

                        // อัพเดตจำนวนใน componentsList
                        boolean listUpdated = false;

                        for (PickingComponentItem component : componentsList) {
                            String componentCode = component.getComponentCode();

                            // ตรวจสอบว่ามีธุรกรรมสำหรับ component นี้หรือไม่
                            if (allocatedQtyByComponent.containsKey(componentCode)) {
                                double totalAllocatedQty = allocatedQtyByComponent.get(componentCode);

                                // ตั้งค่า quantityPicked ด้วย allocatedQty จาก API
                                component.setQuantityPicked(totalAllocatedQty);

                                listUpdated = true;

                                Log.d(TAG, "Updated component: " + componentCode +
                                        " - AllocatedQty from API: " + totalAllocatedQty);
                            } else {
                                // ถ้าไม่มีธุรกรรม ให้ตั้งเป็น 0
                                component.setQuantityPicked(0);
                                listUpdated = true; // บังคับให้โหลด UI ใหม่แม้ค่าเป็น 0
                            }
                        }

                        // อัพเดต UI ถ้ามีการเปลี่ยนแปลง
                        if (listUpdated) {
                            requireActivity().runOnUiThread(() -> {
                                componentAdapter.notifyDataSetChanged();
                                updateComponentSummary();

                                // อัพเดต scanned qty ใน viewRead หากกำลังแสดงอยู่
                                if (currentComponentPosition >= 0 && currentComponentPosition < componentsList.size()) {
                                    PickingComponentItem currentComponent = componentsList.get(currentComponentPosition);
                                    tvPickedQty.setText("Picked: " + currentComponent.getQuantityPicked());
                                }
                                
                                // หน่วงเวลา 500ms ให้ผู้ใช้เห็น Loading อย่างชัดเจนและชัวร์ว่า UI วาดเสร็จ
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> showLoading(false), 1000);
                            });
                        } else {
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> showLoading(false), 1000);
                        }
                    } else {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> showLoading(false), 1000);
                    }
                } else {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> showLoading(false), 1000);
                    Log.e(TAG, "Error loading picking order transactions: " + response.code());
                    showError("Failed to load picking order transactions");
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call<List<PickingOrderTransaction>> call, Throwable t) {
                Log.e(TAG, "Network error loading picking order transactions", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void submitScan() {
        if (currentComponentPosition < 0 || currentComponentPosition >= componentsList.size()) {
            Toast.makeText(requireContext(), "ไม่ได้เลือกคอมโพเนนต์", Toast.LENGTH_SHORT).show();
            return;
        }

        if (scannedTagAdapter == null || scannedTagAdapter.getItems().isEmpty()) {
            Toast.makeText(requireContext(), "ไม่มีรายการที่สแกน", Toast.LENGTH_SHORT).show();
            return;
        }

        // แสดง progress bar
        showLoading(true);

        // สร้างรายการ PickingOrderItem จาก scannedTagItems
        List<PickingTagDto> items = new ArrayList<>();
        PickingComponentItem component = componentsList.get(currentComponentPosition);

        for (ScannedTagItem tagItem : scannedTagAdapter.getItems()) {
            if (tagItem.getScanQty() <= 0) continue; // ส่งเฉพาะรายการที่มียอดสแกนใหม่
            
            PickingTagDto pickingItem = new PickingTagDto();
            pickingItem.setTagNumber(tagItem.getTagNumber());
            pickingItem.setAllocatedQty(tagItem.getScanQty());

            items.add(pickingItem);
        }

        if (items.isEmpty()) {
            showLoading(false);
            Toast.makeText(requireContext(), "ไม่มียอดสแกนใหม่เพื่อบันทึก", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = MainActivity.globalUserId;
        // เรียก API createPickingRenant
        apiService.createPickingRenant(
                pickingOrderId,  // ParentId
                component.getComponentCode(),  // TagRemark
                userID,
                items
        ).enqueue(new Callback<List<ResponseDto>>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<List<ResponseDto>> call, Response<List<ResponseDto>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {

                    List<ResponseDto> resultList = response.body();
                    Log.d(TAG, "Response body: " + (resultList != null ? "Not null, size: " + resultList.size() : "Null"));

                    if (resultList != null && !resultList.isEmpty()) {
                        ResponseDto result = resultList.get(0);

                        Log.d(TAG, "isSuccess: " + result.isSuccess);
                        Log.d(TAG, "message: " + result.message);
                        Log.d(TAG, "statusCode: " + result.statusCode);

                        if (result.isSuccess) {
                            // Handle successful response
                            Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();

                            // ล้างข้อมูลใน ListView
                            scannedTagItems.clear();
                            scannedTagAdapter.notifyDataSetChanged();



                            hideViewRead();
                            // โหลดข้อมูลใหม่

                            loadProductItemStock("");
                            loadPickingOrderTransactions();

                        } else {
                            // Handle API error (isSuccess = false)
                            String errorMessage = result.message != null ? result.message : "Unknown error";
                            Toast.makeText(mContext, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    // ไม่สำเร็จ - error handling เดิม
                    String errorMessage = "เกิดข้อผิดพลาดในการบันทึกข้อมูล: ";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += response.errorBody().string();
                        } else {
                            errorMessage += "รหัส " + response.code();
                        }
                    } catch (Exception e) {
                        errorMessage += e.getMessage();
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseDto>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "การเชื่อมต่อล้มเหลว: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseComponents(String remark) {
        // Clear existing components
        componentsList.clear();

        // Parse the remark string (format: "GFMC050B50=C1,GFSC050050=C2")
        String[] parts = remark.split(",");


        String itemNumber = "";
        // Process each component
        for (String part : parts) {
            // Parse component (format: "GFMC050B50=C1")
            String[] keyValue = part.split("=");
            if (keyValue.length < 2) continue;

            String itemCode = keyValue[0].trim();
            if (itemNumber.length() > 0 ) itemNumber += ",";
            itemNumber += itemCode;
            String typeQuantity = keyValue[1].trim();

            // Extract quantity (from C1, T4, etc.)
            if (typeQuantity.length() < 2) continue;

            // Get the quantity (1, 2, etc.)
            int quantity = Integer.parseInt(typeQuantity.substring(1));

            // Create and add component
            PickingComponentItem component = new PickingComponentItem(itemCode, quantity);
            componentsList.add(component);


        }

        loadProductItemStock(itemNumber);

        // Update adapter and summary
        componentAdapter.notifyDataSetChanged();
        updateComponentSummary();
    }

    private void loadProductItemStock(String productIds) {
        //ProgressDialog progr// Show loading popup
        //ProgressDialog progressDialog = new ProgressDialog(requireContext());
        //progressDialog.setMessage("กำลังโหลดสต็อกสินค้า กรุณารอสักครู่..");
       // progressDialog.setCancelable(false);
       // progressDialog.show();
       // showLoading(true);
        //btScanRFID.setEnabled(false);
        apiService.getProductItemsByProductNumber(productIds).enqueue(new Callback<List<ProductItem>>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<List<ProductItem>> call, Response<List<ProductItem>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    itemstock = response.body();
                    
                    // Populate barcode for each component from itemstock
                    if (itemstock != null) {
                        for (PickingComponentItem comp : componentsList) {
                            for (ProductItem pItem : itemstock) {
                                if (pItem.getProductNumber() != null && pItem.getProductNumber().equals(comp.getComponentCode())) {
                                    if (pItem.getBarcode() != null && !pItem.getBarcode().isEmpty()) {
                                        comp.setBarcode(pItem.getBarcode());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    //showLoading(false);
                    //btScanRFID.setEnabled(true);

                } else {
                    Log.e(TAG, "Error loading product item stock: " + response.code());
                    showError("Failed to load product item stock");
                    //showLoading(false);
                }


                //showLoading(false);
                //showToastOnUiThread("Load Item Success.");
                //progressDialog.dismiss();
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call<List<ProductItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading product item stock", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
                progressDialog.dismiss();
            }
        });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
        int  Bank = 1;
        boolean temp_cb_filter = false;
        if (temp_cb_filter == true) { //  过滤
            String filterData = "000000000000000000000000"; //etData_filter.getText().toString();
            if (TextUtils.isEmpty(filterData)) {
                Toast.makeText(mContext, "过滤数据不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String ptrFilter = "32"; // etPtr_filter.getText().toString();
            if (TextUtils.isEmpty(ptrFilter)) {
                Toast.makeText(mContext, "过滤起始地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String lenStr = "96"; //etLen_filter.getText().toString();
            if (TextUtils.isEmpty(lenStr)) {
                Toast.makeText(mContext, "过滤数据长度不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
            if (filterData == null || filterData.isEmpty() || !filterData.matches(rex)) {
                Toast.makeText(mContext, "过滤的数据必须是十六进制数据", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Integer.parseInt(lenStr) /4 > filterData.length()) {
                Toast.makeText(mContext, "过滤数据长度和过滤内容不匹配", Toast.LENGTH_SHORT).show();
                return;
            }
            if(filterData.length()%2 !=0){
                filterData=filterData+"0";
            }

            int filterPtr = Integer.parseInt(ptrFilter);
            int filterCnt = Integer.parseInt(lenStr);
            int filterBank = RFIDWithUHFBLE.Bank_EPC;
            boolean temp_rbEPC_filter  = false;
            boolean temp_rbTID_filter  = false;
            boolean temp_rbUser_filter  = false;
            if (temp_rbEPC_filter == true) {
                filterBank = RFIDWithUHFBLE.Bank_EPC;
            } else if (temp_rbTID_filter == true) {
                filterBank = RFIDWithUHFBLE.Bank_TID;
            } else if (temp_rbUser_filter == true) {
                filterBank = RFIDWithUHFBLE.Bank_USER;
            }
            data = mContext.uhf.readData(pwdStr,
                    filterBank,
                    filterPtr,
                    filterCnt,
                    filterData,
                    Bank,
                    Integer.parseInt(ptrStr),
                    Integer.parseInt(cntStr)
            );
        } else {
            data = mContext.uhf.readData(pwdStr,
                    Bank,
                    Integer.parseInt(ptrStr),
                    Integer.parseInt(cntStr));
        }
        EtData_Read.setText(data);
        if (data != null && data.length() > 0) {
           // Toast.makeText(mContext, R.string.rfid_msg_read_succ, Toast.LENGTH_SHORT).show();
            Utils.playSound(1);
        } else {
            //Toast.makeText(mContext, R.string.rfid_msg_read_fail, Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        }


        String data_tag_id  = String.valueOf(EtData_Read.getText());
        if (data_tag_id == null) data_tag_id = "";
        if (data_tag_id.trim().length() >= 20 ) findProductFromTagId(data_tag_id);
    }
    private void read2() {
        // 1. quantity = จำนวนที่ต้องการทั้งหมด (Required Qty)
        // 2. scan = จำนวนที่สแกนและบันทึกไปแล้ว (Quantity Picked from server)
        // 2.1 qty = ข้อ 1 - ข้อ 2 (Remaining quantity needed)
        // 3. Pick listview = รายการที่สแกนในเซสชันนี้ (scannedTagItems)
        
        String data_tag_id = EtData_Read.getText().toString().trim();
        
        // ถ้าไม่มีข้อมูลในช่องสแกน ให้ลองอ่านจากเครื่องอ่าน UHF
        if (TextUtils.isEmpty(data_tag_id)) {
            read();
            return;
        }

        // ตรวจสอบความถูกต้องเบื้องต้นของ Tag ID
        if (data_tag_id.length() >= 20) {
            findProductFromTagId(data_tag_id);
        } else {
            Toast.makeText(mContext, "ข้อมูล Tag ไม่สมบูรณ์", Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        }
    }
    
    int existingTagIndex = -1;

    @SuppressLint("LongLogTag")
    private void findProductFromTagId(String tagNumber) {
        if (itemstock != null && currentComponentPosition >= 0) {

            String tempbarcode = tagNumber.substring(0,6);
            if (barcode.contentEquals(tempbarcode))
            {


            }
            else
            {
                //*** กรณีไม่พบข้อมูลในสต็อกเลย ***
                Log.d(TAG, "TagNumber not match: " + tagNumber);

                Utils.playSound(2); // เสียงผิดพลาด

                new AlertDialog.Builder(requireContext())
                        .setTitle("แจ้งเตือน")
                        .setMessage("สินค้าที่สแกนไม่ตรงกับสินค้าที่ต้องจัด!\n(แท็ก: " + tagNumber + ")")
                        .setPositiveButton("ตกลง", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }


            // *** ได้รับ component ที่กำลังเลือกอยู่ ***
            PickingComponentItem currentComponent = componentsList.get(currentComponentPosition);
            String expectedProductNumber = currentComponent.getComponentCode();

            Log.d(TAG, "Scanning for component: " + expectedProductNumber + ", TagNumber: " + tagNumber);

            // *** ตรวจสอบแท็กที่ซ้ำกันในรายการแล้ว ***
            boolean isTagAlreadyAdded = false;

            for (int i = 0; i < scannedTagAdapter.getItems().size(); i++) {
                ScannedTagItem existingTag = scannedTagAdapter.getItems().get(i);
                if (existingTag.getTagNumber().equals(tagNumber)) {
                    isTagAlreadyAdded = true;
                    existingTagIndex = i;
                    break;
                }
            }


            boolean matchFound = false;
            // *** ค้นหาสินค้าจาก tagId หรือ Barcode ***
            for (ProductItem item : itemstock) {

                matchFound = false;
                if (item.getTagNumber() != null && item.getTagNumber().equals(tagNumber)) {
                    matchFound = true;
                }

                if (matchFound) {

                    String scannedProductNumber = item.getProductNumber();
                    Log.d(TAG, "Found product: " + scannedProductNumber + " for Scan/Tag: " + tagNumber);

                    // *** ตรวจสอบว่าเป็นสินค้าตัวเดียวกันกับ component ที่เลือกหรือไม่ ***
                    if (scannedProductNumber != null && expectedProductNumber != null && 
                        scannedProductNumber.trim().equalsIgnoreCase(expectedProductNumber.trim())) {

                        Log.d(TAG, "Product matches expected component: " + expectedProductNumber);

                        // *** ถ้าแท็กนี้มีในรายการแล้ว ให้ถามก่อนว่าต้องการแก้ไขหรือไม่ ***
                        if (isTagAlreadyAdded) {
                            ScannedTagItem existingTag = scannedTagAdapter.getItems().get(existingTagIndex);

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(requireContext());
                            alertBuilder.setTitle("แท็กซ้ำ");
                            alertBuilder.setMessage("แท็กนี้มีในรายการแล้ว จำนวน: " + existingTag.getScanQty() +
                                    "\nต้องการแก้ไขจำนวนหรือไม่?");

                            alertBuilder.setPositiveButton("แก้ไข", (dialog, which) -> {
                                // เรียกแสดง popup เพื่อแก้ไขจำนวน
                                showProductConfirmationDialog(item, existingTagIndex);
                            });

                            alertBuilder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss());

                            alertBuilder.show();
                            return;
                        }

                        // *** แสดง popup ข้อมูลสินค้า ***
                        showProductConfirmationDialog(item, -1);
                        return;

                    } else {
                        // *** สินค้าไม่ตรงกัน ***
                        Log.d(TAG, "Product mismatch - Expected: " + expectedProductNumber +
                                ", Found: " + scannedProductNumber + " - Ignoring scan");
                        
                        Utils.playSound(2); // เสียงผิดพลาด
                        new AlertDialog.Builder(requireContext())
                            .setTitle("แจ้งเตือน")
                            .setMessage("สินค้าไม่ตรงกัน!\n\nแท็ก: " + tagNumber + "\nรหัสที่ต้องการ: " + expectedProductNumber + "\nรหัสที่สแกนได้: " + scannedProductNumber)
                            .setPositiveButton("ตกลง", (dialog, which) -> dialog.dismiss())
                            .show();
                        return;
                    }
                }
            }

//            // *** กรณีไม่พบข้อมูลในสต็อกเลย ***
//           Log.d(TAG, "TagNumber not found in stock: " + tagNumber);
//
//            Utils.playSound(2); // เสียงผิดพลาด
//
//            new AlertDialog.Builder(requireContext())
//                   .setTitle("แจ้งเตือน")
//                   .setMessage("ไม่พบข้อมูลแท็กนี้ในสต็อก!\n(แท็ก: " + tagNumber + ")")
//                  .setPositiveButton("ตกลง", (dialog, which) -> dialog.dismiss())
//                    .show();
//
//            boolean isBarcode = tagNumber != null && tagNumber.length() <= 15;
//
//            if (isBarcode) {
//                new AlertDialog.Builder(requireContext())
//                    .setTitle("แจ้งเตือน")
//                    .setMessage("บาร์โค้ดไม่ตรงกับรายการที่ต้องหยิบ หรือไม่มีในระบบ!\n(บาร์โค้ด: " + tagNumber + ")")
//                    .setPositiveButton("ตกลง", (dialog, which) -> dialog.dismiss())
//                    .show();
//            } else {
//
//            }

        } else {
            // *** ไม่ได้เลือก component หรือไม่มีข้อมูล stock ***
            Log.w(TAG, "No component selected or itemstock is null");
            Utils.playSound(2); // เสียงผิดพลาด
        }
    }


    @SuppressLint("LongLogTag")
    private void showProductConfirmationDialog(final ProductItem item, final int existingTagIndex) {
        final boolean isTagAlreadyAdded = (existingTagIndex >= 0);
        PickingComponentItem component = componentsList.get(currentComponentPosition);

        // --- คำนวณตาม Logic ที่ระบุ ---
        // 1. quantity = จำนวนที่ต้องการทั้งหมด
        final double quantity_1 = component.getQuantityRequired();
        
        // 2. scan = จำนวนที่สแกนและบันทึกไปแล้ว (จาก API)
        final double scan_2 = component.getQuantityPicked();
        
        // 2.1 qty = ข้อ 1 - ข้อ 2 (จำนวนที่ยังขาดอยู่จากเซิร์ฟเวอร์)
        final double qty_2_1 = quantity_1 - scan_2;
        
        // 3. Pick listview (รายการที่สแกนอยู่ในตารางปัจจุบัน)
        // 4. จำนวนในกล่อง = จำนวนที่ในกล่อง (จาก Stock)
        double box_capacity_4 = item.getQuantityInBox();
        
        // 5. จำนวนปัจจุบัน = sum pick จากรายการที่กำลังสแกน (ข้อ 3)
        double temp_session_sum = 0;
        for (ScannedTagItem tag : scannedTagAdapter.getItems()) {
            temp_session_sum += tag.getScanQty();
        }
        final double session_sum_5 = temp_session_sum;
        
        // จำนวนที่มีอยู่เดิมใน Tag นี้ (กรณีแก้ไข)
        final double existingTagQty = isTagAlreadyAdded ? scannedTagAdapter.getItems().get(existingTagIndex).getScanQty() : 0;

        // สูงสุดที่ Tag นี้รับได้รวม = min(ความจุกล่อง, จำนวนที่ยังขาดเป้าหมาย)
        // จำนวนที่ยังขาดเป้าหมายในเซสชันนี้ = qty_2_1 - (session_sum - qty ที่มีอยู่แล้วในแท็กนี้)
        final double absoluteMaxPickable = qty_2_1 - (session_sum_5 - existingTagQty);
        final double maxTagTotal = Math.min(box_capacity_4, absoluteMaxPickable);

        // 6. เพิ่มได้อีก = จำนวนที่ยังใส่แท็กนี้ได้จริงๆ (ไม่ติดลบ)
        final double can_add_more_6 = Math.max(0, maxTagTotal - existingTagQty);
        
        // สร้าง dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isTagAlreadyAdded ? "แก้ไขจำนวนสินค้า (แท็กเดิม)" : "เพิ่มสินค้าใหม่");

        // สร้าง layout สำหรับ dialog
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // ผูกข้อมูลกับ view ใน dialog
        TextView tvProductNumber = dialogView.findViewById(R.id.tvProductNumber);
        TextView tvBarcode = dialogView.findViewById(R.id.tvBarcode);
        TextView tvTagNumber = dialogView.findViewById(R.id.tvTagNumber);
        TextView tvLot = dialogView.findViewById(R.id.tvLot);
        TextView tvLotNumber = dialogView.findViewById(R.id.tvLotNumber);
        TextView tvQuantityInBox = dialogView.findViewById(R.id.tvQuantityInBox);
        TextView tvMaxQty = dialogView.findViewById(R.id.tvMaxQty);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        // แสดงข้อมูลสินค้า
        tvProductNumber.setText("รหัสสินค้า: " + item.getProductNumber());
        tvBarcode.setText("บาร์โค้ด: " + (item.getBarcode() != null ? item.getBarcode() : "-"));
        tvTagNumber.setText("แท็ก: " + (item.getTagNumber() != null ? item.getTagNumber() : "-"));
        tvLot.setText("Lot: " + (item.getLot() != null ? item.getLot() : "-"));
        tvLotNumber.setText("เลขที่ Lot: " + (item.getLotNumber() != null ? item.getLotNumber() : "-"));
        
        // ข้อ 4: จำนวนในกล่อง
        tvQuantityInBox.setText("จำนวนในกล่อง: " + String.format("%.0f", box_capacity_4));

        // ข้อ 5 & 6: แสดงข้อมูลการคำนวณตามโจทย์
        if (isTagAlreadyAdded) {
            // แสดงจำนวนปัจจุบันและจำนวนที่เพิ่มเติมได้อีก (แท็กเดิม)
            tvMaxQty.setText("จำนวนปัจจุบันในลิสต์: " + String.format("%.0f", session_sum_5) +
                            " | เพิ่มได้อีก: " + String.format("%.0f", can_add_more_6));
            etQuantity.setText(String.format("%.0f", existingTagQty));
        } else {
            // แสดงจำนวนรวมในลิสต์และจำนวนที่ยังหยิบเพิ่มได้จริงๆ
            tvMaxQty.setText("จำนวนรวมในลิสต์: " + String.format("%.0f", session_sum_5) +
                            " | เพิ่มได้อีก: " + String.format("%.0f", can_add_more_6));
            etQuantity.setText(String.format("%.0f", can_add_more_6));
        }

        builder.setPositiveButton("ตกลง", null);
        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            etQuantity.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etQuantity, InputMethodManager.SHOW_IMPLICIT);
            etQuantity.selectAll();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                double requestedQty;
                try {
                    requestedQty = Double.parseDouble(etQuantity.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "กรุณาระบุจำนวนที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (requestedQty <= 0) {
                    Toast.makeText(requireContext(), "จำนวนต้องมากกว่า 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // *** ตรวจสอบการป้องกันสแกนเกินจำนวน ข้อ 2.1 ***
                // จำนวนทั้งหมดที่จะมีในระบบ = จำนวนที่บันทึกแล้ว (2) + จำนวนในลิสต์ที่ไม่ได้แก้ + จำนวนใหม่ที่กรอก
                double newSessionTotal = (session_sum_5 - existingTagQty) + requestedQty;
                
                if (newSessionTotal + scan_2 > quantity_1) {
                    double maxAllowedNow = quantity_1 - (scan_2 + (session_sum_5 - existingTagQty));
                    Toast.makeText(requireContext(), "จำนวนรวมเกินความต้องการ! (เพิ่มได้สูงสุด " + String.format("%.0f", maxAllowedNow) + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                // เพิ่มหรือแก้ไขรายการ
                if (isTagAlreadyAdded) {
                    ScannedTagItem existingTag = scannedTagAdapter.getItems().get(existingTagIndex);
                    existingTag.setScanQty(requestedQty);
                    scannedTagAdapter.notifyDataSetChanged();
                    updateSessionScannedQuantity();
                    Toast.makeText(requireContext(), "แก้ไขจำนวนสำเร็จ", Toast.LENGTH_SHORT).show();
                } else {
                    ScannedTagItem newTag = new ScannedTagItem(
                            item.getProductId() != null ? item.getProductId() : 0,
                            item.getTagNumber() != null ? item.getTagNumber() : (item.getBarcode() != null ? item.getBarcode() : ""),
                            item.getLot() != null ? item.getLot() : "-",
                            item.getLotNumber() != null ? item.getLotNumber() : "-",
                            item.getQuantityInBox(),
                            0.0,
                            requestedQty,
                            item.getBarcode() != null ? item.getBarcode() : ""
                    );
                    scannedTagAdapter.addItem(newTag);
                    updateSessionScannedQuantity();
                    Toast.makeText(requireContext(), "เพิ่มรายการสำเร็จ", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            });
        });

        // แสดง Dialog เฉพาะเมื่อยังไม่เกินจำนวนที่ต้องการ
        if (scan_2 + session_sum_5 >= quantity_1 && !isTagAlreadyAdded) {
            Toast.makeText(requireContext(), "สแกนสินค้าครบตามจำนวนที่ต้องการแล้ว", Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        } else {
            dialog.show();
        }
    }

    private void updateSessionScannedQuantity() {
        double temp_session_sum = 0;
        for (ScannedTagItem tag : scannedTagAdapter.getItems()) {
            temp_session_sum += tag.getScanQty();
        }
        tvScannedQty.setText("Scan: " + temp_session_sum);
    }

    private boolean isValidTagForComponent(String tagNumber, String componentCode) {
        // Search stock list for a matching component
        if (stockList != null) {
            for (ProductItem item : stockList) {
                if (item.getTagNumber() != null && item.getTagNumber().equals(tagNumber)) {
                    // For this implementation, consider any tag from stock as valid
                    return true;
                }
            }
        }

        // For testing, consider any tag valid if stock is empty
        return stockList == null || stockList.isEmpty();
    }

    private void updateComponentSummary() {
        StringBuilder summary = new StringBuilder();
        int totalRequired = 0;
        int totalPicked = 0;

        for (PickingComponentItem component : componentsList) {
            totalRequired += component.getQuantityRequired();
            totalPicked += component.getQuantityPicked();

            summary.append(component.getComponentCode())
                    .append(": ")
                    .append(String.format("%.0f", component.getQuantityPicked()))
                    .append("/").append(String.format("%.0f", component.getQuantityRequired()))
                    .append("\n");
        }

        summary.append("\nTotal: ").append(totalPicked).append("/").append(totalRequired);

        tvComponentSummary.setText(summary.toString());

        // Enable save button if any components are picked
        //btnSaveComponents.setEnabled(totalPicked > 0);
    }


    private void saveComponentScans() {
        boolean hasScannedComponents = false;
        List<PickingOrderItem> componentItems = new ArrayList<>();

        for (PickingComponentItem component : componentsList) {
            if (component.getQuantityPicked() > 0) {
                hasScannedComponents = true;

                // For each tag, create a PickingOrderItem
                for (String tagNumber : component.getTagIds()) {
                    PickingOrderItem componentItem = new PickingOrderItem();
                    componentItem.setProductId(0); // You might need to determine appropriate ID
                    componentItem.setQuantity(1);
                    componentItem.setTagNumber(tagNumber);
                    componentItem.setProductNumber(component.getComponentCode());
                    componentItem.setBarcode(component.getBarcode());

                    // Add to scanned tags and to return list
                    componentItems.add(componentItem);
                }
            }
        }

        if (hasScannedComponents) {
            Toast.makeText(requireContext(), "Components saved successfully", Toast.LENGTH_SHORT).show();

            // Notify parent fragment
            if (listener != null) {
                listener.onComponentsSaved(componentItems);
            }
        } else {
            Toast.makeText(requireContext(), "No components scanned", Toast.LENGTH_SHORT).show();
        }
    }


}
