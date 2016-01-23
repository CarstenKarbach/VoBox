package de.karbach.superapp;


import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 28.12.2015.
 */
public class CardActivity extends SingleFragmentSaveOnPauseActivity {
    @Override
    protected Fragment createFragment() {
        CardFragment result =  new CardFragment();

        String lang1Key = getIntent().getStringExtra(CardFragment.PARAMLANG1KEY);
        if(lang1Key != null){
            Bundle arguments = new Bundle();
            arguments.putString(CardFragment.PARAMLANG1KEY, lang1Key);
            result.setArguments(arguments);
        }

        return result;
    }

}
