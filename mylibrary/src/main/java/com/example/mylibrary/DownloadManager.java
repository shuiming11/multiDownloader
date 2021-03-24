package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;

import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_ADD;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_CANCEL;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_CANCEL_ALL;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_PAUSE;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_PAUSE_ALL;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_RESUME;
import static com.example.mylibrary.Constants.KEY_DOWNMLOAD_ACTION_RESUME_ALL;


public class DownloadManager {
    public static DownloadManager mInstance;
    private final Context context;

    private DownloadManager(Context context){
        this.context = context;
    }

    public static synchronized DownloadManager getInstance(Context context){
        if(mInstance == null){
            mInstance = new DownloadManager(context);
        }

        return mInstance;
    }

    public void add( DownloadEntry downloadEntry){
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ENTRY,downloadEntry);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_ADD);
        context.startService(intent);
    }

    public void pause(DownloadEntry downloadEntry){
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ENTRY,downloadEntry);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    public void resume(DownloadEntry downloadEntry){
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ENTRY,downloadEntry);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_RESUME);
        context.startService(intent);

    }

    public void cancel(DownloadEntry downloadEntry){
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ENTRY,downloadEntry);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_CANCEL);
        context.startService(intent);

    }

    public void pauseAll() {
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void cancelAll() {
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_CANCEL_ALL);
        context.startService(intent);
    }

    public void resumeAll() {
        Intent intent = new Intent(context,DownlaodService.class);
        intent.putExtra(Constants.KEY_DOWNMLOAD_ACTION,KEY_DOWNMLOAD_ACTION_RESUME_ALL);
        context.startService(intent);
    }

    public DownloadEntry queryEntryById(String id) {
        return DataChanger.getInstance(context).queryEntryById(id);
    }


    public void saveNeedCancelEntries(DownloadEntry downloadEntry) {
        DataChanger.getInstance(context).saveNeedCancelEntries(downloadEntry);
    }
}
