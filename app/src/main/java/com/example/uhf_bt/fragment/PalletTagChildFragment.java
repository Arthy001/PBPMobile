package com.example.uhf_bt.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.R;
import com.example.uhf_bt.adapter.PalletTagChildAdapter;
import com.example.uhf_bt.models.PalletTag;
import com.example.uhf_bt.models.PalletTransaction;
import com.example.uhf_bt.models.PickingOrderTransaction;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PalletTagChildFragment extends Fragment {
    private static final String TAG = "PalletTagChildFragment";

    private TextView tvPalletNo;
    private TextView tvTotalQuantity;
    private TextView tvPickingOrder;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PalletTagChildAdapter adapter;
    private ImageView ivBack;
    private TextView toolbar_title;
    private SearchView searchView;

    private String palletTagId;
    private String pickingOrder;
    private ApiService apiService;

    public static PalletTagChildFragment newInstance(String palletTagId, String pickingOrder) {
        PalletTagChildFragment fragment = new PalletTagChildFragment();
        Bundle args = new Bundle();
        args.putString("PalletTagId", palletTagId);
        args.putString("PickingOrder", pickingOrder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            palletTagId = getArguments().getString("PalletTagId");
            pickingOrder = getArguments().getString("PickingOrder");
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pallet_tag_child, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        setupListView();
        loadPalletTransactions();
        return view;
    }

    private void initViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);

        tvPalletNo = view.findViewById(R.id.tvPalletNo);
        tvTotalQuantity = view.findViewById(R.id.tvTotalQuantity);
        tvPickingOrder = view.findViewById(R.id.tvPickingOrder);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        toolbar_title.setText("Pallet Details");
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
        }

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(Color.WHITE);
        }

        searchView.setQueryHint("Search transaction...");
    }

    private void setupListView() {
        adapter = new PalletTagChildAdapter(requireContext());
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
    }

    private void loadPalletTransactions() {
        showLoading(true);

        apiService.getTransactionsByPalletTagId(palletTagId).enqueue(new Callback<List<PalletTransaction>>() {
            @Override
            public void onResponse(Call<List<PalletTransaction>> call, Response<List<PalletTransaction>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PalletTransaction> transactions = response.body();
                    updateUI(transactions);
                    if (transactions.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading pallet transactions: " + response.code());
                    showError("Failed to load pallet transactions");
                }
            }

            @Override
            public void onFailure(Call<List<PalletTransaction>> call, Throwable t) {
                Log.e(TAG, "Network error loading pallet transactions", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI(List<PalletTransaction> transactions) {
        if (!transactions.isEmpty()) {
            PalletTransaction firstItem = transactions.get(0);
            tvPalletNo.setText("Pallet: " + palletTagId);
            tvPickingOrder.setText("Picking Order: " +pickingOrder);

            int totalQuantity = 0;
            for (PalletTransaction transaction : transactions) {
                totalQuantity += transaction.getQuantity();
            }
            tvTotalQuantity.setText("Total Quantity: " + totalQuantity);
        }

        adapter.setTransactions(transactions);
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

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadPalletTransactions();
    }
}