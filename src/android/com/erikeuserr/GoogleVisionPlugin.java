package com.erikeuserr;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import static android.app.Activity.RESULT_OK;

public class GoogleVisionPlugin extends CordovaPlugin {
    private CallbackContext callbackContext;
    Boolean detectOne = false;

    //    1. Add to build.gradle (app) compile 'com.google.android.gms:play-services-vision:9.8.0'
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equalsIgnoreCase("detect")) {
            this.callbackContext = callbackContext;

            if(args.getString(0) != null) {
                GoogleVisionActivity.cordova = this.cordova;

                final String regexPatternString = args.getString(0);
                detectOne = args.getBoolean(1);
                final Boolean takePhoto = args.getBoolean(2);

                final Intent intent = new Intent(cordova.getActivity().getApplicationContext(), GoogleVisionActivity.class);
                intent.putExtra("regexPattern", regexPatternString);
                intent.putExtra("detectOne", detectOne);
                intent.putExtra("takePhoto", takePhoto);

                cordova.setActivityResultCallback (this);
                cordova.getActivity().startActivityForResult(intent, 1);
                
                return true;
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            JSONObject obj = new JSONObject();
            try {
                if(detectOne) {
                    obj.put("detections", intent.getStringExtra("detections"));
                }
                else {
                    obj.put("detections", new JSONArray(intent.getStringArrayListExtra("detections")));
                }

                String photo = intent.getStringExtra("photo");
                if(photo != null && photo.length() > 0) {
                    obj.put("photo", intent.getStringExtra("photo"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
        }
        else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
        }
    }
}
