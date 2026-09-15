package com.example.uhf_bt.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.PickingOrderAdapter;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingOrderFragment extends Fragment {
    private static final String TAG = "PickingOrderFragment";
    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PickingOrderAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<PickingOrder> allPickingOrders; // เก็บรายการทั้งหมดไว้สำหรับการกรอง

    private TextView datePickerTextView; // ประกาศตัวแปรสำหรับ TextView

    String currentDate;
    private int selectedTabPosition = 0;

    public static PickingOrderFragment newInstance(String param1, String param2) {
        PickingOrderFragment fragment = new PickingOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel2));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking_order, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allPickingOrders = new ArrayList<>();

        // 1. รับวันที่ปัจจุบัน ถ้ายังไม่มีการเลือก
        if (currentDate == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            currentDate = dateFormat.format(calendar.getTime());
        }

        // 2. ตั้งค่า TextView ให้แสดงวันที่ปัจจุบันหรือที่เลือก
        datePickerTextView.setText(currentDate);

        // 3. เรียกใช้ฟังก์ชัน loadPickingOrders ด้วยวันที่ปัจจุบันหรือที่เลือก
        //loadPickingOrders(currentDate);

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        statusTabLayout = view.findViewById(R.id.statusTabLayout);
        datePickerTextView = view.findViewById(R.id.datePickerTextView);

    }


    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        datePickerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    // เพิ่มฟังก์ชัน showDatePickerDialog เพื่อให้ผู้ใช้สามารถเลือกวันที่ได้
    private void showDatePickerDialog() {
        int year, month, day;
        if (currentDate != null && currentDate.length() == 10) {
            String[] parts = currentDate.split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]) - 1;
            day = Integer.parseInt(parts[2]);
        } else {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        currentDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        datePickerTextView.setText(currentDate);
                        loadPickingOrders(currentDate); // เรียกใช้ฟังก์ชันอีกครั้งด้วยวันที่ที่ผู้ใช้เลือก
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ PickingOrder
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("New"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Picked"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Packed"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Receive Shipping"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("จัด Kanban"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Loading"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Shipped"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancelled"));

        TabLayout.Tab savedTab = statusTabLayout.getTabAt(selectedTabPosition);
        if (savedTab != null) {
            savedTab.select();
        }

        statusTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabPosition = tab.getPosition();
                filterOrdersByStatus(selectedTabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // ไม่ต้องทำอะไร
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // ไม่ต้องทำอะไร
            }
        });
    }

    private void filterOrdersByStatus(int tabPosition) {
        if (allPickingOrders == null) {
            allPickingOrders = new ArrayList<>();
        }

        List<PickingOrder> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allPickingOrders);
                break;
            case 1: // New (0)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 0) {
                        filteredList.add(order);
                    }
                }
                break;
            case 2: // Packed (1)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 1) {
                        filteredList.add(order);
                    }
                }
                break;
            case 3: // Picked (2)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 2) {
                        filteredList.add(order);
                    }
                }
                break;
            case 4: // Receive Shipping (3)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 3) {
                        filteredList.add(order);
                    }
                }
                break;
            case 5: // Preparing Ship (3)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 4) {
                        filteredList.add(order);
                    }
                }
                break;
            case 6: // Loading (4)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 5) {
                        filteredList.add(order);
                    }
                }
                break;
            case 7: // Shipped (4)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == 6) {
                        filteredList.add(order);
                    }
                }
                break;
            case 8: // Cancelled (-1)
                for (PickingOrder order : allPickingOrders) {
                    if (order.getOrderStatus() != null && order.getOrderStatus() == -1) {
                        filteredList.add(order);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setPickingOrders(filteredList);

        // แสดงข้อความเมื่อไม่มีรายการ
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void hideEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void setupSearchView() {
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnSearchClickListener(v -> {
            btnBack.setVisibility(View.GONE);
            toolbar_title.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            searchView.setLayoutParams(params);
        });

        searchView.setOnCloseListener(() -> {
            btnBack.setVisibility(View.VISIBLE);
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

        customizeSearchView();
    }

    private void customizeSearchView() {
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.WHITE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) searchIcon.getLayoutParams();
            params.setMarginEnd(0);
            searchIcon.setLayoutParams(params);
        }

        EditText searchEditText = searchView.findViewById(com.example.uhf_bt.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(Color.WHITE);
        }

        searchView.setQueryHint("Search order...");
    }

    private void setupListView() {
        adapter = new PickingOrderAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrder pickingOrder = adapter.getItem(position);
            openPickingOrderChild(String.valueOf(pickingOrder.getId()));
        });
    }

    private void openPickingOrderChild(String pickingOrderId) {

        // ล้างค่าค้นหาก่อนเปิด Fragment ใหม่
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);

            // คืนสถานะปุ่ม Back และ Title
            btnBack.setVisibility(View.VISIBLE);
            toolbar_title.setVisibility(View.VISIBLE);

            // คืนขนาดของ SearchView
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            searchView.setLayoutParams(params);
        }

        PickingOrderChildFragment detailFragment = new PickingOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("PickingOrderId", pickingOrderId);
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadPickingOrders(String date) {
        showLoading(true);
        apiService.getPickingOrdersbyDate(date).enqueue(new Callback<List<PickingOrder>>() {
            @Override
            public void onResponse(Call<List<PickingOrder>> call, Response<List<PickingOrder>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allPickingOrders = response.body();

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterOrdersByStatus(selectedTabPosition);

                    if (allPickingOrders.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading picking orders: " + response.code());
                    allPickingOrders = new ArrayList<>();
                    filterOrdersByStatus(statusTabLayout.getSelectedTabPosition());
                    showError("Failed to load picking orders");
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrder>> call, Throwable t) {
                Log.e(TAG, "Network error loading picking orders", t);
                showLoading(false);
                allPickingOrders = new ArrayList<>();
                filterOrdersByStatus(statusTabLayout.getSelectedTabPosition());
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (listView != null) {
            listView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPickingOrders(currentDate);
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadPickingOrders(currentDate);
    }
}

//package com.example.uhf_bt.fragment;
//
//import android.graphics.Color;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import androidx.appcompat.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.uhf_bt.R;
//import com.example.uhf_bt.adapter.MoveOrderAdapter;
//import com.example.uhf_bt.adapter.PickingOrderAdapter;
//import com.example.uhf_bt.models.MoveOrder;
//import com.example.uhf_bt.models.PickingOrder;
//import com.example.uhf_bt.services.ApiService;
//import com.example.uhf_bt.services.RetrofitClient;
//
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//
//public class PickingOrderFragment extends Fragment {
//    private static final String TAG = "PickingOrderFragment";
//    private ImageButton btnBack;
//    private TextView toolbar_title;
//    private SearchView searchView;
//    private ListView listView;
//    private ProgressBar progressBar;
//    private View emptyView;
//    private PickingOrderAdapter adapter;
//    private ApiService apiService;
//    // TODO: Rename and change types and number of parameters
//    public static PickingOrderFragment newInstance(String param1, String param2) {
//        PickingOrderFragment fragment = new PickingOrderFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_picking_order, container, false);
//
//        initViews(view);
//        setupBackButton();
//        setupSearchView();
//        setupListView();
//
//        apiService = RetrofitClient.getClient().create(ApiService.class);
//
//        return view;
//    }
//
//    private void initViews(View view) {
//        btnBack = view.findViewById(R.id.btnBack);
//        listView = view.findViewById(R.id.listView);
//        progressBar = view.findViewById(R.id.progressBar);
//        emptyView = view.findViewById(R.id.emptyView);
//        toolbar_title = view.findViewById(R.id.toolbar_title);
//        searchView = view.findViewById(R.id.searchView);
//    }
//
//    private void setupBackButton() {
//        btnBack.setOnClickListener(v -> {
//            if (getActivity() != null) {
//                getActivity().onBackPressed();
//            }
//        });
//    }
//
//    private void setupSearchView() {
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        searchView.setOnSearchClickListener(v -> {
//            btnBack.setVisibility(View.GONE);
//            toolbar_title.setVisibility(View.GONE);
//            ViewGroup.LayoutParams params = searchView.getLayoutParams();
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            searchView.setLayoutParams(params);
//        });
//
//        searchView.setOnCloseListener(() -> {
//            btnBack.setVisibility(View.VISIBLE);
//            toolbar_title.setVisibility(View.VISIBLE);
//            ViewGroup.LayoutParams params = searchView.getLayoutParams();
//            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//            searchView.setLayoutParams(params);
//            return false;
//        });
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (adapter != null) {
//                    adapter.getFilter().filter(newText);
//                }
//                return true;
//            }
//        });
//
//        customizeSearchView();
//    }
//
//    private void customizeSearchView() {
//        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
//        if (searchIcon != null) {
//            searchIcon.setColorFilter(Color.WHITE);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) searchIcon.getLayoutParams();
//            params.setMarginEnd(0);
//            searchIcon.setLayoutParams(params);
//        }
//
//        EditText searchEditText = searchView.findViewById(com.example.uhf_bt.R.id.search_src_text);
//        if (searchEditText != null) {
//            searchEditText.setTextColor(Color.WHITE);
//            searchEditText.setHintTextColor(Color.parseColor("#80FFFFFF"));
//        }
//
//        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
//        if (closeButton != null) {
//            closeButton.setColorFilter(Color.WHITE);
//        }
//
//        searchView.setQueryHint("Search order...");
//    }
//
//    private void setupListView() {
//        adapter = new PickingOrderAdapter(getContext());
//        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            PickingOrder pickingOrder = adapter.getItem(position);
//            openPickingOrderChild(String.valueOf(pickingOrder.getId()));
//        });
//    }
//
//    private void openPickingOrderChild(String pickingOrderId) {
//        PickingOrderChildFragment detailFragment = new PickingOrderChildFragment();
//        Bundle args = new Bundle();
//        args.putString("PickingOrderId", pickingOrderId);
//        detailFragment.setArguments(args);
//
//        if (getActivity() != null) {
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(android.R.id.content, detailFragment)
//                    .addToBackStack(null)
//                    .commit();
//        }
//
//    }
//
//    private void loadPickingOrders() {
//        showLoading(true);
//        apiService.getPickingOrders().enqueue(new Callback<List<PickingOrder>>() {
//            @Override
//            public void onResponse(Call<List<PickingOrder>> call, Response<List<PickingOrder>> response) {
//                showLoading(false);
//                if (response.isSuccessful() && response.body() != null) {
//                    List<PickingOrder> pickingOrders = response.body();
//                    adapter.setPickingOrders(pickingOrders);
//                    if (pickingOrders.isEmpty()) {
//                        showEmptyState();
//                    }
//                } else {
//                    Log.e(TAG, "Error loading move orders: " + response.code());
//                    showError("Failed to load move orders");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<PickingOrder>> call, Throwable t) {
//                Log.e(TAG, "Network error loading move orders", t);
//                showLoading(false);
//                showError("Network error: " + t.getMessage());
//            }
//        });
//    }
//
//    private void showLoading(boolean show) {
//        if (progressBar != null) {
//            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//        if (listView != null) {
//            listView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
//
//    private void showError(String message) {
//        if (getContext() != null) {
//            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void showEmptyState() {
//        if (emptyView != null) {
//            emptyView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        loadPickingOrders();
//    }
//
//    public void refreshData() {
//        if (searchView != null) {
//            searchView.setQuery("", false);
//            searchView.clearFocus();
//        }
//        loadPickingOrders();
//    }
//
//
//}
