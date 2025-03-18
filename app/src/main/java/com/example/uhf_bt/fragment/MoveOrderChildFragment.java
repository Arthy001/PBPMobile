package com.example.uhf_bt.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.MoveOrderChildAdapter;
import com.example.uhf_bt.models.MoveOrderItem;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveOrderChildFragment extends Fragment {

    private static final String TAG = "MoveOrderChildFragment";
    private List<MoveOrderItem> scannedTags = new ArrayList<>();
    private TextView tvOrderNo,tvSourceWarehouse, tvDestinationWarehouse;
    private ListView listView;

    private ProgressBar progressBar;
    private View emptyView;
    private MoveOrderChildAdapter adapter;
    private List<MoveOrderItem> tempList = new ArrayList<>();
    private List<MoveOrderItem> itemList = new ArrayList<>();
    private String moveOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    private MainActivity mContext;

    private Button btnScanRFID, btStop, btClear, btnConfirm ;


    public static MoveOrderChildFragment newInstance(String moveOrderId) {
        MoveOrderChildFragment fragment = new MoveOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("MoveOrderId", moveOrderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            moveOrderId = getArguments().getString("MoveOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tempList = new ArrayList<>();
        scannedTags = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_order_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        loadMoveOrderItems();
        loadMoveOrderTransactions();
        setupButtonListeners();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        tvOrderNo = view.findViewById(R.id.tvOrderNo);
        tvSourceWarehouse = view.findViewById(R.id.tvSourceWarehouse);
        tvDestinationWarehouse = view.findViewById(R.id.tvDestinationWarehouse);
        listView = view.findViewById(R.id.listView);

        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_count);
        tv_total = view.findViewById(R.id.tv_total);

        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btClear = view.findViewById(R.id.btnClear);
        btStop = view.findViewById(R.id.btnStop);
        btnConfirm = view.findViewById(R.id.btnConfirm);
    }

    private void setupToolbar() {

        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupButtonListeners() {
        btnScanRFID.setOnClickListener(v -> scanRFID());
        btClear.setOnClickListener(v -> clearRFID());
        btStop.setOnClickListener(v -> stopRFID());
        btnConfirm.setOnClickListener(v -> confirmMoveOrderTransaction());
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
        adapter = new MoveOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MoveOrderItem item = adapter.getItem(position);
            handleItemClick(item);
        });
    }

    private void loadMoveOrderItems() {
        showLoading(true);

        apiService.getMoveOrderItems(moveOrderId).enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<MoveOrderItem> items = response.body();
                    itemList = response.body();

                    updateUI(items);
                    // Show empty state if no data
                    if (items.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading move order items: " + response.code());
                    showError("Failed to load move order items");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading move order items", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void loadMoveOrderTransactions() {
        showLoading(true);

        apiService.getMoveOrderTransactions(moveOrderId).enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    tempList = response.body();

                    if (!tempList.isEmpty()) {
                        // Iterate through tempList to find matches in itemList
                        for (MoveOrderItem tempItem : tempList) {
                            // Find matching item in itemList
                            for (MoveOrderItem item : itemList) {
                                // Match by productId and lot
                                int xproductId = item.getProductId();
                                int xxproductId = tempItem.getProductId();
                                String xLot = item.getLot();
                                String xxLot = tempItem.getLot();
                                if (item.getProductId() == (tempItem.getProductId())
                                        && item.getLot().equals(tempItem.getLot())) {
                                    // Update quantity success
                                    item.setQuantitySuccess(item.getQuantitySuccess() + 1);
                                    break; // Found match, move to next tempItem
                                }
                            }
                        }

                        // Update adapter with the modified itemList
                        if (adapter != null) {
                            adapter.updateItems(itemList);  // Make sure you have this method in your adapter
                            adapter.notifyDataSetChanged();
                        }

                        // If you're on a background thread, use runOnUiThread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }

                } else {
                    Log.e(TAG, "Error loading move order transactions: " + response.code());
                    showError("Failed to load move order transactions");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading move order transactions", t);
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

    private void updateUI(List<MoveOrderItem> items) {
        if (!items.isEmpty()) {
            MoveOrderItem firstItem = items.get(0);
            tvOrderNo.setText("Order No: " + firstItem.getMoveOrder());
            tvSourceWarehouse.setText("From: " + firstItem.getWarehouseSourceName());
            tvDestinationWarehouse.setText("To: " + firstItem.getWarehouseDestinationName());
        }

        adapter.updateItems(items);

        // Enable/disable buttons based on status
        boolean hasUnfinishedItems = false;
        for (MoveOrderItem item : items) {
            if (item.getMoveTransStatus() != 1) {
                hasUnfinishedItems = true;
                break;
            }
        }
        //btnComplete.setEnabled(!hasUnfinishedItems);
        //btnScanRfid.setEnabled(hasUnfinishedItems);
    }

    private void handleItemClick(MoveOrderItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getProduct())
                .setMessage("Lot: " + item.getLotNumber() + "\n" +
                        "Quantity: " + item.getQuantity())
                .setPositiveButton("Scan", (dialog, which) -> {
                    startScanningForItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startScanningForItem(MoveOrderItem item) {
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
    private ConnectStatus mConnectStatus = new ConnectStatus();
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



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
            uhf = mContext.uhf;
        }
    }


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
        loadMoveOrderItems();

    }


    private void processUHFInfo(UHFTAGInfo info) {
        if (info != null) {
            String epc = info.getEPC();
            Toast.makeText(mContext, "Scanned EPC: " + epc, Toast.LENGTH_SHORT).show();
            processQrCode(epc);
        }
    }

    private MoveOrderItem findItemInTempListByTagId(String tagId) {
        if (tempList == null || tempList.isEmpty() || tagId == null) {
            return null;
        }

        for (MoveOrderItem item : tempList) {
            if (item.getTagId() != null && item.getTagId().equals(tagId)) {
                return item;
            }
        }

        return null;
    }

    private void processQrCode(String qrCode) {
        try {
            // Basic input validation
            if (qrCode == null || qrCode.length() < 11) {
                Toast.makeText(mContext, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                return;
            }

            String barcode = qrCode.substring(0, 6);
            String lot = qrCode.substring(6, 12);
            String mc  = qrCode.substring(12, 14);
            String box_type = qrCode.substring(14, 15);
            String box_size = qrCode.substring(15, 19);
            String running =  qrCode.substring(19, 24);

            boolean found = false;


            String xxTagID = qrCode;
            MoveOrderItem foundItem = findItemInTempListByTagId(xxTagID);
            if (foundItem != null) {
                // Item found - do something with it
                Log.d(TAG, "Found item with tagId: " + foundItem.getTagId());
                Log.d(TAG, "Product ID: " + foundItem.getProductId());
                Log.d(TAG, "Lot: " + foundItem.getLot());
                return;
            } else {
                // Item not found
                //Log.d(TAG, "No item found with tagId: " + xxTagID);
            }

            // Get all items from adapter
            List<MoveOrderItem> items = adapter.getFilteredItems();



            if (items == null) {
                Toast.makeText(mContext, "No items available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Search for matching item
            for (MoveOrderItem item : items) {
                String tempbarcode = item.getBarcode();
                String templot = item.getLot();
                if (item != null &&
                        item.getBarcode() != null &&
                        item.getLot() != null &&
                        item.getBarcode().equals(barcode) &&
                        item.getLot().equals(lot)) {

                    found = true;

                    // Check if this tag was already scanned
                    if (!scannedTags.contains(qrCode)) {
                        // Update quantity scanned
                        item.setQuantityScan(item.getQuantityScan() + 1);

                        // Add tag to scanned list
                        MoveOrderItem xitem = new MoveOrderItem();
                        xitem.setProductId(item.getProductId());
                        xitem.setQuantity(1);
                        xitem.setTagId(qrCode.trim());
                        xitem.setLotNumber(item.getLotNumber());
                        xitem.setLot(lot);
                        scannedTags.add(xitem);

                        // Show success message with runOnUiThread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(mContext,
                                        String.format("Found Item: %s, Lot: %s, Scanned: %.0f/%.0f",
                                                item.getProduct(),
                                                item.getLot(),
                                                item.getQuantityScan(),
                                                item.getQuantity()),
                                        Toast.LENGTH_SHORT).show();

                                // Notify adapter that data has changed
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } else {
                        // Tag already scanned
                        showToastOnUiThread("This tag has already been scanned!");
                    }
                    break;
                }
            }

            if (!found) {
                // Item not found in list
//                showToastOnUiThread(String.format("No matching item found for Item: %s, Lot: %s",
//                        barcode, lot));
            }

        } catch (Exception e) {
            Log.e("MoveOrderChildFragment", "Error processing QR code: " + e.getMessage());
            showToastOnUiThread("Error processing scan: " + e.getMessage());
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

    public void startThread() {
        if (mContext.isScanning) {
            return;
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
    private void clearData() {

        //adapter.notifyDataSetChanged();

        mContext.selectEPC=null;
        selectIndex=-1;

        if (scannedTags != null) {
            scannedTags.clear();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Create new tagList if it's null
        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        loadMoveOrderItems();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void confirmMoveOrderTransaction() {
        // Create list to hold MoveOrderItem objects
//        List<MoveOrderItem> moveOrderItems = new ArrayList<>();
//
//        // Convert scannedTags to MoveOrderItems
//        for (String tagId : scannedTags) {
//            MoveOrderItem item = new MoveOrderItem();
//            item.setProductId(1);  // Set your product ID
//            item.setQuantity(1.0); // Set your quantity
//            item.setTagId(tagId);
//            item.setLotNumber("LOT250115"); // Set your lot number
//            item.setLot("250115");     // Set your lot
//            moveOrderItems.add(item);
//        }

        // Make API call
        String url = "http://192.168.110.79:5240/api/MoveOrderTransactionChild";

        // Create Retrofit service or similar HTTP client
        Call<List<MoveOrderItem>> call = apiService.createMoveOrderTransaction(
                moveOrderId, // Replace with actual parent ID
                scannedTags
        );

        call.enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                if (response.isSuccessful()) {
                    // Handle success
                    Toast.makeText(mContext, "Data sent successfully", Toast.LENGTH_SHORT).show();

                    loadMoveOrderItems();
                    loadMoveOrderTransactions();
                } else {
                    // Handle error
                    Toast.makeText(mContext, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
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