package com.example.daale.researchproject_final;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BeaconScan extends IntentService {

    private String fileName = "";
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;//mod

    private ScanSettings scanSettings;
    private ArrayList<ScanFilter> filterList = new ArrayList<>();

    DBHelper db;
    ArrayList<Beacon> beacons; //beacons from db
    TextToSpeech tts;

    public BeaconScan() {
        super("BeaconScan");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        db = new DBHelper(this);

        beacons = db.getBeacons();

        if(beacons.size() < 3){ //just for dev purposes
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 1, "Office A");
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 2, "Office B");
            db.insertBeacon("295178d3-bf13-4f2a-b2e4-42f717156796", 1, 3, "Restroom A");
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        setScanSettings();

        mBluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();//mod
        mBluetoothLeScanner.startScan(filterList, scanSettings, mScanCallback);
    }

    private void setScanSettings(){
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = builder.build();
    }


    //////////////////////////////////////CITE CONVERSION CODE////////////////////
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

                    if (!uuid.equals("295178D3-BF13-4F2A-B2E4-42F717156796")) { //make sure they're our beacons
                        return;
                    }

                    //Here is your Major value
                    final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                    //Here is your Minor value
                    final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

                    // find index of beacon whose packet receipt caused callback and save index
                    int i = 0;
                    for (Beacon b : beacons) {
                        if (b.getMajor() == major && b.getMin() == minor) {
                            i = beacons.indexOf(b);
                            break;
                        }
                    }

                    //calculate distance of beacon
                    final int distance = beacons.get(i).calculateDistance(result.getRssi());
                    final int prevDistance = beacons.get(i).getPrevDistance();
                    beacons.get(i).setLastRSSI(result.getRssi());

                    writeFile(beacons.get(i));//write to file
                    /*if (beacons.get(i).getCount() == 60)*/
                    if (Math.abs(prevDistance-distance) > 2) { //only notify user via audio if distance changed by 3 or more meters
                        beacons.get(i).setPrevDistance(distance);
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


    /* Write details of a scan to the current file being used by this run
    * Details include: distance, location, rssi, and average rssi.
     * A new file is created each time the application is opened.
     */
    private void writeFile(Beacon beacon){

        if(fileName.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            fileName = calendar.get(Calendar.DAY_OF_YEAR) + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + ".txt";
        }

        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/distance_approximations");
        dir.mkdirs();

            try {
                File file = new File(dir, fileName);

                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                PrintWriter pw = new PrintWriter(fileOutputStream);

                pw.println("Beacon Location: " + beacon.getLocation());
                pw.println("Beacon Calculated Distance: " + beacon.getApproxDistance());
                pw.println("Current RSSI Value: " + beacon.getLastRSSI());
                pw.println("Beacon Average RSSI: " + beacon.getAverageRSSI());
                pw.println();

                pw.flush();
                pw.close();
                fileOutputStream.close();
            } catch (Exception e) {
                Log.e("ERR", e.toString(), e);
            }
        }
    }


