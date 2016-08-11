package com.example.vato.opguitar.TabFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.Switch;

import com.devadvance.circularseekbar.CircularSeekBar;
import com.devadvance.circularseekbar.CircularSeekBar.OnCircularSeekBarChangeListener;
import com.example.vato.opguitar.Adapters.ViewPagerAdapter;
import com.example.vato.opguitar.MainActivity;
import com.example.vato.opguitar.R;
import com.example.vato.opguitar.database.Preset;
import com.example.vato.opguitar.database.PresetProperty;
import com.example.vato.opguitar.managers.DBManager;
import com.example.vato.opguitar.managers.PresetPropertyManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GM on 7/21/2016.
 */


public class MainFragment extends Fragment implements TabHost.OnTabChangeListener{

    private CircularSeekBar distortionGainBar, distortionLevelBar;
    private SwitchCompat distortionEnableSwitch;

    private CircularSeekBar driveGainBar, driveLevelBar;
    private SwitchCompat driveEnableSwitch;

    private CircularSeekBar fuzzGainBar, fuzzLevelBar;
    private SwitchCompat fuzzBoxSwitch;

    private CircularSeekBar phaserDepthBar, phaserRateBar;
    private SwitchCompat phaserSwitch;

    private SwitchCompat reverbSwitch;

    private ImageView record;
    private ImageView play;
    private ImageView stop;

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
        updateValues();
    }

    private void updateValues() {
        DBManager manager = DBManager.getInstance();
        Preset preset = manager.getPresetByName(presetName);
        List<PresetProperty> propertyList = manager.getPropertiesForPreset(preset.getId().intValue());

        for (PresetProperty property: propertyList) {
            switch (property.getOption()) {
                case PresetPropertyManager.DISTORTION_SWITCH:
                    distortionEnableSwitch.setChecked(property.getValue() > 0);
                    break;
                case PresetPropertyManager.DISTORTION_GAIN:
                    distortionGainBar.setProgress(property.getValue());
                    break;
                case PresetPropertyManager.DISTORTION_LEVEL:
                    distortionLevelBar.setProgress(property.getValue());
                    break;

                case PresetPropertyManager.OVERDRIVE_SWITCH:
                    driveEnableSwitch.setChecked(property.getValue() > 0);
                    break;
                case PresetPropertyManager.OVERDRIVE_GAIN:
                    driveGainBar.setProgress(property.getValue());
                    break;
                case PresetPropertyManager.OVERDRIVE_LEVEL:
                    driveLevelBar.setProgress(property.getValue());
                    break;

                case PresetPropertyManager.FUZZBOX_SWITCH:
                    fuzzBoxSwitch.setChecked(property.getValue() > 0);
                    break;
                case PresetPropertyManager.FUZZBOX_GAIN:
                    fuzzGainBar.setProgress(property.getValue());
                    break;
                case PresetPropertyManager.FUZZBOX_LEVEL:
                    fuzzLevelBar.setProgress(property.getValue());
                    break;

                case PresetPropertyManager.PHASER_SWITCH:
                    phaserSwitch.setChecked(property.getValue() > 0);
                    break;
                case PresetPropertyManager.PHASER_DEPTH:
                    phaserDepthBar.setProgress(property.getValue());
                    break;
                case PresetPropertyManager.PHASER_RATE:
                    phaserRateBar.setProgress(property.getValue());
                    break;

                case PresetPropertyManager.REVERB_SWITCH:
                    reverbSwitch.setChecked(property.getValue() > 0);
            }
        }
    }

    private String presetName;

    public MainFragment() {
        fuzzBoxSwitch = null;
        driveEnableSwitch = null;
        phaserSwitch = null;
        reverbSwitch = null;
        presetName = null;
    }

    @Override
    public void onTabChanged(String tabId) {
        if (getView() != null){
            Button button = (Button) getView().findViewById(R.id.save_trigger_popup);
            button.setText(getResources().getText(R.string.update));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        CardView saveButton = (CardView) view.findViewById(R.id.save_trigger_popup);

        distortionGainBar = (CircularSeekBar) view.findViewById(R.id.distortion_gain);
        distortionGainBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setDistortionGain(distortionGainBar.getProgress());
            }
        }));

        distortionLevelBar = (CircularSeekBar) view.findViewById(R.id.distortion_level);
        distortionLevelBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setDistortionLevel(distortionLevelBar.getProgress());
            }
        }));

        distortionEnableSwitch = (SwitchCompat) view.findViewById(R.id.dist_switch);
        distortionEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.setDistortionEnable(distortionEnableSwitch.isChecked());
            }
        });

        driveEnableSwitch = (SwitchCompat) view.findViewById(R.id.over_switch);
        driveEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.setOverDriveEnable(isChecked);
            }
        });

        driveGainBar = (CircularSeekBar) view.findViewById(R.id.overdrive_gain);
        driveGainBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setOverDriveGain(driveGainBar.getProgress());
            }
        }));

        driveLevelBar = (CircularSeekBar) view.findViewById(R.id.overdrive_level);
        driveLevelBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setOverDriveLevel(driveLevelBar.getProgress());
            }
        }));

        fuzzBoxSwitch = (SwitchCompat) view.findViewById(R.id.fuzz_switch);
        fuzzBoxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.setFuzzBoxEnable(isChecked);
            }
        });

        fuzzGainBar = (CircularSeekBar) view.findViewById(R.id.fuzz_gain);
        fuzzGainBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setFuzzBoxGain(fuzzGainBar.getProgress());
            }
        }));

        fuzzLevelBar = (CircularSeekBar) view.findViewById(R.id.fuzz_level);
        fuzzLevelBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setFuzzBoxLevel(fuzzLevelBar.getProgress());
            }
        }));


        phaserSwitch = (SwitchCompat) view.findViewById(R.id.phaser_switch);
        phaserSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.setPhaserEnable(isChecked);
            }
        });

        phaserDepthBar = (CircularSeekBar) view.findViewById(R.id.phaser_depth);
        phaserDepthBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setPhaserDepth(phaserDepthBar.getProgress());
            }
        }));

        phaserRateBar = (CircularSeekBar) view.findViewById(R.id.phaser_rate);
        phaserRateBar.setOnSeekBarChangeListener(new CircleSeekBarListener(new Runnable() {
            @Override
            public void run() {
                MainActivity.setPhaserRate(phaserRateBar.getProgress());
            }
        }));

        reverbSwitch = (SwitchCompat) view.findViewById(R.id.reverb_switch);
        reverbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.setReverbEnable(isChecked);
            }
        });


        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        record = (ImageView) view.findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.looperStart();
                record.startAnimation(animation);
            }
        });

        stop = (ImageView) view.findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.looperStop();
                record.clearAnimation();
            }
        });

        play = (ImageView) view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = false;
            @Override
            public void onClick(View v) {
                if (!isPlaying){
                    MainActivity.looperPlay(true);
                    play.setImageResource(R.drawable.pause_button);
                    isPlaying = true;
                } else {
                    MainActivity.looperPlay(false);
                    play.setImageResource(R.drawable.play_img);
                    isPlaying = false;
                }
            }
        });



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.popup);
                dialog.setTitle("Custom Audio Preferences");
                final EditText editText = (EditText) dialog.findViewById(R.id.editText);
                Button btnSave = (Button) dialog.findViewById(R.id.save_pop_up);
                Button btnCancel = (Button) dialog.findViewById(R.id.cancel);
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = editText.getText().toString();
                        List<PresetProperty> properties = getCurrentState();
                        DBManager manager = DBManager.getInstance();
                        manager.createNewPreset(new Preset(name), properties);
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                        viewPager.setCurrentItem(0);

                        DashboardFragment fragment = (DashboardFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(0);
                        fragment.refresh();
                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return view;
    }

    private List<PresetProperty> getCurrentState() {
        List<PresetProperty> propertyList = new ArrayList<>();

        propertyList.add(new PresetProperty(PresetPropertyManager.DISTORTION_SWITCH,
                distortionEnableSwitch.isChecked()? 1 : 0));
        propertyList.add(new PresetProperty(PresetPropertyManager.DISTORTION_GAIN,
                distortionGainBar.getProgress()));
        propertyList.add(new PresetProperty(PresetPropertyManager.DISTORTION_LEVEL,
                distortionLevelBar.getProgress()));

        propertyList.add(new PresetProperty(PresetPropertyManager.OVERDRIVE_SWITCH,
                driveEnableSwitch.isChecked()? 1 : 0));
        propertyList.add(new PresetProperty(PresetPropertyManager.OVERDRIVE_GAIN,
                driveGainBar.getProgress()));
        propertyList.add(new PresetProperty(PresetPropertyManager.OVERDRIVE_LEVEL,
                driveLevelBar.getProgress()));

        propertyList.add(new PresetProperty(PresetPropertyManager.FUZZBOX_SWITCH,
                fuzzBoxSwitch.isChecked()? 1 : 0));
        propertyList.add(new PresetProperty(PresetPropertyManager.FUZZBOX_GAIN,
                fuzzGainBar.getProgress()));
        propertyList.add(new PresetProperty(PresetPropertyManager.FUZZBOX_LEVEL,
                fuzzLevelBar.getProgress()));

        propertyList.add(new PresetProperty(PresetPropertyManager.PHASER_SWITCH,
                phaserSwitch.isChecked()? 1 : 0));
        propertyList.add(new PresetProperty(PresetPropertyManager.PHASER_DEPTH,
                phaserDepthBar.getProgress()));
        propertyList.add(new PresetProperty(PresetPropertyManager.PHASER_RATE,
                phaserRateBar.getProgress()));

        propertyList.add(new PresetProperty(PresetPropertyManager.REVERB_SWITCH,
                reverbSwitch.isChecked()? 1 : 0));

        return propertyList;
    }

    public class CircleSeekBarListener implements OnCircularSeekBarChangeListener {

        private Runnable runnable;

        public CircleSeekBarListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            this.runnable.run();
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
    }

}