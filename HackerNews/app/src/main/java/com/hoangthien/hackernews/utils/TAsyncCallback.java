package com.hoangthien.hackernews.utils;

import com.hoangthien.hackernews.base.TError;

/**
 * Created by hoangthien on 5/2/17.
 */

public interface TAsyncCallback<E> {

    void onSuccess(E responseData);

    void onError(TError error);
}
