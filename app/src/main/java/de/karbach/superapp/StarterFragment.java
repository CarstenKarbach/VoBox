/**
 MoTAC - digital board for TAC board game
 Copyright (C) 2015-2016  Carsten Karbach

 Contact by mail carstenkarbach@gmx.de
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.superapp;

import android.app.Fragment;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 27.12.2015.
 *
 * This fragment is the entry screen. It allows to navigate to the
 * main activities (box, word list, add new vocabulary). Moreover,
 * the active dictionary can be selected.
 */
public class StarterFragment extends Fragment {

    private class DictionaryAdapter extends BaseAdapter {

        private String[] dictNames;

        public DictionaryAdapter(String[] dictNames) {
            this.dictNames = dictNames;
        }

        @Override
        public int getCount() {
            return dictNames.length;
        }

        @Override
        public Object getItem(int i) {
            return dictNames[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.flagdict_item, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.dict_name);
            ImageView flag1 = (ImageView) convertView.findViewById(R.id.flagview1);
            ImageView flag2 = (ImageView) convertView.findViewById(R.id.flagview2);

            String nameValue = dictNames[position];

            DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
            Dictionary dict = dm.getDictionary(nameValue);

            if(dict != null) {
                int r1 = PictureHelper.getDrawableResourceForLanguage(dict.getBaseLanguage());
                if (flag1 != null) {
                    flag1.setImageResource(r1);
                }
                int r2 = PictureHelper.getDrawableResourceForLanguage(dict.getLanguage());
                if (flag2 != null) {
                    flag2.setImageResource(r2);
                }

                name.setText(nameValue);
            }

            return convertView;
        }
    }

    private void updateSelectedDictionary(Spinner select){
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());

        DictionaryAdapter dapter = new DictionaryAdapter(dm.readDictionaryArray());
        select.setAdapter(dapter);

        Dictionary selectedDict = dm.getSelectedDictionary();
        if(selectedDict != null) {
            for (int i = 0; i < select.getCount(); i++) {
                String name = select.getItemAtPosition(i).toString();
                if (name.equals(selectedDict.getName())) {
                    select.setSelection(i);
                    break;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("carsten", "onCreateView Starter");
        View result = inflater.inflate(R.layout.starter_fragment, container, false);

        Button newCardButton = (Button) result.findViewById(R.id.newcard_button);
        View.OnClickListener newCardOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CardActivity.class);
                startActivity(intent);
            }
        };
        if(newCardButton != null){
            newCardButton.setOnClickListener(newCardOnClick);
        }
        ImageButton newCardImage = (ImageButton) result.findViewById(R.id.newcard_imagebutton);
        if(newCardImage != null){
            newCardImage.setOnClickListener(newCardOnClick);
        }

        Button listButton = (Button) result.findViewById(R.id.list_button);
        View.OnClickListener listOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CardListActivity.class);
                startActivity(intent);
            }
        };
        if(listButton != null){
            listButton.setOnClickListener(listOnClick);
        }
        ImageButton listImage = (ImageButton) result.findViewById(R.id.list_imagebutton);
        if(listImage != null){
            listImage.setOnClickListener(listOnClick);
        }

        Button boxButton = (Button) result.findViewById(R.id.box_button);
        View.OnClickListener boxOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BoxActivity.class);
                startActivity(intent);
            }
        };
        if(boxButton != null){
            boxButton.setOnClickListener(boxOnClick);
        }
        ImageButton boxImage = (ImageButton) result.findViewById(R.id.box_imagebutton);
        if(boxImage != null){
            boxImage.setOnClickListener(boxOnClick);
        }

        final Spinner select = (Spinner) result.findViewById(R.id.language_selection);
        if(select != null){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.languages_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            updateSelectedDictionary(select);

            select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String value = select.getItemAtPosition(position).toString();
                    DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                    dm.selectDictionary(value);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        final ImageButton actionsButton = (ImageButton) result.findViewById(R.id.actions_button);
        if(actionsButton != null){
            actionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(getActivity(), actionsButton);
                    popup.getMenuInflater().inflate(R.menu.dictionary_actions, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.dict_edit){
                                Intent intent = new Intent(getActivity(), DictionaryActivity.class);
                                intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.EDIT.ordinal());
                                startActivity(intent);
                                return true;
                            }
                            if(item.getItemId() == R.id.dict_export){
                                DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                                if(dm == null){
                                    return true;
                                }
                                Dictionary selected = dm.getSelectedDictionary();
                                if(selected == null){
                                    return true;
                                }
                                selected.sendExportedDictionary(getActivity());
                            }
                            if(item.getItemId() == R.id.dict_new){
                                Intent intent = new Intent(getActivity(), DictionaryActivity.class);
                                intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.NEW.ordinal());
                                startActivity(intent);
                            }
                            if(item.getItemId() == R.id.dict_delete){
                                DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                                if(dm == null){
                                    return true;
                                }
                                Dictionary selected = dm.getSelectedDictionary();
                                if(selected == null){
                                    return true;
                                }
                                String todelete = selected.getName();
                                dm.deleteDictionary(selected.getName());

                                Toast.makeText(getActivity(), "Wörterbuch '"+todelete+"' gelöscht", Toast.LENGTH_SHORT).show();

                                updateSelectedDictionary(select);
                            }

                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        return result;
    }
}
