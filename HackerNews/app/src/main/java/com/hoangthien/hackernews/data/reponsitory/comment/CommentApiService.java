package com.hoangthien.hackernews.data.reponsitory.comment;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;

/**
 * Created by thien on 5/11/17.
 */

public interface CommentApiService {

    void getDataList(ArrayList<Long> ids, TAsyncCallback<ArrayList<Comment>> result);


}
