package com.example.daale.researchproject_final;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
/*
 Commented out code alot of code below because it was moved to service.
 */
public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private ArrayList<String> devices = new ArrayList<>();

    private BluetoothLeScanner mBluetoothLeScanner;//mod

    private ArrayAdapter<String> listAdapter;
    private ListView listView;

    private ScanSettings scanSettings;
    private ArrayList<ScanFilter> filterList = new ArrayList<>();

    private void setScanSettings(){
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = builder.build();
    }

    DBHelper db;
    ArrayList<Beacon> beacons;
    TextToSpeech tts;
    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mServiceIntent = new Intent(this.getApplicationContext(), BeaconScan.class);
        mServiceIntent.setData(Uri.parse("h"));
        this.getApplicationContext().startService(mServiceIntent);

        /*db = new DBHelper(this);

        beacons = db.getBeacons();

        if(beacons.size() < 3){ //just for dev purposes
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 1, "Office A");
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 2, "Office B");
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 3, "Restroom A");
        }

        prevDistance = 0;

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        setScanSettings();

        mBluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();//mod

       *//* mHandler = new Handler();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;

                mBluetoothLeScanner.stopScan(mScanCallback);//mod
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);

        mScanning = true;
*/
        //mBluetoothLeScanner.startScan(filterList,scanSettings,mScanCallback);//mod
    }


    /*//////////////////////////////////////CITE CONVERSION CODE////////////////////
    // Here: http://kittensandcode.blogspot.com/2014/08/ibeacons-and-android-parsing-uuid-major.html
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }




    //mod
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            synchronized (this) {
                super.onScanResult(callbackType, result);
                ////////////////////////////////////////////////////////////////////CITE CONVERSION CODE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // Here: http://kittensandcode.blogspot.com/2014/08/ibeacons-and-android-parsing-uuid-major.html
                byte[] scanRecord = result.getScanRecord().getBytes();

                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5) {
                    if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                            ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }

                if (patternFound) {
                    //Convert to hex String
                    byte[] uuidBytes = new byte[16];
                    System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                    String hexString = bytesToHex(uuidBytes);

                    //Here is your UUID
                    final String uuid = hexString.substring(0, 8) + "-" +
                            hexString.substring(8, 12) + "-" +
                            hexString.substring(12, 16) + "-" +
                            hexString.substring(16, 20) + "-" +
                            hexString.substring(20, 32);

                    if (!uuid.equals("295178D3-BF13-4F2A-B2E4-42F717156796")) {
                        return;
                    }

                    //Here is your Major value
                    final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                    //Here is your Minor value
                    final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);


                    Log.i("HHH", "" + uuid);
                    int i = 0;
                    for (Beacon b : beacons) {
                        if (b.getMajor() == major && b.getMin() == minor) {
                            i = beacons.indexOf(b);
                            break;
                        }
                    }

                    final int distance = beacons.get(i).calculateDistance(result.getRssi());
                    //final int prevDistance = beacons.get(i).getPrevDistance();

                    //if(Math.abs(prevDistance-distance) > 1)
                    if (beacons.get(i).getCount() == 60) {
                        //beacons.get(i).setPrevDistance(distance);
                        beacons.get(i).resetCount();
                        final int j = i;
                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    tts.setLanguage(Locale.US);
                                    tts.speak(beacons.get(j).getLocation() + " is approximately" + distance + " meters away", TextToSpeech.QUEUE_FLUSH, null, "Distance");
                                }
                            }
                        });
                    }
                }
            }
        }
    };

*/
}

