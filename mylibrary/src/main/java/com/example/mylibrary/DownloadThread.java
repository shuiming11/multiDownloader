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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
    int downlaodLenth = 0;

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
                    .url(url).
                     addHeader("Connection","close")
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
            FileChannel channelOut = accessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startPos, body.contentLength());
            byte[] buffer = new byte[4096];
            int len = 0;
            while (((startPos + downlaodLenth) != endPos) || ((len = inputStream.read(buffer)) != -1)) {
                if(isPaused || isCanceled){
                    break;
                }
                len = inputStream.read(buffer);
                mappedBuffer.put(buffer, 0, len);
                downloadListener.processChange(index,len);
                downlaodLenth = len;
            }



//            byte[] bytes = new byte[3076];
//            int len = 0;
//            try {
//                len = inputStream.read(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            while ( (len = inputStream.read(bytes)) != -1) {
//                if(isPaused){
//                    break;
//                }
//                accessFile.write(bytes, 0, len);
//                System.out.println("已下载字节：" + accessFile.length()+Thread.currentThread().getName());
//
//                downloadListener.processChange(index,len);
//
//            }
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
        isCanceled = true;
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
