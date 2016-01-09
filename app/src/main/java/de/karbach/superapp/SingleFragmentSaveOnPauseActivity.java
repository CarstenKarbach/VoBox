package de.karbach.superapp;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 05.01.2016.
 */
public abstract class SingleFragmentSaveOnPauseActivity extends SingleFragmentActivity{

    @Override
    protected void onPause() {
        super.onPause();

        DictionaryManagement dm = DictionaryManagement.getInstance(this);
        Dictionary dict = dm.getSelectedDictionary();
        if(dict != null) {
            dict.save(this);
        }
    }

}
