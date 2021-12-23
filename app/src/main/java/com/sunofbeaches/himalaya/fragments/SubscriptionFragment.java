package com.sunofbeaches.himalaya.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunofbeaches.himalaya.R;
import com.sunofbeaches.himalaya.base.BaseFragment;

public class SubscriptionFragment extends BaseFragment {
    private View mRootView;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mRootView = layoutInflater.inflate(R.layout.fragment_subscription, container, false);
        return mRootView;
    }
}
