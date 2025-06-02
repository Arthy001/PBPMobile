package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.PickingComponentAdapter;
import com.example.uhf_bt.adapter.ScannedTagAdapter;
import com.example.uhf_bt.models.PickingComponentItem;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.ProductItem;
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
    private MainActivity mContext;
    private ApiService apiService;

    // View Read section
    private LinearLayout viewRead;

    //private EditText EtData_QTY;
    private TextView tvSelectedItem;
    private TextView tvSelectedQty;
    private TextView tvScannedQty;
    private EditText EtData_Read;
    private Button BtRead;
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
        if (getArguments() != null) {
            pickingOrderId = getArguments().getString("pickingOrderId");
            productName = getArguments().getString("productName");
            remarkString = getArguments().getString("remarkString");
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

        // Parse components from remark
        parseComponents(remarkString);

        loadProductItemStock();
        loadPickingOrderTransactions();
        return view;
    }

    @SuppressLint("LongLogTag")
    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvTitle = view.findViewById(R.id.toolbar_title);
        tvTitle.setText(productName + " - Components");
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

    private void showLoading(boolean show) {
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
            tvScannedQty.setText("Scan: " + component.getQuantityPicked());
            // เพิ่มการตั้งค่าชื่อ component ใน title
            tvReadTitle.setText("Item: " + component.getComponentCode());


            // Reset input fields
            EtData_Read.setText("");
            //EtData_QTY.setText("1");

            // ล้างข้อมูลใน ListView
            scannedTagItems.clear();
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
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<List<PickingOrderTransaction>> call, Response<List<PickingOrderTransaction>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderTransaction> transactions = response.body();

                    if (!transactions.isEmpty()) {
                        // สร้าง map เพื่อนับจำนวนธุรกรรมตาม TagRemark (ComponentCode)
                        Map<String, List<PickingOrderTransaction>> transactionsByComponent = new HashMap<>();

                        // จัดกลุ่มธุรกรรมตาม TagRemark (ComponentCode)
                        for (PickingOrderTransaction transaction : transactions) {
                            int componentProductId= transaction.getProductId(); // ฟิลด์ที่เก็บ component code
                            String componentCode = transaction.getTagRemark(); // ฟิลด์ที่เก็บ component code

                            if (componentCode != null && !componentCode.isEmpty()) {
                                if (!transactionsByComponent.containsKey(componentCode)) {
                                    transactionsByComponent.put(componentCode, new ArrayList<>());
                                }
                                transactionsByComponent.get(componentCode).add(transaction);
                            }
                        }

                        // อัพเดตจำนวนใน componentsList
                        boolean listUpdated = false;

                        for (PickingComponentItem component : componentsList) {
                            String componentCode = component.getComponentCode();

                            // ตรวจสอบว่ามีธุรกรรมสำหรับ component นี้หรือไม่
                            if (transactionsByComponent.containsKey(componentCode)) {
                                List<PickingOrderTransaction> componentTransactions = transactionsByComponent.get(componentCode);
                                int apiPickedQty = componentTransactions.size();
                                int currentPickedQty = component.getQuantityPicked();

                                // อัพเดตเฉพาะเมื่อจำนวนจาก API มากกว่าจำนวนปัจจุบัน
                                if (apiPickedQty > currentPickedQty) {
                                    // เราจะเพิ่ม tagId จาก transaction จริง
                                    for (int i = 0; i < (apiPickedQty - currentPickedQty); i++) {
                                        // ดึง ProductID จากธุรกรรม
                                        int transactionIndex = currentPickedQty + i;

                                        if (transactionIndex < componentTransactions.size()) {
                                            PickingOrderTransaction transaction = componentTransactions.get(transactionIndex);
                                            String tagId = transaction.getTagId(); // สมมติว่ามีฟิลด์ getTagId()

                                            // ถ้าไม่มี tagId ในธุรกรรม ให้สร้าง dummy tagId
                                            if (tagId == null || tagId.isEmpty()) {
                                                tagId = "API_TAG_" + transaction.getProductId() + "_" + i;
                                            }

                                            component.addTagId(tagId);
                                        }
                                    }

                                    listUpdated = true;
                                }
                            }
                        }

                        // อัพเดต UI ถ้ามีการเปลี่ยนแปลง
                        if (listUpdated) {
                            requireActivity().runOnUiThread(() -> {
                                componentAdapter.notifyDataSetChanged();
                                updateComponentSummary();
                            });
                        }
                    }
                } else {
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
        List<PickingOrderItem> items = new ArrayList<>();
        PickingComponentItem component = componentsList.get(currentComponentPosition);

        for (ScannedTagItem tagItem : scannedTagAdapter.getItems()) {
            PickingOrderItem pickingItem = new PickingOrderItem();
            pickingItem.setProductNumber(component.getComponentCode());
            pickingItem.setProductId((tagItem.getProductId()));
            pickingItem.setTagId(tagItem.getTagId());
            pickingItem.setQuantity(tagItem.getPickQty());
            pickingItem.setLot(tagItem.getLot());
            pickingItem.setLotNumber(tagItem.getLotNumber());
            pickingItem.setBarcode(tagItem.getBarcode());
            pickingItem.setAllocatedQty(tagItem.getPickQty());

            items.add(pickingItem);
        }

        // เรียก API createPickingRenant
        apiService.createPickingRenant(
                pickingOrderId,  // ParentId
                component.getComponentCode(),  // TagRemark
                items
        ).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // สำเร็จ - เพิ่ม tag ที่ส่งไปแล้วลงในคอมโพเนนต์
                    for (ScannedTagItem tagItem : scannedTagAdapter.getItems()) {
                        for (int i = 0; i < tagItem.getPickQty(); i++) {
                            component.addTagId(tagItem.getTagId());
                        }
                    }

                    // อัปเดต UI
                    componentAdapter.updateComponent(currentComponentPosition);
                    updateComponentSummary();

                    // อัปเดตข้อมูลใน viewRead
                    tvScannedQty.setText("Scanned QTY: " + component.getQuantityPicked());

                    // ล้างข้อมูลใน ListView
                    scannedTagItems.clear();
                    scannedTagAdapter.notifyDataSetChanged();

                    Toast.makeText(requireContext(), "บันทึกข้อมูลสำเร็จ!", Toast.LENGTH_SHORT).show();

                    // ตรวจสอบว่าครบจำนวนแล้วหรือไม่
                    if (component.isComplete()) {
                        Toast.makeText(requireContext(), "สแกนครบจำนวนแล้ว!", Toast.LENGTH_SHORT).show();
                        hideViewRead();
                    }
                } else {
                    // ไม่สำเร็จ
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
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "การเชื่อมต่อล้มเหลว: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void submitScan() {
//        if (currentComponentPosition < 0 || currentComponentPosition >= componentsList.size()) {
//            Toast.makeText(requireContext(), "No component selected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String scannedData = EtData_Read.getText().toString().trim();
//        if (scannedData.isEmpty()) {
//            Toast.makeText(requireContext(), "No data scanned", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Get quantity
//        int quantity;
//        try {
//            quantity = Integer.parseInt(EtData_QTY.getText().toString().trim());
//            if (quantity <= 0) {
//                Toast.makeText(requireContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        } catch (NumberFormatException e) {
//            Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        PickingComponentItem component = componentsList.get(currentComponentPosition);
//
//        // เพิ่มข้อมูลจาก ListView ทั้งหมด
//        for (ScannedTagItem tagItem : scannedTagItems) {
//            for (int i = 0; i < tagItem.getPickQty(); i++) {
//                component.addTagId(tagItem.getTagId());
//            }
//        }
//
//        // Add the scanned tag multiple times based on quantity
////        for (int i = 0; i < quantity; i++) {
////            component.addTagId(scannedData);
////        }
//
//        // Update UI
//        componentAdapter.updateComponent(currentComponentPosition);
//        updateComponentSummary();
//
//        // Update scanned QTY in viewRead section
//        tvScannedQty.setText("Scanned QTY: " + component.getQuantityPicked());
//
//        // ล้างข้อมูลใน ListView
//        scannedTagItems.clear();
//        scannedTagAdapter.notifyDataSetChanged();
//
//        Toast.makeText(requireContext(), "Component scanned successfully!", Toast.LENGTH_SHORT).show();
//
//        // Clear scan data field
//        EtData_Read.setText("");
//
//        // เพิ่มการตรวจสอบว่าครบจำนวนหรือไม่เพื่อปิด viewRead อัตโนมัติ
//        if (component.isComplete()) {
//            Toast.makeText(requireContext(), "Component completed!", Toast.LENGTH_SHORT).show();
//            hideViewRead();
//        }
//    }

    private void parseComponents(String remark) {
        // Clear existing components
        componentsList.clear();

        // Parse the remark string (format: "GFMC050B50=C1,GFSC050050=C2")
        String[] parts = remark.split(",");

        // Process each component
        for (String part : parts) {
            // Parse component (format: "GFMC050B50=C1")
            String[] keyValue = part.split("=");
            if (keyValue.length < 2) continue;

            String itemCode = keyValue[0].trim();
            String typeQuantity = keyValue[1].trim();

            // Extract quantity (from C1, T4, etc.)
            if (typeQuantity.length() < 2) continue;

            // Get the quantity (1, 2, etc.)
            int quantity = Integer.parseInt(typeQuantity.substring(1));

            // Create and add component
            PickingComponentItem component = new PickingComponentItem(itemCode, quantity);
            componentsList.add(component);
        }

        // Update adapter and summary
        componentAdapter.notifyDataSetChanged();
        updateComponentSummary();
    }

    private void loadProductItemStock() {
        showLoading(true);
        apiService.getProductItemsByStatus(1).enqueue(new Callback<List<ProductItem>>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<List<ProductItem>> call, Response<List<ProductItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    itemstock = response.body();

                    if (!itemstock.isEmpty()) {
                    }
                } else {
                    Log.e(TAG, "Error loading product item stock: " + response.code());
                    showError("Failed to load product item stock");
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call<List<ProductItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading product item stock", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
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
    int existingTagIndex = -1;
    private void findProductFromTagId(String tagId) {
        if (itemstock != null) {
            // ตรวจสอบแท็กที่ซ้ำกันในรายการแล้ว
            boolean isTagAlreadyAdded = false;


            for (int i = 0; i < scannedTagAdapter.getItems().size(); i++) {
                ScannedTagItem existingTag = scannedTagAdapter.getItems().get(i);
                if (existingTag.getTagId().equals(tagId)) {
                    isTagAlreadyAdded = true;
                    existingTagIndex = i;
                    break;
                }
            }

            // ค้นหาสินค้าจาก tagId
            for (ProductItem item : itemstock) {
                if (item.getTagId() != null && item.getTagId().equals(tagId)) {

                    // ตรวจสอบว่าเป็นสินค้าตัวเดียวกันหรือไม่
                    if (item.getProductNumber().equals(temp_product_number)) {

                        // ถ้าแท็กนี้มีในรายการแล้ว ให้ถามก่อนว่าต้องการแก้ไขหรือไม่
                        if (isTagAlreadyAdded) {
                            ScannedTagItem existingTag = scannedTagAdapter.getItems().get(existingTagIndex);

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(requireContext());
                            alertBuilder.setTitle("แท็กซ้ำ");
                            alertBuilder.setMessage("แท็กนี้มีในรายการแล้ว จำนวน: " + existingTag.getPickQty() +
                                    "\nต้องการแก้ไขจำนวนหรือไม่?");

                            alertBuilder.setPositiveButton("แก้ไข", (dialog, which) -> {
                                // เรียกแสดง popup เพื่อแก้ไขจำนวน
                                showProductConfirmationDialog(item, existingTagIndex);
                            });

                            alertBuilder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss());

                            alertBuilder.show();
                            return;
                        }

                        // แสดง popup ข้อมูลสินค้า
                        showProductConfirmationDialog(item, -1);
                        return;
                    } else {
                        Toast.makeText(requireContext(), "สินค้าไม่ตรงกัน!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            // กรณีไม่พบข้อมูล
            Toast.makeText(requireContext(), "ไม่พบ Tag ID ในสต็อก", Toast.LENGTH_SHORT).show();
        }
    }

    //int existingTagQty = 0;

    private void showProductConfirmationDialog(final ProductItem item, final int existingTagIndex) {
        // กำหนดให้ existingTagIndex เป็น -1 เมื่อเป็นรายการใหม่, ค่าอื่นคือดัชนีของรายการที่มีอยู่แล้ว
        final boolean isTagAlreadyAdded = (existingTagIndex >= 0);

        // สร้าง dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isTagAlreadyAdded ? "แก้ไขจำนวนสินค้า" : "เพิ่มสินค้า");

        // สร้าง layout สำหรับ dialog
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // ผูกข้อมูลกับ view ใน dialog
        TextView tvProductNumber = dialogView.findViewById(R.id.tvProductNumber);
        TextView tvBarcode = dialogView.findViewById(R.id.tvBarcode);
        TextView tvLot = dialogView.findViewById(R.id.tvLot);
        TextView tvLotNumber = dialogView.findViewById(R.id.tvLotNumber);
        TextView tvMaxQty = dialogView.findViewById(R.id.tvMaxQty);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        // แสดงข้อมูลสินค้า
        tvProductNumber.setText("รหัสสินค้า: " + item.getProductNumber());
        tvBarcode.setText("บาร์โค้ด: " + (item.getBarcode() != null ? item.getBarcode() : "-"));
        tvLot.setText("Lot: " + (item.getLot() != null ? item.getLot() : "-"));
        tvLotNumber.setText("เลขที่ Lot: " + (item.getLotNumber() != null ? item.getLotNumber() : "-"));

        // คำนวณจำนวนทั้งหมดที่มีอยู่แล้วใน ListView
        double existingTotal = 0;
        for (ScannedTagItem existingTag : scannedTagAdapter.getItems()) {
            existingTotal += existingTag.getPickQty();
        }

        // คำนวณจำนวนที่ต้องการทั้งหมด (Required) และจำนวนที่สแกนไปแล้ว (Scanned)
        PickingComponentItem component = componentsList.get(currentComponentPosition);
        int requiredQty = component.getQuantityRequired();
        int scannedQty = component.getQuantityPicked();

        // คำนวณจำนวนที่ยังสามารถเพิ่มได้
        int remainingQty = requiredQty - scannedQty;

        // จำนวนในกล่อง
        double quantityInBox = item.getQuantityInBox();

        // ถ้ามีแท็กนี้ในรายการแล้ว ให้ลบจำนวนที่มีอยู่แล้วออกจากการคำนวณ
        double existingTagQty;
        if (isTagAlreadyAdded) {
            existingTagQty = scannedTagAdapter.getItems().get(existingTagIndex).getPickQty();
            existingTotal -= existingTagQty;
        } else {
            existingTagQty = 0;
        }

        // คำนวณจำนวนสูงสุดที่สามารถเพิ่มได้
        double maxAddableQty = Math.min((int)quantityInBox, remainingQty - existingTotal);

        // กรณีสแกนซ้ำ แสดงข้อความให้ทราบ
        if (isTagAlreadyAdded) {
            tvMaxQty.setText("จำนวนปัจจุบัน: " + existingTagQty + " | เพิ่มได้อีก: " + maxAddableQty);
            etQuantity.setText(String.valueOf(existingTagQty)); // ตั้งค่าเริ่มต้นเป็นจำนวนที่มีอยู่เดิม
        } else {
            tvMaxQty.setText("จำนวนสูงสุดที่เพิ่มได้: " + maxAddableQty);
            etQuantity.setText("1"); // ตั้งค่าเริ่มต้นเป็น 1 สำหรับรายการใหม่
        }

        // สร้างปุ่มตกลงและยกเลิก
        builder.setPositiveButton("ตกลง", null); // เราจะ override คลิกอีเวนต์ด้านล่าง
        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss());

        // สร้าง dialog
        AlertDialog dialog = builder.create();

        // เพิ่มการทำ Auto Focus ที่ EditText
        dialog.setOnShowListener(dialogInterface -> {
            etQuantity.requestFocus();

            // เปิดคีย์บอร์ดสำหรับ EditText
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etQuantity, InputMethodManager.SHOW_IMPLICIT);

            // เลือกข้อความทั้งหมดใน EditText
            etQuantity.selectAll();

            // Override คลิกอีเวนต์ของปุ่มตกลง (เนื่องจากต้องทำใน OnShowListener)
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // ตรวจสอบจำนวนที่ใส่
                int requestedQty;
                try {
                    requestedQty = Integer.parseInt(etQuantity.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "กรุณาระบุจำนวนที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ตรวจสอบว่าจำนวนไม่เป็น 0 และไม่เกินจำนวนสูงสุด
                if (requestedQty <= 0) {
                    Toast.makeText(requireContext(), "จำนวนต้องมากกว่า 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (requestedQty > maxAddableQty + (isTagAlreadyAdded ? existingTagQty : 0)) {
                    Toast.makeText(requireContext(), "จำนวนเกินกว่าที่เพิ่มได้", Toast.LENGTH_SHORT).show();
                    return;
                }

                // เพิ่มหรือแก้ไขรายการใน ListView
                if (isTagAlreadyAdded) {
                    // อัปเดตจำนวนสำหรับแท็กที่มีอยู่แล้ว
                    ScannedTagItem existingTag = scannedTagAdapter.getItems().get(existingTagIndex);
                    existingTag.setPickQty(requestedQty);
                    scannedTagAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "อัปเดตจำนวนสำเร็จ: " + item.getProductNumber(), Toast.LENGTH_SHORT).show();
                } else {
                    // เพิ่มรายการใหม่
                    ScannedTagItem newTag = new ScannedTagItem(
                            item.getProductId(),
                            item.getTagId(),
                            item.getLot() != null ? item.getLot() : "-",
                            item.getLotNumber() != null ? item.getLotNumber() : "-",
                            requestedQty,
                            item.getBarcode()
                    );
                    scannedTagAdapter.addItem(newTag);
                    Toast.makeText(requireContext(), "เพิ่มรายการสำเร็จ: " + item.getProductNumber(), Toast.LENGTH_SHORT).show();
                }

                // ปิด dialog
                dialog.dismiss();
            });
        });

        // แสดง dialog
        dialog.show();
    }

    private boolean isValidTagForComponent(String tagId, String componentCode) {
        // Search stock list for a matching component
        if (stockList != null) {
            for (ProductItem item : stockList) {
                if (item.getTagId() != null && item.getTagId().equals(tagId)) {
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
                    .append(component.getQuantityPicked())
                    .append("/").append(component.getQuantityRequired())
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
                for (String tagId : component.getTagIds()) {
                    PickingOrderItem componentItem = new PickingOrderItem();
                    componentItem.setProductId(0); // You might need to determine appropriate ID
                    componentItem.setQuantity(1);
                    componentItem.setTagId(tagId);
                    componentItem.setProductNumber(component.getComponentCode());

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