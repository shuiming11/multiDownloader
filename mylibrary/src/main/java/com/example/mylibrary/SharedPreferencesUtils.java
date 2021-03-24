package com.example.mylibrary;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesUtils {
    private static String name = "download";


    public static void saveDownloadEntry(Context ctx, String key,DownloadEntry bookList) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("config", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bookList);
        editor.putString(key, json);
        editor.commit();
    }

    public static void saveDownloadEntryList(Context ctx, String key,ArrayList<DownloadEntry> downloadEntries) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("config", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(downloadEntries);
        editor.putString(key, json);
        editor.commit();
    }




    public static DownloadEntry getDownloadEntry(Context ctx,String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("config", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);
        Type type = new TypeToken<DownloadEntry>() {
        }.getType();
        DownloadEntry entry = gson.fromJson(json, type);
        return entry;
    }


    public static ArrayList<DownloadEntry> getCacheList(Context ctx,String key) {

        SharedPreferences sharedPreferences = ctx.getSharedPreferences("config", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<DownloadEntry>>() {
        }.getType();
        ArrayList<DownloadEntry> entries = gson.fromJson(json, type);
        return entries;
    }

    //将 HashMap 用sp存储起来
    public static void saveHashMap(Context context,HashMap<String,DownloadEntry> map,String key){

        Gson gson = new Gson();
        String json = gson.toJson(map);

        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= context.getSharedPreferences("config", Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //步骤3：将获取过来的值放入文件
        editor.putString(key,json);
        editor.commit();


    }



    //将json格式的字符串从sp中取出来转化为hashMap
    public static HashMap<String,DownloadEntry> getHashMap(Context context,String key){

        SharedPreferences sharedPreferences= context.getSharedPreferences("config", Context .MODE_PRIVATE);
        String json =sharedPreferences.getString(key,"");
        HashMap<String,DownloadEntry> map;

        Type type = new TypeToken<HashMap<String, DownloadEntry>>(){}.getType();
        Gson gson = new Gson();
        map = gson.fromJson(json, type);

        return map;
    }




} 
