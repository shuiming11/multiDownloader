package com.example.mylibrary;

import android.os.Environment;
import android.webkit.DownloadListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadThread implements Runnable {

    private final String url;
    private final int startPos;
    private final int endPos;
    private final DownloadListener downloadListener;
    private final int index;
    private String path;
    private boolean isCanceled;
    private volatile boolean isPaused;
    private String fileName;

    public DownloadThread(String url,int startPos, int endPos,DownloadListener downloadListener, int index,String fileName) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.path = Environment.getExternalStorageDirectory() + File.separator+"haha" +
                File.separator + "abc.apk";
        this.downloadListener = downloadListener;
        this.index = index;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        InputStream is=null;
        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory, fileName);
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "rw");
            String range = String.format(Locale.CHINESE, "bytes=%d-%d", startPos,endPos);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .header("range", range)
                    .build();
            // 使用OkHttp请求服务器
            Call call = client.newCall(request);
            Response response = null;
            response = call.execute();
            // 连接服务器成功
            ResponseBody body = response.body();
            System.out.println("文件大小：" + body.contentLength());
            accessFile.seek(startPos);
            InputStream inputStream = body.byteStream();
            byte[] bytes = new byte[2048];
            int len = 0;
            try {
                len = inputStream.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (len != -1) {
                if(isPaused){
                    break;
                }
                accessFile.write(bytes, 0, len);
                System.out.println("已下载字节：" + file.length()+Thread.currentThread().getName());
                downloadListener.processChange(index,len);
                len = inputStream.read(bytes);
            }
            System.out.println("文件下载完毕：");
        } catch (Exception e) {
            e.printStackTrace();
        }


        
    }

    public void pause() {
        isPaused = true;
        System.out.println("暂停啦-------");
        Thread.currentThread().interrupt();
    }


    public void cancel() {
        isPaused = true;
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
            System.out.println("已删除");
        }
        Thread.currentThread().interrupt();
    }

    interface DownloadListener{
        void processChange(int index,long process);
        void onDownloadComplete(int index);
        void onDownloadError(String message);
    }

}
