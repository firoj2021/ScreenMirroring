package com.domainbangla.usbdatatransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.domainbangla.usbdatatransfer.common.Logger;
import com.domainbangla.usbdatatransfer.presentation.DemoPresentation;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = "SenderActivity";

    private static final String ACTION_USB_ACCESSORY_PERMISSION =
            "com.domainbangla.usbdatatransfer.ACTION_USB_ACCESSORY_PERMISSION";

    private static final String MANUFACTURER = "Android";
    private static final String MODEL = "Accessory Display";

    private UsbManager mUsbManager;
    private AccessoryReceiver mReceiver;
    private TextView mLogTextView;
    private Logger mLogger;
    private Presenter mPresenter;

    private boolean mConnected;
    private UsbAccessory mAccessory;

    private DisplaySourceService mDisplaySourceService;

    private UsbAccessoryStreamTransport mTransport;

    private Button btnConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        mLogTextView = (TextView) findViewById(R.id.logTextView);
        mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLogger = new TextLogger();
        mPresenter = new Presenter();

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null){
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                    this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            mUsbManager.requestPermission(accessory, pendingIntent);
            return;
        }

        // Open the accessory.
        ParcelFileDescriptor fd = mUsbManager.openAccessory(accessory);
        if (fd == null) {
            return;
        }

        // All set.
        mConnected = true;
        mAccessory = accessory;
        mTransport = new UsbAccessoryStreamTransport(mLogger, fd);
        startServices();
        mTransport.startReading();
    }

    private void disconnect() {
        stopServices();
        mConnected = false;
        mAccessory = null;
        if (mTransport != null) {
            mTransport = null;
        }
    }

    private void startServices() {
        mDisplaySourceService = new DisplaySourceService(this, mTransport, mPresenter);
        mDisplaySourceService.start();
    }

    private void stopServices() {
        if (mDisplaySourceService != null) {
            mDisplaySourceService.stop();
            mDisplaySourceService = null;
        }
    }

    private static boolean isSink(UsbAccessory accessory) {
        return MANUFACTURER.equals(accessory.getManufacturer())
                && MODEL.equals(accessory.getModel());
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


    class Presenter implements DisplaySourceService.Callbacks {
        private DemoPresentation mPresentation;

        @Override
        public void onDisplayAdded(Display display) {
            mLogger.log("Accessory display added: " + display);
            mPresentation = new DemoPresentation(SenderActivity.this, display, mLogger);
            mPresentation.show();
        }

        @Override
        public void onDisplayRemoved(Display display) {
            if (mPresentation != null) {
                mPresentation.dismiss();
                mPresentation = null;
            }
        }
    }

}