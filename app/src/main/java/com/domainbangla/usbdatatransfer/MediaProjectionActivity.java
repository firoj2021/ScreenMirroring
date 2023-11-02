package com.domainbangla.usbdatatransfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;


public class MediaProjectionActivity extends Activity {
    private static final String TAG = "MediaProjectionActivity";

    private TextView mLogTextView;
    private EditText mAddressText;
    private Button mButton;
    public static boolean mConnected;

    private MediaProjectionManager mProjectionManager;

    private static final int PERMISSION_CODE = 1;

    public static String mSinkAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);
        mLogTextView = (TextView) findViewById(R.id.logTextView);
        mAddressText = (EditText) findViewById(R.id.address);
        mButton = (Button) findViewById(R.id.btnConnect);
        mButton.setText("Start Mirroring");
        mAddressText.setText(SourceTcpActivity.ip);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mConnected) {
                    mSinkAddress = mAddressText.getText().toString();
                    startProjection();
                }
            }
        });

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setVisibility(View.GONE);

        mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private void startProjection() {
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
    }


    private void stopProjection() {
        startService(ScreenMirrorService.getStopIntent(this));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged()");
    }

//    private MediaProjection.Callback mMediaProjectionCallback =
//        new MediaProjection.Callback() {
//            @Override
//            public void onStop() {
//                disconnect();
//            }
//        };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }

        startService(ScreenMirrorService.getStartIntent(this, resultCode, data));

//        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//        if (mMediaProjection != null) {
//            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
//            connect();
//        }


    }

//    private void connect() {
//        if (mMediaProjection != null) {
//            new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... params) {
//                    try {
//                        final Socket socket = new Socket(mSinkAddress, 1234);
//                        MediaProjectionActivity.this.runOnUiThread(new Runnable() {
//                            public void run() {
//                                mTransport = new SourceTcpTransport(mLogger, socket);
//                                mButton.setText(R.string.button_disconnect);
//                                startServices();
//                                mTransport.startReading();
//                                mLogger.log("Connected.");
//                                mConnected = true;
//                            }
//                        });
//                    } catch (Exception e) {
//                        mLogger.log("Socket connection error");
//                    }
//                    return null;
//                }
//            }.execute();
//        }
//    }

//    private void disconnect() {
//        if (!mConnected) return;
//        mLogger.log("Disconnecting from TCP sink");
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                MediaProjectionActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        stopServices();
//                    }
//                });
//                if (mTransport != null) {
//                    mTransport.close();
//                    mTransport = null;
//                }
//                mMediaProjection = null;
//                MediaProjectionActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        mConnected = false;
//                        mButton.setText(R.string.button_connect);
//                    }
//                });
//                return null;
//            }
//        }.execute();
//    }

//    private void startServices() {
//        if (mMediaProjection != null) {
//            mMediaProjectionService = new MediaProjectionService(this, mTransport, mMediaProjection);
//            mMediaProjectionService.start();
//        }
//    }

//    private void stopServices() {
//        if (mMediaProjectionService != null) {
//            mMediaProjectionService.stop();
//            mMediaProjectionService = null;
//        }
//    }

//    class TextLogger extends Logger {
//        @Override
//        public void log(final String message) {
//            Log.d(TAG, message);
//
//            mLogTextView.post(new Runnable() {
//                @Override
//                public void run() {
//                    mLogTextView.append(message);
//                    mLogTextView.append("\n");
//                }
//            });
//        }
//    }
}
