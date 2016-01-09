package de.karbach.superapp;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 */
public class TestActivity extends SingleFragmentSaveOnPauseActivity {

    public static final String PARAMBOX = "de.karbach.superapp.TestActivity.BOX";
    public static final String PARAMREALTEST = "de.karbach.superapp.TestActivity.REALTEST";

    @Override
    protected Fragment createFragment() {

        int box = getIntent().getIntExtra(PARAMBOX, 1);
        Boolean realTest = getIntent().getBooleanExtra(PARAMREALTEST, false);

        DictionaryManagement dm = DictionaryManagement.getInstance(this);
        Dictionary dict = dm.getSelectedDictionary();

        ArrayList<Card> boxCards = dict.getCardsForBox(box);

        Bundle arguments  = new Bundle();
        arguments.putSerializable(TestFragment.PARAMTESTCARDS, boxCards);
        arguments.putBoolean(TestFragment.PARAMISREALTEST, realTest);

        TestFragment result = new TestFragment();
        result.setArguments(arguments);

        return result;
    }
}
