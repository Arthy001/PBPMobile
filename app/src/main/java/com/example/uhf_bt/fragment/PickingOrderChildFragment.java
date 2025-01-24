package com.example.uhf_bt.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.PickingOrderChildAdapter;
import com.example.uhf_bt.models.PickingOrderItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingOrderChildFragment extends Fragment {
    private static final String TAG = "PickingOrderChildFrag";

    private TextView tvOrderNo;
    private TextView tvCustomer;
    private TextView tvOrderDate;
    private ListView listView;
    private Button btnStart;
    private Button btnComplete;
    private ProgressBar progressBar;
    private View emptyView;
    private PickingOrderChildAdapter adapter;
    private List<PickingOrderItem> itemList = new ArrayList<>();
    private String pickingOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    public static PickingOrderChildFragment newInstance(String pickingOrderId) {
        PickingOrderChildFragment fragment = new PickingOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("PickingOrderId", pickingOrderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pickingOrderId = getArguments().getString("PickingOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking_order_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        loadPickingOrderItems();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        tvOrderNo = view.findViewById(R.id.tvOrderNo);
        tvCustomer = view.findViewById(R.id.tvCustomer);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        listView = view.findViewById(R.id.listView);
        btnStart = view.findViewById(R.id.btnStart);
        btnComplete = view.findViewById(R.id.btnComplete);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        btnStart.setOnClickListener(v -> handleStart());
        btnComplete.setOnClickListener(v -> handleComplete());
    }

    private void setupToolbar() {
//        if (getActivity() instanceof AppCompatActivity) {
//            AppCompatActivity activity = (AppCompatActivity) getActivity();
//            activity.setSupportActionBar(toolbar);
//            if (activity.getSupportActionBar() != null) {
//                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
//                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
//            }
//        }

        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
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
        adapter = new PickingOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrderItem item = adapter.getItem(position);
            handleItemClick(item);
        });
    }

    private void loadPickingOrderItems() {
        showLoading(true);

        apiService.getPickingOrderItems(pickingOrderId).enqueue(new Callback<List<PickingOrderItem>>() {
            @Override
            public void onResponse(Call<List<PickingOrderItem>> call, Response<List<PickingOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrderItem> items = response.body();
                    updateUI(items);
//                    if (items.isEmpty()) {
//                        showEmptyState();
//                    }
                } else {
                    Log.e(TAG, "Error loading picking order items: " + response.code());
                    showError("Failed to load picking order items");
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading picking order items", t);
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

    private void updateUI(List<PickingOrderItem> items) {
        if (!items.isEmpty()) {
            PickingOrderItem firstItem = items.get(0);
            tvOrderNo.setText("Order No: " + firstItem.getPickingOrder());
            tvCustomer.setText("Customer: " + firstItem.getCustomer());
            tvOrderDate.setText("Order Date: " + firstItem.getOrderDate());
        }

        adapter.updateItems(items);

        // Enable/disable buttons based on total amount
        double totalAmount = 0;
        for (PickingOrderItem item : items) {
            totalAmount += item.getTotal();
        }
        btnComplete.setEnabled(totalAmount > 0);
        btnStart.setEnabled(totalAmount > 0);
    }

    private void handleItemClick(PickingOrderItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getProduct())
                .setMessage(String.format("Quantity: %d\nUnit Price: ฿%.2f\nTotal: ฿%.2f",
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotal()))
                .setPositiveButton("Scan", (dialog, which) -> {
                    startScanningForItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startScanningForItem(PickingOrderItem item) {
        // TODO: Implement scanning functionality
        Toast.makeText(requireContext(), "Scanning not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void handleStart() {
        btnStart.setEnabled(false);
        btnComplete.setEnabled(true);
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
        loadPickingOrderItems();
    }
}