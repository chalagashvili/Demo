package com.example.vato.opguitar.TabFragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.example.vato.opguitar.Adapters.GridAdapter;
import com.example.vato.opguitar.Adapters.ViewPagerAdapter;
import com.example.vato.opguitar.R;
import com.example.vato.opguitar.database.Preset;
import com.example.vato.opguitar.database.PresetProperty;
import com.example.vato.opguitar.managers.DBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GM on 7/21/2016.
 */
public class DashboardFragment extends Fragment {

    private GridView gridView;
    GridAdapter gridAdapter;

    public DashboardFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        gridView = (GridView) view.findViewById(R.id.grid_view);
        List<Preset> presetList = DBManager.getInstance().getAllPresets();
        List<String> presetNameList = new ArrayList<>();

        for (Preset preset: presetList) {
            presetNameList.add(preset.getPreset());
        }

        gridAdapter = new GridAdapter(getContext(), presetNameList);
        gridView.setAdapter(gridAdapter);
//        setMultiChoiceListenerToGridView(gridAdapter);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.deleter);
                dialog.setTitle("Deletion");
                Button btnDelete = (Button) dialog.findViewById(R.id.delete_pop);
                Button btnCancel = (Button) dialog.findViewById(R.id.cancel_pop);

                final String current = (String) gridAdapter.getItem(position);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBManager manager = DBManager.getInstance();
                        manager.removePreset(current);
                        refresh();
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
                return true;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String presetName = ((TextView) view).getText().toString();
                String presetName = (String) parent.getAdapter().getItem(position);

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(1);

                MainFragment fragment = (MainFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(1);
                fragment.setPresetName(presetName);

                Toast.makeText(getContext(), presetName, Toast.LENGTH_LONG).show();

            }
        });

        return view;
    }

    public void refresh() {
        List<Preset> presetList = DBManager.getInstance().getAllPresets();
        List<String> presetNameList = new ArrayList<>();

        for (Preset preset: presetList) {
            presetNameList.add(preset.getPreset());
        }
        gridAdapter.updateList(presetNameList);
        gridAdapter.notifyDataSetChanged();
    }
}