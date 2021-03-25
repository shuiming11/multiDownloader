package com.example.mylibrary;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;


public class DownloadTask implements ConnectThread.OnConnectListener, DownloadThread.DownloadListener {
    private final Handler mHandler;
    private final ExecutorService executorService;
    private final Context cotext;
    private volatile DownloadEntry entry;
    private boolean isPause;
    private boolean isCancel;
    private ConnectThread connectThread;
    private ArrayList<DownloadThread> mDowmloadingTasks = new ArrayList<>();
    long lastTime = 0l;
  //  private DownloadThread downloadThread;

    public DownloadTask(DownloadEntry entry, Handler handler, ExecutorService executorService, Context context){
        this.entry = entry;
        this.mHandler = handler;
        this.executorService = executorService;
        this.cotext = context;

    }
    public void pause() {
        isPause = true;
        if(connectThread != null && connectThread.isRunning()){
            connectThread.cancel();
        }
        for (int i = 0; i < mDowmloadingTasks.size(); i++) {
            mDowmloadingTasks.get(i).pause();
            System.out.println("mDowmloadingTasks.size-------"+mDowmloadingTasks.size());
        }

        entry.status = DownloadEntry.DownlaodStatus.pause;
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);

       // downloadThread.pause();
    }

    public void cancel() {
        isCancel = true;
        for (int i = 0; i < mDowmloadingTasks.size(); i++) {
            mDowmloadingTasks.get(i).cancel();
            System.out.println("mDowmloadingTasks.size-------"+mDowmloadingTasks.size());
        }

        entry.status = DownloadEntry.DownlaodStatus.cancel;
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);
    }

    public void start() {
        connectThread = new ConnectThread(entry.url,this);
        executorService.execute(connectThread);
        entry.status = DownloadEntry.DownlaodStatus.downloading;
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);
        DataChanger.getInstance(cotext).saveEntries(entry);
        //DataChanger.getInstance().postStatus(entry);
       // DataChanger.getInstance().postStatus(entry);
    }


    @Override
    public void onConnect(boolean isSupportRange, int totalLength) {
        entry.isSuportRange = isSupportRange;
        entry.totalLenth = totalLength;
        if(entry.isSuportRange){
            startMultiThreadDownload();
        }else {
            startSingleThreadDownload();
        }

    }

    private void startMultiThreadDownload() {
        entry.status = DownloadEntry.DownlaodStatus.downloading;
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);
        int block = entry.totalLenth / Constants.DOWNLOAD_MAX_THREAD;
        int startPos = 0;
        int endPos = 0;
        if(entry.ranges == null){
            entry.ranges = new HashMap<>();
            for (int i = 0; i < Constants.DOWNLOAD_MAX_THREAD; i++) {
                entry.ranges.put(i,0l);
            }


        }
        for (int i = 0; i < Constants.DOWNLOAD_MAX_THREAD; i++) {
            startPos = (int) (i * block + entry.ranges.get(i));
            if(i == Constants.DOWNLOAD_MAX_THREAD - 1){
                endPos = entry.totalLenth ;
                System.out.println("start------->"+startPos);

            }else {
                endPos = (i +1 ) * block-1;
                System.out.println("start------->"+startPos);
            }
            if(startPos < endPos){
                DownloadThread downloadThread = new DownloadThread(entry.url,startPos,endPos,this,i,entry.filename);
                executorService.execute(downloadThread);
                mDowmloadingTasks.add(downloadThread);
            }

        }
    }

    private void startSingleThreadDownload() {
    }

    @Override
    public void onError(String message) {
        entry.status = DownloadEntry.DownlaodStatus.error;

        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);
    }

    @Override
    public synchronized void processChange(int index,long process) {
        //int range = entry.ranges.get(index)+process;
        entry.ranges.put(index,entry.ranges.get(index)+process);
        entry.currentLength += process;
        int percent = (int) (entry.currentLength *100l / entry.totalLenth);
        entry.percent = percent;
        //entry.status = DownloadEntry.DownlaodStatus.downloading;
        if(entry.currentLength >= entry.totalLenth){
            entry.percent = 100;
            entry.status = DownloadEntry.DownlaodStatus.completed;
             Message msg = mHandler.obtainMessage();
             msg.obj = entry;
             mHandler.sendMessage(msg);
        }else {
            if(System.currentTimeMillis() - lastTime >1000){
                EventBus.getDefault().post(entry);
                lastTime = System.currentTimeMillis();
            }
        }


      //  mHandler.postDelayed()
       // if(process * 100 / entry.totalLenth>1)


        DataChanger.getInstance(cotext).saveEntries(entry);
       // Message msg = mHandler.obtainMessage();

//        msg.obj = entry;
//       mHandler.sendMessage(msg);


    }

    @Override
    public void onDownloadComplete(int index) {

    }





    @Override
    public void onDownloadError(String message) {
        entry.status = DownloadEntry.DownlaodStatus.error;
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        mHandler.sendMessage(msg);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

        }
    };
}
