package com.xrtech.xrmirroring;

import static com.xrtech.xrmirroring.SenderActivity.mTransport;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.xrtech.xrmirroring.common.Logger;

import java.util.Objects;

public class ScreenMirrorService extends Service {
    private static final String TAG = "ScreenCaptureService";
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final String DATA = "DATA";
    private static final String ACTION = "ACTION";
    private static final String START = "START";
    private static final String STOP = "STOP";

    private Context mContext;

    private Handler mHandler;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private MediaProjectionService mMediaProjectionService;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;

    private Logger mLogger;

    public static Intent getStartIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, ScreenMirrorService.class);
        intent.putExtra(ACTION, START);
        intent.putExtra(RESULT_CODE, resultCode);
        intent.putExtra(DATA, data);
        return intent;
    }

    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, ScreenMirrorService.class);
        intent.putExtra(ACTION, STOP);
        return intent;
    }

    private static boolean isStartCommand(Intent intent) {
        return intent.hasExtra(RESULT_CODE) && intent.hasExtra(DATA)
                && intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), START);
    }

    private static boolean isStopCommand(Intent intent) {
        return intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), STOP);
    }


    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mLogger = new TextLogger();
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    class TextLogger extends Logger {
        @Override
        public void log(final String message) {
            Log.d(TAG, message);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStartCommand(intent)) {
            // create notification
            Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
            startForeground(notification.first, notification.second);
            // start projection
            int resultCode = intent.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED);
            Intent data = intent.getParcelableExtra(DATA);
            startProjection(resultCode, data);
        } else if (isStopCommand(intent)) {
            stopProjection();
            stopSelf();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void startProjection(int resultCode, Intent data) {
        mLogger.log("resultCode:"+resultCode);
        mProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjectionService = new MediaProjectionService(mContext, mTransport, mMediaProjection);
        mMediaProjectionService.start();

    }

    private void stopProjection() {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMediaProjection != null) {
                        mMediaProjection.stop();
                        if (mTransport != null) {
                            mTransport.close();
                            mTransport = null;
                        }
                        mMediaProjection = null;
                    }
                }
            });
        }
    }
}
