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

import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

import static org.junit.Assert.*;

/**
 * Start dictionary activity and click buttons.
 */
@RunWith(RobolectricTestRunner.class)
public class DictionaryFragmentTest {

    @Test
    public void testButtonClicks(){
        DictionaryActivity activity = Robolectric.buildActivity(DictionaryActivity.class).setup().get();
        final Button saveButton = activity.findViewById(R.id.save_button);
        saveButton.performClick();

        final Button deleteButton = activity.findViewById(R.id.delete_button);
        deleteButton.performClick();
    }

    @Test
    public void testCardUpWithDifferentMaxValue(){
        DictionaryActivity activity = Robolectric.buildActivity(DictionaryActivity.class).setup().get();

        Dictionary dict = new Dictionary("mydict");
        dict.setBoxcount(2);
        Card card = new Card("a", "b");

        DictionaryManagement dm  = DictionaryManagement.getInstance(activity);
        dm.addDictionaryObject(dict);
        dm.selectDictionary(dict.getName());

        assertEquals(1, card.getBox());
        card.boxUp(activity);
        assertEquals(2, card.getBox());
        card.boxUp(activity);
        assertEquals(2, card.getBox());

        dict.setBoxcount(3);
        card.boxUp(activity);
        assertEquals(3, card.getBox());
        card.boxUp(activity);
        assertEquals(3, card.getBox());

    }
}
