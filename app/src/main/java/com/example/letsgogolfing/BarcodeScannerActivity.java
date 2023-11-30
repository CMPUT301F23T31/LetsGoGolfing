package com.example.letsgogolfing;

import android.util.Log;

public class BarcodeScannerActivity {

    private String barcode_string;


    public void setBarcode_string(String barcode_string) {
        this.barcode_string = barcode_string;
    }

    public String getBarcode_string() {
        return barcode_string;
    }

    // print barcode_string to logcat
    public void printBarcodeString() {
        Log.d("BarcodeScannerActivity", barcode_string);
    }

}
