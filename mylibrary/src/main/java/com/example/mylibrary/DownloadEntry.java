package com.example.mylibrary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class DownloadEntry implements Serializable {
    public String id;
    public String name;
    public String url;
    public String filename;
    public boolean enableNotifyDataChange = true;





    public enum DownlaodStatus{idle,waiting,downloading,pause
        ,resume,cancel,completed,connecting,error,updating};

    public DownlaodStatus status = DownlaodStatus.idle;

    public volatile int currentLength;
    public int totalLenth;

    public boolean isSuportRange;

    public volatile HashMap<Integer,Long> ranges;

    public int percent;



    public DownloadEntry(String id,String url,String filename) {
        this.id = id;
        this.url = url;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return
                "id='" + id  +
                ", status=" + status +
                ", fileName=" + filename +
                ", current=" + currentLength +
                ", total=" + totalLenth +
                '}';
    }

    //必须重写equals和hashcode()方法，不然在ListActivity的indexOf那里会得不到index
    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
