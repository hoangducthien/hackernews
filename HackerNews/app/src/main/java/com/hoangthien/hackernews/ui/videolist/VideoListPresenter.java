package com.hoangthien.hackernews.ui.videolist;

import android.text.TextUtils;

import com.hoangthien.hackernews.base.BasePresenter;
import com.hoangthien.hackernews.data.localdatastorage.DataCache;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentReponsitory;

import java.util.ArrayList;

/**
 * Created by thien on 4/25/17.
 */

public class VideoListPresenter extends BasePresenter<VideoListView> {

    private CommentReponsitory mReponsitory;
    private int mType;
    private String mNextPageToken;
    private String mSourceId;
    private ArrayList<Comment> mData = new ArrayList<>();
    private DataCache mDataCache;

    public VideoListPresenter(VideoListView view, CommentReponsitory reponsitory) {
        super(view);
        mReponsitory = reponsitory;
        mDataCache = DataCache.getInstance();
    }


    public void getData(int type, String sourceId) {
        mType = type;
        mSourceId = sourceId;
//        switch (type) {
//            case Comment.FACEBOOK:
//                mReponsitory.getFacebookVideos(sourceId, "", mDataCallback);
//                break;
//            case Comment.YOUTUBE_CHANNEL:
//                mReponsitory.getYoutbeChannelVideos(sourceId, "", mDataCallback);
//                break;
//            case Comment.YOUTUBE_PLAYLIST:
//                mReponsitory.getYoutbePlayListVideos(sourceId, "", mDataCallback);
//                break;
//        }
    }

    public void loadNextPage() {
        if (!TextUtils.isEmpty(mNextPageToken)) {
            getView().showLoadmore();
//            switch (mType) {
//                case Comment.FACEBOOK:
//                    mReponsitory.getFacebookVideos(mSourceId, mNextPageToken, mLoadmoreCallback);
//                    break;
//                case Comment.YOUTUBE_CHANNEL:
//                    mReponsitory.getYoutbeChannelVideos(mSourceId, mNextPageToken, mLoadmoreCallback);
//                    break;
//                case Comment.YOUTUBE_PLAYLIST:
//                    mReponsitory.getYoutbePlayListVideos(mSourceId, mNextPageToken, mLoadmoreCallback);
//                    break;
//            }
        }
    }

//    private TAsyncCallback<VideoResponse> mLoadmoreCallback = new TAsyncCallback<VideoResponse>() {
//        @Override
//        public void onSuccess(VideoResponse responseData) {
//            if (getView() != null) {
//                mData.addAll(responseData.videos);
//                mNextPageToken = responseData.nextPageToken;
//                boolean canLoadmore = !TextUtils.isEmpty(mNextPageToken);
//                getView().showData(mData, canLoadmore);
//                getView().hideLoadmore();
//            }
//        }
//
//        @Override
//        public void onError(TError error) {
//            if (getView() != null) {
//                getView().showError(error, R.string.retry, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        loadNextPage();
//                    }
//                });
//                getView().hideLoadmore();
//            }
//        }
//    };

//    private TAsyncCallback<VideoResponse> mDataCallback = new TAsyncCallback<VideoResponse>() {
//        @Override
//        public void onSuccess(VideoResponse responseData) {
//            if (getView() != null) {
//                mData.addAll(responseData.videos);
//                mDataCache.setActiveList(mData);
//                mNextPageToken = responseData.nextPageToken;
//                boolean canLoadmore = !TextUtils.isEmpty(mNextPageToken);
//                addAd();
//                getView().showData(mData, canLoadmore);
//                getView().hideInitLoading();
//            }
//        }
//
//        @Override
//        public void onError(TError error) {
//            if (getView() != null) {
//                getView().showError(error, R.string.retry, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getData(mType, mSourceId);
//                    }
//                });
//            }
//        }
//    };


    public void handleItemSelected(int position) {
//        if (mType == Comment.YOUTUBE_CHANNEL || mType == Comment.YOUTUBE_PLAYLIST) {
//            getView().startPlayerActivity(position, YoutubePlayerActivity.class);
//        } else {
//            getView().startPlayerActivity(position, NormalVideoPlayerActivity.class);
//        }
    }


}
