package com.example.uhf_bt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.R;
import com.google.android.material.button.MaterialButton;

public class CreateCheckStockFragment extends Fragment {

    private Spinner spinnerWarehouse;
    private EditText etDescription;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_check_stock, container, false);
        
        initViews(view);
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        
        spinnerWarehouse = view.findViewById(R.id.spinnerWarehouse);
        etDescription = view.findViewById(R.id.etDescription);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> {
            // TODO: Implement save logic, call backend API to create CheckStock
            Toast.makeText(getContext(), "Save clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
