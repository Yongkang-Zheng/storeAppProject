package com.example.storeapp.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.storeapp.FavouritesAdapter;
import com.example.storeapp.Model;
import com.example.storeapp.R;
import com.example.storeapp.StoreDbHelper;
import com.example.storeapp.StoreEntry;

import java.util.ArrayList;
import java.util.List;

public class FavouritsFragment extends Fragment {
    private static String TAG = "FavouritsFragment";

    List<Model> mFavouriteList = new ArrayList<>();
    RecyclerView mRecyclerView;
    private FavouritesAdapter mFavouriteAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public interface OnDeleteListener {
        public void OnDelete(Model model);
    }

    OnDeleteListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnDeleteListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDeleteListener");
        }
    }


    public static FavouritsFragment newInstance(int index) {
        FavouritsFragment fragment = new FavouritsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = root.findViewById(R.id.mRecyclerView);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下载数据
                initFavouritesData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        initFavouritesData();

        return root;
    }

    public void initFavouritesData(){
        queryFromDatabse();
    }

    public void queryFromDatabse() {
        Log.d(TAG,"queryFromDatabse");
        mFavouriteList.clear();
        new Thread(new Runnable() {
            //  @Override
            public void run() {
                StoreDbHelper mDbHelper = new StoreDbHelper(getActivity());
                // Create and/or open a database to read from it
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                // Perform this raw SQL query "SELECT * FROM favourites"
                // to get a Cursor that contains all rows from the favourites table.
                Cursor cursor = db.rawQuery("SELECT * FROM " + StoreEntry.TABLE_NAME, null);
                try {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            Model model = new Model();
                            model.setId(cursor.getString(1));
                            model.setName(cursor.getString(2));
                            model.setAddress(cursor.getString(3));
                            model.setDistance(cursor.getString(6));
                            model.setFeatureList(cursor.getString(7));

                            mFavouriteList.add(model);
                        }
                    }
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                } finally {
                    // Always close the cursor when you're done reading from it. This releases all its
                    // resources and makes it invalid.
                    cursor.close();
                    db.close();
                }
            }
        }).start();

    }

    //AsyncTask aTask;
    private Handler mHandler;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 2:
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mFavouriteAdapter = new FavouritesAdapter(R.layout.favourite_list_item, mFavouriteList);
                    mFavouriteAdapter.openLoadAnimation();
                    mRecyclerView.setAdapter(mFavouriteAdapter);

                    //item children click event
                    mFavouriteAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                        @Override
                        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                            switch (view.getId()){
                                case R.id.button:
                                    Toast.makeText(getActivity(), "removed from favourite list", Toast.LENGTH_SHORT).show();
                                    Model model = new Model();
                                    model = mFavouriteList.get(position);
                                    deleteRecord(model);

                                    mFavouriteList.remove(position);
                                    mFavouriteAdapter.notifyItemRemoved(position);

                                    listener.OnDelete(model);

                                    break;
                                default:
                                    Toast.makeText(getActivity(),"你点击了item"+(position+1),Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                    break;
            }
        }
    };

    void deleteRecord (Model model) {
        new Thread(new Runnable() {
            //  @Override
            public void run() {
                StoreDbHelper mDbHelper = new StoreDbHelper(getActivity());
                // Create and/or open a database to read from it
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                long id = db.delete(StoreEntry.TABLE_NAME, "uid=" + model.getId(), null);

                Log.d(TAG,"delete id="+id + " name=" + model.getName());

                if (db != null) {
                    db.close();
                }
            }
        }).start();

    }
}
