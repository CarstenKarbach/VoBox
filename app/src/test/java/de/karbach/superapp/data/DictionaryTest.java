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
package de.karbach.superapp.data;

import android.os.Environment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.IOException;

import de.karbach.superapp.StarterActivity;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DictionaryTest {

    @Test
    public void testCreation(){
        Dictionary dict = new Dictionary("test");
        assertEquals("test", dict.getName());
    }

    @Test
    public void testAddCard(){
        Dictionary dict = new Dictionary("test");
        Card c = new Card("a", "b");
        dict.addCard(c);
        assertEquals(1, dict.getCards().size());

        dict.addCard(null);
        assertEquals(1, dict.getCards().size());

        Card emptyLang = new Card(null, null);
        dict.addCard(emptyLang);
        assertEquals(1, dict.getCards().size());

        dict.addCard(c);
        assertEquals(1, dict.getCards().size());

        c.setLesson("dont care");
        dict.addCard(c);
        assertEquals(1, dict.getCards().size());
        assertEquals("dont care", dict.getCards().get(0).getLesson());

        dict.deleteCard(0);
        assertEquals(0, dict.getCards().size());

        dict.addCard(c);
        assertEquals(1, dict.getCards().size());

        Card another = new Card("b", "c");
        Card another2 = new Card("b2", "c2");
        dict.addCard(another);
        dict.addCard(another2);
        assertEquals(3, dict.getCards().size());

        dict.deleteCard(c);
        dict.deleteCard(another);
        dict.deleteCard(another2);
        assertEquals(0, dict.getCards().size());
    }

    @Test
    public void testGetCardByLang1(){
        Dictionary dict = new Dictionary("test");
        Card card1 = new Card("a", "b");
        Card card2 = new Card("c", "d");
        dict.addCard(card1);dict.addCard(card2);

        Card found = dict.getCardByLang1("a");
        assertEquals(card1, found);
        found = dict.getCardByLang1("c");
        assertEquals(card2, found);
        found = dict.getCardByLang1("b");
        assertNull(found);
    }

    @Test
    public void testGetPosForCard(){
        Dictionary dict = new Dictionary("test");
        Card card1 = new Card("a", "b");
        Card card2 = new Card("c", "d");
        dict.addCard(card1);dict.addCard(card2);

        assertEquals(0, dict.getPosForCard(card1));
        assertEquals(1, dict.getPosForCard(card2));
    }

    @Test
    public void testGetFilenameForStore(){
        Dictionary dict = new Dictionary("test");
        assertNotNull(dict.getFilenameForStore());
        assertNotNull(dict.getFilenameForStore("csv"));
    }

    @Test
    public void testLoadIfPossible(){
        StarterActivity activity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        Dictionary dict = new Dictionary("test");
        dict.loadIfPossible(activity);
        assertEquals(0, dict.getCards().size());
    }

    @Test
    public void testSaveAndLoad(){
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        StarterActivity activity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        Dictionary dict = new Dictionary("test");
        Card card1 = new Card("a", "b");
        Card card2 = new Card("c", "d");
        dict.addCard(card1);dict.addCard(card2);
        boolean result = dict.save(activity);
        assertTrue(result);

        Dictionary dict2 = new Dictionary("test");
        assertEquals("test", dict2.getName());
        dict2.loadIfPossible(activity);
        dict2.setName("test");
        assertEquals(2, dict2.getCards().size());
        assertNotEquals(dict2.getCards().get(0), dict.getCards().get(0));
        assertEquals(dict2.getCards().get(0).getLang1(), dict.getCards().get(0).getLang1());

        boolean success = dict2.saveToObj(activity);
        assertTrue(success);
        try {
            dict2.loadFromObj(activity);
        } catch (IOException e) {
           e.printStackTrace();
            fail("IOException Should not happen");
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException Should not happen");
        }
        assertEquals(2, dict2.getCards().size());
        assertNotEquals(dict2.getCards().get(0), dict.getCards().get(0));
        assertEquals(dict2.getCards().get(0).getLang1(), dict.getCards().get(0).getLang1());

        dict2.sendExportedDictionary(activity);

        dict2.deleteFile(activity);
        dict2.loadIfPossible(activity);
        assertEquals(2, dict2.getCards().size());
    }

    @Test
    public void testGetCardsForBox(){
        Dictionary dict = new Dictionary("test");
        Card card1 = new Card("a", "b");
        Card card2 = new Card("c", "d");
        dict.addCard(card1);dict.addCard(card2);

        assertEquals(2, dict.getCardsForBox(1).size());
        assertEquals(0, dict.getCardsForBox(2).size());
        assertEquals(0, dict.getCardsForBox(20).size());

        card1.boxUp();
        card2.boxUp();
        assertEquals(0, dict.getCardsForBox(1).size());
        assertEquals(2, dict.getCardsForBox(2).size());
    }
}
