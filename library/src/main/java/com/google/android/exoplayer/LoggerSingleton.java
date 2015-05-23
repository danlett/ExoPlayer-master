package com.google.android.exoplayer;

/**
 * Created by Dani on 2015.04.07..
 */
public class LoggerSingleton {
    public static LoggerSingleton instance = null;

    public String bufferLength;
    public String currentFormat;
    public String[] availableFormats;
    public String videoCodec;
    public String audioCodec;
    public String idealFormat;
    public String minBufferTime;
    public int forcedFormat=-1;
    public StringBuilder log = new StringBuilder();
    public String logFileName;

    protected LoggerSingleton(){}

    public static synchronized LoggerSingleton getInstance(){
        if(null == instance){
            instance = new LoggerSingleton();
        }
        return instance;
    }
}
