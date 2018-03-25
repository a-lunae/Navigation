package com.example.navigation;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateTask extends AsyncTask<String, Void, JSONObject> {
    Context context;
    public JSONObject res = new JSONObject();

    public UpdateTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        return loadJSON(urls[0]);
    }

    public JSONObject loadJSON(String url) {

        JSONParser jParser = new JSONParser();
        // здесь параметры необходимые в запрос добавляем
        List<NameValuePair> params = new ArrayList<>();
        //params.add(new BasicNameValuePair("my_param", "param_value"));
        // посылаем запрос методом POST

        return jParser.makeHttpRequest(url, "POST", params);
    }

    @Override
    protected void onPostExecute(JSONObject jsonData) {
        // если какой-то фейл, проверяем на null
        // фейл может быть по многим причинам: сервер сдох, нет сети на устройстве и т.д.
        if (jsonData != null) {
            super.onPostExecute(jsonData);
            // прочитать параметр, который отправил сервер;
            // здесь вместо "result" подставляйте то, что вам надо
            res = jsonData;
            //Log.d("JSON RES", res.toString());
        }
    }

}