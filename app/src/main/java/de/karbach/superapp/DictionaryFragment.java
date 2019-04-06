/*
 VoBox - train your vocabulary
 Copyright (C) 2015-2019  Carsten Karbach

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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 *
 * Show dictionary and allow to configure settings.
 * Two modes: NEW for storing a new dictionary, edit for editing an existing dictionary.
 */
public class DictionaryFragment extends Fragment{

    /**
     * Mode parameter loaded in create function
     */
    public static String PARAMMODE = "de.karbach.superapp.DictionaryFragment.mode";

    /**
     * Available languages in German (used as ID in dictionary settings)
     */
    public static List<String> languages;

    /**
     * Check if user is currently renaming the dictionary
     * Compares selected dictionary name with the current name in dictionary_name edit text
     * @param rootView
     * @return true if user renames dictionary, false if not
     */
    protected boolean isRenaming(View rootView){
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary selected = dm.getSelectedDictionary();
        final EditText nameview = rootView.findViewById(R.id.dictionary_name);
        String currentName = nameview.getText().toString();
        return ! currentName.equals(selected.getName());
    }

    /**
     * Check if dictionary name in name view is already in use in {@link DictionaryManagement}
     * @param rootView
     * @return true if name is in use, false otherwise
     */
    protected boolean nameAlreadyTaken(View rootView){
        final EditText nameview = rootView.findViewById(R.id.dictionary_name);
        if(nameview != null){
            String name = nameview.getText().toString();
            DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
            boolean existing = dm.dictionaryExists(name);
            if(existing){
                return true;
            }
        }
        return false;
    }

    /**
     * Set label for the button and enable state.
     *
     * @param button the button to adapt
     * @param rootView root view of fragment
     */
    protected void updateSaveLabel(Button button, View rootView){
        if(mode == Mode.EDIT){
            button.setText(getString(R.string.label_save));
            boolean enabled = !isRenaming(rootView) || !nameAlreadyTaken(rootView);
            button.setEnabled( enabled );
        }
        else{
            button.setText(getString(R.string.label_add));
            button.setEnabled(true);
            if(rootView != null){
                if(nameAlreadyTaken(rootView)){
                    button.setEnabled(false);
                }
            }
        }
    }

    /**
     * Load current settings of dictionary into the view.
     * In edit mode only the data of the currently selected dictionary can be shown.
     * In new mode shows also dictionary data for dictionaries with the given name in name edit text
     * E.g. load number of cards, number of boxes and languages in dict.
     * @param fragmentView
     * @param updateselected
     */
    protected void reloadDictionaryData(View fragmentView, boolean updateselected){
        View view = fragmentView == null ? getView(): fragmentView;
        final EditText nameview = view.findViewById(R.id.dictionary_name);
        nameview.setEnabled(true);
        final DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();
        if(updateselected) {
            if (dict != null) {
                nameview.setText(dict.getName());
            }
        }
        final Button saveButton = view.findViewById(R.id.save_button);
        updateSaveLabel(saveButton, view);

        TextView wordcount = view.findViewById(R.id.wordcount);
        Dictionary currentDict = dm.getDictionary(nameview.getText().toString());
        if(mode == Mode.EDIT){
            currentDict = dict;
        }

        String count = "-";
        if(currentDict != null){
            count = String.valueOf(currentDict.getCards().size());
        }
        wordcount.setText(count);

        Spinner baseLanguageSpinner = view.findViewById(R.id.flag_selection1);
        Spinner languageSpinner = view.findViewById(R.id.flag_selection2);
        if(currentDict != null){
            int pos = 0;
            pos = languages.indexOf(currentDict.getLanguage());
            if(pos < 0 ){
                pos = 0;
            }
            languageSpinner.setSelection(pos);
            pos = languages.indexOf(currentDict.getBaseLanguage());
            if(pos < 0 ){
                pos = 0;
            }
            baseLanguageSpinner.setSelection(pos);
        }

        TextView actionLabel = view.findViewById(R.id.actionlabel);
        if(mode == Mode.EDIT){
            actionLabel.setText(getString(R.string.label_edit_dict));
        }
        else{
            actionLabel.setText(getString(R.string.label_new_dict));
        }

        final Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setVisibility(View.VISIBLE);
        if(mode == Mode.NEW){
            deleteButton.setVisibility(View.INVISIBLE);
        }

        int boxcount = 5;
        if(currentDict != null) {
            boxcount = currentDict.getBoxcount();
        }
        final Spinner boxSpinner = view.findViewById(R.id.boxcountspinner);
        for (int i = 0; i < boxSpinner.getCount(); i++) {
            String cvalue = (String) boxSpinner.getAdapter().getItem(i);
            if(Integer.valueOf(cvalue) == boxcount){
                boxSpinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Adapter for flags in language selection.
     */
    private class FlagAdapter extends BaseAdapter {

        /**
         * List of languages to show.
         * Usually identical to DictionaryFragment.languages
         */
        private List<String> flags;

        /**
         *
         * @param flags list of languages which can be selected
         */
        public FlagAdapter(List<String> flags) {
            this.flags = flags;
        }

        @Override
        public int getCount() {
            return flags.size();
        }

        @Override
        public Object getItem(int i) {
            return flags.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.flag_item, parent, false);
            }

            TextView langtext = (TextView) convertView.findViewById(R.id.flag_language_text);
            ImageView flag = (ImageView) convertView.findViewById(R.id.flagview);

            String language = flags.get(position);
            PictureHelper ph = new PictureHelper(getActivity());
            int r1 = ph.getDrawableResourceForLanguage(language);
            if(flag != null) {
                flag.setImageResource(r1);
            }

            langtext.setText(ph.getDisplaynameForLanguage(language));

            return convertView;
        }
    }

    /**
     * Crate flag adapter and connect it to spinner.
     * @param rootView
     */
    protected void setupLanguageSpinner(View rootView){
        int[] ids = new int[]{R.id.flag_selection1, R.id.flag_selection2};
        for(int id: ids) {
            Spinner languageSpinner = rootView.findViewById(id);
            List<String> flags = languages;
            FlagAdapter fa = new FlagAdapter(flags);
            languageSpinner.setAdapter(fa);

            if(id == R.id.flag_selection1){
                int germanIndex = languages.indexOf("Deutsch");
                if(germanIndex < 0){
                    germanIndex = 0;
                }
                languageSpinner.setSelection(germanIndex);
            }

            if(id == R.id.flag_selection2){
                int englishIndex = languages.indexOf("Englisch");
                if(englishIndex < 0){
                    englishIndex = 1;
                }
                languageSpinner.setSelection(englishIndex);
            }
        }
    }

    /**
     * Mode type for this fragment's use.
     * NEW: Create new dictionary
     * EDIT: adapt existing dictionary
     */
    public enum Mode {
        NEW, EDIT
    }

    /**
     * Currently active mode of this fragment
     */
    private Mode mode = Mode.NEW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] allLanguages = getActivity().getResources().getStringArray(R.array.all_languages_array);
        languages = Arrays.asList(allLanguages);

        setRetainInstance(true);

        Bundle arguments = getArguments();
        if(arguments != null) {
            int paramvalue = arguments.getInt(PARAMMODE, Mode.NEW.ordinal());
            mode = Mode.values()[paramvalue];
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.dictionarysettings_fragment, container, false);

        setupLanguageSpinner(result);

        final EditText nameview = result.findViewById(R.id.dictionary_name);
        final DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());

        reloadDictionaryData(result, mode == Mode.EDIT);

        final Button saveButton = result.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newDict = nameview.getText().toString();

                boolean existing = dm.dictionaryExists(newDict);

                if(mode == Mode.NEW && existing){
                    Toast.makeText(getActivity(), getString(R.string.toast_name_used), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mode == Mode.EDIT){
                    Dictionary selected = dm.getSelectedDictionary();
                    if(! newDict.equals(selected.getName())){
                        boolean renamed = dm.renameDictionary(selected.getName(), newDict);
                        if(renamed){
                            Toast.makeText(getActivity(), getString(R.string.toast_dict_renamed, newDict), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity(), getString(R.string.toast_dict_not_renamed), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                else {
                    dm.addDictionary(newDict);
                }
                dm.selectDictionary(newDict);

                Dictionary dict = dm.getSelectedDictionary();
                Spinner languageSpinner = getView().findViewById(R.id.flag_selection2);
                Spinner baselanguageSpinner = getView().findViewById(R.id.flag_selection1);
                Spinner boxcountSpinner = getView().findViewById(R.id.boxcountspinner);
                String language = (String) languageSpinner.getSelectedItem();
                String baselanguage = (String) baselanguageSpinner.getSelectedItem();

                if(language != null && baselanguage != null && language.equals(baselanguage)){
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.alert_save_title))
                            .setMessage(getString(R.string.toast_error_samelanguage))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.label_ok, null)
                            .show();
                    return;
                }

                int boxcount = Integer.valueOf((String) boxcountSpinner.getSelectedItem());
                dict.setLanguage(language);
                dict.setBaseLanguage(baselanguage);
                dict.setBoxcount(boxcount);
                dict.save(getActivity());

                String toast = getString(R.string.toast_new_dict_saved, newDict);
                if(mode==Mode.EDIT){
                    toast = getString(R.string.toast_edit_dict_saved, newDict);
                }

                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();

                if(mode == Mode.NEW){
                    mode = Mode.EDIT;
                }

                reloadDictionaryData(null, true);
            }
        });

        final Button deleteButton = result.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deleteDict = nameview.getText().toString();
                dm.deleteDictionary(deleteDict);

                Toast.makeText(getActivity(), getString(R.string.toast_deleted_dict, deleteDict), Toast.LENGTH_SHORT).show();
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
