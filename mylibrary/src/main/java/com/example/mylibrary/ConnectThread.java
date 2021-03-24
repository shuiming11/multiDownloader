package com.example.mylibrary;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

public class ConnectThread implements Runnable{
    private final OnConnectListener listener;
    private String url;
    private volatile boolean isRunning;


    public ConnectThread(String url,OnConnectListener onConnectListener) {
        this.listener = onConnectListener;
        this.url = url;
    }
    @Override
    public void run() {
        isRunning = true;

        String url = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
        okhttp3.Request request = new okhttp3.Request.
                Builder().
                header("range", "bytes = 0-"+100).
                url(url).
                get().build();

        OkHttpClient okHttpClient = new OkHttpClient();

        final Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isRunning = false;
                listener.onError(e.getMessage());
                e.printStackTrace();
                Log.d("TAG", "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                int responseCode = response.code();
                int totalLength = (int) response.body().contentLength();
                System.out.println("------------>"+response.body().contentLength());
                boolean isSupportRange = false;
                if(responseCode == 200){
                    isSupportRange = true;
                }
                isRunning = false;
                listener.onConnect(isSupportRange,totalLength);
                Log.d("TAG", "response: " + response.toString());
            }
        });







//        HttpURLConnection connection = null;
//        try {
//            connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Range","bytes = 0-"+Integer.MAX_VALUE);
//            connection.setReadTimeout(600000);
//            int responseCode = connection.getResponseCode();
//            int totalLength = connection.getContentLength();
//            boolean isSupportRange = false;
//            if(responseCode == HttpsURLConnection.HTTP_PARTIAL){
//                isSupportRange = true;
//            }
//            isRunning = false;
//            listener.onConnect(isSupportRange,totalLength);
//        } catch (IOException e) {
//            isRunning = false;
//            listener.onError(e.getMessage());
//            e.printStackTrace();
//        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    interface OnConnectListener{
        void onConnect(boolean isSupportRange,int totalLength);
        void onError(String message);
    }
}
