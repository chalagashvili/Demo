package com.example.vato.opguitar.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.vato.opguitar.database.DaoMaster;
import com.example.vato.opguitar.database.DaoSession;
import com.example.vato.opguitar.database.Preset;
import com.example.vato.opguitar.database.PresetDao;
import com.example.vato.opguitar.database.PresetProperty;
import com.example.vato.opguitar.database.PresetPropertyDao;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by GM on 7/24/16.
 */
public class DBManager {

    protected Context context;
    protected DaoMaster daoMaster;
    protected DaoSession daoSession;
    protected SQLiteDatabase database;
    protected DaoMaster.DevOpenHelper presetHelper, propertyHelper;
    protected static DBManager manager = null;

    private DBManager(Context context) {
        this.context = context;
        presetHelper = new DaoMaster.DevOpenHelper(this.context, "PRESET", null);
        propertyHelper = new DaoMaster.DevOpenHelper(this.context, "PRESET_PROPERTY", null);
    }

    public static DBManager getInstance(Context context) {
        if (manager == null) {
            manager = new DBManager(context);
        }
        return manager;
    }

    public void openReadableDB(DaoMaster.DevOpenHelper mHelper) {
        database = mHelper.getReadableDatabase();
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    public void openWritableDB(DaoMaster.DevOpenHelper mHelper) {
        database = mHelper.getWritableDatabase();
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    public void closeDBConnections() {
        if (daoSession != null) {
            daoSession.clear();
            daoSession = null;
        }
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public void clearAll() {
        clearDB(presetHelper);
        clearDB(propertyHelper);
    }

    private void clearDB(DaoMaster.DevOpenHelper mHelper) {
        openWritableDB(mHelper);
        PresetDao presetDao = daoSession.getPresetDao();
        presetDao.deleteAll();
        daoSession.clear();
    }

    public List<Preset> getAllPresets() {
        openReadableDB(presetHelper);
        PresetDao presetDao = daoSession.getPresetDao();
        Query<Preset> presetQuery = presetDao.queryBuilder().orderAsc(
                PresetDao.Properties.Preset
        ).build();
        List<Preset> presetList = new ArrayList<>(presetQuery.list());
        daoSession.clear();
        return presetList;
    }

    public Preset getPresetByName(String name) {
        openReadableDB(presetHelper);
        PresetDao presetDao = daoSession.getPresetDao();
        Query<Preset> presetQuery = presetDao.queryBuilder().where(
                PresetDao.Properties.Preset.eq(name)
        ).build();
        List<Preset> list = presetQuery.list();
        daoSession.clear();
        return list.size() > 0? list.get(0) : null;
    }

    public int deletePreset(String name) {
        if (PresetPropertyManager.PROPERTY_LIST.contains(name)) {
            return -1;
        }
        openReadableDB(presetHelper);
        PresetDao presetDao = daoSession.getPresetDao();
        Query<Preset> presetQuery = presetDao.queryBuilder().where(
                PresetDao.Properties.Preset.eq(name)
        ).build();
        List<Preset> presetList = presetQuery.list();
        int answer = -1;
        for (Preset preset: presetList) {
            answer = preset.getId().intValue();
            presetDao.delete(preset);
        }
        daoSession.clear();
        return answer;
    }

    public void addPreset(String name) {
        openWritableDB(presetHelper);
        PresetDao presetDao = daoSession.getPresetDao();
        Preset preset = new Preset();
        preset.setPreset(name);
        presetDao.insertOrReplace(preset);
        daoSession.clear();
    }

    public List<PresetProperty> getPropertiesForPreset(Integer id) {
        openReadableDB(propertyHelper);
        PresetPropertyDao propertyDao = daoSession.getPresetPropertyDao();
        Query<PresetProperty> propertyQuery = propertyDao.queryBuilder().where(
                PresetPropertyDao.Properties.PresetId.eq(id)
        ).build();
        List<PresetProperty> properties = new ArrayList<>(propertyQuery.list());
        daoSession.clear();
        return properties;
    }

    public void addPresetProperties(List<PresetProperty> properties) {
        openWritableDB(propertyHelper);
        PresetPropertyDao propertyDao = daoSession.getPresetPropertyDao();
        for (PresetProperty property: properties) {
            propertyDao.insertOrReplace(property);
        }
        daoSession.clear();
    }

    public void deletePresetProperties(int presetId) {
        openReadableDB(presetHelper);
        PresetPropertyDao propertyDao = daoSession.getPresetPropertyDao();
        Query<PresetProperty> propertyQuery = propertyDao.queryBuilder().where(
                PresetPropertyDao.Properties.PresetId.eq(presetId)
        ).build();
        List<PresetProperty> propertyList = propertyQuery.list();
        for (PresetProperty property: propertyList) {
            propertyDao.delete(property);
        }
        daoSession.clear();
    }

    public void createNewPreset(Preset preset, List<PresetProperty> properties) {
        addPreset(preset.getPreset());
        Preset current = getPresetByName(preset.getPreset());
        for (PresetProperty property: properties) {
            property.setPresetId(current.getId().intValue());
        }
        addPresetProperties(properties);
    }

    public static DBManager getInstance() {
        return manager;
    }

    public void removePreset(String name) {
        int presetId = deletePreset(name);
        if (presetId != -1) {
            deletePresetProperties(presetId);
        }
    }
}
