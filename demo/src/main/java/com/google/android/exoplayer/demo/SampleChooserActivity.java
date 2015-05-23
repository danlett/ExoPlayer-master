/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import com.google.android.exoplayer.demo.Samples.Sample;
import com.google.android.exoplayer.demo.full.FullPlayerActivity;
import com.google.android.exoplayer.demo.simple.SimplePlayerActivity;
import com.google.android.exoplayer.util.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * An activity for selecting from a number of samples.
 */
public class SampleChooserActivity extends Activity {

  public static final int OPEN_DIALOG = 1;
    private static String PREF_RECENT_1 ="sharedpreferencesrecent1";
    private static String PREF_RECENT_2 ="sharedpreferencesrecent2";
    private static String PREF_RECENT_3 ="sharedpreferencesrecent3";
    private static String PREF_RECENT_4 ="sharedpreferencesrecent4";
    private static String PREF_RECENT_5 ="sharedpreferencesrecent5";
    private ArrayList<String> recents;

    @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_chooser_activity);

    ListView sampleList = (ListView) findViewById(R.id.sample_list);
    final SampleAdapter sampleAdapter = new SampleAdapter(this);

    sampleAdapter.add(new Header("Simple player"));
    sampleAdapter.addAll((Object[]) Samples.SIMPLE);
    sampleAdapter.add(new Header("YouTube DASH"));
    sampleAdapter.addAll((Object[]) Samples.YOUTUBE_DASH_MP4);
    sampleAdapter.add(new Header("Widevine GTS DASH"));
    sampleAdapter.addAll((Object[]) Samples.WIDEVINE_GTS);
    sampleAdapter.add(new Header("SmoothStreaming"));
    sampleAdapter.addAll((Object[]) Samples.SMOOTHSTREAMING);
    sampleAdapter.add(new Header("Misc"));
    sampleAdapter.addAll((Object[]) Samples.MISC);
    if (DemoUtil.EXPOSE_EXPERIMENTAL_FEATURES) {
      sampleAdapter.add(new Header("YouTube WebM DASH (Experimental)"));
      sampleAdapter.addAll((Object[]) Samples.YOUTUBE_DASH_WEBM);
    }

    sampleList.setAdapter(sampleAdapter);
    sampleList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = sampleAdapter.getItem(position);
        if (item instanceof Sample) {
          onSampleSelected((Sample) item);
        }
      }
    });


  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        // Building menuitems for the first time
        if(menu.size()==1){
            menu.clear();
            menu.add(0,100,1,"Open DASH URL ...");
            menu.add(0,0,2,null);
            menu.add(0,1,3,null);
            menu.add(0,2,4,null);
            menu.add(0,3,5,null);
            menu.add(0,4,6,null);
        }

        // Updating menuitems
        MenuItem m;
        for(int i=0;i<5;i++){
            m = menu.findItem(i);
            m.setTitle(urlStringShortener(recents.get(i)));
            if(recents.get(i).equalsIgnoreCase("-"))
                m.setEnabled(false);
            else m.setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public String urlStringShortener(String s){
        if(s==null)
            return s;
        if(s.length()<30)
            return s;
        StringBuffer s2 = new StringBuffer();
        s2.append("..."+s.substring(s.length()-26,s.length()));
        return s2.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Open
        if(item.getItemId()==100){
            Intent i = new Intent(SampleChooserActivity.this, OpenActivity.class);
            startActivityForResult(i,OPEN_DIALOG);
        }
        //Recent urls (1-5)(id:11-15)
        else if(item.getItemId()>=0 && item.getItemId()<5){

            Sample s = new Sample("dialog sample", "09",
                    recents.get(item.getItemId()), DemoUtil.TYPE_DASH, false, true);

            //Open sample
            onSampleSelected(s);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Read shared preferences of recent urls
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        recents = new ArrayList<>();
        recents.add(0,sp.getString(PREF_RECENT_1, "-"));
        recents.add(1,sp.getString(PREF_RECENT_2, "-"));
        recents.add(2,sp.getString(PREF_RECENT_3,"-"));
        recents.add(3,sp.getString(PREF_RECENT_4,"-"));
        recents.add(4,sp.getString(PREF_RECENT_5, "-"));
    }

    public void saveRecents(){
        // Save shared preferences of recent urls
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(PREF_RECENT_1,recents.get(0));
        e.putString(PREF_RECENT_2,recents.get(1));
        e.putString(PREF_RECENT_3,recents.get(2));
        e.putString(PREF_RECENT_4,recents.get(3));
        e.putString(PREF_RECENT_5,recents.get(4));
        e.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OPEN_DIALOG && resultCode == RESULT_OK){
            Sample s = new Sample("dialog sample", "09",
                    data.getStringExtra("url"), DemoUtil.TYPE_DASH, false, true);

            //Updating recents list
            ArrayList<String> a = new ArrayList<>();
            a.add(0,data.getStringExtra("url"));
            a.add(1,recents.get(0));
            a.add(2,recents.get(1));
            a.add(3,recents.get(2));
            a.add(4,recents.get(3));
            recents = a;
            saveRecents();
            invalidateOptionsMenu();

            //Open sample
            onSampleSelected(s);
        }
    }

    private void onSampleSelected(Sample sample) {
    if (Util.SDK_INT < 18 && sample.isEncypted) {
      Toast.makeText(getApplicationContext(), R.string.drm_not_supported, Toast.LENGTH_SHORT)
          .show();
      return;
    }
    Class<?> playerActivityClass = sample.fullPlayer ? FullPlayerActivity.class
        : SimplePlayerActivity.class;
    Intent mpdIntent = new Intent(this, playerActivityClass)
        .setData(Uri.parse(sample.uri))
        .putExtra(DemoUtil.CONTENT_ID_EXTRA, sample.contentId)
        .putExtra(DemoUtil.CONTENT_TYPE_EXTRA, sample.type);
    startActivity(mpdIntent);
  }

  private static class SampleAdapter extends ArrayAdapter<Object> {

    public SampleAdapter(Context context) {
      super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        int layoutId = getItemViewType(position) == 1 ? android.R.layout.simple_list_item_1
            : R.layout.sample_chooser_inline_header;
        view = LayoutInflater.from(getContext()).inflate(layoutId, null, false);
      }
      Object item = getItem(position);
      String name = null;
      if (item instanceof Sample) {
        name = ((Sample) item).name;
      } else if (item instanceof Header) {
        name = ((Header) item).name;
      }
      ((TextView) view).setText(name);
      return view;
    }

    @Override
    public int getItemViewType(int position) {
      return (getItem(position) instanceof Sample) ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
      return 2;
    }

  }

  private static class Header {

    public final String name;

    public Header(String name) {
      this.name = name;
    }

  }

}
