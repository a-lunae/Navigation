package com.example.navigation;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // конструктор
    public JSONParser() {

    }

    /**
     * Получить ответ по ссылке в формате json
     *
     * @param url    запрашиваемая страница
     * @param method GET or POST
     * @param params параметры, которые необходимо передать
     * @return
     */
    public JSONObject makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {

        // создаём HTTP запрос
        try {

            if (Objects.equals(method, "POST")) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-type", "application/json");
                String s, s1;
                s = "{\n" +
                        "  \"homeMobileCountryCode\": 250,\n" +
                        "  \"homeMobileNetworkCode\": 20,\n" +
                        "  \"radioType\": \"gsm\",\n" +
                        "  \"carrier\": \"TELE2\",\n" +
                        "  \"considerIp\": \"false\",\n" +
                        "  \"cellTowers\": [ ],\n" +
                        "  \"wifiAccessPoints\": [\n";
                int k = 0;
                for (WifiAndCellCollector.WifiInfo wifiInfo : WifiAndCellCollector.wifiInfos) {
                    s = s + "\t{\n" +
                            "        \"macAddress\": \"" + wifiInfo.mac + "\",\n" +
                            "        \"signalStrength\": " + wifiInfo.signalStrength + ",\n" +
                            "        \"signalToNoiseRatio\": 0\n";
                    if (k + 1 == WifiAndCellCollector.wifiInfos.size()) {
                        s = s + "\t}\n";
                    } else {
                        s = s + "\t},\t\n";
                    }
                    k = k + 1;
                }
                s = s + "  ]\n" +
                        "}\n" +
                        "\n";

           /*     s = "{\n" +
                        "  \"homeMobileCountryCode\": 250,\n" +
                        "  \"homeMobileNetworkCode\": 20,\n" +
                        "  \"radioType\": \"gsm\",\n" +
                        "  \"carrier\": \"TELE2\",\n" +
                        "  \"considerIp\": \"false\",\n" +
                        "  \"cellTowers\": [ ],\n" +
                        "  \"wifiAccessPoints\": [\n" +
                        "\t{\n" +
                        "        \"macAddress\": \"34:a8:4e:9a:bc:9e\",\n" +
                        "        \"signalStrength\": -79,\n" +
                        "        \"signalToNoiseRatio\": 0\n" +
                        "\t},\t\n" +
                        "\t{\n" +
                        "        \"macAddress\": \"34:a8:4e:9a:bc:9f\",\n" +
                        "        \"signalStrength\": -73,\n" +
                        "        \"signalToNoiseRatio\": 0\n" +
                        "\t},\t\n" +
                        "\t{\n" +
                        "        \"macAddress\": \"34:a8:4e:9a:bc:90\",\n" +
                        "        \"signalStrength\": -72,\n" +
                        "        \"signalToNoiseRatio\": 0\n" +
                        "\t},\t\n" +
                        "\t{\n" +
                        "        \"macAddress\": \"ac:f1:df:c4:9a:69\",\n" +
                        "        \"signalStrength\": -48,\n" +
                        "        \"signalToNoiseRatio\": 0\n" +
                        "\t},\t\n" +
                        "\t{\n" +
                        "        \"macAddress\": \"9c:5c:8e:ae:b3:74\",\n" +
                        "        \"signalStrength\": -83,\n" +
                        "        \"signalToNoiseRatio\": 0\n" +
                        "\t}\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n";*/


                StringEntity se = new StringEntity(s);
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } else if (Objects.equals(method, "GET")) {

                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }


        // пробуем распарсит JSON объект
        try {
            jObj = new JSONObject(json);
            Log.d("JOBJ", jObj.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }


        return jObj;

    }
}