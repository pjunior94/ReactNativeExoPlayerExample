package com.exoplayerreactnativejava;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.ArrayList;

public class PlayerModule extends ReactContextBaseJavaModule {

    PlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private static final String TAG = PlayerModule.class.getSimpleName();
    // URL to the catalog API
    private static final String API_CATALOG = "https://drm-quick-start.azurewebsites.net/api/catalog/videos";
    // URL to the authorization service API
    private static final String API_AUTH = "https://drm-quick-start.azurewebsites.net/api/authorization/";

    // hardcoded license server URL
    public static final String WIDEVINE_LICENSE_SERVER = "https://drm-widevine-licensing.axtest.net/AcquireLicense";

    @Override
    public String getName() {
        return "PlayerExample";
    }

    private String mLicenseToken;
    public static RequestQueue requestQueue;
    private ArrayList<String> mVideoNames = new ArrayList<>();

    @ReactMethod
    void startPlayer() {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, PlayerActivity.class);
        context.startActivity(intent);
    }

    private void makeAuthorizationRequest(final int position) {
        Request request = new StringRequest(Request.Method.GET,
                API_AUTH + android.net.Uri.encode(mVideoNames.get(position)),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // We got a license token! We are all set to start playback.
                        // We just pass it on to the player activity started in startVideoActivity
                        // method and have it take care of the rest.
                        mLicenseToken = response.substring(1,response.length()-1);
                        startPlayer(position);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "License token was not loaded with error: " + error.getMessage());
            }
        });
        requestQueue.add(request);
    }
}
