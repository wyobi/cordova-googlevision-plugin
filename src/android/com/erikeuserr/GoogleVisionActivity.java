package com.erikeuserr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.erikeuserr.testapp.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleVisionActivity extends Activity {
    private SurfaceView cameraView;
    private TextView textView;
    private CameraSource cameraSource;
    private final int RequestCameraPermissionID = 1001;
    private Pattern pattern;
    private final NoDuplicatesList<String> foundBarcodesAndText = new NoDuplicatesList<String>();
    private boolean isTaskRunning;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        final String regexPattern = intent.getStringExtra("regexPattern");

        System.out.println("regex pattern " + regexPattern);
        pattern = Pattern.compile(regexPattern);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);

        final Activity activity = this;

        BarcodeTextDetector barcodeTextRecognizer = new BarcodeTextDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.EAN_13)
                .build();

        if (!barcodeTextRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeTextRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            barcodeTextRecognizer.setProcessor(new Detector.Processor<BarcodeText>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<BarcodeText> detections) {
                    final SparseArray<BarcodeText> items = detections.getDetectedItems();

                    if(items.get(1) != null){
                        final BarcodeText foundItems = items.get(1);

                        // Barcodes.
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i =0; i < foundItems.getBarcodes().size(); i++) {
                                    final String displayValue = foundItems.getBarcodes().valueAt(i).displayValue;
                                    stringBuilder.append(displayValue + "\n");
                                    foundBarcodesAndText.add(displayValue);
                                }

                                textView.setText(stringBuilder.toString());
                            }
                        });

                        // Text recognition.
                        final ArrayList<String> filteredTextBlocks = filterItems(foundItems.getTextBlocks());

                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i =0; i < filteredTextBlocks.size(); i++) {
                                    stringBuilder.append(filteredTextBlocks.get(i) + "\n");
                                }

                                textView.setText(stringBuilder.toString());
                            }
                        });

                        foundBarcodesAndText.addAll(filteredTextBlocks);

                        if(!foundBarcodesAndText.isEmpty() && !isTaskRunning){
                            sendDetectionsToWebView();
                            isTaskRunning = true;
                        }
                    }
                }
            });
        }
    }

    private void sendDetectionsToWebView(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("detections", foundBarcodesAndText);
                setResult(RESULT_OK, intent);
                finish();
            }
        }, 2000);
    }

    private ArrayList<String> filterItems(SparseArray<TextBlock> items){
        ArrayList<String> filteredItems = new ArrayList<String>();

        for(int i =0; i<items.size(); i++)
        {
            TextBlock item = items.valueAt(i);
            Matcher m  = pattern.matcher(item.getValue());

            if(m.find()){
                for(int j = 0; j < m.groupCount(); j++){
                    filteredItems.add(m.group(j));
                }
            }
        }

        return filteredItems;
    }
}

class NoDuplicatesList<E> extends ArrayList<E> {
    @Override
    public boolean add(E e) {
        if (this.contains(e)) {
            return false;
        }
        else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<E>(collection);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<E>(collection);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void add(int index, E element) {
        if (this.contains(element)) {
            return;
        }
        else {
            super.add(index, element);
        }
    }
}