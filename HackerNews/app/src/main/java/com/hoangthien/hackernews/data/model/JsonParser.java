package com.hoangthien.hackernews.data.model;

import org.json.JSONObject;

/**
 * Created by thien on 5/15/17.
 */
public interface JsonParser<E> {

    E fromJson(JSONObject jsonObject);

}
