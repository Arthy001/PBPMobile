package com.example.uhf_bt.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.MoveOrderAdapter;
import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveOrderFragment extends Fragment {

    private static final String TAG = "MoveOrderFragment";

    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private MoveOrderAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<MoveOrder> allMoveOrders; // เก็บรายการทั้งหมดไว้สำหรับการกรอง

    public static MoveOrderFragment newInstance(String param1, String param2) {
        MoveOrderFragment fragment = new MoveOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MoveOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_order, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allMoveOrders = new ArrayList<>();

        return view;
    }

    private void initViews(View view) {
        searchView = view.findViewById(R.id.searchView);
        btnBack = view.findViewById(R.id.btnBack);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        statusTabLayout = view.findViewById(R.id.statusTabLayout);
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ MoveOrder
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("New"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("In Progress"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Success"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancel"));

        statusTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrdersByStatus(tab.getPosition());
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
        if (allMoveOrders == null || allMoveOrders.isEmpty()) {
            return;
        }

        List<MoveOrder> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allMoveOrders);
                break;
            case 1: // New (0)
                for (MoveOrder order : allMoveOrders) {
                    if (order.getStatus() == 0) {
                        filteredList.add(order);
                    }
                }
                break;
            case 2: // In Progress (1)
                for (MoveOrder order : allMoveOrders) {
                    if (order.getStatus() == 1) {
                        filteredList.add(order);
                    }
                }
                break;
            case 3: // Success (2)
                for (MoveOrder order : allMoveOrders) {
                    if (order.getStatus() == 2) {
                        filteredList.add(order);
                    }
                }
                break;
            case 4: // Cancel (-1)
                for (MoveOrder order : allMoveOrders) {
                    if (order.getStatus() == -1) {
                        filteredList.add(order);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setMoveOrders(filteredList);

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

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
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
        adapter = new MoveOrderAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MoveOrder moveOrder = adapter.getItem(position);
            openMoveOrderChild(String.valueOf(moveOrder.getId()));
        });
    }

    private void openMoveOrderChild(String moveOrderId) {

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

        MoveOrderChildFragment detailFragment = new MoveOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("MoveOrderId", moveOrderId);
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadMoveOrders() {
        showLoading(true);
        apiService.getMoveOrders().enqueue(new Callback<List<MoveOrder>>() {
            @Override
            public void onResponse(Call<List<MoveOrder>> call, Response<List<MoveOrder>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allMoveOrders = response.body();

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterOrdersByStatus(selectedTabPosition);

                    if (allMoveOrders.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading move orders: " + response.code());
                    showError("Failed to load move orders");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrder>> call, Throwable t) {
                Log.e(TAG, "Network error loading move orders", t);
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
        loadMoveOrders();
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadMoveOrders();
    }
}

