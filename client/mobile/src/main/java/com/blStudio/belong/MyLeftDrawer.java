/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blStudio.belong;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class MyLeftDrawer extends RecyclerView.Adapter<MyLeftDrawer.ViewHolder>{
    private String[] mDataSet;
    private OnItemClickListener mListener;
    // navigation drawer相关
    private static DrawerLayout mDrawerLayout;
    public static RecyclerView mDrawerList;
    private static ActionBarDrawerToggle mDrawerToggle;
    private static CharSequence mDrawerTitle;
    private static CharSequence mTitle;
    public static String[] mDrawerTitles;
    private static Activity mApp;

    public static void init(Activity activity){
        mApp = activity;
        mTitle = mApp.getResources().getString(R.string.app_name);
        mDrawerTitle = mApp.getResources().getString(R.string.drawer_title);
        mDrawerTitles = mApp.getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) mApp.findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) mApp.findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setLayoutManager(new LinearLayoutManager(mApp));

        mDrawerToggle = new ActionBarDrawerToggle(
                mApp,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                mApp.getActionBar().setTitle(mTitle);
                mApp.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                mApp.getActionBar().setTitle(mDrawerTitle);
                mApp.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public static void selectItem(int position) {
        //应用内配置语言
        Resources resources =mApp.getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。

        switch(position){
            case 0:
                config.locale = Locale.ENGLISH;
                break;
            case 1:
                config.locale = Locale.CHINA;
                break;
            case 2:
                config.locale = Locale.TAIWAN;
                break;
            case 3:
                config.locale = Locale.JAPANESE;
                break;
            default:break;
        }
        resources.updateConfiguration(config, dm);
        mApp.finish();
        mApp.startActivity(mApp.getIntent());
        SharedPreferences sharedPreferences=mApp.getSharedPreferences("setting", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("locale", config.locale.toString()).commit();
    };

    // Interface for receiving click events from cells.
    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    // Custom viewHolder for our planet views.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    public MyLeftDrawer(String[] myDataset, OnItemClickListener listener) {
        mDataSet = myDataset;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.drawer_list_item, parent, false);
        TextView tv = (TextView) v.findViewById(android.R.id.text1);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(mDataSet[position]);
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

    public static void onConfigurationChanged(Configuration newConfig) {
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public static boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawerList);
    }

    public static boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    public static void syncState(){
        mDrawerToggle.syncState();
    }
}
