package com.xrtech.xrmirroring;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xrtech.xrmirroring.common.Logger;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = "SenderActivity";

    private static final String ACTION_USB_ACCESSORY_PERMISSION =
            "com.xrtech.xrmirroring.ACTION_USB_ACCESSORY_PERMISSION";

    private UsbManager mUsbManager;
    private AccessoryReceiver mReceiver;
    private TextView mLogTextView;
    private Logger mLogger;

    private boolean mConnected;
    private UsbAccessory mAccessory;

    public static UsbAccessoryStreamTransport mTransport;

    private Button btnConnect,btnShare;

    private static final int PERMISSION_CODE = 129;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnShare = (Button) findViewById(R.id.btnShare);
        mLogTextView = (TextView) findViewById(R.id.logTextView);
        mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLogger = new TextLogger();

        mLogger.log("Waiting for accessory display sink to be attached to USB...");

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(ACTION_USB_ACCESSORY_PERMISSION);
        mReceiver = new AccessoryReceiver();
        registerReceiver(mReceiver, filter);

        Intent intent = getIntent();
        if (intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
            UsbAccessory accessory =
                    (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (accessory != null) {
                onAccessoryAttached(accessory);
            }
        } else {
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            if (accessories != null) {
                for (UsbAccessory accessory : accessories) {
                    onAccessoryAttached(accessory);
                }
            }
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsbAccessory[] accessories = mUsbManager.getAccessoryList();
                if (!mConnected && accessories != null) {
                    for (UsbAccessory accessory : accessories) {
                        onAccessoryAttached(accessory);
                    }
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnected && mTransport != null){
                    startProjection();
                }else {
                    showToast("Please connect first");
                }
            }
        });
    }

    private void startProjection() {
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
    }


    private void stopProjection() {
        if (isMyServiceRunning(ScreenMirrorService.class)){
            startService(ScreenMirrorService.getStopIntent(this));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PERMISSION_CODE && resultCode != RESULT_OK) {
            showToast("Screen Cast Permission Denied");
            return;
        }

        //stop first
        //stopProjection();

        //Then start again
        if (!isMyServiceRunning(ScreenMirrorService.class)){
            startService(ScreenMirrorService.getStartIntent(this, resultCode, data));
        }
    }

    private void onAccessoryAttached(UsbAccessory accessory) {
        mLogger.log("USB accessory attached: " + accessory);
        if (!mConnected) {
            connect(accessory);
        }
    }

    private void onAccessoryDetached(UsbAccessory accessory) {
        if (mConnected && accessory.equals(mAccessory)) {
            disconnect();
        }
    }

    private void connect(UsbAccessory accessory) {

        if (mConnected) {
            disconnect();
        }

        // Check whether we have permission to access the accessory.
        if (!mUsbManager.hasPermission(accessory)) {
            Intent intent = new Intent(ACTION_USB_ACCESSORY_PERMISSION);
            intent.setPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, PendingIntent.FLAG_MUTABLE);
            mUsbManager.requestPermission(accessory, pendingIntent);
            return;
        }

        // Open the accessory.
        ParcelFileDescriptor fd = mUsbManager.openAccessory(accessory);
        if (fd == null) {
            return;
        }

        mConnected = true;
        mAccessory = accessory;
        mTransport = new UsbAccessoryStreamTransport(mLogger, fd);
        mTransport.startReading();
    }

    private void disconnect() {
        mConnected = false;
        mAccessory = null;
        if (mTransport != null) {
            mTransport = null;
        }
    }

    class AccessoryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbAccessory accessory = intent.<UsbAccessory>getParcelableExtra(
                    UsbManager.EXTRA_ACCESSORY);
            if (accessory != null) {
                String action = intent.getAction();
                if (action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
                    onAccessoryAttached(accessory);
                } else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
                    onAccessoryDetached(accessory);
                } else if (action.equals(ACTION_USB_ACCESSORY_PERMISSION)) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        onAccessoryAttached(accessory);
                    } else {
                        mLogger.logError("Accessory permission denied: " + accessory);
                    }
                }
            }
        }
    }

    class TextLogger extends Logger {
        @Override
        public void log(final String message) {
            mLogTextView.post(new Runnable() {
                @Override
                public void run() {
                    mLogTextView.append(message);
                    mLogTextView.append("\n");
                }
            });
        }
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        stopProjection();
    }

}