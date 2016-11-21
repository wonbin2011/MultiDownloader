package com.wonbin.multidownloader.javabean;

/**
 * Created by wonbin on 11/18/16.
 */

public class Info {

    private int thid;
    private int done;
    private String path;

    public Info(int thid, int done, String path) {
        this.thid = thid;
        this.done = done;
        this.path = path;
    }

    public int getThid() {
        return thid;
    }

    public int getDone() {
        return done;
    }

    public String getPath() {
        return path;
    }

    public void setThid(int thid) {
        this.thid = thid;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "##Info : " + "[" + "tid = " + thid + ", path = " + path +", done = "
                + done +"]";
    }

}
