package com.exoplayerreactnativejava;

import android.os.Looper;

import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;

public class CustomDrmSessionManager implements DrmSessionManager {
    @Override
    public boolean canAcquireSession(DrmInitData drmInitData) {
        return false;
    }

    @Override
    public DrmSession acquireSession(Looper playbackLooper, DrmInitData drmInitData) {
        return null;
    }

    @Override
    public void releaseSession(DrmSession drmSession) {

    }
}
