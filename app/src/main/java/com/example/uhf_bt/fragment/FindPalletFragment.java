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
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Picked"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Packed"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("รับเข้าจัดส่ง"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("จัด Kanban"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("โหลดสินค้า"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("จัดส่งแล้ว"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancel"));

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

    private void filterPalletsByStatus(int tabPosition) {
        if (allPalletTags == null) {
            allPalletTags = new ArrayList<>();
        }

        List<PalletTag> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allPalletTags);
                break;
            case 1: // New (0)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 0) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 2: // Picked (1)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 1) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 3: // Packed (2)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 2) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 4: // รับเข้าจัดส่ง (3)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 3) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 5: // PreparingSHip (4)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 4) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 6: // Loading (5)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 5) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 7: // จัดส่งแล้ว (5)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 6) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 8: // Cancel (-1)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == -1) {
                        filteredList.add(pallet);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setPalletTags(filteredList);

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

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PalletTag palletTag = adapter.getItem(position);
            openPalletChild(
                    String.valueOf(palletTag.getPalletTagNumber()),
                    String.valueOf(palletTag.getPickingNumber()),
                    String.valueOf(palletTag.getPickingOrderId())
            );
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

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterPalletsByStatus(selectedTabPosition);

                    if (allPalletTags.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading pallet tags: " + response.code());
                    allPalletTags = new ArrayList<>();
                    filterPalletsByStatus(statusTabLayout.getSelectedTabPosition());
                    showError("Failed to load pallet tags");
                }
            }

            @Override
            public void onFailure(Call<List<PalletTag>> call, Throwable t) {
                Log.e(TAG, "Network error loading pallet tags", t);
                showLoading(false);
                allPalletTags = new ArrayList<>();
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

