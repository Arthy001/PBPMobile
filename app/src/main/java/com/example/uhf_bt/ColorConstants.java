package com.example.uhf_bt;

import android.graphics.Color;

public class ColorConstants {
    // Background Colors for List Items
    public static final int BG_SUCCESS = Color.rgb(144, 238, 144);  // Light green
    public static final int BG_SCAN = Color.rgb(173, 216, 230);     // Light blue
    public static final int BG_PARTIAL = Color.rgb(255, 255, 224);  // Light yellow
    public static final int BG_DEFAULT = Color.WHITE;               // Default white

    // Text Colors (if needed)
    public static final int TEXT_PRIMARY = Color.BLACK;
    public static final int TEXT_SECONDARY = Color.rgb(86,86,86);

    // Status Colors (if needed)
    public static final int STATUS_SUCCESS = Color.rgb(46, 125, 50);    // Dark green
    public static final int STATUS_WARNING = Color.rgb(237, 108, 2);    // Orange
    public static final int STATUS_ERROR = Color.rgb(211, 47, 47);      // Red

    // Utility method to get background color based on conditions
    public static int getMoveOrderItemBackground(double quantity, double success, double scan) {
        if (success == quantity) {
            return BG_SUCCESS;
        } else if (scan == quantity) {
            return BG_SCAN;
        } else if (scan > 0 || success > 0) {
            return BG_PARTIAL;
        }
        return BG_DEFAULT;
    }
}
