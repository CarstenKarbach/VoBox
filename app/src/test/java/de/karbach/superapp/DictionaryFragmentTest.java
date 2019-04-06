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

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.Spinner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

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

    @Test
    public void testSaveSameLanguages(){
        Dictionary dict = new Dictionary("mydict");
        DictionaryActivity activity = Robolectric.buildActivity(DictionaryActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(activity);
        dm.addDictionaryObject(dict);
        dm.selectDictionary("mydict");

        Spinner languageSpinner = activity.findViewById(R.id.flag_selection2);
        Spinner baselanguageSpinner = activity.findViewById(R.id.flag_selection1);

        languageSpinner.setSelection(0);
        baselanguageSpinner.setSelection(0);

        final Button saveButton = activity.findViewById(R.id.save_button);
        saveButton.performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlert = Shadows.shadowOf(dialog);
        assertEquals(activity.getResources().getString(R.string.toast_error_samelanguage), shadowAlert.getMessage());

        //Try to successfully save
        ShadowAlertDialog.reset();

        baselanguageSpinner.setSelection(1);
        saveButton.performClick();

        dialog = ShadowAlertDialog.getLatestAlertDialog();

        assertNull(dialog);
    }
}
