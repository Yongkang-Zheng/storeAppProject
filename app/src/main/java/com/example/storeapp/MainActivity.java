package com.example.storeapp;

import android.os.Bundle;

import com.example.storeapp.ui.main.FavouritsFragment;
import com.example.storeapp.ui.main.StoresFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.storeapp.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity
        implements StoresFragment.OnAddOrRemovedListener, FavouritsFragment.OnDeleteListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    public void OnAddOrRemoved(Model model) {
        String tag = "android:switcher:" + R.id.view_pager + ":" + 1;
        FavouritsFragment favouritsFragment = (FavouritsFragment) getSupportFragmentManager().findFragmentByTag(tag);
        favouritsFragment.initFavouritesData();
    }

    public void OnDelete(Model model) {
        String tag = "android:switcher:" + R.id.view_pager + ":" + 0;
        StoresFragment storesFragment = (StoresFragment) getSupportFragmentManager().findFragmentByTag(tag);
        storesFragment.initStoresData();
    }
}