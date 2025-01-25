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
import com.example.uhf_bt.adapter.MoveOrderAdapter;
import com.example.uhf_bt.adapter.PickingOrderAdapter;
import com.example.uhf_bt.models.MoveOrder;
import com.example.uhf_bt.models.PickingOrder;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PickingOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PickingOrderFragment extends Fragment {
    private static final String TAG = "PickingOrderFragment";
    private ImageButton btnBack;
    private TextView toolbar_title;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;
    private View emptyView;
    private PickingOrderAdapter adapter;
    private ApiService apiService;
    // TODO: Rename and change types and number of parameters
    public static PickingOrderFragment newInstance(String param1, String param2) {
        PickingOrderFragment fragment = new PickingOrderFragment();
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
        View view = inflater.inflate(R.layout.fragment_picking_order, container, false);

        initViews(view);
        setupBackButton();
        setupSearchView();
        setupListView();

        apiService = RetrofitClient.getClient().create(ApiService.class);

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        searchView = view.findViewById(R.id.searchView);
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
        adapter = new PickingOrderAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PickingOrder pickingOrder = adapter.getItem(position);
            openPickingOrderChild(String.valueOf(pickingOrder.getId()));
        });
    }

    private void openPickingOrderChild(String pickingOrderId) {
        PickingOrderChildFragment detailFragment = new PickingOrderChildFragment();
        Bundle args = new Bundle();
        args.putString("PickingOrderId", pickingOrderId);
        detailFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

    private void loadPickingOrders() {
        showLoading(true);
        apiService.getPickingOrders().enqueue(new Callback<List<PickingOrder>>() {
            @Override
            public void onResponse(Call<List<PickingOrder>> call, Response<List<PickingOrder>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PickingOrder> pickingOrders = response.body();
                    adapter.setPickingOrders(pickingOrders);
                    if (pickingOrders.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Error loading move orders: " + response.code());
                    showError("Failed to load move orders");
                }
            }

            @Override
            public void onFailure(Call<List<PickingOrder>> call, Throwable t) {
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
        loadPickingOrders();
    }

    public void refreshData() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        loadPickingOrders();
    }


}