package com.sunofbeaches.himalaya.presenters;

import android.support.annotation.Nullable;

import com.sunofbeaches.himalaya.interfaces.IAlbumDetailPresenter;
import com.sunofbeaches.himalaya.interfaces.IAlbumDetailViewCallback;
import com.sunofbeaches.himalaya.utils.Constants;
import com.sunofbeaches.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {

    private static final String TAG = "AlbumDetailPresenter";
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private static AlbumDetailPresenter sInstance = null;
    private List<Track> mTracks = new ArrayList<>();
    private Album mTargetAlbum = null;

    /**
     * 获取单例对象
     *
     * @return
     */
    public static AlbumDetailPresenter getInstance() {
        if(sInstance == null) {
            synchronized(AlbumDetailPresenter.class) {
                if(sInstance == null) {
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }

    public void setTargetAlbum(Album album) {
        this.mTargetAlbum = album;
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        if (!mCallbacks.contains(detailViewCallback)) {
            mCallbacks.add(detailViewCallback);
            if (mTargetAlbum != null) {
                detailViewCallback.onAlbumLoaded(mTargetAlbum);
            }
        }
    }

    @Override
    public void unRegisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        mCallbacks.remove(detailViewCallback);
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.ALBUM_ID, albumId + "");
        map.put(DTransferConstants.PAGE, page + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.COUNT_DEFAULT + "");
        CommonRequest.getTracks(map, new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(@Nullable TrackList trackList) {
                LogUtil.d(TAG,"Current Thread --> " + Thread.currentThread().getName());
                if(trackList!=null) {
                    List<Track> tracks = trackList.getTracks();
                    mTracks.addAll(tracks);
                    LogUtil.d(TAG, "get trackList --> " + tracks.size());
                    handleResult(mTracks);
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void handleResult(List<Track> trackList) {
        for(IAlbumDetailViewCallback mCallback:mCallbacks){
            mCallback.onDetailListLoaded(trackList);
        }
    }
}
