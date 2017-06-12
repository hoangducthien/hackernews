package com.hoangthien.hackernews.data.reponsitory.home;

import android.text.TextUtils;
import android.util.Log;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.HttpUtils;
import com.hoangthien.hackernews.utils.TAsyncCallback;
import com.hoangthien.hackernews.utils.TConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by thien on 5/11/17.
 */

public class HomeApiServiceImpl implements HomeApiService {

    public static final String ID_LIST = "topstories.json";

    public static final String DATA_LIST = "item/%d.json";

    @Override
    public void getIdList(TAsyncCallback<List<Long>> result) {
        String url = TConstants.DOMAIN + ID_LIST;
        String data = HttpUtils.requestHttpGET(url);
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                ArrayList<Long> ids = new ArrayList<>();
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    ids.add(jsonArray.optLong(i));
                }
                result.onSuccess(ids);
            } catch (JSONException e) {
                Log.e("getIdList", " " + e.getMessage());
                result.onError(new TError(R.string.request_failed));
            }
        } else {
            result.onError(new TError(R.string.request_failed));
        }
    }


    @Override
    public void getDataList(List<Long> ids, TAsyncCallback<List<News>> result) {
        ArrayList<News> newses = new ArrayList<>();
        for (Long id : ids) {
            News news = getData(id);
            if (news != null) {
                newses.add(news);
            }
        }
        result.onSuccess(newses);
    }

    public News getData(Long id) {
        String url = String.format(Locale.US, TConstants.DOMAIN + DATA_LIST, id);
        String data = HttpUtils.requestHttpGET(url);
        try {
            JSONObject jsonObject = new JSONObject(data);
            return new News(jsonObject);
        } catch (JSONException e) {
            Log.e("getData", " " + e.getMessage());
        }
        return null;
    }

}
