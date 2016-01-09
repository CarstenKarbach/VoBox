package de.karbach.superapp.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.R;

/**
 * Created by Carsten on 28.12.2015.
 */
public class DictionaryManagement {

    private static final String languageSelectKey = "de.karbach.superapp.data.DictionaryManagement.languageSelection";
    private static final String preferencesName = "de.karbach.superapp.data.DictionaryManagement";

    private List<Dictionary> dicts;

    private Dictionary selected;

    private static DictionaryManagement instance;

    private Context context;

    private DictionaryManagement(Context context){
        this.context = context;
        dicts = new ArrayList<Dictionary>();
        //Load or create default dictionaries
        String[] languages = context.getResources().getStringArray(R.array.languages_array);
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

        Dictionary newDict = new Dictionary(name);
        newDict.loadIfPossible(context);
        dicts.add(newDict);
        selected = newDict;
        storeSelectedInPreferences(name);
        return selected;
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
        dicts.add(newDict);
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

    public void deleteDictionary(String name){
        Dictionary dict = getDictionary(name);
        if(dict != null){
            dicts.remove(dict);
            dict.deleteFile(context);
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
