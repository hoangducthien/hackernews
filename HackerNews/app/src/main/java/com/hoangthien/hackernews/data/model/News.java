package com.hoangthien.hackernews.data.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by thien on 4/25/17.
 */

public class News implements JsonParser<News> {

    private String mBy;
    private int descendants;
    private String id;
    private ArrayList<Long> kids = new ArrayList<>();
    private int score;
    private long time;
    private String title;
    private String type;
    private String url;

    public News(JSONObject jsonObject) {
        mBy = jsonObject.optString("by");
        descendants = jsonObject.optInt("descendants");
        id = jsonObject.optString("id");
        JSONArray jsonArray = jsonObject.optJSONArray("kids");
        if (jsonArray != null) {
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                kids.add(jsonArray.optLong(i));
            }
        }
        score = jsonObject.optInt("score");
        time = jsonObject.optLong("time");
        title = jsonObject.optString("title");
        type = jsonObject.optString("type");
        url = jsonObject.optString("url");

    }

    public String getBy() {
        return mBy;
    }

    public void setBy(String by) {
        mBy = by;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public News fromJson(JSONObject jsonObject) {
        return new News(jsonObject);
    }

}
