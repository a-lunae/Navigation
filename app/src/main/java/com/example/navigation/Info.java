package com.example.navigation;

import org.json.JSONException;
import org.json.JSONObject;

class Info {

    private static final int ID_UNKNOWN = 0;
    private static final int ID_ERROR = 1;
    private static final int ID_POSITION = 2;
    private static final int ID_LATITUDE = 3;
    private static final int ID_LONGTITUDE = 4;
    private static final int ID_ALTITUDE = 5;
    private static final int ID_PRECISION = 6;
    private static final int ID_TYPE = 7;

    private static final String TAG_ERROR = "error";
    private static final String TAG_POSITION = "position";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGTITUDE = "longitude";
    private static final String TAG_ALTITUDE = "altitude";
    private static final String TAG_PRECISION = "precision";
    private static final String TAG_TYPE = "type";

    public String lbsLatitude;
    public String lbsLongtitude;
    public String lbsAltitude;
    public String lbsPrecision;
    public String lbsType;

    public boolean isError;
    public String errorMessage;


    public static Info parseJsonData(JSONObject response) throws JSONException {
        Info result = new Info();

        JSONObject jsonObject = (JSONObject) response.get("location");
        result.lbsLatitude = jsonObject.getString("lat");
        result.lbsLongtitude = jsonObject.getString("lng");
        result.lbsPrecision = response.getString("accuracy");

        return result;
    }
}
