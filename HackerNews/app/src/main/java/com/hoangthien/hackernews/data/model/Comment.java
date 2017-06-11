package com.hoangthien.hackernews.data.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by thien on 5/15/17.
 */

public class Comment implements JsonParser<Comment> {

    private String mBy;
    private String id;
    private ArrayList<Long> kids = new ArrayList<>();
    private long time;
    private String text;
    private String type;
    private String replyOf;

    public Comment(JSONObject jsonObject) {
        mBy = jsonObject.optString("by");
        id = jsonObject.optString("id");
        JSONArray jsonArray = jsonObject.optJSONArray("kids");
        if (jsonArray != null) {
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                kids.add(jsonArray.optLong(i));
            }
        }
        time = jsonObject.optLong("time");
        type = jsonObject.optString("type");
    }

    public String getBy() {
        return mBy;
    }

    public void setBy(String by) {
        mBy = by;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Long> getKids() {
        return kids;
    }

    public void setKids(ArrayList<Long> kids) {
        this.kids = kids;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReplyOf() {
        return replyOf;
    }

    public void setReplyOf(String replyOf) {
        this.replyOf = replyOf;
    }

    @Override
    public Comment fromJson(JSONObject jsonObject) {
        return new Comment(jsonObject);
    }

}
