package com.exoplayerreactnativejava;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class PlayerModule extends ReactContextBaseJavaModule {

    PlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "PlayerExample";
    }

    @ReactMethod
    void startPlayer() {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, PlayerActivity.class);
        context.startActivity(intent);
    }
}
