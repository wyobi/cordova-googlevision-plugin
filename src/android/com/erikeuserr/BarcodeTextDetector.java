package com.erikeuserr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.internal.client.BarcodeDetectorOptions;
import com.google.android.gms.vision.barcode.internal.client.zzb;
import com.google.android.gms.vision.internal.client.FrameMetadataParcel;
import com.google.android.gms.vision.text.internal.client.LineBoxParcel;
import com.google.android.gms.vision.text.internal.client.RecognitionOptions;
import com.google.android.gms.vision.text.internal.client.TextRecognizerOptions;
import com.google.android.gms.vision.text.internal.client.zzg;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class BarcodeTextDetector extends Detector<BarcodeText> {

    private final zzb aNX;
    private final zzg aON;

    private BarcodeTextDetector(zzb zzb, zzg zzg){
        this.aNX = zzb;
        this.aON = zzg;
    }

    public SparseArray<BarcodeText> detect(Frame frame) {
        if(frame == null) {
            throw new IllegalArgumentException("No frame supplied.");
        } else {
            FrameMetadataParcel frameMetadataParcel = FrameMetadataParcel.zzc(frame);

            SparseArray<Barcode> barcodeSparseArray = detectBarcodes(frame, frameMetadataParcel);
            SparseArray<TextBlock> textBlockSparseArray = detectTextBlocks(frame, frameMetadataParcel);

            SparseArray<BarcodeText>  barcodesTextBlocks = new SparseArray<BarcodeText>();
            barcodesTextBlocks.append(1, new BarcodeText(barcodeSparseArray, textBlockSparseArray));

            return barcodesTextBlocks;
        }
    }

    private SparseArray<TextBlock> detectTextBlocks(Frame frame, FrameMetadataParcel frameMetadataParcel){
        RecognitionOptions recognitionOptions = new RecognitionOptions(1, new Rect());
        Bitmap var4;
        if(frame.getBitmap() != null) {
            var4 = frame.getBitmap();
        } else {
            Frame.Metadata var5 = frame.getMetadata();
            var4 = this.zza(frame.getGrayscaleImageData(), var5.getFormat(), frameMetadataParcel.width, frameMetadataParcel.height);
        }

        var4 = this.zzb(var4, frameMetadataParcel);
        if(!recognitionOptions.aPa.isEmpty()) {
            Rect var6 = this.zza(recognitionOptions.aPa, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), frameMetadataParcel);
            recognitionOptions.aPa.set(var6);
        }

        frameMetadataParcel.rotation = 0;
        return this.zza(this.aON.zza(var4, frameMetadataParcel, recognitionOptions));
    }

    private SparseArray<Barcode> detectBarcodes(Frame frame, FrameMetadataParcel frameMetadataParcel){
        Barcode[] barcodes;
        if(frame.getBitmap() != null) {
            barcodes = this.aNX.zza(frame.getBitmap(), frameMetadataParcel);
            if(barcodes == null) {
                throw new IllegalArgumentException("Internal barcode detector error; check logcat output.");
            }
        } else {
            ByteBuffer var4 = frame.getGrayscaleImageData();
            barcodes = this.aNX.zza(var4, frameMetadataParcel);
        }

        SparseArray sparseArray = new SparseArray(barcodes.length);
        Barcode[] barcodesCopy = barcodes;
        int barcodesLenght = barcodes.length;

        for(int var7 = 0; var7 < barcodesLenght; ++var7) {
            Barcode barcode = barcodesCopy[var7];
            sparseArray.append(barcode.rawValue.hashCode(), barcode);
        }

        return sparseArray;
    }

    public SparseArray<TextBlock> zza(Frame frame, RecognitionOptions recognitionOptions) {
        if(frame == null) {
            throw new IllegalArgumentException("No frame supplied.");
        } else {
            // should work with Barcode and Text
            FrameMetadataParcel frameMetadataParcel = FrameMetadataParcel.zzc(frame);

            Bitmap var4;
            if(frame.getBitmap() != null) {
                var4 = frame.getBitmap();
            } else {
                Frame.Metadata var5 = frame.getMetadata();
                var4 = this.zza(frame.getGrayscaleImageData(), var5.getFormat(), frameMetadataParcel.width, frameMetadataParcel.height);
            }

            var4 = this.zzb(var4, frameMetadataParcel);
            if(!recognitionOptions.aPa.isEmpty()) {
                Rect var6 = this.zza(recognitionOptions.aPa, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), frameMetadataParcel);
                recognitionOptions.aPa.set(var6);
            }

            frameMetadataParcel.rotation = 0;
            return this.zza(this.aON.zza(var4, frameMetadataParcel, recognitionOptions));
        }
    }

    private Bitmap zza(ByteBuffer var1, int var2, int var3, int var4) {
        byte[] var5;
        if(var1.hasArray() && var1.arrayOffset() == 0) {
            var5 = var1.array();
        } else {
            var5 = new byte[var1.capacity()];
            var1.get(var5);
        }

        ByteArrayOutputStream var6 = new ByteArrayOutputStream();
        YuvImage var7 = new YuvImage(var5, var2, var3, var4, (int[])null);
        var7.compressToJpeg(new Rect(0, 0, var3, var4), 100, var6);
        byte[] var8 = var6.toByteArray();
        return BitmapFactory.decodeByteArray(var8, 0, var8.length);
    }

    private Bitmap zzb(Bitmap var1, FrameMetadataParcel var2) {
        int var3 = var1.getWidth();
        int var4 = var1.getHeight();
        if(var2.rotation != 0) {
            Matrix var5 = new Matrix();
            var5.postRotate((float)this.zzabm(var2.rotation));
            var1 = Bitmap.createBitmap(var1, 0, 0, var3, var4, var5, false);
        }

        if(var2.rotation == 1 || var2.rotation == 3) {
            var2.width = var4;
            var2.height = var3;
        }

        return var1;
    }

    private Rect zza(Rect var1, int var2, int var3, FrameMetadataParcel var4) {
        switch(var4.rotation) {
            case 1:
                return new Rect(var3 - var1.bottom, var1.left, var3 - var1.top, var1.right);
            case 2:
                return new Rect(var2 - var1.right, var3 - var1.bottom, var2 - var1.left, var3 - var1.top);
            case 3:
                return new Rect(var1.top, var2 - var1.right, var1.bottom, var2 - var1.left);
            default:
                return var1;
        }
    }

    private SparseArray<TextBlock> zza(LineBoxParcel[] var1) {
        SparseArray var2 = new SparseArray();
        LineBoxParcel[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            LineBoxParcel var6 = var3[var5];
            SparseArray var7 = (SparseArray)var2.get(var6.aOY);
            if(var7 == null) {
                var7 = new SparseArray();
                var2.append(var6.aOY, var7);
            }

            var7.append(var6.aOZ, var6);
        }

        SparseArray var8 = new SparseArray(var2.size());

        for(var4 = 0; var4 < var2.size(); ++var4) {
            var8.append(var2.keyAt(var4), new TextBlock((SparseArray)var2.valueAt(var4)));
        }

        return var8;
    }

    private int zzabm(int var1) {
        switch(var1) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                throw new IllegalArgumentException("Unsupported rotation degree.");
        }
    }

    public static class Builder {
        private Context mContext;
        private TextRecognizerOptions textRecognizerOptions;
        private BarcodeDetectorOptions barcodeDetectorOptions;

        public Builder(Context var1) {
            this.mContext = var1;
            this.textRecognizerOptions = new TextRecognizerOptions();
            this.barcodeDetectorOptions = new BarcodeDetectorOptions();
        }

        public BarcodeTextDetector.Builder setBarcodeFormats(int mode) {
            this.barcodeDetectorOptions.aNZ = mode;
            return this;
        }

        public BarcodeTextDetector build() {
            zzg zzg = new zzg(this.mContext, this.textRecognizerOptions);
            zzb zzb = new zzb(this.mContext, this.barcodeDetectorOptions);

            return new BarcodeTextDetector(zzb, zzg);
        }
    }
}
