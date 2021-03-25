package com.example.multitaskdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylibrary.DownloadEntry;
import com.example.mylibrary.DownloadManager;
import com.example.mylibrary.Trace;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ListActivity extends AppCompatActivity {
    private ListView mListView;
    private DownloadManager mDownloadManager;
    private Button btn_pause_resume_all;
    private List<DownloadEntry> mDownloadEntrys = new ArrayList<>();
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private DownloadAdapter mDownloadAdapter;
    private Button btn_cancel_all;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        EventBus.getDefault().register(this);
        verifyStoragePermissions(this);
        mListView = findViewById(R.id.listview);
        btn_cancel_all = findViewById(R.id.btn_cancel_all);
        btn_pause_resume_all = findViewById(R.id.btn_pause_resume_all);
        DownloadEntry downloadEntry1 = new DownloadEntry("1","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190714.apk");
        DownloadEntry downloadEntry2 = new DownloadEntry("2","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190715.apk");
        DownloadEntry downloadEntry3 = new DownloadEntry("3","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190716.apk");
        DownloadEntry downloadEntry4 = new DownloadEntry("4","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190717.apk");
        DownloadEntry downloadEntry5 = new DownloadEntry("5","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190718.apk");
        DownloadEntry downloadEntry6 = new DownloadEntry("6","http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk","20190719.apk");


        mDownloadEntrys.add(downloadEntry1);
        mDownloadEntrys.add(downloadEntry2);
        mDownloadEntrys.add(downloadEntry3);
        mDownloadEntrys.add(downloadEntry4);
        mDownloadEntrys.add(downloadEntry5);
        mDownloadEntrys.add(downloadEntry6);


        mDownloadManager = DownloadManager.getInstance(getApplicationContext());
        mDownloadManager.saveNeedCancelEntries(downloadEntry1);
        mDownloadManager.saveNeedCancelEntries(downloadEntry2);
        mDownloadManager.saveNeedCancelEntries(downloadEntry3);
        mDownloadManager.saveNeedCancelEntries(downloadEntry4);
        mDownloadManager.saveNeedCancelEntries(downloadEntry5);
        mDownloadManager.saveNeedCancelEntries(downloadEntry6);
        DownloadEntry downloadEntry = null;
        DownloadEntry realEntry = null;
        int len = mDownloadEntrys.size();
        for (int i = 0; i < len; i++) {
            downloadEntry = mDownloadEntrys.get(i);
            realEntry = mDownloadManager.queryEntryById(downloadEntry.id);
            if(realEntry != null){
                mDownloadEntrys.remove(i);
                mDownloadEntrys.add(i,realEntry);
            }
        }
//        mDownloadManager.addOberser(mDataWatcher);
        mDownloadAdapter = new DownloadAdapter();
        mListView.setAdapter(mDownloadAdapter);
        btn_pause_resume_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_pause_resume_all.getText().equals("PAUSEALL")){
                    mDownloadManager.pauseAll();
                    btn_pause_resume_all.setText("resume_all");

                }else {
                    btn_pause_resume_all.setText("PAUSEALL");
                    mDownloadManager.resumeAll();
                }
            }
        });

        btn_cancel_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.cancelAll();
            }
        });
    }


     class DownloadAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDownloadEntrys == null?0:mDownloadEntrys.size();
        }

        @Override
        public Object getItem(int position) {
            return mDownloadEntrys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             final ViewHolder viewHolder;
            if(convertView ==null ){
                convertView = LayoutInflater.from(ListActivity.this)
                        .inflate(R.layout.item_list,null);
                viewHolder = new ViewHolder();
                viewHolder.mDownloanBtn = convertView.findViewById(R.id.btn_download);
                viewHolder.mTvLabel = convertView.findViewById(R.id.tvLabel);
                viewHolder.mBtnCancel = convertView.findViewById(R.id.btn_cancel);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final DownloadEntry entry = mDownloadEntrys.get(position);
            viewHolder.mTvLabel.setText(entry.toString());
            if(entry.status == DownloadEntry.DownlaodStatus.pause){
                viewHolder.mDownloanBtn.setText("已暂停");
            }else if(entry.status == DownloadEntry.DownlaodStatus.idle){
                viewHolder.mDownloanBtn.setText("下载");
            }else if(entry.status == DownloadEntry.DownlaodStatus.downloading){
                viewHolder.mDownloanBtn.setText("下载中");
            }else if(entry.status == DownloadEntry.DownlaodStatus.waiting){
                viewHolder.mDownloanBtn.setText("等待");
            }else if(entry.status == DownloadEntry.DownlaodStatus.completed){
                viewHolder.mDownloanBtn.setText("已完成");
            }
            viewHolder.mDownloanBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
                        if(entry.status == DownloadEntry.DownlaodStatus.idle || entry.status == DownloadEntry.DownlaodStatus.pause){
                            mDownloadManager.add(entry);
                            viewHolder.mDownloanBtn.setText("下载中");
                        }else if(entry.status == DownloadEntry.DownlaodStatus.pause){
                            mDownloadManager.resume(entry);
                        }else if(entry.status == DownloadEntry.DownlaodStatus.downloading || entry.status == DownloadEntry.DownlaodStatus.waiting){
                            mDownloadManager.pause(entry);
                            viewHolder.mDownloanBtn.setText("已暂停");

                        }
                    }
                    return false;
                }
            });

            viewHolder.mBtnCancel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getActionMasked()){
                        case MotionEvent.ACTION_DOWN:
                            mDownloadManager.cancel(entry);
                            break;
                    }
                    return false;
                }
            });
            return convertView;
        }
    }

    static class ViewHolder{
        TextView mTvLabel;
        Button mDownloanBtn;
        Button mBtnCancel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mDownloadManager!= null){
           // mDownloadManager.resumeAll();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDownloadManager.pauseAll();
    }



    public static void verifyStoragePermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity,"申请权限", Toast.LENGTH_SHORT).show();

            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(activity,new String[]{


                    Manifest.permission.WRITE_EXTERNAL_STORAGE,

                    Manifest.permission.READ_EXTERNAL_STORAGE},100);

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void chnage(DownloadEntry downloadEntry){
        int index = mDownloadEntrys.indexOf(downloadEntry);
        if(index != -1){
            mDownloadEntrys.remove(index);
            if(downloadEntry.status != DownloadEntry.DownlaodStatus.cancel){
                mDownloadEntrys.add(index,downloadEntry);
            }

            mDownloadAdapter.notifyDataSetChanged();
        }

        Trace.e(downloadEntry.toString());
    }
}
