package com.hoangthien.hackernews.data.dataUtils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.TError;

/**
 * Created by thien on 4/28/17.
 */

public class DataParser {

    public static final int TYPE_JSON_OBJECT = 1;
    public static final int TYPE_JSON_ARRAY = 2;

    public static void checkError(String data, int type, ParseResponse parseResponse) {
        if (TextUtils.isEmpty(data)) {
            parseResponse.setError(TError.noInternet());
        } else {
            try {
                switch (type) {
                    case TYPE_JSON_ARRAY:
                        parseResponse.setJSONArray(new JSONArray(data));
                        break;
                    case TYPE_JSON_OBJECT:
                        parseResponse.setJSONObject(new JSONObject(data));
                        break;
                }
            } catch (JSONException e) {
                Log.e("DataParser", data + " " + e.getMessage());
                parseResponse.setError(new TError(R.string.request_failed));
            }
        }
    }

    public static void checkError(Map data, ParseResponse parseResponse) {
        if (data == null || data.isEmpty()) {
            parseResponse.setError(TError.noInternet());
        } else {
            parseResponse.setJSONObject(new JSONObject(data));
        }
    }


}
