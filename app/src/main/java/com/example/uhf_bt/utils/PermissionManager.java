package com.example.uhf_bt.utils;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final String TAG = "PermissionManager";
    private static final String PREF_PERMISSIONS = "user_permissions";
    
    // Permission Constants
    public static final String PERM_MOVE_ORDER = "Permissions.Mobile.MoveOrder.View";
    public static final String PERM_PICK_ORDER = "Permissions.Mobile.PickOrder.View";
    public static final String PERM_GOODS_RECEIVE = "Permissions.Mobile.GoodsReceive.View";
    public static final String PERM_RFID_LOADING = "Permissions.Mobile.RFIDLoading.View";
    public static final String PERM_DELIVERY = "Permissions.Mobile.Delivery.View";
    public static final String PERM_FIND_PRODUCT = "Permissions.Mobile.FindProduct.View";
    public static final String PERM_STOCK_COUNT = "Permissions.Mobile.StockCount.View";
    public static final String PERM_CHECK_STOCK = "Permissions.Mobile.CheckStock.View";

    private List<String> userPermissions = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    public PermissionManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        loadPermissionsFromPreferences();
    }

    public boolean hasPermission(String permission) {
        return userPermissions != null && userPermissions.contains(permission);
    }

    public void setPermissions(List<String> permissions) {
        this.userPermissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
        savePermissionsToPreferences();
    }

    public List<String> getPermissions() {
        return new ArrayList<>(userPermissions);
    }

    public void clearPermissions() {
        userPermissions.clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_PERMISSIONS);
        editor.apply();
        Log.d(TAG, "Cleared all permissions");
    }

    private void savePermissionsToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String permissionsString = android.text.TextUtils.join(",", userPermissions);
        editor.putString(PREF_PERMISSIONS, permissionsString);
        editor.apply();
        Log.d(TAG, "Saved permissions: " + permissionsString);
    }

    private void loadPermissionsFromPreferences() {
        String permissionsString = sharedPreferences.getString(PREF_PERMISSIONS, "");
        userPermissions.clear();
        
        if (!permissionsString.isEmpty()) {
            String[] permArray = permissionsString.split(",");
            for (String perm : permArray) {
                if (!perm.trim().isEmpty()) {
                    userPermissions.add(perm.trim());
                }
            }
        }
        Log.d(TAG, "Loaded permissions: " + permissionsString);
    }

    public void applyToButton(MaterialButton button, String permission) {
        if (button != null) {
            button.setVisibility(hasPermission(permission) ? View.VISIBLE : View.GONE);
        }
    }
}
