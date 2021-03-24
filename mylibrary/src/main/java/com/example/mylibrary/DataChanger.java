package com.example.mylibrary;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

public class DataChanger extends Observable {
    private static DataChanger mInstance;
    private LinkedHashMap<String,DownloadEntry> mOperatedEntries;
    private static Context context;
    private HashMap<String,DownloadEntry> cacheEntries;
    private ArrayList<DownloadEntry> downloadEntries = new ArrayList<>();

    private DataChanger(Context context){
        mOperatedEntries = new LinkedHashMap<>();
        this.context = context;
        cacheEntries = SharedPreferencesUtils.getHashMap(context,Constants.KEY_DOWNLOAD_CACHE_LIST);
        if(cacheEntries == null){
            cacheEntries = new HashMap<>();
        }

        for(Map.Entry<String,DownloadEntry> entry : cacheEntries.entrySet()){
            if(entry.getValue() != null){
                mOperatedEntries.put(entry.getValue().id,entry.getValue());
            }

        }
    }

    public synchronized static DataChanger getInstance(Context context){
        if(mInstance == null){
            mInstance = new DataChanger(context);
        }
        return mInstance;
    }
    public void saveEntries(DownloadEntry entry){
      //  setChanged();
      //  notifyObservers(entry);
        mOperatedEntries.put(entry.id,entry);
        try {
            cacheEntries.put(entry.id,entry);
            SharedPreferencesUtils.saveHashMap(context,cacheEntries,Constants.KEY_DOWNLOAD_CACHE_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveNeedCancelEntries(DownloadEntry entry){
        downloadEntries.add(entry);
        try {
            SharedPreferencesUtils.saveDownloadEntryList(context,Constants.KEY_DOWNLOAD_NEED_CANCEL_LIST,downloadEntries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public ArrayList<DownloadEntry> queryObserableEntries(){
        ArrayList<DownloadEntry> entries = null;
        for(Map.Entry<String,DownloadEntry> entry : mOperatedEntries.entrySet()){
            if(entry.getValue().status == DownloadEntry.DownlaodStatus.pause){
                if(entries == null){
                    entries = new ArrayList<>();
                }
                entries.add(entry.getValue()) ;
            }

        }

        return entries;
    }

    public ArrayList<DownloadEntry> queryNeedCancelEntries(){
        ArrayList<DownloadEntry> entries = null;

        entries = SharedPreferencesUtils.getCacheList(context,Constants.KEY_DOWNLOAD_NEED_CANCEL_LIST);
        return entries;
    }




    public DownloadEntry queryEntryById(String id){
        return mOperatedEntries.get(id);
    }

    public DownloadEntry queryEntryByCache(String id) {
        return SharedPreferencesUtils.getDownloadEntry(context,id);
    }

    public void addOperatedToMap(DownloadEntry entry) {
        mOperatedEntries.put(entry.id,entry);
    }
}
