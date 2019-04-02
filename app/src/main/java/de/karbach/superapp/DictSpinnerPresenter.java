package de.karbach.superapp;

import android.app.Activity;
import android.widget.Spinner;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Connect dictionary adapter to a spinner holding all dictionaries.
 */
public class DictSpinnerPresenter {

    /**
     * Context
     */
    private Activity activity;
    /**
     * Spinner view
     */
    private Spinner dictSpinner;

    /**
     * Store context and view
     * @param activity
     * @param dictSpinner
     */
    public DictSpinnerPresenter(Activity activity, Spinner dictSpinner){
        this.activity = activity;
        this.dictSpinner = dictSpinner;
    }

    /**
     * Update adapter with all available dictionaries.
     * Set the currently selected dictionary.
     */
    public void updateSelectedDictionary(){
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);

        DictionaryAdapter dapter = new DictionaryAdapter(dm.readDictionaryArray(), activity);
        dictSpinner.setAdapter(dapter);

        Dictionary selectedDict = dm.getSelectedDictionary();
        if(selectedDict != null) {
            for (int i = 0; i < dictSpinner.getCount(); i++) {
                String name = dictSpinner.getItemAtPosition(i).toString();
                if (name.equals(selectedDict.getName())) {
                    dictSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Release storage of activity and spinner view.
     * Call this, when view dictSpinner is destroyed or access to the presenter is nolonger needed.
     */
    public void freeMe(){
        this.activity = null;
        this.dictSpinner = null;
    }

}
