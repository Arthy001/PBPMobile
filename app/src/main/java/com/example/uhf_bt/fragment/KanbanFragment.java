package com.example.uhf_bt.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
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
import com.example.uhf_bt.adapter.KanbanAdapter;
import com.example.uhf_bt.adapter.PalletTagAdapter;
import com.example.uhf_bt.models.Kanban;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.rscja.team.qcom.deviceapi.K;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KanbanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KanbanFragment extends Fragment {
    private static final String TAG = "KanbanFragment";
    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private KanbanAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<Kanban> allKanban; // เก็บรายการทั้งหมดไว้สำหรับการกรอง
    private Button btnReceivePallet; // เพิ่มตัวแปรสำหรับปุ่มรับพาแลท

    public static KanbanFragment newInstance(String param1, String param2) {
        KanbanFragment fragment = new KanbanFragment();
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
        View view = inflater.inflate(R.layout.fragment_kanban, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();
        setupReceivePalletButton(); // เพิ่มการตั้งค่าปุ่มรับพาแลท

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allKanban = new ArrayList<>();

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
    }

    private void setupStatusTabs() {
        // เพิ่ม tabs ตามสถานะของ Pallet
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Ready"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Preparing"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Success"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Loading"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Ship"));
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Cancel"));

        statusTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterKanbansByStatus(tab.getPosition());
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
            openKanbanFragment();
        });
    }

    // เพิ่มเมธอดสำหรับเปิด Fragment รับพาแลท
    private void openKanbanFragment() {
        // สร้าง Fragment ใหม่ (คุณอาจต้องสร้าง ReceivePalletFragment ด้วย)
        KanbanPreparingFragment receivePalletFragment = new KanbanPreparingFragment();

        // เปิด Fragment ใหม่
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, receivePalletFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void filterKanbansByStatus(int tabPosition) {
        if (allKanban == null || allKanban.isEmpty()) {
            return;
        }

        List<Kanban> filteredList = new ArrayList<>();

        // กรองตามสถานะที่เลือก
        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allKanban);
                break;
            case 1: // Ready (0)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == 1) {
                        filteredList.add(kanban);
                    }
                }
                break;
            case 2: // Preparing (1)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == 2) {
                        filteredList.add(kanban);
                    }
                }
                break;
            case 3: // Success (2)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == 3) {
                        filteredList.add(kanban);
                    }
                }
                break;
            case 4: // Loading (3)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == 4) {
                        filteredList.add(kanban);
                    }
                }
                break;
            case 5: // Shipp (3)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == 5) {
                        filteredList.add(kanban);
                    }
                }
                break;
            case 6: // Cancel (3)
                for (Kanban kanban : allKanban) {
                    if (kanban.getStatus() == -1) {
                        filteredList.add(kanban);
                    }
                }
                break;
        }

        // อัพเดทรายการที่แสดงใน ListView
        adapter.setKanbans(filteredList);

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
        adapter = new KanbanAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Kanban kanban = adapter.getItem(position);
            openKanbanPreparing(String.valueOf(kanban.getId()), String.valueOf(kanban.getKanbanNo()));
        });
    }

    private void openKanbanPreparing(String kanbanId, String kanbanNo) {

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

        KanbanPreparingFragment detailFragment = new KanbanPreparingFragment();
        Bundle args = new Bundle();
        args.putString("kanbanId", kanbanId);
        args.putString("kanbanNo", kanbanNo);
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadKanbans() {
        showLoading(true);
        apiService.getKanbans().enqueue(new Callback<List<Kanban>>() {
            @Override
            public void onResponse(Call<List<Kanban>> call, Response<List<Kanban>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allKanban = response.body();

                    // กรองตามแท็บที่เลือกอยู่
                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterKanbansByStatus(selectedTabPosition);

                    if (allKanban.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading Kanban: " + response.code());
                    showError("Failed to load Kanban");
                }
            }

            @Override
            public void onFailure(Call<List<Kanban>> call, Throwable t) {
                Log.e(TAG, "Network error loading Kanban", t);
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
        loadKanbans();
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadKanbans();
    }
}