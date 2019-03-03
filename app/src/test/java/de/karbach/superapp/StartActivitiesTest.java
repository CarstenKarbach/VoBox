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
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import androidx.test.core.app.ApplicationProvider;
import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

import static android.view.MotionEvent.ACTION_DOWN;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class StartActivitiesTest {

    @Test
    public void startActivityTest() {
        StarterActivity activity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        assertFalse(activity.showUpButton());
    }

    @Test
    public void startDictionaryActivity(){
        DictionaryActivity activity = Robolectric.buildActivity(DictionaryActivity.class).setup().get();
        assertTrue(activity.showUpButton());
    }

    @Test
    public void startCardActivity(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

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
        ListGeneratorActivity activity = Robolectric.buildActivity(ListGeneratorActivity.class).setup().get();
    }

    @Test
    public void startTestActivity(){
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    }

    @Test
    public void testPictureHelperNull(){
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
        PictureHelper ph = new PictureHelper(activity);
        int res = ph.getDrawableResourceForLanguage(null);
        assertEquals(R.drawable.flag_german, res);
    }

    @Test
    public void startBoxActivity(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.selectDictionary("Englisch");
        //Put some cards in all the boxes
        Dictionary dict = dm.getSelectedDictionary();
        for(int i=0; i<5; i++){
            for(int j=0; j<10; j++){
                Card card = new Card(String.valueOf(i)+"//"+String.valueOf(j), String.valueOf(i)+"//"+String.valueOf(j+j));
                card.setBox(i+1);
                dict.addCard(card);
            }
        }

        ActivityController<BoxActivity> controller = Robolectric.buildActivity(BoxActivity.class);

        controller.create().start().postCreate(null).resume();

        BoxActivity ba = controller.get();
        ShadowActivity shadow = Shadows.shadowOf(ba);

        for(int box=1; box<=5; box++) {
            BoxView bv = ba.findViewById(BoxFragment.boxids[box - 1]);
            assertNotNull(bv);
            bv.fling(10000);
            bv.fling(-10000);

            FragmentManager fm = ba.getFragmentManager();
            Fragment f = fm.findFragmentById(R.id.fragment_container);
            assertTrue(f instanceof BoxFragment);

            BoxFragment bf = (BoxFragment) f;
            bf.showPopup(box);

            bf.startBoxTraining(box, false);

            Intent expectedIntentTest = new Intent(ba, TestActivity.class);
            Intent expectedIntentList = new Intent(ba, CardListActivity.class);
            Intent actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.startBoxTraining(box, true);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.showList(box);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentList.getComponent());
        }
    }
}
