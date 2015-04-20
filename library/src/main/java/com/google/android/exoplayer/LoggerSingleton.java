package com.google.android.exoplayer;

/**
 * Created by Dani on 2015.04.07..
 */
public class LoggerSingleton {
    public static LoggerSingleton mInstance= null;

    public String bufferLength;
    public String currentFormat;
    public String[] availableFormats;
    public String videoCodec;
    public String audioCodec;
    public String idealFormat;
    public String minBufferTime;
    public int forcedFormat=-1;

    protected LoggerSingleton(){}

    public static synchronized LoggerSingleton getInstance(){
        if(null == mInstance){
            mInstance = new LoggerSingleton();
        }
        return mInstance;
    }
}
