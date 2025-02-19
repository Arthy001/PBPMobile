package com.example.uhf_bt.fragment;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.ProductItem;
import com.example.uhf_bt.services.ApiService;
import com.example.uhf_bt.services.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindProductFragment extends Fragment {
    private static final String TAG = "FindProductFragment";

    private TextInputEditText etScanTag;
    private CardView cardProductDetails;
    private TextView tvProductName, tvProductNumber, tvLotNumber, tvStock, tvWarehouse;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_product, container, false);
        initViews(view);
        setupScanListener();
        return view;
    }

    private void initViews(View view) {
        etScanTag = view.findViewById(R.id.etScanTag);
        cardProductDetails = view.findViewById(R.id.cardProductDetails);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductNumber = view.findViewById(R.id.tvProductNumber);
        tvLotNumber = view.findViewById(R.id.tvLotNumber);
        tvStock = view.findViewById(R.id.tvStock);
        tvWarehouse = view.findViewById(R.id.tvWarehouse);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void setupScanListener() {
        etScanTag.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String tagId = etScanTag.getText().toString().trim();
                if (!tagId.isEmpty()) {
                    findProductByTag(tagId);
                }
                return true;
            }
            return false;
        });
    }

    private void findProductByTag(String tagId) {
        showLoading(true);

        apiService.getProductItemByTag(tagId).enqueue(new Callback<ProductItem>() {
            @Override
            public void onResponse(Call<ProductItem> call, Response<ProductItem> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ProductItem product = response.body();
                    displayProductDetails(product);
                } else {
                    showError("Product not found");
                    cardProductDetails.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ProductItem> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                cardProductDetails.setVisibility(View.GONE);
            }
        });
    }

    private void displayProductDetails(ProductItem product) {
        cardProductDetails.setVisibility(View.VISIBLE);
        tvProductName.setText("Product: " + product.getProductName());
        tvProductNumber.setText("Product Number: " + product.getProductNumber());
        tvLotNumber.setText("Lot Number: " + product.getLotNumber());
        tvStock.setText("Stock: " + product.getStock());
        tvWarehouse.setText("Warehouse: " + product.getWarehouseName());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        etScanTag.setEnabled(!show);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}