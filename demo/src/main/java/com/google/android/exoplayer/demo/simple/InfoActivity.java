package com.google.android.exoplayer.demo.simple;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.exoplayer.LoggerSingleton;
import com.google.android.exoplayer.demo.R;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        EditText t = (EditText) findViewById(R.id.textInfoWindow);
        t.setText("Media codec information:\n"+LoggerSingleton.getInstance().videoCodec
                +" "+LoggerSingleton.getInstance().audioCodec
        +"\n\nAvailable video formats:\n");
        for(int i=0;i<LoggerSingleton.getInstance().availableFormats.length;i++)
            t.append(LoggerSingleton.getInstance().availableFormats[i]);
        t.append(
        "\nIdeal format:\n"+LoggerSingleton.getInstance().idealFormat+
        "\n\nCurrent format:\n"+LoggerSingleton.getInstance().currentFormat+
        "\n\nMinimum buffer time:\n"+LoggerSingleton.getInstance().minBufferTime);
    }
}
