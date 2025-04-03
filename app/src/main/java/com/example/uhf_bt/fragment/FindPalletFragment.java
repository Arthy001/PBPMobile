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
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.PalletTagAdapter;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

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

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allPalletTags = new ArrayList<>();

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
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ Pallet
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("New"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Picked"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Packed"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("รับเข้าจัดส่ง"));
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

    private void filterPalletsByStatus(int tabPosition) {
        if (allPalletTags == null || allPalletTags.isEmpty()) {
            return;
        }

        List<PalletTag> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allPalletTags);
                break;
            case 1: // New (0)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 1) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 2: // Picked (1)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 2) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 3: // Packed (2)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 3) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 4: // รับเข้าจัดส่ง (3)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 4) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 5: // โหลดสินค้า (4)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 5) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 6: // จัดส่งแล้ว (5)
                for (PalletTag pallet : allPalletTags) {
                    if (pallet.getPalletStatus() == 6) {
                        filteredList.add(pallet);
                    }
                }
                break;
            case 7: // Cancel (-1)
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
        adapter = new PalletTagAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PalletTag palletTag = adapter.getItem(position);
            openPalletChild(String.valueOf(palletTag.getPalletTagId()), String.valueOf(palletTag.getPickingOrder()));
        });
    }

    private void openPalletChild(String palletTagid, String PickingOrder) {

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
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadPalletTags() {
        showLoading(true);
        apiService.getPalletTags().enqueue(new Callback<List<PalletTag>>() {
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
                    showError("Failed to load pallet tags");
                }
            }

            @Override
            public void onFailure(Call<List<PalletTag>> call, Throwable t) {
                Log.e(TAG, "Network error loading pallet tags", t);
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
        loadPalletTags();
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadPalletTags();
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
//import com.example.uhf_bt.adapter.PalletTagAdapter;
//import com.example.uhf_bt.models.MoveOrder;
//import com.example.uhf_bt.models.PalletTag;
//import com.example.uhf_bt.services.ApiService;
//import com.example.uhf_bt.services.RetrofitClient;
//
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link PickingOrderFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class FindPalletFragment extends Fragment {
//    private static final String TAG = "PickingOrderFragment";
//    private ImageButton btnBack;
//    private TextView toolbar_title;
//    private SearchView searchView;
//    private ListView listView;
//    private ProgressBar progressBar;
//    private View emptyView;
//    private PalletTagAdapter adapter;
//    private ApiService apiService;
//    // TODO: Rename and change types and number of parameters
//    public static FindPalletFragment newInstance(String param1, String param2) {
//        FindPalletFragment fragment = new FindPalletFragment();
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
//        View view = inflater.inflate(R.layout.fragment_find_pallet, container, false);
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
//        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
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
//        adapter = new PalletTagAdapter(getContext());
//        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            PalletTag palletTaG = adapter.getItem(position);
//            openPalletChild(String.valueOf(palletTaG.getPalletTagId()),String.valueOf(palletTaG.getPickingOrder()));
//        });
//    }
//
//    private void openPalletChild(String palletTagid , String PickingOrder) {
//        PalletChildFragment detailFragment = new PalletChildFragment();
//        Bundle args = new Bundle();
//        args.putString("PalletTagId", palletTagid);
//        args.putString("PickingOrder", PickingOrder);
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
//    private void loadPalletTags() {
//        showLoading(true);
//        apiService.getPalletTags().enqueue(new Callback<List<PalletTag>>() {
//            @Override
//            public void onResponse(Call<List<PalletTag>> call, Response<List<PalletTag>> response) {
//                showLoading(false);
//                if (response.isSuccessful() && response.body() != null) {
//                    List<PalletTag> palletTags = response.body();
//                    adapter.setPalletTags(palletTags);
//                    if (palletTags.isEmpty()) {
//                        showEmptyState();
//                    }
//                } else {
//                    Log.e(TAG, "Error loading pallet tags: " + response.code());
//                    showError("Failed to load pallet tags");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<PalletTag>> call, Throwable t) {
//                Log.e(TAG, "Network error loading pallet tags", t);
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
//        if (emptyView != null) {
//            emptyView.setVisibility(View.GONE);
//        }
//    }
//
//    private void showError(String message) {
//        Log.e(TAG, "Error: " + message);
//        if (getContext() != null) {
//            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void showEmptyState() {
//        if (emptyView != null) {
//            emptyView.setVisibility(View.VISIBLE);
//        }
//        if (listView != null) {
//            listView.setVisibility(View.GONE);
//        }
//    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        loadPalletTags();
//    }
//
//    public void refreshData() {
//        if (searchView != null) {
//            searchView.setQuery("", false);
//            searchView.clearFocus();
//        }
//       loadPalletTags();
//    }
//
//
//}