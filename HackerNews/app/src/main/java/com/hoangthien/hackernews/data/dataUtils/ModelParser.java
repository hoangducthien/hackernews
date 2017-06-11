package com.hoangthien.hackernews.data.dataUtils;

import org.json.JSONObject;

/**
 * Created by thien on 4/28/17.
 */

public interface ModelParser<E> {

    int mErrorCode = 0;
    String mErrorMessage = "";

    E fromJSon(JSONObject jsonObject);

}
