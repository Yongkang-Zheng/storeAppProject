package com.example.storeapp.ui.main;

import android.content.ContentValues;
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
import com.example.storeapp.Model;
import com.example.storeapp.R;
import com.example.storeapp.StoreDbHelper;
import com.example.storeapp.StoreEntry;
import com.example.storeapp.StoresAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StoresFragment extends Fragment {
    private static String TAG = "StoresFragment";

    List<Model> mStoreList = new ArrayList<>();
    RecyclerView mRecyclerView;
    private StoresAdapter mStoreAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public interface OnAddOrRemovedListener {
        public void OnAddOrRemoved(Model model);
    }

    OnAddOrRemovedListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnAddOrRemovedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnAddOrRemovedListener");
        }
    }

    public static StoresFragment newInstance(int index) {
        StoresFragment fragment = new StoresFragment();
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
                initStoresData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        initStoresData();

        return root;
    }

    public void initStoresData(){
        sendRequestWithHttpURLConnection();
    }

    public void sendRequestWithHttpURLConnection() {
        Log.d(TAG,"sendRequestWithHttpURLConnection");
        mStoreList.clear();
        StringBuilder sb =  new StringBuilder();
        new Thread(new Runnable() {
            //  @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(" https://mopjapaneastgateway.plexure.io/store/v2/stores?latitude=26.333351598841787&longitude=127.79896146273005&radius=100000000&size=100");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader bin =  new BufferedReader(new InputStreamReader(in));
                        String inputLine;
                        while ((inputLine = bin.readLine()) != null){
                            sb.append(inputLine);
                        }

                        try {
                            JSONArray jsonArray = new JSONArray(sb.toString());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String id = jsonArray.getJSONObject(i).getString("id");
                                String name = jsonArray.getJSONObject(i).getString("name");
                                String address = jsonArray.getJSONObject(i).getString("address");
                                String distance = jsonArray.getJSONObject(i).getString("distance");

                                //List<String> featureList = new ArrayList<String>();
                                StringBuilder featureList =  new StringBuilder();
                                JSONArray featureListArray = new JSONArray(jsonArray.getJSONObject(i).getString("featureList"));
                                for (int j = 0; j < featureListArray.length(); j++) {
                                    //featureList.add(featureListArray.getString(j));
                                    featureList.append(featureListArray.getString(j));
                                    if (j < featureListArray.length() - 1) {
                                        featureList.append(',');
                                        featureList.append(' ');
                                    }
                                }
                                String str = featureList.toString();
                                if (str.equals("")) {
                                    str = "NONE";
                                }

                                String toggleButton = "ADDED";

                                boolean flag = querySavedState(id);
                                if (!flag) {
                                    toggleButton = "ADDED";
                                } else {
                                    toggleButton = "REMOVED";
                                }

                                // Log.d(TAG, "id=" + id + " name=" + name + " address=" + address + " distance=" + distance + " featureList=" + featureList.toString());

                                Model model = new Model();
                                model.setId(id);
                                model.setName(name);
                                model.setAddress(address);
                                model.setDistance(distance);
                                model.setFeatureList(str);
                                model.setToggleButton(toggleButton);

                                mStoreList.add(model);
                            }

                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //return sb.toString();
                        //readJsonStream(in);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                //return null;
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
                case 1:
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mStoreAdapter = new StoresAdapter( R.layout.list_item, mStoreList);
                    mStoreAdapter.openLoadAnimation();
                    mRecyclerView.setAdapter(mStoreAdapter);

                    //条目子控件点击事件
                    mStoreAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                        @Override
                        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                            switch (view.getId()){
                                case R.id.toggleButton:
                                    Model model = new Model();
                                    model = mStoreList.get(position);

                                    final boolean flag;
                                    flag = querySavedState(model.getId());
                                    // flag == flase no selected
                                    // flag == true selected
                                    if (!flag) {
                                        Toast.makeText(getActivity(), "added to favourite list", Toast.LENGTH_SHORT).show();
                                        view.setSelected(true);
                                        setDataToDatabase(model);
                                    } else {
                                        Toast.makeText(getActivity(), "removed from favourite list", Toast.LENGTH_SHORT).show();
                                        view.setSelected(false);
                                        deleteRecord(model);
                                    }
                                    listener.OnAddOrRemoved(model);
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

    private boolean querySavedState(String id) {
        StoreDbHelper mDbHelper = new StoreDbHelper(getActivity());
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(StoreEntry.TABLE_NAME, new String[] {"name"}, "uid=" + id, null, null, null, null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Log.d(TAG,"id:" + id + "have added to favourite list return false");
                    return true;
                }
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
            db.close();
        }
        //Log.d(TAG,"id:" + id + "have not added to favourite list return true");
        return false;
    }

    private void setDataToDatabase(Model model){
        Log.d(TAG,"setDataToDatabase");
        new Thread(new Runnable() {
            //  @Override
            public void run() {
                StoreDbHelper mDbHelper = new StoreDbHelper(getActivity());

                // Create and/or open a database to read from it
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(StoreEntry.UID, model.getId());
                values.put(StoreEntry.NAME, model.getName());
                values.put(StoreEntry.ADDRESS,model.getAddress());
                values.put(StoreEntry.DISTANCE,model.getDistance());
                values.put(StoreEntry.FEATURELIST,model.getFeatureList());

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(StoreEntry.TABLE_NAME, null, values);

                if (db != null) {
                    db.close();
                }
                Log.d(TAG, "inset id="+newRowId + " name=" + model.getName());
            }
        }).start();

    }

    void deleteRecord (Model model) {
        new Thread(new Runnable() {
            //  @Override
            public void run() {
                StoreDbHelper mDbHelper = new StoreDbHelper(getActivity());
                // Create and/or open a database to read from it
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                long id = db.delete(StoreEntry.TABLE_NAME, "uid=" + model.getId(), null);

                if (db != null) {
                    db.close();
                }

                Log.d(TAG,"delete id="+id + " name=" + model.getName());
            }
        }).start();

    }
}
