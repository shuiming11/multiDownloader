package com.example.mylibrary;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.mylibrary.DownloadEntry.DownlaodStatus.cancel;
import static com.example.mylibrary.DownloadEntry.DownlaodStatus.completed;
import static com.example.mylibrary.DownloadEntry.DownlaodStatus.idle;
import static com.example.mylibrary.DownloadEntry.DownlaodStatus.pause;


public class DownlaodService extends Service {
    private HashMap<String,DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService mExecutorService;
    private LinkedBlockingDeque<DownloadEntry> mWaitingQuenue = new LinkedBlockingDeque<>();
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            DownloadEntry entry = (DownloadEntry) msg.obj;
            //todo:check if need to delete
            if(entry.status == completed){
                mDownloadingTasks.remove(entry.id);
            }
            switch (entry.status){
                case pause:
                case completed:
                case cancel:
                    checkNext();
                    break;
            }
            System.out.println("currentthrea-----"+Thread.currentThread().getName());
            EventBus.getDefault().post(entry);
            DataChanger.getInstance(getApplicationContext()).saveEntries(entry);
          //  DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    };

    private void checkNext() {
        DownloadEntry nextEntry = mWaitingQuenue.poll();
        //Toast.makeText(getApplicationContext(),entry.id+"",Toast.LENGTH_SHORT).show();
        if(nextEntry != null){
            addDowmload(nextEntry);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorService = Executors.newCachedThreadPool();
        HashMap<String,DownloadEntry> downloadEntryHashMap = SharedPreferencesUtils.
                getHashMap(getApplicationContext(),Constants.KEY_DOWNLOAD_CACHE_LIST);
        if(downloadEntryHashMap != null){
            for(Map.Entry<String,DownloadEntry> entry : downloadEntryHashMap.entrySet()){
                DataChanger.getInstance(getApplicationContext())
                        .addOperatedToMap(entry.getValue());

            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            DownloadEntry downloadEntry = (DownloadEntry) intent.
                    getSerializableExtra(Constants.KEY_DOWNMLOAD_ENTRY);
            int action = intent.getIntExtra(Constants.KEY_DOWNMLOAD_ACTION, -1);
            doAction(action, downloadEntry);
        }



        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        //check action,do related action
        switch (action) {
            case Constants.KEY_DOWNMLOAD_ACTION_ADD:
                addDowmload(entry);
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_CANCEL_ALL:
                cancelAll();
                break;
            case Constants.KEY_DOWNMLOAD_ACTION_RESUME_ALL:
                resumeAll();
                break;
                
        }

    }

    private void resumeAll() {
        ArrayList<DownloadEntry> needResumEntries = DataChanger.getInstance(getApplicationContext()).queryObserableEntries();
        if(needResumEntries != null){
            for(DownloadEntry downloadEntry:needResumEntries){
                addDowmload(downloadEntry);
            }
        }

    }

    private void pauseAll() {
        while (mWaitingQuenue.iterator().hasNext()){
            DownloadEntry downloadEntry = mWaitingQuenue.poll();
            downloadEntry.status = pause;
            EventBus.getDefault().post(downloadEntry);
            DataChanger.getInstance(getApplicationContext()).saveEntries(downloadEntry);
        }


        for(Map.Entry<String,DownloadTask> entry :mDownloadingTasks.entrySet()){
            entry.getValue().pause();
        }

        mDownloadingTasks.clear();
    }

    //todo:处理cancelAll
    private void cancelAll() {
        ArrayList<DownloadEntry> needCancelEntries = DataChanger.getInstance(getApplicationContext()).queryNeedCancelEntries();
        for (int i = 0; i < needCancelEntries.size(); i++) {
            needCancelEntries.get(i).status = cancel;
            EventBus.getDefault().post(needCancelEntries.get(i));
        }

        mDownloadingTasks.clear();
    }


    private void pauseDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if(downloadTask != null){
            downloadTask.pause();
        }else {
            mWaitingQuenue.remove(entry);
            entry.status =  DownloadEntry.DownlaodStatus.pause;
            Message msg = mHandler.obtainMessage();
            msg = mHandler.obtainMessage();
            msg.obj = entry;
            mHandler.sendMessage(msg);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        addDowmload(entry);
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if(downloadTask != null){
            downloadTask.cancel();
        }else {
            if(entry.status == pause || entry.status == idle ){
                entry.status = cancel;
                EventBus.getDefault().post(entry);
            }
            mWaitingQuenue.remove(entry);
        }


    }

    private void addDowmload(DownloadEntry downloadEntry){
        if(mDownloadingTasks.size() >= Constants.DOWNLOAD_MAX_COUNT){
            mWaitingQuenue.offer(downloadEntry);
            downloadEntry.status = DownloadEntry.DownlaodStatus.waiting;
            EventBus.getDefault().post(downloadEntry);
            //DataChanger.getInstance(getApplicationContext()).postStatus(downloadEntry);
        }else {
            startDownload(downloadEntry);
            downloadEntry.status = DownloadEntry.DownlaodStatus.downloading;
            EventBus.getDefault().post(downloadEntry);
           // DataChanger.getInstance(getApplicationContext()).postStatus(downloadEntry);
        }

        DataChanger.getInstance(getApplicationContext()).saveEntries(downloadEntry);

    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask downloadTask = new DownloadTask(entry,mHandler,mExecutorService,getApplicationContext());
        mDownloadingTasks.put(entry.id,downloadTask);
        downloadTask.start();
    }
}
