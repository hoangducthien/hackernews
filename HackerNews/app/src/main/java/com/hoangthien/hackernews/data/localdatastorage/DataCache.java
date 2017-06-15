package com.hoangthien.hackernews.data.localdatastorage;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.data.model.News;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoangthien on 5/20/17.
 */

public class DataCache {

    private static DataCache mInstance;
    private ArrayList<News> mNewsList;
    private ArrayList<Comment> mComments;
    private ArrayList<Long> mNewsIds;

    public static synchronized DataCache getInstance() {
        if (mInstance == null) {
            mInstance = new DataCache();
        }
        return mInstance;
    }

    private DataCache() {
    }

    public void setNewsList(ArrayList<News> newsList) {
        mNewsList = newsList;
    }

    public void setNewsIds(ArrayList<Long> ids) {
        mNewsIds = ids;
    }

    public void addToNewsList(List<News> newsList) {
        if (mNewsList == null) {
            mNewsList = new ArrayList<>(newsList);
        } else {
            mNewsList.addAll(newsList);
        }
    }


    public ArrayList<News> getNewsList() {
        return mNewsList;
    }

    public ArrayList<Long> getNewsIds() {
        return mNewsIds;
    }

    public ArrayList<Comment> getComments() {
        return mComments;
    }

    public void setComments(ArrayList<Comment> comments) {
        mComments = comments;
    }

    public void addComments(List<Comment> comments) {
        if (mComments == null) {
            mComments = new ArrayList<>(comments);
        } else {
            mComments.addAll(comments);
        }
    }
}
