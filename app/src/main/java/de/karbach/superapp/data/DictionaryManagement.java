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

import android.content.Context;
import android.content.SharedPreferences;

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

    /**
     * Key for shared preferences where to store all dictionary names the user created
     */
    private static final String LANGUAGES_ARRAY = "de.karbach.superapp.data.DictionaryManagement.languageArray";

    /**
     *
     * @return set of dictionary names available
     */
    public Set<String> readDictionaryList(){
        Set<String> defaultSet = new HashSet<String>();
        String[] defaults = context.getResources().getStringArray(R.array.default_languages_array);
        defaultSet.addAll(Arrays.asList(defaults));

        SharedPreferences sp = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return sp.getStringSet(DictionaryManagement.LANGUAGES_ARRAY, defaultSet);
    }

    /**
     *
     * @return set of dictionary names as String array
     */
    public String[] readDictionaryArray(){
        Set<String> set = readDictionaryList();
        String[] result = set.toArray(new String[0]);

        return result;
    }

    /**
     * Init DictionaryManagement in Singleton context
     * @param context
     */
    private DictionaryManagement(Context context){
        this.context = context;
        dicts = new ArrayList<Dictionary>();

        //Load or create default dictionaries
        List<String> defaultLanguages = Arrays.asList( context.getResources().getStringArray(R.array.default_languages_array) );
        List<String> dictImports = Arrays.asList( context.getResources().getStringArray(R.array.default_languages_dictionary_samples) );
        Set<String> languages = readDictionaryList();
        for(String lang: languages){
            addDictionary(lang);
            Dictionary newDict = getDictionary(lang);
            if(defaultLanguages.contains(lang) && newDict != null && !newDict.dictionaryFileExists(context)){
                int defaultIndex = defaultLanguages.indexOf(lang);
                if(defaultIndex >= 0 && defaultIndex < dictImports.size()){
                    String importSampleString = dictImports.get(defaultIndex);
                    Dictionary sampleFromString = Dictionary.loadImported(importSampleString, true);
                    sampleFromString.setName(newDict.getName());
                    sampleFromString.setLanguage(newDict.getLanguage());
                    sampleFromString.setBaseLanguage(newDict.getBaseLanguage());
                    integrateDictionary(sampleFromString);
                }
            }
        }

        String selected = getStoredSelectedDictionaryName();
        if(selected != null){
            selectDictionary(selected);
        }
    }

    /**
     * Get singleton of dictionary management for the given context
     * @param context
     * @return singleton instance
     */
    public static DictionaryManagement getInstance(Context context){
        if(instance != null){
            return instance;
        }
        instance = new DictionaryManagement(context);
        return instance;
    }

    /**
     * Store the selected dictionary name in preferences
     * @param selectedName
     */
    protected void storeSelectedInPreferences(String selectedName){
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(languageSelectKey, selectedName);
        editor.commit();
    }

    /**
     *
     * @return the stored dictionary name, which was selected
     */
    protected String getStoredSelectedDictionaryName(){
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return sharedPref.getString(languageSelectKey, null);
    }

    /**
     * Select a dictionary. The selected dictionary is used in many activities
     * e.g. for finding words for boxes or for editing the dictionary preferences.
     * Creates new dictionary if no dictionary with that name exists.
     * @param name
     * @return the selected dictionary
     */
    public Dictionary selectDictionary(String name){
        if(name == null){
            selected = null;
            storeSelectedInPreferences(name);
            return null;
        }
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

    /**
     * Directly add a dictionary into the list of dictionaries
     * @param dict
     * @return true on success, false on error
     */
    public boolean addDictionaryObject(Dictionary dict){
        if(dict == null || dict.getName() == null){
            return false;
        }
        if(getDictionary(dict.getName()) != null){
            return false;
        }

        dicts.add(dict);

        Set<String> existing = readDictionaryList();
        if(! existing.contains(dict.getName())){
            existing.add(dict.getName());
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putStringSet(DictionaryManagement.LANGUAGES_ARRAY, existing).commit();
        }

        return true;
    }

    /**
     * Create a new dictionary, load data if available, add dictionary to the dicts variable.
     * @param name
     */
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

        addDictionaryObject(newDict);
    }

    /**
     * Get access to a dictionary with the given name
     * @param name
     * @return found dictionary, null if none exists with that name
     */
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

    /**
     *
     * @param name
     * @return true if dictionary exists for that name, false if not
     */
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
     * Rename a dictionary from old to new name
     * @param oldname
     * @param newName
     * @return true on success, false on error
     */
    public boolean renameDictionary(String oldname, String newName){
        if(newName == null || newName.length()<=0){
            return false;
        }

        Dictionary dict = getDictionary(oldname);
        if(dict == null){
            return false;
        }

        deleteDictionary(oldname);

        dict.setName(newName);

        return addDictionaryObject(dict);
    }

    /**
     *
     * @return might return null, if selectDictionary was not yet executed
     */
    public Dictionary getSelectedDictionary(){
        return selected;
    }

    /**
     * Replace an existing dictionary object with the name of newDictionary with the given value.
     * @param newDictionary
     */
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

    /**
     * Merge cards of newDictionary into the existing dictionary with the same name.
     * Afterwards, the existing dictionary will contain the old cards and the new ones.
     * If there are cards with the same lang1 value, these are updated.
     * @param newDictionary
     */
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

    /**
     * Save data for all dictionaries.
     * Saves them to file.
     */
    public void saveAll(){
        for(Dictionary dict: dicts){
            dict.save(context);
        }
    }
}
