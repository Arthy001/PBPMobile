package com.example.uhf_bt.fragment;

import static com.example.uhf_bt.services.Settings.appMode;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.drm.ProcessedData;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.MoveOrderChildTagWithStatusAdapter;
import com.example.uhf_bt.adapter.PickingOrderChildAdapter;
import com.example.uhf_bt.adapter.PickingOrderChildTagWithStatusAdapter;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.models.MoveTagDto;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.models.PickingTagDto;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.models.ResponseDto;
import com.example.uhf_bt.models.TagPickWithStatus;
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
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingOrderChildFragment extends Fragment {
    private static final String TAG = "PickingOrderChildFrag";
    private EditText tvBarcode,EtData_Read;
    private Button btnScanBarocde,btnLockPallet,btScanRFID, btStop, btClear, btnConfirm ;
    private LinearLayout view_barcode,viewAction;
    private TextView tvTotalQty, tvTotalScan, tvTotalSuccess, tvNotificationCount, tvOrderNo, tvCustomer;
    private ImageView ivNotification,ivBack;
    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;
    // Add these variables to your PickingOrderChildFragment class
    private Spinner spinnerPallet;
    private ArrayAdapter<String> palletAdapter;
    private boolean isPalletLocked = false;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PickingOrderChildAdapter adapter;
    private List<PickingOrderItem> itemList = new ArrayList<>();
    private List<PickingOrderItem> tempList = new ArrayList<>();
    private List<ProductItem> stockList = new ArrayList<>();
    private List<PickingOrderItem> scannedTags = new ArrayList<>();
    private String pickingOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private TextView toolbar_title;
    private SearchView searchView;
    private MainActivity mContext;
    private Boolean loadingSuccess = false;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel2));
        }
        if (getArguments() != null) {
            pickingOrderId = getArguments().getString("PickingOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking_order_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();
        loadPickingOrderItems();


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
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize total quantity TextViews
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        tvTotalScan = view.findViewById(R.id.tvTotalScan);
        tvTotalSuccess = view.findViewById(R.id.tvTotalSuccess);

        tvTotalScan.setOnClickListener(v -> {
            showScannedTagNumbers();
        });

        tvTotalSuccess.setOnClickListener(v -> {
            showSuccessTagNumbers();
        });


        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_count);
        tv_total = view.findViewById(R.id.tv_total);


        EtData_Read = view.findViewById(R.id.EtData_Read);
        //EtData_QTY = view.findViewById(R.id.EtData_QTY);
        btScanRFID = view.findViewById(R.id.btnScanRFID);
        btStop   = view.findViewById(R.id.btnStop);
        btClear = view.findViewById(R.id.btnClear);
        btClear = view.findViewById(R.id.btnClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        spinnerPallet = view.findViewById(R.id.spinnerPallet);
        btnLockPallet = view.findViewById(R.id.btnLockPallet);

        tvBarcode = view.findViewById(R.id.tvBarcode);
        btnScanBarocde = view.findViewById(R.id.btnScanBarcode);

        view_barcode = view.findViewById(R.id.view_barcode);

        if (appMode == "RFID" )
        {
            view_barcode.setVisibility(View.GONE);
        }

        // Initialize spinner adapter
        palletAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        palletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPallet.setAdapter(palletAdapter);

        setupPalletControls();

    }

    // Add this method to setup pallet controls
    private void setupPalletControls() {

        btnLockPallet.setOnClickListener(v -> togglePalletLock());

        spinnerPallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPalletLocked) {
                    String selectedPallet = (String) parent.getItemAtPosition(position);
                    filterItemsByPallet(selectedPallet);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    // Add this method to toggle pallet lock
    private void togglePalletLock() {
        isPalletLocked = !isPalletLocked;
        btnLockPallet.setText(isPalletLocked ? "Unlock" : "Lock");

        if (isPalletLocked) {
            String selectedPallet = (String) spinnerPallet.getSelectedItem();
            adapter.setPalletLocked(true, selectedPallet);
        } else {
            adapter.setPalletLocked(false, null);
        }
    }

    // Add this method to filter items by pallet
    private void filterItemsByPallet(String pallet) {
        if (adapter != null && isPalletLocked) {
            adapter.setPalletLocked(true, pallet);
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
        String temp_data = data;
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
        if (data_tag_id.trim().length() >= 20 ) processQrCode(data_tag_id);
    }

    // Update loadPickingOrderTransactions to call updateTotalQuantities after updating items
    private void loadPickingOrderTransactions() {
        //showLoading(true);

        apiService.getPickingOrderTransactions(pickingOrderId).enqueue(new Callback<List<PickingOrderTransaction>>() {
            @Override
            public void onResponse(Call<List<PickingOrderTransaction>> call, Response<List<PickingOrderTransaction>> response) {
                //showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderTransaction> transactions = response.body();

                    if (!transactions.isEmpty()) {
                        // Create a map to count transactions per picking order item
                        Map<Integer, Integer> transactionCounts = new HashMap<>();

                        // Count transactions per picking order item
                        for (PickingOrderTransaction transaction : transactions) {
                            int pickingOrderItemId = transaction.getPickingOrderItemId();
                            Integer currentCount = transactionCounts.get(pickingOrderItemId);
                            if (currentCount == null) {
                                transactionCounts.put(pickingOrderItemId, 1);
                            } else {
                                transactionCounts.put(pickingOrderItemId, currentCount + 1);
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

                                    // Update total quantities
                                    updateTotalQuantities();
                                }
                            });
                        }

                        // Save transactions for reference
                        tempList = new ArrayList<>();
                        for (PickingOrderTransaction trans : transactions) {
                            PickingOrderItem item = new PickingOrderItem();
                            item.setProductId(trans.getProductId());
                            item.setProductName(trans.getProduct());
                            item.setQuantity(trans.getQuantity());
                            item.setTagNumber(trans.getTagNumber());
                            item.setLotNumber(trans.getLotNumber());
                            item.setLot(trans.getLot());
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
            args.putString("productName", item.getProductName());
            args.putString("remarkString", item.getRemark());
            args.putString("pickingOrderItemId", String.valueOf(item.getPickingOrderItemId()));
            //args.putString("barcode", item.getBarcode());
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
                    .setTitle(item.getProductName())
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




    // Add method to update total quantities
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
            tvTotalScan.setText(String.format("%.0f", totalScan));
            tvTotalSuccess.setText(String.format("%.0f", totalSuccess));
        }
    }
    private void setupButtonListeners() {
        btScanRFID.setOnClickListener(v -> scanRFID());
        btStop.setOnClickListener(v -> stop());
        btClear.setOnClickListener(v -> clearRFID());
        btnConfirm.setOnClickListener(v -> confirmPickingOrderTransaction());
        btnScanBarocde.setOnClickListener(v -> scanBarcode());
        //btnQRCode.setOnClickListener(v -> scanQRCode());

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
        adapter = new PickingOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrderItem item = adapter.getItem(position);
            String temp_remark = item.getRemark();
            if (temp_remark != null  )
            {
                if (temp_remark.trim().length() > 0)  handleItemClick(item);
                else {
                    showItemTagNumbers(item);

                }
            }
            else
            {
                showItemTagNumbers(item);
            }

        });
    }

    private void scanRFID() {
        // TODO: Implement RFID scanning logic
        //  Toast.makeText(mContext, "RFID scanning not implemented yet", Toast.LENGTH_SHORT).show();
        startThread();
    }
    private void loadPickingOrderItems() {
        showLoading(true);

        apiService.getPickingOrderItems(pickingOrderId).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                //showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    //List<PickingOrderItem> items = response.body();
                    itemList = response.body();
                    updateUI(itemList);

                } else {
                    Log.e(TAG, "Error loading picking order items: " + response.code());
                    showError("Failed to load picking order items");
                }

                loadPickingOrderTransactions();

                if (itemList.size()> 0 )
                {
                    int pickingStatus = 0;
                    pickingStatus = itemList.get(0).getPickingOrderStatus();
                    String  productIds = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        productIds = getUniqueProductIdsAsStringLegacy(itemList);
                    }

                    loadProductItemStock(productIds);

                }
                else
                {
                    btnConfirm.setEnabled(false);
                    btScanRFID.setEnabled(false);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getUniqueProductIdsAsStringLegacy(List<PickingOrderItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return "";
        }
        java.util.Set<Integer> uniqueProductIds = new java.util.HashSet<>();
        for (PickingOrderItem item : itemList) {
            uniqueProductIds.add(item.getProductId());
        }
        java.util.StringJoiner joiner = new java.util.StringJoiner(",");
        for (Integer id : uniqueProductIds) {
            joiner.add(String.valueOf(id));
        }
        return joiner.toString();
    }

    private void loadProductItemStock(String productIds) {
        //ProgressDialog progr// Show loading popup
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("กำลังโหลดสต็อกสินค้า กรุณารอสักครู่..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        showLoading(true);
        btScanRFID.setEnabled(false);
        apiService.getProductItemsByProductId(productIds).enqueue(new Callback<List<ProductItem>>() {
            @Override
            public void onResponse(Call<List<ProductItem>> call, Response<List<ProductItem>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    stockList = response.body();
                    //showLoading(false);
                    btScanRFID.setEnabled(true);

                } else {
                    Log.e(TAG, "Error loading product item stock: " + response.code());
                    showError("Failed to load product item stock");
                    //showLoading(false);
                }


                showLoading(false);
                showToastOnUiThread("Load Item Success.");
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<ProductItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading product item stock", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
                progressDialog.dismiss();
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
        if (!items.isEmpty()) {
            PickingOrderItem firstItem = items.get(0);
            tvOrderNo.setText("DO: " + firstItem.getPickingNumber());
            tvCustomer.setText("ลูกค้า: " + firstItem.getCustomer());
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
            if (item.getPalletName() != null && !item.getPalletName().isEmpty()) {
                uniquePallets.add(item.getPalletName());
            }
        }

        // Update spinner
        palletAdapter.clear();
        palletAdapter.addAll(new ArrayList<>(uniquePallets));
        palletAdapter.notifyDataSetChanged();

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

    @Override
    public void onPause() {
        super.onPause();
        // หยุดการสแกนทันทีเมื่อ Fragment นี้ถูกซ่อน (เช่น กดเปลี่ยนหน้า)
        if (mContext != null && mContext.isScanning) {
            stop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ป้องกันอีกชั้นเมื่อ View ถูกทำลาย
        if (mContext != null && mContext.isScanning) {
            stop();
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
        
        // Safety check: ensure tagList and tempDatas are in sync
        // If index is out of bounds, treat as new tag
        if(exists && tagList != null && index >= 0 && index < tagList.size()){
            tagMap = tagList.get(index);
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(Integer.parseInt(tagMap.get(MainActivity.TAG_COUNT), 10) + 1));
        }else {
            tagMap = new HashMap<>();
            tagMap.put(MainActivity.TAG_EPC, info.getEPC());
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(1));
            
            // Ensure index is valid for ArrayList.add()
            // Index must be between 0 and list.size() (inclusive)
            // Use the minimum size of both lists to ensure they stay in sync
            int minSize = Math.min(tempDatas.size(), tagList.size());
            int safeIndex = Math.max(0, Math.min(index, minSize));
            tempDatas.add(safeIndex, info);
            tagList.add(safeIndex, tagMap);
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

    public static PickingOrderChildFragment newInstance(String pickingOrderId) {
        PickingOrderChildFragment fragment = new PickingOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("PickingOrderId", pickingOrderId);
        fragment.setArguments(args);
        return fragment;
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
    private ProductItem findItemStockByTagId(String tagId) {
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

    private void scanBarcode()
    {
        String barcode= String.valueOf(tvBarcode.getText());
        if (barcode != null) processQrCode(barcode);
    }

    // Update processQrCode method to handle quantity tracking
    private void processQrCode(String qrCode) {
        try {
            // Basic input validation
            if (qrCode == null || qrCode.length() < 11) {
                //Toast.makeText(mContext, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                String msg = qrCode +  " Invalid format";
                //showToastOnUiThread(msg);
                notificationMessages.add(msg);
                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน

                return;
            }

            String chkTagId = qrCode.substring(0,1);

            if (chkTagId.contains("F")) {
                //Toast.makeText(mContext, qrCode + " ไม่ใช่แท็กสินค้า!" , Toast.LENGTH_SHORT).show();
                String msg = qrCode +  " แท็กพาเลท";
                //showToastOnUiThread(msg);
                notificationMessages.add(msg);
                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                return;
            }
            else if (chkTagId.contains("C"))
            {
                String xxTagID = qrCode;
                ProductItem foundItem = findItemStockByTagId(xxTagID);

                if (foundItem == null) {
                    Log.d(TAG, "No item stock found with tagId: " + xxTagID);
                    String msg = qrCode  + " ไม่มีอยู่ในสต็อก";
                    notificationMessages.add(msg);
                    //showToastOnUiThread(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                // Get all items from adapter
                List<PickingOrderItem> items = adapter.getFilteredItems();

                if (items == null) {
                    String msg = "No items available";
                    notificationMessages.add(msg);
                    //showToastOnUiThread(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                boolean found = false;

                // Search for matching item
                for (PickingOrderItem item : items) {
                    String temp_box_type = item.getBoxType();
                    String temp_box_type2 = foundItem.getItemType();

                    if (item != null && item.getGenRfidPartial().equals( foundItem.getTagNumber())  ) {
                        found = true;

                        int pickingItemId = item.getId();
                        // Check if we've already reached the required quantity
                        if (item.getQuantitySuccess() + item.getQuantityScan() >= item.getQuantity()) {
                            String msg = item.getProductNumber() +  " สแกนสินค้าครบแล้ว";
                            notificationMessages.add(msg);
                            //showToastOnUiThread(msg);
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
                            double temp_allocated_qty = item.getAllocatedQty();
                            // Add tag to scanned list
                            PickingOrderItem newScannedItem = new PickingOrderItem();
                            newScannedItem.setProductId(item.getProductId());
                            newScannedItem.setQuantity(1);
                            newScannedItem.setTagNumber(qrCode.trim());
                            newScannedItem.setLotNumber(foundItem.getLotNumber());
                            newScannedItem.setLot(foundItem.getLot());
                            newScannedItem.setId(item.getPickingOrderItemId());
                            newScannedItem.setPickingOrderItemId(pickingItemId);
                            newScannedItem.setItemType(temp_box_type2);
                            newScannedItem.setAllocatedQty(foundItem.getQuantityInBox());
                            newScannedItem.setPalletTagNumber(item.getPalletTagNumber());
                            newScannedItem.setRemark(item.getRemark());
                            scannedTags.add(newScannedItem);


                            btnConfirm.setEnabled(true);
                            // Show success message
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    adapter.notifyDataSetChanged();

                                    // Add this:
                                    updateTotalQuantities();
                                });
                            }
                        } else {
                            String msg = qrCode +  " สแกนไปแล้ว";
                            //showToastOnUiThread(msg);
                            notificationMessages.add(msg);
                            hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                            updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                        }
                        break;




                    }

                }

                if (!found) {
                    String msg = qrCode +  " ไม่ตรงกับสินค้าทีต้องจัด";
                    //showToastOnUiThread(msg);
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
                    Log.d(TAG, "No item stock found with tagId: " + xxTagID);
                    String msg = qrCode  + " ไม่มีอยู่ในสต็อก";
                    notificationMessages.add(msg);
                    //showToastOnUiThread(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                // Get all items from adapter
                List<PickingOrderItem> items = adapter.getFilteredItems();

                if (items == null) {
                    String msg = "No items available";
                    notificationMessages.add(msg);
                    //showToastOnUiThread(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                    return;
                }

                boolean found = false;

                // Search for matching item
                for (PickingOrderItem item : items) {
                    String temp_box_type = item.getBoxType();
                    String temp_box_type2 = foundItem.getItemType();

                    if (item != null && item.getProductId() == foundItem.getProductId()  ) {

                        if (temp_box_type.equals(temp_box_type2) )
                        {
                            found = true;

                            int pickingItemId = item.getId();
                            // Check if we've already reached the required quantity
                            if (item.getQuantitySuccess() + item.getQuantityScan() >= item.getQuantity()) {
                                String msg = item.getProductNumber() +  " สแกนสินค้าครบแล้ว";
                                notificationMessages.add(msg);
                                //showToastOnUiThread(msg);
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
                                newScannedItem.setItemType(temp_box_type2);
                                newScannedItem.setAllocatedQty(foundItem.getQuantityInBox());
                                newScannedItem.setPalletTagNumber(item.getPalletTagNumber());
                                scannedTags.add(newScannedItem);


                                btnConfirm.setEnabled(true);
                                // Show success message
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        adapter.notifyDataSetChanged();

                                        // Add this:
                                        updateTotalQuantities();
                                    });
                                }
                            } else {
                                String msg = qrCode +  " สแกนไปแล้ว";
                                //showToastOnUiThread(msg);
                                notificationMessages.add(msg);
                                hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                                updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                            }
                            break;
                        }
                        else
                        {
                            String msg = qrCode +  " สถานะเป็นกล่องเศษ! ";
                            //showToastOnUiThread(msg);
                            notificationMessages.add(msg);
                            hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                            updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                            break;
                        }

                    }
                }

                if (!found) {
                    String msg = qrCode +  " ไม่ตรงกับสินค้าทีต้องจัด";
                    //showToastOnUiThread(msg);
                    notificationMessages.add(msg);
                    hasNotifications = true;  // ตั้งค่าให้มีการแจ้งเตือน
                    updateNotificationVisibility();  // อัพเดทการแสดงผลไอคอนแจ้งเตือน
                }
            }



        } catch (Exception e) {
            String msg =  "Error processing QR code: " + e.getMessage();
            Log.e(TAG, msg);
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

    // Fixed clearData method in PickingOrderChildFragment.java
    private void clearData() {
        // Reset scan tracking variables

        scannedTags.clear();
        itemList.clear();
        tempList.clear();
        stockList.clear();
        notificationMessages.clear();
        hasNotifications = false;
        updateNotificationVisibility();

        updateTotalQuantities();
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

        btnConfirm.setEnabled(false);
    // Reload picking order

        checkNotifications();

        loadPickingOrderItems();

    }



    private void confirmPickingOrderTransaction() {

        try
        {
            Log.i(TAG, "stop mContext.isScanning=false");
            handler.removeMessages(FLAG_TIME_OVER);
            if(mContext.isScanning) {

                stopInventory();
            }
            mContext.isScanning=false;
            cancelInventoryTask();

        }
        catch (Exception ex)
        {}

        List<PickingTagDto> sendTags = new ArrayList<>();

        if (scannedTags != null && !scannedTags.isEmpty()) {
            // **แก้ไข: สร้าง MoveTagDto object ใหม่ในแต่ละรอบของลูป**
            for (PickingOrderItem scannedTag : scannedTags) {
                PickingTagDto item = new PickingTagDto(); // สร้างใหม่ในทุกรอบ
                item.setPickingOrderItemId(scannedTag.getId());
                item.setTagNumber(scannedTag.getTagNumber());
                item.setRemark(scannedTag.getRemark());
                item.setAllocatedQty(scannedTag.getAllocatedQty());
                item.setPalletTagNumber(scannedTag.getPalletTagNumber());
                sendTags.add(item);
            }
        }

        if (sendTags.isEmpty()) {
            Toast.makeText(mContext, "No scanned tags to confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = MainActivity.globalUserId;
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
                        btnConfirm.setEnabled(false);
                        btScanRFID.setEnabled(false);
                        clearData();

                        Toast.makeText(mContext, "Pick successfully", Toast.LENGTH_LONG).show();


                    } else {
                        // มีรายการที่ไม่สำเร็จ
                        String errorMsg = "Save failed:\n\n" + errorMessages.toString();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Pick Errors")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();

                        Toast.makeText(mContext, "Confirm Pick failed ", Toast.LENGTH_SHORT).show();
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

    // Method สำหรับแสดงรายการ TagNumber ทั้งหมดที่สแกนแล้ว
    private void showScannedTagNumbers() {
        if (scannedTags.isEmpty()) {
            Toast.makeText(mContext, "ไม่มี TagNumber ที่สแกนแล้ว", Toast.LENGTH_SHORT).show();
            return;
        }

        // สร้างรายการ TagNumber พร้อม ProductName
        List<String> tagDisplayList = new ArrayList<>();
        for (PickingOrderItem tag : scannedTags) {
            // หาชื่อสินค้าจาก productId
            String productName = getProductNameById(tag.getProductId());
            tagDisplayList.add(String.format("%s - %s", tag.getTagNumber(), productName));
        }

        // แสดงใน Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("TagNumber ที่สแกนแล้ว (" + scannedTags.size() + " รายการ)");

        String[] tagArray = tagDisplayList.toArray(new String[0]);
        builder.setItems(tagArray, (dialog, which) -> {
            // เมื่อกดเลือก TagNumber ให้แสดงรายละเอียด
            showTagDetails(scannedTags.get(which));
        });

        builder.setNegativeButton("ปิด", null);
        builder.show();
    }

    // Method สำหรับแสดงรายการ TagNumber ที่บันทึกสำเร็จแล้ว
    private void showSuccessTagNumbers() {
        if (tempList.isEmpty()) {
            Toast.makeText(mContext, "ไม่มี Tag ที่บันทึกสำเร็จแล้ว", Toast.LENGTH_SHORT).show();
            return;
        }

        // สร้างรายการ TagNumber ที่บันทึกสำเร็จ
        List<String> tagDisplayList = new ArrayList<>();
        for (PickingOrderItem tag : tempList) {
            String productNumber = tag.getProductName();
            tagDisplayList.add(String.format("%s - %s", tag.getTagNumber(), productNumber));
        }

        // แสดงใน Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("TagNumber ที่บันทึกสำเร็จ (" + tempList.size() + " รายการ)");

        String[] tagArray = tagDisplayList.toArray(new String[0]);
        builder.setItems(tagArray, (dialog, which) -> {
            // เมื่อกดเลือก TagNumber ให้แสดงรายละเอียด
            showTagDetails(tempList.get(which));
        });

        builder.setNegativeButton("ปิด", null);
        builder.show();
    }

    // Method สำหรับแสดงรายการ TagNumber ของ item ที่เลือก
    private void showItemTagNumbers(PickingOrderItem selectedItem) {
        List<String> itemTags = new ArrayList<>();

        // หา TagNumber จาก scannedTags ที่ตรงกับ item นี้
        for (PickingOrderItem tag : scannedTags) {
            if (tag.getProductId() == selectedItem.getProductId()) {
                itemTags.add(tag.getTagNumber());
            }
        }

        // หา TagNumber จาก tempList (ที่บันทึกแล้ว) ที่ตรงกับ item นี้
        for (PickingOrderItem tag : tempList) {
            if (tag.getProductId() == selectedItem.getProductId()) {
                itemTags.add(tag.getTagNumber());
            }
        }

        if (itemTags.isEmpty()) {
            Toast.makeText(mContext, "ไม่มี TagNumber สำหรับสินค้านี้", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sort TagNumbers in ascending order
        Collections.sort(itemTags);

        // แสดงใน Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(selectedItem.getProductNumber() + "\r\n (" + itemTags.size() + " รายการ)");

        String[] tagArray = itemTags.toArray(new String[0]);
        builder.setItems(tagArray, (dialog, which) -> {
            // Copy TagNumber to clipboard
            String selectedTag = itemTags.get(which).replace(" (บันทึกแล้ว)", "");
            copyToClipboard(selectedTag);
            Toast.makeText(mContext, "คัดลอก TagNumber: " + selectedTag, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("ปิด", null);
        builder.show();
    }

    // Method สำหรับแสดงรายการ TagNumber ของ item ที่เลือก
//    private void showItemTagNumbers(PickingOrderItem selectedItem) {
//        List<String> itemTags = new ArrayList<>();
//
//        // หา TagNumber จาก scannedTags ที่ตรงกับ item นี้
//        for (PickingOrderItem tag : scannedTags) {
//            if (tag.getProductId() == selectedItem.getProductId()) {
//                itemTags.add(tag.getTagNumber());
//            }
//        }
//
//        // หา TagNumber จาก tempList (ที่บันทึกแล้ว) ที่ตรงกับ item นี้
//        for (PickingOrderItem tag : tempList) {
//            if (tag.getProductId() == selectedItem.getProductId()) {
//                itemTags.add(tag.getTagNumber() );
//            }
//        }
//
//        if (itemTags.isEmpty()) {
//            Toast.makeText(mContext, "ไม่มี TagNumber สำหรับสินค้านี้", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // แสดงใน Dialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        builder.setTitle(selectedItem.getProductNumber() + "\r\n (" + itemTags.size() + " รายการ)");
//
//        String[] tagArray = itemTags.toArray(new String[0]);
//        builder.setItems(tagArray, (dialog, which) -> {
//            // Copy TagNumber to clipboard
//            String selectedTag = itemTags.get(which).replace(" (บันทึกแล้ว)", "");
//            copyToClipboard(selectedTag);
//            Toast.makeText(mContext, "คัดลอก TagNumber: " + selectedTag, Toast.LENGTH_SHORT).show();
//        });
//
//        builder.setNegativeButton("ปิด", null);
//        builder.show();
//    }

    // Method สำหรับแสดงรายละเอียดของ Tag
    private void showTagDetails(PickingOrderItem tag) {
        String productName = getProductNameById(tag.getProductId());

        String details = String.format(
                "TagNumber: %s\n" +
                        "สินค้า: %s\n" +
                        "Lot Number: %s\n" +
                        "Lot: %s\n" +
                        "จำนวน: %d\n" +
                        "ประเภทกล่อง: %s",
                tag.getTagNumber(),
                productName,
                tag.getLotNumber() != null ? tag.getLotNumber() : "ไม่ระบุ",
                tag.getLot() != null ? tag.getLot() : "ไม่ระบุ",
                tag.getQuantity(),
                tag.getItemType() != null ? tag.getItemType() : "ไม่ระบุ"
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("รายละเอียด TagNumber");
        builder.setMessage(details);

        builder.setPositiveButton("คัดลอก TagNumber", (dialog, which) -> {
            copyToClipboard(tag.getTagNumber());
            Toast.makeText(mContext, "คัดลอก TagNumber แล้ว", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("ปิด", null);
        builder.show();
    }

    // Helper method สำหรับหาชื่อสินค้าจาก ProductId
    private String getProductNameById(int productId) {
        for (PickingOrderItem item : itemList) {
            if (item.getProductId() == productId) {
                return item.getProductName() != null ? item.getProductName() : item.getProductNumber();
            }
        }
        return "ไม่ทราบชื่อสินค้า";
    }

    // Helper method สำหรับ copy ข้อความไปที่ clipboard
    private void copyToClipboard(String text) {
        if (getContext() != null) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("TagNumber", text);
            clipboard.setPrimaryClip(clip);
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


}
