package com.example.vato.opguitar.database;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "PRESET_PROPERTY".
 */
public class PresetProperty {

    private Long id;
    private int presetId;
    private int option;
    private int value;

    // KEEP FIELDS - put your custom fields here
    public PresetProperty(int option, int value) {
        this.option = option;
        this.value = value;
    }
    // KEEP FIELDS END

    public PresetProperty() {
    }

    public PresetProperty(Long id) {
        this.id = id;
    }

    public PresetProperty(Long id, int presetId, int option, int value) {
        this.id = id;
        this.presetId = presetId;
        this.option = option;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPresetId() {
        return presetId;
    }

    public void setPresetId(int presetId) {
        this.presetId = presetId;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
