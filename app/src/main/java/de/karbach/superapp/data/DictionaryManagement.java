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

package de.karbach.superapp.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.karbach.superapp.R;

/**
 * Created by Carsten on 28.12.2015.
 *
 * This is the main point for storing and accessing dictionaries.
 * For each language one dictionary is stored and can be accessed from
 * everywhere in the app via the Singleton pattern. One dictionary is
 * marked as the selected dictionary, which is the one the user selects
 * on the starter app (StarterActivity). Convenient save and store
 * functions are part of the DictionaryManagement functionality.
 *
 * Most of the activities use the selected dictionary to work on.
 * E.g. when a new card is added, it is automatically added to the
 * currently selected dictionary. Or if a list of all cards should be
 * shown, the cards of the selected dictionary is shown.
 */
public class DictionaryManagement {

    /**
     * Key in preferences to store the selected dictionary
     */
    private static final String languageSelectKey = "de.karbach.superapp.data.DictionaryManagement.languageSelection";
    /**
     * Name of the preferences for the DictionaryManagement class
     */
    private static final String preferencesName = "de.karbach.superapp.data.DictionaryManagement";

    /**
     * A list of all available dictionaryies
     */
    private List<Dictionary> dicts;

    /**
     * The currently selected dictionary or null, if non was selected yet
     */
    private Dictionary selected;
    /**
     * Singleton instance of this class
     */
    private static DictionaryManagement instance;
    /**
     * Context from the app to save and load files
     */
    private Context context;

    private static final String LANGUAGES_ARRAY = "de.karbach.superapp.data.DictionaryManagement.languageArray";

    public Set<String> readDictionaryList(){
        Set<String> defaultSet = new HashSet<String>();
        String[] defaults = context.getResources().getStringArray(R.array.languages_array);
        defaultSet.addAll(Arrays.asList(defaults));

        SharedPreferences sp = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return sp.getStringSet(DictionaryManagement.LANGUAGES_ARRAY, defaultSet);
    }

    public String[] readDictionaryArray(){
        Set<String> set = readDictionaryList();
        String[] result = set.toArray(new String[0]);

        return result;
    }

    private DictionaryManagement(Context context){
        this.context = context;
        dicts = new ArrayList<Dictionary>();

        //Load or create default dictionaries
        Set<String> languages = readDictionaryList();
        for(String lang: languages){
            addDictionary(lang);
        }

        String selected = getStoredSelectedDictionaryName();
        if(selected != null){
            selectDictionary(selected);
        }
    }

    public static DictionaryManagement getInstance(Context context){
        if(instance != null){
            return instance;
        }
        instance = new DictionaryManagement(context);
        return instance;
    }

    protected void storeSelectedInPreferences(String selectedName){
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(languageSelectKey, selectedName);
        editor.commit();
    }

    protected String getStoredSelectedDictionaryName(){
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return sharedPref.getString(languageSelectKey, null);
    }

    public Dictionary selectDictionary(String name){
        for(Dictionary dict: dicts){
            if(dict.getName().equals(name)){
                selected = dict;
                storeSelectedInPreferences(name);
                return selected;
            }
        }

        //Not found => create new
        addDictionary(name);
        Dictionary newDict = getDictionary(name);
        if(newDict != null){
            return selectDictionary(name);
        }
        return null;
    }

    public void addDictionary(String name){
        for(Dictionary dict: dicts){
            if(dict.getName().equals(name)){
                return;
            }
        }

        Dictionary newDict = new Dictionary(name);
        newDict.setLanguage(name);
        newDict.loadIfPossible(context);
        newDict.setName(name);
        dicts.add(newDict);

        Set<String> existing = readDictionaryList();
        if(! existing.contains(name)){
            existing.add(name);
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putStringSet(DictionaryManagement.LANGUAGES_ARRAY, existing).commit();
        }
    }

    public Dictionary getDictionary(String name){
        for(Dictionary dict: dicts){
            if(name == null && dict.getName() == null){
                return dict;
            }
            if(name != null && name.equals(dict.getName()) ){
                return dict;
            }
        }
        return null;
    }

    public boolean dictionaryExists(String name){
        return getDictionary(name) != null;
    }

    public void deleteDictionary(String name){
        Dictionary dict = getDictionary(name);
        if(dict != null){
            dicts.remove(dict);
            dict.deleteFile(context);
        }

        Set<String> existing = readDictionaryList();
        if(existing.contains(name)){
            existing.remove(name);
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putStringSet(DictionaryManagement.LANGUAGES_ARRAY, existing).commit();
        }

        Dictionary selected = getSelectedDictionary();
        if(selected != null && selected.getName().equals(name)){
            String[] others = readDictionaryArray();
            if(others.length > 0){
                selectDictionary(others[0]);
            }
        }
    }

    /**
     *
     * @return might return null, if selectDictionary was not yet executed
     */
    public Dictionary getSelectedDictionary(){
        return selected;
    }

    public void replaceDictionary(Dictionary newDictionary){
        int existingPos = -1;
        String newName = newDictionary.getName();
        Dictionary selected = getSelectedDictionary();
        for(int i=0; i<dicts.size(); i++){
            Dictionary dict = dicts.get(i);
            if(newName.equals(dict.getName())){
                existingPos = i;
                break;
            }
        }

        if(existingPos != -1){
            dicts.set(existingPos, newDictionary);
            if(selected != null){
                this.selectDictionary(selected.getName());
            }
        }

        saveAll();
    }

    public void integrateDictionary(Dictionary newDictionary){
        Dictionary existingDict = getDictionary(newDictionary.getName());
        if(existingDict == null){
            return;
        }

        for(Card card: newDictionary.getCards()){
            existingDict.addCard(card);
        }

        saveAll();
    }

    public void saveAll(){
        for(Dictionary dict: dicts){
            dict.save(context);
        }
    }
}
