package com.exoplayerreactnativejava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.security.Key;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class PlayerActivity extends AppCompatActivity {

    // bandwidth meter to measure and estimate bandwidth
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String CommunicationKeyId = "3DB51E27-1E9D-4FB9-B515-A9DD00B77A14";

    protected static String LicenseToken = "";

//    protected static final String LicenseToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
//            "eyJ2ZXJzaW9uIjoxLCJjb21fa2V5X2lkIjoiNjllNTQwODgtZTllMC00NTMwLThjMWEtMWViNmRj" +
//            "ZDBkMTRlIiwibWVzc2FnZSI6eyJ0eXBlIjoiZW50aXRsZW1lbnRfbWVzc2FnZSIsInZlcnNpb24iOj" +
//            "IsImNvbnRlbnRfa2V5c19zb3VyY2UiOnsiaW5saW5lIjpbeyJpZCI6IjZlNWExZDI2LTI3NTctNDdkNy04MD" +
//            "Q2LWVhYTVkMWQzNGI1YSIsInVzYWdlX3BvbGljeSI6IlBvbGljeSBBIn1dfSwiY29udGVudF9rZXlfdXNhZ2Vf" +
//            "cG9saWNpZXMiOlt7Im5hbWUiOiJQb2xpY3kgQSIsInBsYXlyZWFkeSI6eyJtaW5fZGV2aWNlX3NlY3VyaXR5X2" +
//            "xldmVsIjoxNTAsInBsYXlfZW5hYmxlcnMiOlsiNzg2NjI3RDgtQzJBNi00NEJFLThGODgtMDhBRTI1NUIwMUE" +
//            "3Il19LCJ3aWRldmluZSI6e319XX19.1ie6MpTxLn8fNz29ERynMaMOnuRI2sSAxLhBysLybac";

    private FrameworkMediaDrm mediaDrm;
    protected String userAgent;
    private SimpleExoPlayer player;
    private PlayerView playerView;
    private DataSource.Factory dataSourceFactory;
    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        playerView = findViewById(R.id.video_view);
        dataSourceFactory = buildDataSourceFactory();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

    private String GenerateToken(){
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String entitlement_message = "{\"version\": 1,\"com_key_id\": \"3DB51E27-1E9D-4FB9-B515-A9DD00B77A14\",\"message\": {\"type\": \"entitlement_message\",\"version\": 2,\"content_keys_source\": {\"inline\": [{\"id\": \"d9dbd0cd-de99-4e91-b3c3-1f7cbe640096\",\"usage_policy\": \"Policy A\"}]},\"content_key_usage_policies\": [{\"name\": \"Policy A\",\"playready\": {\"min_device_security_level\": 150,\"play_enablers\": [\"786627D8-C2A6-44BE-8F88-08AE255B01A7\"]},\"widevine\": {}}]}}";

        LicenseToken = Jwts.builder().setPayload(entitlement_message).signWith(key).compact();

        return LicenseToken;
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
            UUID uuid, String licenseUrl,
            String[] keyRequestPropertiesArray,
            boolean multiSession) throws UnsupportedDrmException {
        HttpDataSource.Factory licenseDataSourceFactory = buildHttpDataSourceFactory();
        HttpMediaDrmCallback drmCallback =
                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }

        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
        return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
    }

    public DataSource.Factory buildDataSourceFactory() {
        return new DefaultDataSourceFactory(this, buildHttpDataSourceFactory());
    }


    private void initializePlayer() {
        Intent intent = getIntent();
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
        UUID drmSchemeUuid = Util.getDrmUuid("widevine");
        String[] keyRequestPropertiesArray = new String[]{"X-AxDRM-Message", GenerateToken()};

        try {
            drmSessionManager =
                    buildDrmSessionManagerV18(
                            drmSchemeUuid, "https://drm-widevine-licensing.axtest.net/AcquireLicense", keyRequestPropertiesArray, false);
        }
        catch (UnsupportedDrmException ex) {
            System.out.println("Caught in main.");
//            throw ex;
        }

//        Uri uri = Uri.parse("https://media.axprod.net/TestVectors/v6-MultiDRM/Manifest_1080p.mpd");
        Uri uri = Uri.parse("http://192.168.1.177/manifest.mpd");
        MediaSource source = buildMediaSource(uri);

        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), drmSessionManager);
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        player.prepare(source, true, false);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
