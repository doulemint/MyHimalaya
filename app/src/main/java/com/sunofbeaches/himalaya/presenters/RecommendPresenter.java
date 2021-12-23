package com.sunofbeaches.himalaya.presenters;

import android.support.annotation.Nullable;

import com.sunofbeaches.himalaya.interfaces.IRecommendPresenter;
import com.sunofbeaches.himalaya.interfaces.IRecommendViewCallback;
import com.sunofbeaches.himalaya.utils.Constants;
import com.sunofbeaches.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG = "RecommendPresenter";

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private List<Album> mCurrentRecommend = null;

    private RecommendPresenter() {
    }

    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象
     *
     * @return
     */
    public static RecommendPresenter getInstance() {
        if(sInstance == null) {
            synchronized(RecommendPresenter.class) {
                if(sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    public List<Album> getCurrentRecommend() {
        return mCurrentRecommend;
    }

    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
        if(mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callback) {
        if(mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }

    @Override
    public void getRecommendList() {

        LogUtil.d(TAG,"getRecommendList -- > from network..");
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_DEFAULT + "");
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(@Nullable GussLikeAlbumList gussLikeAlbumList) {
                LogUtil.d(TAG,"Thread name --> " + Thread.currentThread().getName());
                if(gussLikeAlbumList != null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    //返回到UI
                    handleResult(albumList);
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG,"error -->" + i);
                LogUtil.d(TAG,"erroMsg -->" + s);
            }
        });

    }

    private void handleResult(List<Album> albumList) {
        if(mCallbacks!=null){
            for(IRecommendViewCallback callback : mCallbacks) {
                callback.onRecommendListLoaded(albumList);
            }
        }
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }



}
