package com.erikeuserr;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
    private int mCameraFacing = CameraSource.CAMERA_FACING_BACK;

    public static CordovaInterface cordova;
    private static boolean sTorchState = false;
    private boolean detectOne = false;
    private boolean takePhoto = false;

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
        Camera camera = getCameraObject(cameraSource);
        if (camera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(sTorchState ? "torch" : "off");
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Terrible hack as the Vision CameraSource no longer supports flash access
    private Camera getCameraObject(CameraSource cameraSource) {
        Field[] cFields = cameraSource.getClass().getDeclaredFields();
        Camera _cam = null;
        try {
            for (int i = 0; i < cFields.length; i++) {
                Field item = cFields[i];
                if (item.getType().getName().equals("android.hardware.Camera")) {
                    item.setAccessible(true);
                    try {
                        _cam = (Camera) item.get(cameraSource);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _cam;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getAppResource("activity_main", "layout"));

        final Intent intent = getIntent();
        final String regexPattern = intent.getStringExtra("regexPattern");

        detectOne = intent.getBooleanExtra("detectOne", false);
        takePhoto = intent.getBooleanExtra("takePhoto", false);

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
                if (!filteredTextBlocks.isEmpty()) {
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
                    if (!foundText.isEmpty() && !isTaskRunning) {
                        sendDetectionsToWebView();
                        isTaskRunning = true;
                    }
                }
            }
        });

        final Activity thisActivity = this;
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
            Toast.makeText(getApplicationContext(), "Detector dependencies are not yet available", Toast.LENGTH_LONG).show();
        } else {
            cameraSource = new CameraSource.Builder(this, textRecognizer)
                    .setFacing(mCameraFacing)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(4.0f)
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
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }

                        Camera camera = getCameraObject(cameraSource);
                        setCameraDisplayOrientation(thisActivity, camera);

                        cameraSource.stop();
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera camera = getCameraObject(cameraSource);

//        if (isPreviewRunning) {
//            camera.stopPreview();
//        }

        setCameraDisplayOrientation(this, camera);

        //previewCamera();
    }

    public void setCameraDisplayOrientation(Activity activity, Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraFacing, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void sendDetectionsToWebView() {
        if(takePhoto) {
            cameraSource.takePicture(() -> {

            }, bytes -> {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        if(detectOne) {
                            if(foundText.size() > 0) {
                                intent.putExtra("detections", foundText.get(0));
                            }
                            else {
                                intent.putExtra("detections", "");
                            }
                        }
                        else {
                            intent.putStringArrayListExtra("detections", foundText);
                        }
                        
                        if(bytes != null && bytes.length > 0) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Bitmap saveBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            saveBitmap = Bitmap.createScaledBitmap(saveBitmap, 1080, 810, false);
                            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

                            String encodedBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                            intent.putExtra("photo", encodedBase64);
                        }
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, 2000);
            });
        }
        else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    if(detectOne) {
                        if(foundText.size() > 0) {
                            intent.putExtra("detections", foundText.get(0));
                        }
                        else {
                            intent.putExtra("detections", "");
                        }
                    }
                    else {
                        intent.putStringArrayListExtra("detections", foundText);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }, 2000);
        }
    }

    private ArrayList<String> filterItems(SparseArray<TextBlock> items){
        ArrayList<String> filteredItems = new ArrayList<String>();
        if(items.size() == 0) {
            return filteredItems;
        }

        int count = 0;
        for(int i = 0; i < items.size(); i++) {
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
            for(int i = 0; i < items.size(); i++) {
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
