package de.karbach.superapp;

import android.app.Activity;
import android.widget.Spinner;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Connect dictionary adapter to a spinner holding all dictionaries.
 */
public class DictSpinnerPresenter {

    private Activity activity;
    private Spinner dictSpinner;

    public DictSpinnerPresenter(Activity activity, Spinner dictSpinner){
        this.activity = activity;
        this.dictSpinner = dictSpinner;
    }

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

    public void freeMe(){
        this.activity = null;
        this.dictSpinner = null;
    }

}
