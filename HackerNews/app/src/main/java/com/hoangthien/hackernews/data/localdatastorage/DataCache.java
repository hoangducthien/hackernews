package com.hoangthien.hackernews.data.localdatastorage;

import com.hoangthien.hackernews.data.model.Comment;

import java.util.ArrayList;

/**
 * Created by hoangthien on 5/20/17.
 */

public class DataCache {

    private static DataCache mInstance;
    private ArrayList<Comment> mActiveList;

    public static synchronized DataCache getInstance() {
        if (mInstance == null) {
            mInstance = new DataCache();
        }
        return mInstance;
    }

    private DataCache() {
    }

}
