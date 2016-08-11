package com.example.vato.opguitar.Adapters;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vato.opguitar.R;
import com.example.vato.opguitar.managers.PresetPropertyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GM on 7/22/2016.
 */
public class GridAdapter extends BaseAdapter {
    private Context context;
    private List<String> itemList;
    private SparseBooleanArray selectedItems;

    public GridAdapter(Context context, List<String> list) {
        this.context = context;
        this.itemList = list;
        this.selectedItems = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        ViewHolder viewHolder;
        if (convertView == null) {
            rootView = View.inflate(context, R.layout.grid_view_item, null);
            TextView nameView = (TextView) rootView.findViewById(R.id.grid_item_text_view);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.icon_id);
            viewHolder = new ViewHolder();
            viewHolder.presetName = nameView;
            viewHolder.musicIcon = imageView;
            rootView.setTag(viewHolder);
        } else {
            rootView = convertView;
            viewHolder = (ViewHolder) rootView.getTag();
        }
        if (viewHolder != null){
            viewHolder.presetName.setText((String)getItem(position));
            viewHolder.musicIcon.setImageResource(getProperIcon((String)getItem(position)));
        }
        return rootView;
    }


    private int getProperIcon(String name){
        switch(name){
            case PresetPropertyManager.METAL:
                return R.drawable.metal_music;
            case PresetPropertyManager.CLASSIC:
                return R.drawable.classic_music;
            case PresetPropertyManager.ROCK:
                return R.drawable.rock_music;
            case PresetPropertyManager.COUNTRY:
                return R.drawable.country_music;
//            case PresetPropertyManager.PROGRESSIVE:
//                return R.drawable.dj;
            case PresetPropertyManager.LOUD:
                return R.drawable.loudspeaker;
            case PresetPropertyManager.BASS:
                return R.drawable.bass_drum;
            case PresetPropertyManager.RETRO:
                return R.drawable.tape_drive;
            case PresetPropertyManager.PROGRESSIVE:
                return R.drawable.park_concert_shell;
            default:
                return R.drawable.music_record;
        }
    }


    public void markIt(int position, View view) {
        boolean val = !this.selectedItems.get(position);
        if (val){
            this.selectedItems.put(position, val);
        }
        else{
            this.selectedItems.delete(position);
        }
        notifyDataSetChanged();
    }


    public void dismarkAll() {
        this.selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (this.itemList == null) {
            return 0;
        }
        return this.itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(List<String> presetNameList) {
        itemList.clear();
        itemList.addAll(presetNameList);
    }

    private class ViewHolder {
        TextView presetName;
        ImageView musicIcon;
    }
}
