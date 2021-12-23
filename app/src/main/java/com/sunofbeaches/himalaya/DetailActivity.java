package com.sunofbeaches.himalaya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.sunofbeaches.himalaya.adapters.TrackListAdapter;
import com.sunofbeaches.himalaya.base.BaseActivity;
import com.sunofbeaches.himalaya.interfaces.IAlbumDetailViewCallback;
import com.sunofbeaches.himalaya.presenters.AlbumDetailPresenter;
import com.sunofbeaches.himalaya.presenters.PlayerPresenter;
import com.sunofbeaches.himalaya.presenters.RecommendPresenter;
import com.sunofbeaches.himalaya.utils.ImageBlur;
import com.sunofbeaches.himalaya.utils.LogUtil;
import com.sunofbeaches.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, TrackListAdapter.ItemClickListener, UILoader.OnRetryClickListener{
    private static final String TAG = "DetailActivity";
    private long mCurrentId = -1;
    private int mCurrentPage = 1;
    private UILoader mUiLoader;
    private Album mCurrentAlbum;
    private ImageView mLargeCover;
    private ImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private FrameLayout mDetailListContainer;
    private List<Track> mCurrentTracks = null;

    private AlbumDetailPresenter mAlbumDetailPresenter ;
    private RecyclerView mDetailList;
    private TrackListAdapter mDetailListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        initView();
        initPresenter();
    }

    private void initPresenter() {
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);
    }

    private void initView() {

        mDetailListContainer = this.findViewById(R.id.detail_list_container);
        //
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(DetailActivity.this);
        }
        mLargeCover = this.findViewById(R.id.iv_large_cover);
        mSmallCover = this.findViewById(R.id.viv_small_cover);
        mAlbumTitle = this.findViewById(R.id.tv_album_title);
        mAlbumAuthor = this.findViewById(R.id.tv_album_author);

    }

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        //RecyclerView的使用步骤
        //第一步:设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(layoutManager);
        //第二步:设置适配器
        mDetailListAdapter = new TrackListAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        //设置item的上下间距
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 2);
                outRect.right = UIUtil.dip2px(view.getContext(), 2);
            }
        });

        mDetailListAdapter.setItemClickListener(this);

        return detailListView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {

    }

    @Override
    public void onNetworkError() {

    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onAlbumLoaded(Album album) {
        this.mCurrentAlbum = album;
        long id = album.getId();

        LogUtil.d(TAG, "album -- > " + id);
        mCurrentId = id;

        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) id, mCurrentPage);
        }
        //拿数据，显示Loading状态
//        if (mUiLoader != null) {
//            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
//        }
        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }

        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }

        if (mSmallCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }

        //做毛玻璃效果
        if (mLargeCover != null && null != mLargeCover) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargeCover.getDrawable();
                    if (drawable != null) {
                        //到这里才说明是有图片的
                        ImageBlur.makeBlur(mLargeCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError");
                }
            });

        }
    }

    @Override
    public void onDetailListLoaded(List<Track> tracks) {
        this.mCurrentTracks = tracks;
        //判断数据结果，根据结果控制UI显示
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }

        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //更新/设置UI数据
        mDetailListAdapter.setData(tracks);
    }

    //track detail list click
    @Override
    public void onItemClick(List<Track> detailData, int position) {
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData, position);
        //跳转到播放器界面
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    //network connection goes wrong and retry
    @Override
    public void onRetryClick() {

    }
}
