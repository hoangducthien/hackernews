package com.hoangthien.hackernews.data.dataUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.JsonParser;

/**
 * Created by thien on 4/28/17.
 */

public class ParseResponse<E> {

    private TError mTError;
    private E mResult;
    private JSONObject mJSONObject;
    private JSONArray mJSONArray;
    private ArrayList<E> mListResult;
    private JsonParser<E> mJsonParser;


    public ParseResponse(JsonParser<E> jsonParser) {
        mJsonParser = jsonParser;
    }

    public void createResult(JSONObject jsonObject) {
        mResult = mJsonParser.fromJson(jsonObject);
    }


    public void createListResult(JSONArray jsonArray) {
        mListResult = new ArrayList<>();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            mListResult.add(mJsonParser.fromJson(jsonArray.optJSONObject(i)));
        }
    }

    public TError getTError() {
        return mTError;
    }

    public void setError(TError TError) {
        mTError = TError;
    }

    public E getResult() {
        return mResult;
    }

    public ArrayList<E> getListResult() {
        return mListResult;
    }

    public void setJSONObject(JSONObject JSONObject) {
        mJSONObject = JSONObject;
    }

    public void setJSONArray(JSONArray JSONArray) {
        mJSONArray = JSONArray;
    }

    public JSONObject getJSONObject() {
        return mJSONObject;
    }

    public JSONArray getJSONArray() {
        return mJSONArray;
    }
}
