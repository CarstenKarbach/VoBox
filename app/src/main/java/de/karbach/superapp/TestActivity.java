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

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 *
 * The activity for practising and testing vocabularies. Practise means
 * cards are not moved on success or failure. A real test really moves
 * the cards to higher or lower boxes.
 */
public class TestActivity extends SingleFragmentSaveOnPauseActivity {
    /**
     * Parameter for the tested box (int)
     */
    public static final String PARAMBOX = "de.karbach.superapp.TestActivity.BOX";
    /**
     * Parameter for a boolean value indicating, whether this is a real test or only practise.
     */
    public static final String PARAMREALTEST = "de.karbach.superapp.TestActivity.REALTEST";

    /**
     * Set a list of cards with this parameter to generate a test/training with a given list of cards
     */
    public static final String PARAMDIRECTCARDS = "de.karbach.superapp.TestActivity.DIRECTCARDS";

    @Override
    protected Fragment createFragment() {

        ArrayList<Card> testCards = (ArrayList<Card>) getIntent().getSerializableExtra(PARAMDIRECTCARDS);

        Boolean realTest = getIntent().getBooleanExtra(PARAMREALTEST, false);

        if(testCards == null) {
            int box = getIntent().getIntExtra(PARAMBOX, 1);
            DictionaryManagement dm = DictionaryManagement.getInstance(this);
            Dictionary dict = dm.getSelectedDictionary();

            ArrayList<Card> boxCards = dict.getCardsForBox(box);
            testCards = boxCards;
        }

        Bundle arguments  = new Bundle();
        arguments.putSerializable(TestFragment.PARAMTESTCARDS, testCards);
        arguments.putBoolean(TestFragment.PARAMISREALTEST, realTest);

        TestFragment result = new TestFragment();
        result.setArguments(arguments);

        return result;
    }
}
