package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
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

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.adapter.ProductItemAdapter;
import com.example.uhf_bt.database.AppDatabase;
import com.example.uhf_bt.database.ProductItemDao;
import com.example.uhf_bt.models.ProductItemEntity;
import com.example.uhf_bt.models.ProductItemLite;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.example.uhf_bt.tool.CheckUtils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import retrofit2.http.Tag;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductItemFragment extends Fragment {

    private static final String TAG = "ProductItemFragment";
    private EditText tvBarcode;
    private Button btnScanBarocde;
    private Button btnExport;
    private TextView tvTotalItems, tvTotalScanned, tvTotalRemaining;
    private ImageView ivNotification;
    private TextView tvNotificationCount;
    private List<String> scannedTags = new ArrayList<>();
    private TextView tvWarehouseName, tvLocationName;
    private ListView listView;

    private ProgressBar progressBar;
    private View emptyView;
    private ProductItemAdapter adapter;
    private List<ProductItemLite> itemList = new ArrayList<>(); // สำหรับแสดงผล
    private ProductItemDao productItemDao;
    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<Intent> exportCsvLauncher;
    private ActivityResultLauncher<Intent> exportTxtLauncher;
    private String warehouseId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    private MainActivity mContext;

    private Button btnScanRFID, btStop, btClear;

    // RFID related variables
    private RFIDWithUHFBLE uhf;
    static boolean isExit_ = false;
    private boolean isExit = false;
    private EditText  etTime;
    private TextView tv_count, tv_time, tv_total;
    private ConnectStatus mConnectStatus = new ConnectStatus();
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

    public static ProductItemFragment newInstance(String warehouseId) {
        ProductItemFragment fragment = new ProductItemFragment();
        Bundle args = new Bundle();
        args.putString("WarehouseId", warehouseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // แก้ไขตรงนี้
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel6));
        }

        if (getArguments() != null) {
            warehouseId = getArguments().getString("WarehouseId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
        scannedTags = new ArrayList<>();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_item, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        setupButtonListeners();
        loadProductItems();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        tvWarehouseName = view.findViewById(R.id.tvWarehouseName);
        tvLocationName = view.findViewById(R.id.tvLocationName);
        listView = view.findViewById(R.id.listView);

        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        etTime = view.findViewById(R.id.etTime);
        tv_count = view.findViewById(R.id.tv_count);
        tv_time = view.findViewById(R.id.tv_time);
        tv_total = view.findViewById(R.id.tv_total);

        btnScanRFID = view.findViewById(R.id.btnScanRFID);
        btClear = view.findViewById(R.id.btnClear);
        btStop = view.findViewById(R.id.btnStop);
        btnExport = view.findViewById(R.id.btnExport); // เพิ่มบรรทัดนี้
        //btnUpdate = view.findViewById(R.id.btnUpdate);

        ivNotification = view.findViewById(R.id.ivNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        // Initialize total TextViews
        tvTotalItems = view.findViewById(R.id.tvTotalItems);
        tvTotalScanned = view.findViewById(R.id.tvTotalScanned);
        tvTotalRemaining = view.findViewById(R.id.tvTotalRemaining);

        tvBarcode = view.findViewById(R.id.tvBarcode);
        btnScanBarocde = view.findViewById(R.id.btnScanBarcode);
    }



    private void setupToolbar() {
        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Add notification button click listener
        ivNotification.setOnClickListener(v -> {
            showNotificationsDialog();
        });
    }

    private void setupButtonListeners() {
        btnScanRFID.setOnClickListener(v -> scanRFID());
        btClear.setOnClickListener(v -> clearRFID());
        btStop.setOnClickListener(v -> stopRFID());
        btnExport.setOnClickListener(v -> showExportDialog()); // เพิ่มบรรทัดนี้
        // btnUpdate.setOnClickListener(v -> updateProductLocation());
        btnScanBarocde.setOnClickListener(v -> scanBarcode());
        tagList = new ArrayList<>();

        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onKeyDown(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                if (!isExit && mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
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

            @SuppressLint("LongLogTag")
            @Override
            public void onKeyUp(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                if (keycode == 4) {
                    stop();
                }
            }
        });
    }

    private void scanBarcode()
    {
        String barcode= String.valueOf(tvBarcode.getText());
        if (barcode != null)    processTagCode(barcode);
    }

    // เพิ่ม method ใหม่สำหรับแสดง Export Dialog
    private void showExportDialog() {
//        if (itemList.isEmpty()) {
//            Toast.makeText(mContext, "No scanned items to export", Toast.LENGTH_SHORT).show();
//            return;
//        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Export Scanned Items");
        builder.setMessage("Choose export format for " + itemList.size() + " scanned items:");

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
        String filename = "scanned_items_" + sdf.format(new Date()) + ".csv";
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        exportCsvLauncher.launch(intent);
    }

    private void exportScannedItemsAsText() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String filename = "scanned_items_" + sdf.format(new Date()) + ".txt";
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        exportTxtLauncher.launch(intent);
    }

    private void writeCsvToUri(Uri uri) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            writer.write("No,Product Number,Product Name,Product Group,Tag Number,Lot Number,Warehouse,Status,Scan Time\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:dd:ss", Locale.US);
            String currentTime = dateFormat.format(new Date());

            for (int i = 0; i < itemList.size(); i++) {
                ProductItemLite item = itemList.get(i);
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        i + 1,
                        escapeCSVField(item.getProductNumber()),
                        escapeCSVField(item.getProductName()),
                        escapeCSVField(item.getProductGroupName() != null ? item.getProductGroupName() : "N/A"),
                        escapeCSVField(item.getTagNumber() != null ? item.getTagNumber() : "N/A"),
                        escapeCSVField(item.getLotNumber() != null ? item.getLotNumber() : "N/A"),
                        escapeCSVField(item.getWarehouseName() != null ? item.getWarehouseName() : "N/A"),
                        escapeCSVField(getStatusText(item.getProductItemStatus())),
                        currentTime));
            }
            writer.flush();
            Toast.makeText(mContext, "CSV Exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV", e);
            Toast.makeText(mContext, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void writeTxtToUri(Uri uri) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String currentTime = dateFormat.format(new Date());

            writer.write("=== SCANNED ITEMS EXPORT ===\n");
            writer.write("Export Date: " + currentTime + "\n");
            writer.write("Total Scanned Items: " + itemList.size() + "\n\n");

            for (int i = 0; i < itemList.size(); i++) {
                ProductItemLite item = itemList.get(i);
                writer.write("ITEM #" + (i + 1) + "\n");
                writer.write("├─ Product Number: " + (item.getProductNumber() != null ? item.getProductNumber() : "N/A") + "\n");
                writer.write("├─ Product Name: " + (item.getProductName() != null ? item.getProductName() : "N/A") + "\n");
                writer.write("├─ Status: " + getStatusText(item.getProductItemStatus()) + "\n\n");
            }
            writer.flush();
            Toast.makeText(mContext, "Text Exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error writing TXT", e);
            Toast.makeText(mContext, "Error exporting TXT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // เพิ่ม helper method สำหรับ escape CSV fields
    private String escapeCSVField(String field) {
        if (field == null) {
            return "N/A";
        }

        // Escape double quotes by doubling them
        field = field.replace("\"", "\"\"");

        // If field contains comma, quote, or newline, wrap in quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            field = "\"" + field + "\"";
        }

        return field;
    }

    private List<String> notificationMessages = new ArrayList<>();
    private boolean hasNotifications = false;

    private void checkNotifications() {
        loadNotifications();
        // Update UI based on notification status
        hasNotifications = !notificationMessages.isEmpty();
        updateNotificationVisibility();
    }

    private void saveNotifications() {
        if (getContext() != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("PBPPrefs", Context.MODE_PRIVATE);
            String json = new Gson().toJson(notificationMessages);
            prefs.edit().putString("ScanNotifications", json).apply();
        }
    }

    private void loadNotifications() {
        if (getContext() != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("PBPPrefs", Context.MODE_PRIVATE);
            String json = prefs.getString("ScanNotifications", null);
            if (json != null) {
                Type type = new TypeToken<ArrayList<String>>(){}.getType();
                List<String> savedList = new Gson().fromJson(json, type);
                if (savedList != null) {
                    notificationMessages.clear();
                    notificationMessages.addAll(savedList);
                }
            }
        }
    }

    private void simulateGetNotifications() {
        // Simulate getting notifications - you can implement your logic here
    }

    private void addScanNotification(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // เช็คถ้ามีข้อความนี้อยู่แล้ว ไม่ต้องเพิ่มซ้ำ
                if (!notificationMessages.contains(message)) {
                    notificationMessages.add(0, message);
                    hasNotifications = true;
                    updateNotificationVisibility();
                    saveNotifications(); // บันทึกลงเครื่องทันที
                }
            });
        }
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
            builder.setPositiveButton("Close", null);
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

            // Add Export CSV button
            builder.setNeutralButton("Export CSV", (dialog, which) -> {
                exportNotificationsAsCSV();
            });

            // Add Export Text button
            builder.setNegativeButton("Export TXT", (dialog, which) -> {
                exportNotificationsAsText();
            });

            // Add Mark All as Read button
            builder.setPositiveButton("Mark All Read", (dialog, which) -> {
                notificationMessages.clear();
                hasNotifications = false;
                updateNotificationVisibility();
                saveNotifications(); // ล้างข้อมูลที่บันทึกไว้ในเครื่องด้วย
                Toast.makeText(mContext, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            });
        }

        // เพิ่มปุ่ม Close
        if (!notificationMessages.isEmpty()) {
            builder.setPositiveButton("Close", null);
        }

        builder.show();
    }

    private void exportNotificationsAsCSV() {
        try {
            // Create timestamp for filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            String fileName = "notifications_" + timestamp + ".csv";

            // Create file in Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File csvFile = new File(downloadsDir, fileName);

            // Write CSV content
            FileWriter writer = new FileWriter(csvFile);

            // Write CSV header
            writer.append("No,Timestamp,Message\n");

            // Write notification data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String currentTime = dateFormat.format(new Date());

            for (int i = 0; i < notificationMessages.size(); i++) {
                String message = notificationMessages.get(i);
                // Escape commas and quotes in CSV
                message = message.replace("\"", "\"\"");
                if (message.contains(",") || message.contains("\"") || message.contains("\n")) {
                    message = "\"" + message + "\"";
                }

                writer.append(String.valueOf(i + 1))
                        .append(",")
                        .append(currentTime)
                        .append(",")
                        .append(message)
                        .append("\n");
            }

            writer.flush();
            writer.close();

            // Show success message and offer to open file
            showExportSuccessDialog(csvFile, "CSV");

            Log.d(TAG, "CSV file exported successfully: " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error exporting CSV file", e);
            Toast.makeText(mContext, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error exporting CSV", e);
            Toast.makeText(mContext, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Export notifications as Text file
     */
    private void exportNotificationsAsText() {
        try {
            // Create timestamp for filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            String fileName = "notifications_" + timestamp + ".txt";

            // Create file in Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File textFile = new File(downloadsDir, fileName);

            // Write text content
            FileWriter writer = new FileWriter(textFile);

            // Write header
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String currentTime = dateFormat.format(new Date());

            writer.append("=== NOTIFICATION EXPORT ===\n");
            writer.append("Export Date: ").append(currentTime).append("\n");
            writer.append("Total Notifications: ").append(String.valueOf(notificationMessages.size())).append("\n");
            writer.append("==============================\n\n");

            // Write notifications
            for (int i = 0; i < notificationMessages.size(); i++) {
                writer.append(String.valueOf(i + 1))
                        .append(". ")
                        .append(notificationMessages.get(i))
                        .append("\n\n");
            }

            writer.append("=== END OF EXPORT ===\n");
            writer.flush();
            writer.close();

            // Show success message and offer to open file
            showExportSuccessDialog(textFile, "Text");

            Log.d(TAG, "Text file exported successfully: " + textFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error exporting text file", e);
            Toast.makeText(mContext, "Error exporting text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error exporting text", e);
            Toast.makeText(mContext, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showExportSuccessDialog(File file, String fileType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Export Successful");
        builder.setMessage(fileType + " file exported successfully!\n\nFile: " + file.getName() +
                "\nLocation: Downloads folder\n\nWould you like to open the file?");

        builder.setPositiveButton("Open", (dialog, which) -> {
            openFile(file);
        });

        builder.setNegativeButton("Later", (dialog, which) -> {
            Toast.makeText(mContext, "File saved to Downloads folder", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * Open exported file with default app
     */
    private void openFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, getMimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "No app found to open this file type", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error opening file", e);
            Toast.makeText(mContext, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get MIME type for file
     */
    private String getMimeType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv")) {
            return "text/csv";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "*/*";
        }
    }

    /**
     * Check and request storage permission (for Android 6.0+)
     * เพิ่ม method นี้ถ้าต้องการจัดการ permission
     */
    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return requireContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Request storage permission
     */
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION
            );
        }
    }

    private static final int REQUEST_STORAGE_PERMISSION = 1001;

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

        searchView.setQueryHint("Search product...");
    }

    private void setupListView() {
        adapter = new ProductItemAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);


        // Single click - show item details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ProductItemLite item = adapter.getItem(position);
            handleItemClick(item);
        });

        // Long click - open location fragment
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            ProductItemLite item = adapter.getItem(position);
            handleItemLongClick(item);
            return true; // Consume the long click event
        });
    }

    private void handleItemLongClick(ProductItemLite item) {
        // Check if tag number is available
        if (item.getTagNumber() == null || item.getTagNumber().isEmpty()) {
            Toast.makeText(mContext, "No tag number available for this item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Product Locating");

        String message = String.format("Locate product:\n%s (%s)\n\nTag: %s",
                item.getProductName(),
                item.getProductNumber(),
                item.getTagNumber());

        builder.setMessage(message);

        builder.setPositiveButton("Start Locating", (dialog, which) -> {
            openProductLocatingFragment(item);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void openProductLocatingFragment(ProductItemLite item) {
        try {
            // Create ProductLocatingFragment with item data
//            ProductLocatingFragment locatingFragment = ProductLocatingFragment.newInstance(
//                    item.getTagNumber(),
//                    item.getProductName(),
//                    item.getProductNumber()
//            );

            //GoodsReceiveItemFragment detailFragment = GoodsReceiveItemFragment.newInstance(goodsReceiveId);

//            if (getActivity() != null) {
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(android.R.id.content, locatingFragment)
//                        .addToBackStack(null)
//                        .commit();
//            }

            // Navigate to ProductLocatingFragment
//            if (getActivity() != null && getActivity() instanceof MainActivity) {
//                // ใช้ android.R.id.content แทน
//                getActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(android.R.id.content, locatingFragment)
//                        //.addToBackStack("ProductLocating")
//                        .commit();
//
//                Log.d(TAG, "Opened ProductLocatingFragment for tag: " + item.getTagNumber());
//            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening ProductLocatingFragment", e);
            Toast.makeText(mContext, "Error opening location view: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalCounts() {
        dbExecutor.execute(() -> {
            int totalItems = productItemDao.getTotalItemCount();
            int totalScanned = productItemDao.getScannedItemCount();
            int totalRemaining = totalItems - totalScanned;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotalItems.setText(String.valueOf(totalItems));
                    tvTotalScanned.setText(String.valueOf(totalScanned));
                    tvTotalRemaining.setText(String.valueOf(totalRemaining));
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
        // Check available memory before sync
        if (!hasEnoughMemory()) {
            Toast.makeText(mContext, "Low memory. Please close other apps and try again.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Skipping sync due to low memory");
            return;
        }
        
        showLoading(true);
        btnScanRFID.setEnabled(false);

        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("PBPPrefs", Context.MODE_PRIVATE);
        String lastSyncTime = prefs.getString("Shared_ProductItem_SyncTime", null);

        apiService.getProductItemsLiteStream(lastSyncTime).enqueue(new Callback<okhttp3.ResponseBody>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
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
                                batch.add(convertToEntity(lite));

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
                                    
                                    // Log progress every 5000 items
                                    if (count % 5000 == 0) {
                                        Log.d(TAG, "Synced " + count + " items...");
                                    }
                                    
                                    // Small delay to allow GC
                                    try { Thread.sleep(10); } catch (InterruptedException e) {}
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
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Log.d(TAG, "Loaded " + finalCount + " items to Room via streaming");
                                    // Refresh display list to show scanned items after sync
                                    refreshDisplayList();
                                    updateTotalCounts();
                                    showLoading(false);
                                    btnScanRFID.setEnabled(true);
                                    Toast.makeText(mContext, "Synced " + finalCount + " items successfully.", Toast.LENGTH_SHORT).show();
                                });
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Streaming error", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                    showError("Sync error: " + e.getMessage());
                                });
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Error loading product items: " + response.code());
                    showError("Failed to load product items");
                    showLoading(false);
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error loading product items", t);
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

    private void updateUI(List<ProductItemLite> items) {
        dbExecutor.execute(() -> {
            String warehouse = "Unknown";
            if (!items.isEmpty()) {
                warehouse = items.get(0).getWarehouseName();
            } else {
                ProductItemEntity any = productItemDao.getAnyItem();
                if (any != null) warehouse = any.getWarehouseName();
            }
            
            final String finalWarehouse = warehouse != null ? warehouse : "-";
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvWarehouseName.setText("Warehouse: " + finalWarehouse);
                });
            }
        });

        adapter.updateItems(items);
        updateTotalCounts();
    }

    private void handleItemClick(ProductItemLite item) {
        // Show item details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Product Item Details");

        String itemInfo = String.format("Product: %s\nProduct Number: %s\nProduct Group: %s\nTag Number: %s\nLot Number: %s\nWarehouse: %s\nStatus: %s\nScanned: %s",
                item.getProductName(),
                item.getProductNumber(),
                item.getProductGroupName() != null ? item.getProductGroupName() : "N/A",
                item.getTagNumber() != null ? item.getTagNumber() : "N/A",
                item.getLotNumber() != null ? item.getLotNumber() : "N/A",
                item.getWarehouseName() != null ? item.getWarehouseName() : "N/A",
                getStatusText(item.getProductItemStatus() != null ? item.getProductItemStatus() : -100),
                item.isScanned() ? "Yes" : "No");

        builder.setMessage(itemInfo);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private String getStatusText(int status) {
        switch (status) {
            case -1: return "Cancel";
            case -2: return "Out of Stock";
            case 0: return "NEW";
            case 1: return "STOCK";
            case 2: return "PICK";
            case 3: return "PACK";
            case 4: return "RECEIVE SHIPPING";
            case 5: return "PREPARING SHIP";
            case 6: return "LOADING";
            case 7: return "SHIPPED";
            case 8: return "Move Out";
            case 98: return "Adjust--";
            case 99: return "Adjust++";
            default: return "Unknown (" + status + ")";
        }
    }

    // RFID Handler
    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("LongLogTag")
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
                        btnScanRFID.setEnabled(true);
                    } else {
                        //Utils.playSound(2);
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
                        btnScanRFID.setEnabled(false);
                    } else {
                        //Utils.playSound(2);
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
            processTagCode(xx_tag);
        }
        tagMap.put(MainActivity.TAG_USER, info.getUser());
        tagMap.put(MainActivity.TAG_DATA, data);
        tagMap.put(MainActivity.TAG_TID, info.getTid());
        tagMap.put(MainActivity.TAG_RSSI, info.getRssi() == null ? "" : info.getRssi());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
            uhf = mContext.uhf;
            productItemDao = AppDatabase.getInstance(context).productItemDao();

            if (tagList == null) {
                tagList = new ArrayList<>();
            }

            if (tempDatas == null) {
                tempDatas = new ArrayList<>();
            }

            mContext.addConnectStatusNotice(mConnectStatus);
        }
    }

    private void processTagCode(String actualTag) {
        dbExecutor.execute(() -> {
            ProductItemEntity entity = productItemDao.findByTag(actualTag);
            if (entity != null) {
                if (!entity.isScanned()) {
                    entity.setScanned(true);
                    productItemDao.update(entity);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Utils.playSound(1);
                            updateTotalCounts();
                            refreshDisplayList();
                        });
                    }
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        //Utils.showToast(getContext(), "Tag not found: " + actualTag);
                        addScanNotification(actualTag + " - Tag not found");
                    });
                }
            }
        });
    }

    private void refreshDisplayList() {
        dbExecutor.execute(() -> {
            // In this fragment, we usually show scanned items as they are found
            List<ProductItemEntity> entities = productItemDao.getScannedItems();
            List<ProductItemLite> lites = new ArrayList<>();
            for (ProductItemEntity e : entities) {
                lites.add(convertToLite(e));
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    itemList.clear();
                    itemList.addAll(lites);
                    adapter.updateItems(itemList);
                    emptyView.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
                    updateUI(itemList); // Update warehouse name if needed
                });
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

    private ProductItemEntity convertToEntity(ProductItemLite lite) {
        ProductItemEntity entity = new ProductItemEntity();
        entity.setTagNumber(lite.getTagNumber());
        entity.setProductName(lite.getProductName());
        entity.setProductNumber(lite.getProductNumber());
        entity.setProductGroupName(lite.getProductGroupName());
        entity.setLotNumber(lite.getLotNumber());
        entity.setWarehouseName(lite.getWarehouseName());
        entity.setProductItemStatus(lite.getProductItemStatus());
        entity.setScanned(lite.isScanned());
        entity.setLastUpdateDate(lite.getLastUpdateDate());
        entity.setNotDeleted(lite.isNotDeleted());
        return entity;
    }

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

    @SuppressLint("LongLogTag")
    private void stop() {
        Log.i(TAG, "stop mContext.isScanning=false");
        handler.removeMessages(FLAG_TIME_OVER);
        if (mContext.isScanning) {
            stopInventory();
        }
        mContext.isScanning = false;
        cancelInventoryTask();
    }

    @SuppressLint("LongLogTag")
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
        boolean result = false;
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
        if (mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }

    private int selectIndex = -1;

    private void clearData() {
        dbExecutor.execute(() -> {
            productItemDao.clearScannedStatus();
            getActivity().runOnUiThread(() -> {
                tagList = new ArrayList<>();
                tempDatas = new ArrayList<>();
                
                // Force listView to refresh
                if (listView != null) {
                    listView.invalidateViews();
                }

                updateTotalCounts();
                refreshDisplayList();

                // แสดง empty state อีกครั้ง
                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                }
                
                Utils.showToast(getContext(), "Scanned data cleared locally.");
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        checkNotifications();
        refreshDisplayList(); // โหลดรายการที่เคยแสกนไว้จาก Database มาแสดง

        if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            btnScanRFID.setEnabled(true);
        } else {
            btnScanRFID.setEnabled(false);
        }
    }

    private void scanRFID() {
        startThread();
    }

    private void stopRFID() {
        //btnUpdate.setEnabled(true);
        stop();
    }

    private void clearRFID() {
        Log.d(TAG, "clearRFID called");

        // เคลียร์ notifications
        notificationMessages.clear();
        hasNotifications = false;
        updateNotificationVisibility();
        saveNotifications(); // ล้างข้อมูลที่บันทึกในเครื่องเมื่อสั่ง Clear ทั้งหมด

        dbExecutor.execute(() -> {
            // Match StockCountFragment: Only clear scanned status, don't delete all or reload
            productItemDao.clearScannedStatus();
            
            getActivity().runOnUiThread(() -> {
                tagList = new ArrayList<>();
                tempDatas = new ArrayList<>();
                itemList.clear();
                
                if (adapter != null) {
                    adapter.updateItems(new ArrayList<>());
                    adapter.notifyDataSetChanged();
                }

                if (listView != null) {
                    listView.invalidateViews();
                }

                updateTotalCounts();

                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                }

                Toast.makeText(mContext, "Scanned data cleared", Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void clearSearch() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
    }

    public void refreshData() {
        clearSearch();
        clearData(); // Call the refactored clearData
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }

        Toast.makeText(mContext, "Reset completed. Ready to scan again.", Toast.LENGTH_SHORT).show();
    }

    class ConnectStatus implements MainActivity.IConnectStatus {
        @SuppressLint("LongLogTag")
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
                    btnScanRFID.setEnabled(true);
                }
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                stop();
                btClear.setEnabled(true);
                btStop.setEnabled(false);
                btnScanRFID.setEnabled(false);
            }
        }
    }
}
