package com.example.uhf_bt.fragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import com.example.uhf_bt.adapter.CheckStockAdapter;
import com.example.uhf_bt.models.CheckStock;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckStockFragment extends Fragment {

    private static final String TAG = "CheckStockFragment";

    private ImageButton btnBack;
    private TextView toolbar_title;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private ProgressBar progressBar;
    private TextView datePickerTextView;
    private FloatingActionButton btnCreateCheckStock;

    private CheckStockAdapter adapter;
    private ApiService apiService;
    private TabLayout statusTabLayout;
    private List<CheckStock> allCheckStocks;
    
    private SearchView searchView;
    private ListView listView;

    String currentDate;

    public static CheckStockFragment newInstance() {
        return new CheckStockFragment();
    }

    public CheckStockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pastel1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_stock, container, false);

        initViews(view);
        setupBackButton();
        setupStatusTabs();
        setupSearchView();
        setupListView();

        apiService = RetrofitClient.getClient().create(ApiService.class);
        allCheckStocks = new ArrayList<>();

        if (currentDate == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            currentDate = dateFormat.format(calendar.getTime());
        }

        datePickerTextView.setText(currentDate);

        return view;
    }

    private void initViews(View view) {
        searchView = view.findViewById(R.id.searchView);
        btnBack = view.findViewById(R.id.btnBack);
        
        btnCreateCheckStock = view.findViewById(R.id.btnCreateCheckStock);
        btnCreateCheckStock.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new CreateCheckStockFragment())
                    .addToBackStack(null)
                    .commit();
        });

        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        statusTabLayout = view.findViewById(R.id.statusTabLayout);
        datePickerTextView = view.findViewById(R.id.datePickerTextView);
    }

    private void setupStatusTabs() {
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
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterOrdersByStatus(int tabPosition) {
        if (allCheckStocks == null) {
            allCheckStocks = new ArrayList<>();
        }

        List<CheckStock> filteredList = new ArrayList<>();

        switch (tabPosition) {
            case 0: // All
                filteredList = new ArrayList<>(allCheckStocks);
                break;
            case 1: // New
                for (CheckStock order : allCheckStocks) {
                    if ("New".equalsIgnoreCase(order.getStatus())) {
                        filteredList.add(order);
                    }
                }
                break;
            case 2: // In Progress
                for (CheckStock order : allCheckStocks) {
                    if ("InProgress".equalsIgnoreCase(order.getStatus()) || "In Progress".equalsIgnoreCase(order.getStatus())) {
                        filteredList.add(order);
                    }
                }
                break;
            case 3: // Success/Completed
                for (CheckStock order : allCheckStocks) {
                    if ("Completed".equalsIgnoreCase(order.getStatus()) || "Success".equalsIgnoreCase(order.getStatus())) {
                        filteredList.add(order);
                    }
                }
                break;
            case 4: // Cancel
                for (CheckStock order : allCheckStocks) {
                    if ("Cancel".equalsIgnoreCase(order.getStatus()) || "Cancelled".equalsIgnoreCase(order.getStatus())) {
                        filteredList.add(order);
                    }
                }
                break;
        }

        adapter.setCheckStocks(filteredList);

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
                        currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        datePickerTextView.setText(currentDate);
                        loadCheckStocks(currentDate);
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

        searchView.setQueryHint("Search stock check...");
    }

    private void setupListView() {
        adapter = new CheckStockAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckStock checkStock = adapter.getItem(position);
            openCheckStockChild(String.valueOf(checkStock.getId()));
        });
    }

    private void openCheckStockChild(String checkStockId) {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);

            btnBack.setVisibility(View.VISIBLE);
            toolbar_title.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            searchView.setLayoutParams(params);
        }

        CheckStockChildFragment detailFragment = CheckStockChildFragment.newInstance(checkStockId);
        
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadCheckStocks(String date) {
        showLoading(true);
        apiService.getCheckStocksByDate(date).enqueue(new Callback<List<CheckStock>>() {
            @Override
            public void onResponse(Call<List<CheckStock>> call, Response<List<CheckStock>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allCheckStocks = response.body();

                    int selectedTabPosition = statusTabLayout.getSelectedTabPosition();
                    filterOrdersByStatus(selectedTabPosition);

                    if (allCheckStocks.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading check stocks: " + response.code());
                    allCheckStocks = new ArrayList<>();
                    filterOrdersByStatus(statusTabLayout.getSelectedTabPosition());
                    showError("Failed to load check stocks");
                }
            }

            @Override
            public void onFailure(Call<List<CheckStock>> call, Throwable t) {
                Log.e(TAG, "Network error loading check stocks", t);
                showLoading(false);
                allCheckStocks = new ArrayList<>();
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
        loadCheckStocks(currentDate);
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadCheckStocks(currentDate);
    }
}
