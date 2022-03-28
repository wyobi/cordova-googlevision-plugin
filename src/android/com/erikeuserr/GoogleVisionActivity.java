package com.erikeuserr;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;

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
    private static CameraSource cameraSource;
    private final int RequestCameraPermissionID = 1001;
    private Pattern pattern;
    private final NoDuplicatesList<String> foundText = new NoDuplicatesList<String>();
    private boolean isTaskRunning;

    public static CordovaInterface cordova;
    private static boolean sTorchState = false;

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

    private int getAppResource(String name, String type) {
        return this.getResources().getIdentifier(name, type, this.getPackageName());
    }

    public void toggleTorch(View view) {
        sTorchState = !sTorchState;

        if (sTorchState) {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        else {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getAppResource("activity_main", "layout"));

        final Intent intent = getIntent();
        final String regexPattern = intent.getStringExtra("regexPattern");

        System.out.println("regex pattern " + regexPattern);
        pattern = Pattern.compile(regexPattern);

        cameraView = (SurfaceView) findViewById(getAppResource("surface_view", "id"));
        textView = (TextView) findViewById(getAppResource("text_view", "id"));

        final Activity activity = this;

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, resultCode, 1000).show();
            return;
        }

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext())
                .build();
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                final ArrayList<String> filteredTextBlocks = filterItems(items);
                if(!filteredTextBlocks.isEmpty()){
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            final String displayValue = filteredTextBlocks.get(0);
                            stringBuilder.append(displayValue + "\n");
                            textView.setText(stringBuilder.toString());
                        }
                    });

                    foundText.addAll(filteredTextBlocks);
                    if(!foundText.isEmpty() && !isTaskRunning){
                        sendDetectionsToWebView();
                        isTaskRunning = true;
                    }
                }
            }
        });

        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
            Toast.makeText(getApplicationContext(), "Detector dependencies are not yet available", Toast.LENGTH_LONG).show();
        } else {
            cameraSource = new CameraSource.Builder(this, textRecognizer)
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
                                    new String[] { Manifest.permission.CAMERA },
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
        }
    }

    private void sendDetectionsToWebView(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("detections", foundText);
                setResult(RESULT_OK, intent);
                finish();
            }
        }, 2000);
    }

    private ArrayList<String> filterItems(SparseArray<TextBlock> items){
        ArrayList<String> filteredItems = new ArrayList<String>();
        if(items.size() == 0) {
            return filteredItems;
        }

        int count = 0;
        for(int i =0; i<items.size(); i++) {
            TextBlock item = items.valueAt(i);
            if(item != null) {
                if(pattern != null && !pattern.pattern().equalsIgnoreCase("null")) {
                    Matcher matcher  = pattern.matcher(item.getValue());
                    while (matcher.find()) {
                        filteredItems.add(matcher.group());
                    }
                }
                else {
                    filteredItems.add(item.getValue());
                    count++;
                }
            }
        }

        if(count == 0 && pattern != null) {
            StringBuilder sb = new StringBuilder();
            for(int i =0; i<items.size(); i++) {
                TextBlock item = items.valueAt(i);
                sb.append(item.getValue());
                if(i < items.size() - 1) {
                    sb.append('\n');
                }
            }

            String str = sb.toString();
            if(str.indexOf('<') > 0) { //Issues with MRZ TODO upgrade OCR
                str = str.replace(" <", "<");
            }

            Matcher m = pattern.matcher(str);
            while (m.find()) {
                filteredItems.add(m.group());
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
