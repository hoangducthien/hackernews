package com.hoangthien.hackernews.data.reponsitory.comment;

import android.util.Log;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.HttpUtils;
import com.hoangthien.hackernews.utils.TAsyncCallback;
import com.hoangthien.hackernews.utils.TConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by thien on 5/11/17.
 */

public class CommentApiServiceImpl implements CommentApiService {

    public static final String DATA_LIST = "item/%d.json";

    @Override
    public void getDataList(ArrayList<Long> ids, TAsyncCallback<ArrayList<Comment>> result) {
        ArrayList<Comment> comments = new ArrayList<>();
        for (Long id : ids) {
            getData(id, comments, null);
        }
        result.onSuccess(comments);
    }

    public void getData(Long id, ArrayList<Comment> comments, Comment parent) {
        String url = TConstants.DOMAIN + DATA_LIST + id;
        String data = HttpUtils.requestHttpGET(url);
        try {
            JSONObject jsonObject = new JSONObject(data);
            Comment comment = new Comment(jsonObject);
            if ("comment".equals(comment.getType())) {
                if (parent != null) {
                    comment.setReplyOf(parent.getId());
                }
                comments.add(comment);
                ArrayList<Long> ids = comment.getKids();
                if (ids != null && ids.size() > 0) {
                    if (parent == null) {
                        parent = comment;
                    }
                    for (Long commentId : ids) {
                        getData(commentId, comments, parent);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("getData", " " + e.getMessage());
        }
    }

}
