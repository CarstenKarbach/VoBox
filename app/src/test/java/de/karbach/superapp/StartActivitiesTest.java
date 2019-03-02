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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class StartActivitiesTest {

    @Test
    public void startActivityTest() {
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        assertFalse(activity.showUpButton());
    }

    @Test
    public void startDictionaryActivity(){
        DictionaryActivity activity = Robolectric.setupActivity(DictionaryActivity.class);
        assertTrue(activity.showUpButton());
    }

    @Test
    public void startCardActivity(){
        StarterActivity starteractivity = Robolectric.setupActivity(StarterActivity.class);

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary selected = dm.getSelectedDictionary();
        Card exampleCard = new Card("Beispiel", "example");
        assertEquals("example", exampleCard.getLang2());
        selected.addCard(exampleCard);

        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(CardFragment.PARAMLANG1KEY, "Beispiel");
        ActivityController<CardActivity> actController = Robolectric.buildActivity(CardActivity.class);
        actController.get().setIntent(intent);
        actController.create();

        Dictionary dict = dm.getSelectedDictionary();
        assertNotNull(dict.getBaseLanguage());
        System.out.println(dict.getBaseLanguage());

        //Button clicks
        CardActivity ca = actController.get();
        final TextView lang2 = (TextView) ca.findViewById(R.id.lang2_text);
        lang2.setText("example_test");
        Button save = (Button) ca.findViewById(R.id.save_button);
        save.performClick();
        selected = dm.getSelectedDictionary();
        exampleCard = selected.getCardByLang1("Beispiel");
        assertEquals("example_test", exampleCard.getLang2());

        Button delete = (Button) ca.findViewById(R.id.delete_button);
        delete.performClick();
        exampleCard = selected.getCardByLang1("Beispiel");
        assertNull(exampleCard);
    }

    @Test
    public void startListGenActivity(){
        ListGeneratorActivity activity = Robolectric.setupActivity(ListGeneratorActivity.class);
    }

    @Test
    public void startTestActivity(){
        TestActivity activity = Robolectric.setupActivity(TestActivity.class);
    }

    @Test
    public void testPictureHelperNull(){
        TestActivity activity = Robolectric.setupActivity(TestActivity.class);
        PictureHelper ph = new PictureHelper(activity);
        int res = ph.getDrawableResourceForLanguage(null);
        assertEquals(R.drawable.flag_german, res);
    }
}
