package com.example.storeapp;

import android.util.Log;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class FavouritesAdapter extends BaseQuickAdapter<Model, BaseViewHolder> {
    private static String TAG = "FavouritesAdapter";

    public FavouritesAdapter(@LayoutRes int layoutResId, @Nullable List<Model> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Model item) {
        //可链式调用赋值
        helper.setText(R.id.name, item.getName())
                .setText(R.id.address, item.getAddress())
                .setText(R.id.distance, item.getDistance() + "km")
                .setText(R.id.featureList, "FEATURES: " + item.getFeatureList())
                .addOnClickListener(R.id.button);

        // Log.d(TAG,"name:"+item.getName());

        //获取当前条目position
        int position = helper.getLayoutPosition();
    }
}
