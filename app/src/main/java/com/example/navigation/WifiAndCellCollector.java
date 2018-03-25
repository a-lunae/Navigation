package com.example.navigation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.GZIPOutputStream;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class WifiAndCellCollector extends PhoneStateListener implements Runnable, LocationListener {

    private static final String[] lbsPostName = new String[]{"xml"};
    private static final String[] lbsContentType = new String[]{"xml"};

    private static final String[] wifipoolPostName = new String[]{"data"};
    private static final String[] wifipoolContentType = new String[]{"xml"};
    private static final String[] wifipoolContentTypeGzipped = new String[]{"xml/gzip"};

    public static final String PROTOCOL_VERSION = "1.0";
    public static final String API_KEY = "ABTscVoBAAAAKeEuDwIAe-HLPCouf8wjy8JdAwcqiH-vQ_AAAAAAAAAAAAARWlStDaePjxGa1HmMRmmutu8aPA==";

    public static final String LBS_API_HOST = "http://api.lbs.yandex.net/geolocation";
    public static final String WIFIPOOL_HOST = "http://api.lbs.yandex.net/partners/wifipool?";

    public static final String GSM = "gsm";
    public static final String CDMA = "cdma";

    private static final long COLLECTION_TIMEOUT = 30000;
    private static final long WIFI_SCAN_TIMEOUT = 30000;
    private static final long GPS_SCAN_TIMEOUT = 2000;
    private static final long GPS_OLD = 3000;               // если со времени фикса прошло больше времени, то данные считаются устаревшие
    private static final long SEND_TIMEOUT = 30000;

    private Context context;
    private LbsLocationListener listener;
    private SimpleDateFormat formatter;
    private TelephonyManager tm;

    private String mcc;
    private String mnc;
    private int cellId, lac, signalStrength;

    private WifiManager wifi;
    private long lastWifiScanTime;
    public static List<WifiInfo> wifiInfos;


    private volatile boolean isRun;

    public static Map<Integer,String> networkTypeStr;
    static {
        networkTypeStr = new HashMap<Integer,String>();
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO_0");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO_A");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_IDEN, "IDEN");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UNKNOWN, "UNKNOWN");
    }

    @SuppressLint("SimpleDateFormat")
    public WifiAndCellCollector(Context context, LbsLocationListener listener, String uuid) {
        this.listener = listener;
        this.context = context;
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        formatter = new SimpleDateFormat("ddMMyyyy:HHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        wifiInfos = new ArrayList<WifiInfo>();
        wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lastWifiScanTime = 0;
    }

    public void startCollect() {
        isRun = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        (new Thread(this)).start();
    }

    public void stopCollect() {
        isRun = false;
        if (tm != null) {
            tm.listen(this, PhoneStateListener.LISTEN_NONE);
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.removeUpdates(this);
    }

    @Override
    public void run() {
        while (isRun) {
            collectWifiInfo();
            try {
                Thread.sleep(COLLECTION_TIMEOUT);
            } catch (InterruptedException ie) {}
        }
    }

    public void collectWifiInfo() {
        wifiInfos.clear();
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();

        if (wifi != null && wifi.isWifiEnabled()) {
            List<ScanResult> wifiNetworks = wifi.getScanResults();
            if (wifiNetworks != null && wifiNetworks.size() > 0) {
                for (ScanResult net:wifiNetworks) {
                    if (    Objects.equals(net.BSSID, "ac:f1:df:c4:9a:69") ||
                            Objects.equals(net.BSSID, "34:a8:4e:9a:bc:90") ||
                            Objects.equals(net.BSSID, "34:a8:4e:9a:bc:9f") ||
                            Objects.equals(net.BSSID, "34:a8:4e:9a:bc:9e") ||
                            Objects.equals(net.BSSID, "1c:99:4c:5d:ce:40") ||
                            Objects.equals(net.BSSID, "1c:bd:b9:b5:d2:86") ||
                            Objects.equals(net.BSSID, "9c:5c:8e:ae:b3:74"))
                    {

                        WifiInfo info = new WifiInfo();
                        info.mac = net.BSSID.toUpperCase();
                        char[] mac = net.BSSID.toUpperCase().toCharArray();
                        info.signalStrength = net.level;
                        char ch;
                        StringBuilder ssid = new StringBuilder(12);
                        for (char aMac : mac) {
                            ch = aMac;
                            if (ch != ':') {
                                ssid.append(ch);
                            }
                        }
                        info.ssid = ssid.toString();
                        info.name = net.SSID;
                        wifiInfos.add(info);

                        cv.put("name", info.name);
                        cv.put("mac", info.mac);
                        cv.put("signalStrength", info.signalStrength);

                        String whereClause = "mac = ?";
                        String[] whereArgs = new String[] {info.mac};
                        Cursor cursor = db.query("mytable", null, whereClause, whereArgs, null, null, null);
                        if(cursor.getCount()<1) {
                            db.insert("mytable", null, cv);
                            Log.d("DB ", "ROW INSERTED "+info.mac+" "+info.name);

                        } else {
                            cursor.moveToFirst();
                            db.update("mytable", cv, whereClause, whereArgs);
                            Log.d("DB ", "ROW UPDATED "+info.mac+" "+info.name);
                        }
                        cursor.close();
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            if (lastWifiScanTime > currentTime) {
                lastWifiScanTime = currentTime;
            } else if (currentTime - lastWifiScanTime > WIFI_SCAN_TIMEOUT) {
                lastWifiScanTime = currentTime;
                wifi.startScan();
            }
        }
    }

    @Override
    public void onSignalStrengthChanged(int asu) {
        signalStrength = -113 + 2 * asu;
    }
    
    @Override
    public void onCellLocationChanged(CellLocation location) {
      if (location != null) {
          if (location instanceof GsmCellLocation) {
              GsmCellLocation gsmLoc = (GsmCellLocation) location;
              lac = gsmLoc.getLac();
              cellId = gsmLoc.getCid();
          }
      }
    }
    
    public void requestMyLocation() {
        UpdateTask updateTask = new UpdateTask(context);
        JSONObject response = updateTask.loadJSON("https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyAdgdCvAoWpvfcB51PJ86TPRFVoHK7SXXU");
        Log.d("JSON RES", response.toString());
        Info info = new Info();
        try {
            info = Info.parseJsonData(response);
        } catch (Exception e) {
            info.isError = true;
            info.errorMessage = "Не удалось получить местоположение";
        }
        if (listener != null) {
            listener.onLocationChange(info);
        }

        Log.d("WIFI INFO ","+++++++++");
        for (WifiInfo wifiChunks: wifiInfos) {
            Log.d("Wifi: ", wifiChunks.mac + " " + wifiChunks.signalStrength);
        }
        Log.d("WIFI INFO ","+++++++++");
    }

    protected static final boolean[] WWW_FORM_URL = new boolean[256];

    // Static initializer for www_form_url
    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL[i] = true;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL[i] = true;
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL[i] = true;
        }
        // special chars
        WWW_FORM_URL['-'] = true;
        WWW_FORM_URL['_'] = true;
        WWW_FORM_URL['.'] = true;
        WWW_FORM_URL['*'] = true;
        // blank to be replaced with +
        WWW_FORM_URL[' '] = true;
    }
    
    public static byte[] encodeUrl(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        boolean[] urlsafe = WWW_FORM_URL;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b = 256 + b;
            }
            if (urlsafe[b]) {
                if (b == ' ') {
                    b = '+';
                }
                buffer.write(b);
            } else {
                buffer.write('%');
                char hex1 = Character.toUpperCase(forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(forDigit(b & 0xF, 16));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        return buffer.toByteArray();
    }
    
    private static char forDigit(int digit, int radix) {
        if ((digit >= radix) || (digit < 0)) {
            return '\0';
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            return '\0';
        }
        if (digit < 10) {
            return (char)('0' + digit);
        }
        return (char)('a' - 10 + digit);
    }

    public class WifiInfo {
        public String mac;
        public int signalStrength;
        
        private String ssid;
        public String name;
    }

    @Override
    public void onProviderDisabled(String provider) {
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        
    }
}
