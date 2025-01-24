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
import com.example.uhf_bt.adapter.MoveOrderChildAdapter;
import com.example.uhf_bt.models.MoveOrderItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveOrderChildFragment extends Fragment {
    private static final String TAG = "MoveOrderChildFragment";

    private TextView tvOrderNo;
    private TextView tvSourceWarehouse;
    private TextView tvDestinationWarehouse;
    private ListView listView;
    private Button btnStart;
    private Button btnComplete;
    private ProgressBar progressBar;
    private View emptyView;
    private MoveOrderChildAdapter adapter;
    private List<MoveOrderItem> itemList = new ArrayList<>();
    private String moveOrderId;
    private ApiService apiService;

    // Toolbar related views
    private Toolbar toolbar;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    public static MoveOrderChildFragment newInstance(String moveOrderId) {
        MoveOrderChildFragment fragment = new MoveOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("MoveOrderId", moveOrderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            moveOrderId = getArguments().getString("MoveOrderId");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_order_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        loadMoveOrderItems();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);

        tvOrderNo = view.findViewById(R.id.tvOrderNo);
        tvSourceWarehouse = view.findViewById(R.id.tvSourceWarehouse);
        tvDestinationWarehouse = view.findViewById(R.id.tvDestinationWarehouse);
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
        adapter = new MoveOrderChildAdapter(requireContext(), itemList);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MoveOrderItem item = adapter.getItem(position);
            handleItemClick(item);
        });
    }

    private void loadMoveOrderItems() {
        showLoading(true);

        apiService.getMoveOrderItems(moveOrderId).enqueue(new Callback<List<MoveOrderItem>>() {
            @Override
            public void onResponse(Call<List<MoveOrderItem>> call, Response<List<MoveOrderItem>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<MoveOrderItem> items = response.body();
                    updateUI(items);
                    // Show empty state if no data
                    if (items.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading move order items: " + response.code());
                    showError("Failed to load move order items");
                }
            }

            @Override
            public void onFailure(Call<List<MoveOrderItem>> call, Throwable t) {
                Log.e(TAG, "Network error loading move order items", t);
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

    private void updateUI(List<MoveOrderItem> items) {
        if (!items.isEmpty()) {
            MoveOrderItem firstItem = items.get(0);
            tvOrderNo.setText("Order No: " + firstItem.getMoveOrder());
            tvSourceWarehouse.setText("From: " + firstItem.getWarehouseSourceName());
            tvDestinationWarehouse.setText("To: " + firstItem.getWarehouseDestinationName());
        }

        adapter.updateItems(items);

        // Enable/disable buttons based on status
        boolean hasUnfinishedItems = false;
        for (MoveOrderItem item : items) {
            if (item.getMoveTransStatus() != 1) {
                hasUnfinishedItems = true;
                break;
            }
        }
        btnComplete.setEnabled(!hasUnfinishedItems);
        btnStart.setEnabled(hasUnfinishedItems);
    }

    private void handleItemClick(MoveOrderItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getProduct())
                .setMessage("Lot: " + item.getLotNumber() + "\n" +
                        "Quantity: " + item.getQuantity())
                .setPositiveButton("Scan", (dialog, which) -> {
                    startScanningForItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startScanningForItem(MoveOrderItem item) {
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
        loadMoveOrderItems();
    }
}