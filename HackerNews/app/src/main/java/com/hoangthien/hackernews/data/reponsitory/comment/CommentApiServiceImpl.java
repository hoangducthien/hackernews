package com.hoangthien.hackernews.data.reponsitory.comment;

import android.text.TextUtils;
import android.util.Log;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.HttpUtils;
import com.hoangthien.hackernews.utils.TAsyncCallback;
import com.hoangthien.hackernews.utils.TConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by thien on 5/11/17.
 */

public class CommentApiServiceImpl implements CommentApiService {

    public static final String DATA_LIST = "item/%d.json";

    @Override
    public void getDataList(List<Long> ids, TAsyncCallback<List<Comment>> result) {
        ArrayList<Comment> comments = new ArrayList<>();
        for (Long id : ids) {
            getData(id, comments, null);
        }
        result.onSuccess(comments);
    }

    public void getData(Long id, ArrayList<Comment> comments, Comment parent) {
        String url = String.format(Locale.US, TConstants.DOMAIN + DATA_LIST, id);
        String data = HttpUtils.requestHttpGET(url);
        if (!TextUtils.isEmpty(data)) {
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

}
