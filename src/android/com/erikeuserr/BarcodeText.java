package com.erikeuserr;

import android.util.SparseArray;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeText {
    private SparseArray<Barcode> barcodes;
    private SparseArray<TextBlock> textBlocks;

    public BarcodeText(SparseArray<Barcode> barcodes, SparseArray<TextBlock> textBlocks) {
        this.barcodes = barcodes;
        this.textBlocks = textBlocks;
    }

    public SparseArray<Barcode> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(SparseArray<Barcode> barcodes) {
        this.barcodes = barcodes;
    }

    public SparseArray<TextBlock> getTextBlocks() {
        return textBlocks;
    }

    public void setTextBlocks(SparseArray<TextBlock> textBlocks) {
        this.textBlocks = textBlocks;
    }
}
