package com.example.uhf_bt.fragment;

import android.graphics.Color;
import android.os.Bundle;
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
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.PalletTagAdapter;
import com.example.uhf_bt.models.PalletGroupSummary;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPalletFragment extends Fragment {
    private static final String TAG = "FindPalletFragment";
    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PalletTagAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<PalletTag> allPalletTags; // เก็บรายการทั้งหมดไว้สำหรับการกรอง
    private Button btnReceivePallet; // เพิ่มตัวแปรสำหรับปุ่มรับพาแลท
    private TextView datePickerTextView;
    String currentDate;

    public static FindPalletFragment newInstance(String param1, String param2) {
        FindPalletFragment fragment = new FindPalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_pallet, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();
        setupReceivePalletButton(); // เพิ่มการตั้งค่าปุ่มรับพาแลท

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allPalletTags = new ArrayList<>();

        // 1. รับวันที่ปัจจุบัน ถ้ายังไม่มีการเลือก
        if (currentDate == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            currentDate = dateFormat.format(calendar.getTime());
        }

        // 2. ตั้งค่า TextView ให้แสดงวันที่ปัจจุบันหรือที่เลือก
        datePickerTextView.setText(currentDate);

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
        btnReceivePallet = view.findViewById(R.id.btnReceivePallet); // เพิ่มการอ้างอิงปุ่มรับพาแลท
        datePickerTextView = view.findViewById(R.id.datePickerTextView);
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ Pallet
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("New"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Pick"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Pack"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("รับเข้าจัดส่ง"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("RFIDLoading"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("RFID SUCCESS"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("โหลดขึ้นรถ"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Shipped"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancelled"));

        statusTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterPalletsByStatus(tab.getPosition());
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

    // เพิ่มเมธอดสำหรับตั้งค่าปุ่มรับพาแลท
    private void setupReceivePalletButton() {
        btnReceivePallet.setOnClickListener(v -> {
            openReceivePalletFragment();
        });
    }

    // เพิ่มเมธอดสำหรับเปิด Fragment รับพาแลท
    private void openReceivePalletFragment() {
        // สร้าง Fragment ใหม่ (คุณอาจต้องสร้าง ReceivePalletFragment ด้วย)
        ReceivePalletFragment receivePalletFragment = new ReceivePalletFragment();

        // เปิด Fragment ใหม่
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, receivePalletFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private List<PalletGroupSummary> groupPalletTags(List<PalletTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, PalletGroupSummary> groupMap = new LinkedHashMap<>();
        for (PalletTag tag : tags) {
            if (tag == null) continue;
            String pickingNumber = tag.getPickingNumber() != null ? tag.getPickingNumber() : "";
            String groupKey = !pickingNumber.isEmpty() ? pickingNumber : String.valueOf(tag.getPickingOrderId());

            if (!groupMap.containsKey(groupKey)) {
                PalletGroupSummary group = new PalletGroupSummary(tag);
                groupMap.put(groupKey, group);
            } else {
                groupMap.get(groupKey).addPalletTag(tag);
            }
        }
        return new ArrayList<>(groupMap.values());
    }

    private void filterPalletsByStatus(int tabPosition) {
        if (allPalletTags == null) {
            allPalletTags = new ArrayList<>();
        }

        List<PalletGroupSummary> allGroups = groupPalletTags(allPalletTags);
        List<PalletGroupSummary> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allGroups);
                break;
            case 1: // New (Register = 0)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(0)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 2: // Pick (1)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(1)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 3: // Pack (2)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(2)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 4: // ReceiveShipping (3)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(3)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 5: // RFIDLoading (4)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(4)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 6: // RFIDLoadingSuccess (5)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(5)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 7: // Loading (6)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(6)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 8: // Shipped (7)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(7)) {
                        filteredList.add(group);
                    }
                }
                break;
            case 9: // Cancelled (-1)
                for (PalletGroupSummary group : allGroups) {
                    if (group.hasStatus(-1)) {
                        filteredList.add(group);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setGroupList(filteredList);

        // แสดงข้อความเมื่อไม่มีรายการ
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateTabCounts(List<PalletGroupSummary> allGroups) {
        if (statusTabLayout == null) return;

        int allCount = allGroups != null ? allGroups.size() : 0;
        int newCount = 0, pickCount = 0, packCount = 0, receiveCount = 0;
        int rfidLoadingCount = 0, rfidSuccessCount = 0, loadingCount = 0, shippedCount = 0, cancelCount = 0;

        if (allGroups != null) {
            for (PalletGroupSummary group : allGroups) {
                if (group.hasStatus(0)) newCount++;
                if (group.hasStatus(1)) pickCount++;
                if (group.hasStatus(2)) packCount++;
                if (group.hasStatus(3)) receiveCount++;
                if (group.hasStatus(4)) rfidLoadingCount++;
                if (group.hasStatus(5)) rfidSuccessCount++;
                if (group.hasStatus(6)) loadingCount++;
                if (group.hasStatus(7)) shippedCount++;
                if (group.hasStatus(-1)) cancelCount++;
            }
        }

        setTabText(0, "All (" + allCount + ")");
        setTabText(1, "New (" + newCount + ")");
        setTabText(2, "Pick (" + pickCount + ")");
        setTabText(3, "Pack (" + packCount + ")");
        setTabText(4, "รับเข้าจัดส่ง (" + receiveCount + ")");
        setTabText(5, "RFIDLoading (" + rfidLoadingCount + ")");
        setTabText(6, "RFID SUCCESS (" + rfidSuccessCount + ")");
        setTabText(7, "โหลดขึ้นรถ (" + loadingCount + ")");
        setTabText(8, "Shipped (" + shippedCount + ")");
        setTabText(9, "Cancelled (" + cancelCount + ")");
    }

    private void setTabText(int position, String text) {
        if (statusTabLayout != null) {
            TabLayout.Tab tab = statusTabLayout.getTabAt(position);
            if (tab != null) {
                tab.setText(text);
            }
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

        datePickerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

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
                        loadPalletTags(currentDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
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
        adapter = new PalletTagAdapter(getContext());
        listView.setAdapter(adapter);

        adapter.setOnItemClickListener((group, position) -> {
            if (group != null) {
                openPalletChild(
                        group.getFirstPalletTagNumber(),
                        group.getPickingNumber(),
                        String.valueOf(group.getPickingOrderId())
                );
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PalletGroupSummary group = adapter.getItem(position);
            if (group != null) {
                openPalletChild(
                        group.getFirstPalletTagNumber(),
                        group.getPickingNumber(),
                        String.valueOf(group.getPickingOrderId())
                );
            }
        });
    }

    private void openPalletChild(String palletTagid, String PickingOrder, String pickinkOrderId) {

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

        PalletChildFragment detailFragment = new PalletChildFragment();
        Bundle args = new Bundle();
        args.putString("PalletTagId", palletTagid);
        args.putString("PickingOrder", PickingOrder);
        args.putString("pickingOrderId", pickinkOrderId);
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadPalletTags(String date) {
        showLoading(true);
        apiService.getPalletTagsByDate(date).enqueue(new Callback<List<PalletTag>>() {
            @Override
            public void onResponse(Call<List<PalletTag>> call, Response<List<PalletTag>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allPalletTags = response.body();

                    List<PalletGroupSummary> allGroups = groupPalletTags(allPalletTags);
                    updateTabCounts(allGroups);

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterPalletsByStatus(selectedTabPosition);

                    if (allPalletTags.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading pallet tags: " + response.code());
                    allPalletTags = new ArrayList<>();
                    updateTabCounts(new ArrayList<>());
                    filterPalletsByStatus(statusTabLayout.getSelectedTabPosition());
                    showError("Failed to load pallet tags");
                }
            }

            @Override
            public void onFailure(Call<List<PalletTag>> call, Throwable t) {
                Log.e(TAG, "Network error loading pallet tags", t);
                showLoading(false);
                allPalletTags = new ArrayList<>();
                updateTabCounts(new ArrayList<>());
                filterPalletsByStatus(statusTabLayout.getSelectedTabPosition());
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
        loadPalletTags(currentDate);
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadPalletTags(currentDate);
    }
}

