package cn.hzh.xlistview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.hzh.xlistview.view.XListView;

public class MainActivity extends AppCompatActivity
{
    private XListView mXListView;
    private BaseAdapter mAdapter;
    private LinkedList<String> mDatas = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initData();
        initView();
    }

    private void initData()
    {
        int i = 'A';
        for(; i <= 'Z'; i++)
        {
            mDatas.add((char)i + "");
        }
    }

    private void initView()
    {
        mXListView = (XListView) findViewById(R.id.id_listview);
        mXListView.enablePullRefresh(new XListView.XListViewRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new RefreshTask().execute();
            }
        });
        mXListView.startRefresh();

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, mDatas);

        mXListView.setAdapter(mAdapter);
    }

    public class RefreshTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            sleep(3000);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            mDatas.addFirst("data--1");
            mDatas.addFirst("data--2");
            mAdapter.notifyDataSetChanged();
            mXListView.stopRefresh();
        }
    }

    void sleep(long mills)
    {
        try
        {
            Thread.sleep(mills);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
