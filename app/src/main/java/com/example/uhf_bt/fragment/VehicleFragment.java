package com.example.uhf_bt.fragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.VehicleAdapter;
import com.example.uhf_bt.models.VehicleDto;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleFragment extends Fragment {
    private static final String TAG = "VehicleFragment";
    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private VehicleAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<VehicleDto> allVehicles; // เก็บรายการทั้งหมดไว้สำหรับการกรอง
    private Button btnManageVehicle; // เพิ่มตัวแปรสำหรับปุ่มจัดการรถ

    public static VehicleFragment newInstance(String param1, String param2) {
        VehicleFragment fragment = new VehicleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel5));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();
        setupManageVehicleButton(); // เพิ่มการตั้งค่าปุ่มจัดการรถ

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allVehicles = new ArrayList<>();

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
        btnManageVehicle = view.findViewById(R.id.btnManageVehicle); // เพิ่มการอ้างอิงปุ่มจัดการรถ
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ Vehicle ตามลำดับขั้นตอนงาน
        statusTabLayout.removeAllTabs();
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Ready"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Check In"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Loading"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Shipped"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Success"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancelled"));

        statusTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterVehiclesByStatus(tab.getPosition());
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

    // เพิ่มเมธอดสำหรับตั้งค่าปุ่มจัดการรถ
    private void setupManageVehicleButton() {
        btnManageVehicle.setOnClickListener(v -> {
            openVehicleManagement();
        });
    }

    // เพิ่มเมธอดสำหรับเปิด Fragment จัดการรถ
    private void openVehicleManagement() {
         // เรียกใช้งาน ScanVehicleFragment ตัวใหม่
         ScanVehicleFragment vehicleManagementFragment = ScanVehicleFragment.newInstance();

         //เปิด Fragment ใหม่
         if (getActivity() != null) {
             getActivity().getSupportFragmentManager().beginTransaction()
                     .replace(android.R.id.content, vehicleManagementFragment)
                     .addToBackStack(null)
                     .commit();
         }

        //Toast.makeText(getContext(), "Vehicle Management feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void filterVehiclesByStatus(int tabPosition) {
        if (allVehicles == null || allVehicles.isEmpty()) {
            return;
        }

        List<VehicleDto> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allVehicles);
                break;
            case 1: // Ready (1)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == 1) {
                        filteredList.add(vehicle);
                    }
                }
                break;
            case 2: // CheckIn (11)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == 11) {
                        filteredList.add(vehicle);
                    }
                }
                break;
            case 3: // Loading (12)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == 12) {
                        filteredList.add(vehicle);
                    }
                }
                break;
            case 4: // Shipped (13)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == 13) {
                        filteredList.add(vehicle);
                    }
                }
                break;
            case 5: // DeliverySuccess (14)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == 14) {
                        filteredList.add(vehicle);
                    }
                }
                break;
            case 6: // Cancelled (-1)
                for (VehicleDto vehicle : allVehicles) {
                    if (vehicle.getVehicleStatus() == -1) {
                        filteredList.add(vehicle);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setVehicles(filteredList);

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
        if (listView != null) {
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
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

        searchView.setQueryHint("Search vehicle...");
    }

    private void setupListView() {
        adapter = new VehicleAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            VehicleDto vehicle = adapter.getItem(position);
            openVehicleDetail(String.valueOf(vehicle.getId()),
                    vehicle.getLicensePlate(),
                    vehicle.getDriver(),
                    vehicle.getVehicleTag(),
                    vehicle.getNumber(),
                    String.valueOf(vehicle.getVehicleStatus()));
        });
    }

    private void openVehicleDetail(String vehicleId, String licensePlate, String driver
            , String vehicleTag, String number, String vehicleStatus) {

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

        // สร้าง Fragment รายละเอียดรถ
          VehicleDetailFragment detailFragment = VehicleDetailFragment.newInstance(
                  vehicleId, licensePlate, driver, vehicleTag, number, vehicleStatus
          );

         if (getActivity() != null) {
             getActivity().getSupportFragmentManager().beginTransaction()
                     .replace(android.R.id.content, detailFragment)
                     .addToBackStack(null)
                     .commit();
         }

        Toast.makeText(getContext(), "Vehicle Detail: " + licensePlate + " - " + driver, Toast.LENGTH_SHORT).show();
    }

    private void loadVehicles() {
        showLoading(true);
        apiService.getVehicles().enqueue(new Callback<List<VehicleDto>>() {
            @Override
            public void onResponse(Call<List<VehicleDto>> call, Response<List<VehicleDto>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allVehicles = response.body();

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterVehiclesByStatus(selectedTabPosition);

                    if (allVehicles.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading Vehicles: " + response.code());
                    showError("Failed to load Vehicles");
                }
            }

            @Override
            public void onFailure(Call<List<VehicleDto>> call, Throwable t) {
                Log.e(TAG, "Network error loading Vehicles", t);
                showLoading(false);
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
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Log.e(TAG, "Error: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (listView != null) {
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVehicles();
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadVehicles();
    }
}
