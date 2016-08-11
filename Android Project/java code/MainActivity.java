/*
 * Copyright 2015 The Android Open Source Project
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

package com.example.vato.opguitar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vato.opguitar.Adapters.ViewPagerAdapter;
import com.example.vato.opguitar.TabFragments.DashboardFragment;
import com.example.vato.opguitar.TabFragments.MainFragment;
import com.example.vato.opguitar.TabFragments.StreamFragment;
import com.example.vato.opguitar.database.Preset;
import com.example.vato.opguitar.database.PresetProperty;
import com.example.vato.opguitar.managers.DBManager;
import com.example.vato.opguitar.managers.PresetPropertyManager;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int AUDIO_ECHO_REQUEST = 0;

    TextView status_view;
    String nativeSampleRate;
    String nativeSampleBufSize;
    boolean supportRecording;
    Boolean isPlaying;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DBManager manager;

    private String[] requestPermissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onPause() {
        stopEcho(viewPager);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        manager = DBManager.getInstance(getApplicationContext());

        fillDefaultDatabase();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initPermissions();

        queryNativeAudioParameters();

//         initialize native audio system
        updateNativeAudioUI();
        if (supportRecording) {
            createSLEngine(Integer.parseInt(nativeSampleRate), Integer.parseInt(nativeSampleBufSize));
        }
        isPlaying = false;

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);
        manageTabs();
    }

    private void initPermissions() {
        for (String permission : requestPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {permission}, 1);
            }
        }
    }

    private void fillDefaultDatabase() {
        manager.clearAll();
        List<Preset> presetList = manager.getAllPresets();
        if (presetList.size() == 0) {
            for (String preset : PresetPropertyManager.PROPERTY_LIST) {
                List<PresetProperty> properties = PresetPropertyManager.getPresetProperties(preset);
                manager.createNewPreset(new Preset(preset), properties);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        startEcho(viewPager);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopEcho(viewPager);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startEcho(viewPager);
    }

    private void manageTabs() {
        tabLayout.getTabAt(0).setIcon(R.mipmap.ic_dashboard_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.mipmap.ic_build_white_24dp);
        tabLayout.getTabAt(2).setIcon(R.mipmap.ic_cloud_white_24dp);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DashboardFragment(), "Dashboard");
        adapter.addFragment(new MainFragment(), "Main");
        adapter.addFragment(new StreamFragment(), "Stream");
        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        if (supportRecording) {
            if (isPlaying) {
                stopPlay();
            }
            deleteSLEngine();
            isPlaying = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEcho() {
        if (!supportRecording || isPlaying) {
            return;
        }
        if (!createSLBufferQueueAudioPlayer()) {
            return;
        }
        if (!createAudioRecorder()) {
            deleteSLBufferQueueAudioPlayer();
            return;
        }
        startPlay();   //this must include startRecording()
        isPlaying = true;
    }

    public void startEcho(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_ECHO_REQUEST);
            return;
        }
        startEcho();
    }

    public void stopEcho(View view) {
        if (!supportRecording || !isPlaying) {
            return;
        }
        stopPlay();  //this must include stopRecording()
        updateNativeAudioUI();
        deleteSLBufferQueueAudioPlayer();
        deleteAudioRecorder();
        isPlaying = false;
    }

    public void getLowLatencyParameters(View view) {
        updateNativeAudioUI();
        return;
    }

    private void queryNativeAudioParameters() {
        AudioManager myAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nativeSampleRate = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        nativeSampleBufSize = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int recBufSize = AudioRecord.getMinBufferSize(
                Integer.parseInt(nativeSampleRate),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        supportRecording = true;
        if (recBufSize == AudioRecord.ERROR ||
                recBufSize == AudioRecord.ERROR_BAD_VALUE) {
            supportRecording = false;
        }
    }

    private void updateNativeAudioUI() {
        if (!supportRecording) {
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        /*
         * if any permission failed, the sample could not play
         */
        if (AUDIO_ECHO_REQUEST != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 1 ||
                grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            /*
             * When user denied the permission, throw a Toast to prompt that RECORD_AUDIO
             * is necessary; on UI, we display the current status as permission was denied so
             * user know what is going on.
             * This application go back to the original state: it behaves as if the button
             * was not clicked. The assumption is that user will re-click the "start" button
             * (to retry), or shutdown the app in normal way.
             */
            return;
        }

        /*
         * When permissions are granted, we prompt the user the status. User would
         * re-try the "start" button to perform the normal operation. This saves us the extra
         * logic in code for async processing of the button listener.
         */

        startEcho();
    }

    /*
     * Loading our Libs
     */
    static {
        System.loadLibrary("echo");
    }

    /*
     * jni function implementations...
     */
    public static native void createSLEngine(int rate, int framesPerBuf);

    public static native void deleteSLEngine();

    public static native boolean createSLBufferQueueAudioPlayer();

    public static native void deleteSLBufferQueueAudioPlayer();

    public static native boolean createAudioRecorder();

    public static native void deleteAudioRecorder();

    public static native void startPlay();

    public static native void stopPlay();

    public static native void setDistortionEnable(boolean value);

    public static native void setDistortionLevel(int value);

    public static native void setDistortionGain(int value);

    public static native void setOverDriveEnable(boolean value);

    public static native void setOverDriveLevel(int value);

    public static native void setOverDriveGain(int value);

    public static native void setFuzzBoxEnable(boolean value);

    public static native void setFuzzBoxGain(int value);

    public static native void setFuzzBoxLevel(int value);

    public static native void setPhaserEnable(boolean value);

    public static native void setPhaserDepth(int value);

    public static native void setPhaserRate(int value);

    public static native void setReverbEnable(boolean value);

    public static native void startRecording();

    public static native String stopRecording();

    public static native void looperStart();

    public static native void looperStop();

    public static native void looperPlay(boolean value);

}
