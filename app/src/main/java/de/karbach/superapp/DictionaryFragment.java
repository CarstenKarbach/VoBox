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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 *
 * Show dictionary and allow to configure settings
 */
public class DictionaryFragment extends Fragment{

    protected void updateSaveLabel(Button button, String currentText){
        final DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        if(dm.dictionaryExists(currentText)){
            button.setText("speichern");
        }
        else{
            button.setText("hinzufügen");
        }
    }

    protected void reloadDictionaryData(View fragmentView, boolean updateselected){
        View view = fragmentView == null ? getView(): fragmentView;
        final EditText nameview = view.findViewById(R.id.dictionary_name);
        final DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();
        if(updateselected) {
            if (dict != null) {
                nameview.setText(dict.getName());
            }
        }
        final Button saveButton = view.findViewById(R.id.save_button);
        updateSaveLabel(saveButton, nameview.getText().toString());

        TextView wordcount = view.findViewById(R.id.wordcount);
        Dictionary currentDict = dm.getDictionary(nameview.getText().toString());

        String count = "-";
        if(currentDict != null){
            count = String.valueOf(currentDict.getCards().size());
        }
        wordcount.setText(count);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.dictionarysettings_fragment, container, false);

        final EditText nameview = result.findViewById(R.id.dictionary_name);
        final DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());

        reloadDictionaryData(result, true);

        final Button saveButton = result.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newDict = nameview.getText().toString();

                boolean existing = dm.dictionaryExists(newDict);

                dm.addDictionary(newDict);
                dm.selectDictionary(newDict);

                String toast = "Neues Wörterbuch '"+newDict+"' gespeichert";
                if(existing){
                    toast = "Änderungen am Wörterbuch '"+newDict+"' gespeichert";
                }

                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
                reloadDictionaryData(null, true);
            }
        });

        final Button deleteButton = result.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deleteDict = nameview.getText().toString();
                dm.deleteDictionary(deleteDict);

                Toast.makeText(getActivity(), "Wörterbuch '"+deleteDict+"' gelöscht", Toast.LENGTH_SHORT).show();
                reloadDictionaryData(null, true);
            }
        });

        nameview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                reloadDictionaryData(null, false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return result;
    }

}
