package com.example.vato.opguitar.managers;

import com.example.vato.opguitar.database.PresetProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by GM on 7/24/16.
 */
public class PresetPropertyManager {

    public static final int DISTORTION_SWITCH = 0;
    public static final int DISTORTION_GAIN = 1;
    public static final int DISTORTION_LEVEL = 2;

    public static final int OVERDRIVE_SWITCH = 3;
    public static final int OVERDRIVE_GAIN = 4;
    public static final int OVERDRIVE_LEVEL = 5;

    public static final int FUZZBOX_SWITCH = 6;
    public static final int FUZZBOX_GAIN = 7;
    public static final int FUZZBOX_LEVEL = 8;

    public static final int PHASER_SWITCH = 9;
    public static final int PHASER_DEPTH = 10;
    public static final int PHASER_RATE = 11;

    public static final int REVERB_SWITCH = 12;

    public static final String METAL = "Metal";
    public static final String CLASSIC = "Classic";
    public static final String ROCK = "Rock";
    public static final String COUNTRY = "Country";
    public static final String LOUD = "Loud";
    public static final String BASS = "Bass";
    public static final String RETRO = "Retro";
    public static final String PROGRESSIVE = "Progressive";

    public static final List<String> PROPERTY_LIST = Arrays.asList(
            METAL, CLASSIC, ROCK, COUNTRY, LOUD, BASS, RETRO, PROGRESSIVE
    );

    public static List<PresetProperty> getPresetProperties(String presetName) {
        List<PresetProperty> properties = new ArrayList<>();
        switch (presetName) {
            case METAL: properties = getMetalProperties(); break;
            case CLASSIC: properties = getClassicProperties(); break;
            case ROCK: properties = getRockProperties(); break;
            case COUNTRY: properties = getCountryProperties(); break;
            case LOUD: properties = getLoudProperties(); break;
            case BASS: properties = getBassProperties(); break;
            case RETRO: properties = getRetroProperties(); break;
            case PROGRESSIVE: properties = getProgressiveProperties(); break;
        }
        return properties;
    }

    private static List<PresetProperty> getProgressiveProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 1));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 3));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 3));

        properties.add(new PresetProperty(PHASER_SWITCH, 1));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 10));

        properties.add(new PresetProperty(REVERB_SWITCH, 0));
        return properties;
    }

    private static List<PresetProperty> getRetroProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 1));
        properties.add(new PresetProperty(PHASER_DEPTH, 1));
        properties.add(new PresetProperty(PHASER_RATE, 10));

        properties.add(new PresetProperty(REVERB_SWITCH, 1));
        return properties;
    }

    private static List<PresetProperty> getBassProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 1));
        return properties;
    }

    private static List<PresetProperty> getLoudProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 1));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 4));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 9));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 1));
        return properties;
    }

    private static List<PresetProperty> getRockProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 1));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 8));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 8));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 1));
        return properties;
    }

    private static List<PresetProperty> getClassicProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 0));
        return properties;
    }

    private static List<PresetProperty> getMetalProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 1));
        properties.add(new PresetProperty(DISTORTION_GAIN, 4));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 2));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 0));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 0));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 0));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 0));
        return properties;
    }

    public static List<PresetProperty> getCountryProperties() {
        List<PresetProperty> properties = new ArrayList<>();
        properties.add(new PresetProperty(DISTORTION_SWITCH, 0));
        properties.add(new PresetProperty(DISTORTION_GAIN, 0));
        properties.add(new PresetProperty(DISTORTION_LEVEL, 0));

        properties.add(new PresetProperty(OVERDRIVE_SWITCH, 0));
        properties.add(new PresetProperty(OVERDRIVE_GAIN, 0));
        properties.add(new PresetProperty(OVERDRIVE_LEVEL, 0));

        properties.add(new PresetProperty(FUZZBOX_SWITCH, 1));
        properties.add(new PresetProperty(FUZZBOX_GAIN, 4));
        properties.add(new PresetProperty(FUZZBOX_LEVEL, 10));

        properties.add(new PresetProperty(PHASER_SWITCH, 0));
        properties.add(new PresetProperty(PHASER_DEPTH, 0));
        properties.add(new PresetProperty(PHASER_RATE, 0));

        properties.add(new PresetProperty(REVERB_SWITCH, 0));
        return properties;
    }
}
