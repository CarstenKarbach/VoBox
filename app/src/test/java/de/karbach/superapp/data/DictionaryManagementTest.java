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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Set;

import de.karbach.superapp.StarterActivity;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DictionaryManagementTest {

    @Test
    public void testReadDictionaryList(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);
        Set<String> dictList = dm.readDictionaryList();
        assertTrue(dictList.size() > 0);
    }

    @Test
    public void testSelectNewDictionary(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);
        int oldLength = dm.readDictionaryArray().length;
        Dictionary dict = dm.selectDictionary("hahagibtsnicht");
        assertEquals("hahagibtsnicht", dict.getName());
        int newLength = dm.readDictionaryArray().length;

        assertEquals(oldLength+1, newLength);

        assertTrue(dm.dictionaryExists("hahagibtsnicht"));

        dm.deleteDictionary("hahagibtsnicht");

        newLength = dm.readDictionaryArray().length;
        assertEquals(oldLength, newLength);
    }

    @Test
    public void testReplaceDictionary(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);

        Dictionary newenglish = new Dictionary("Englisch");
        Dictionary existing = dm.getDictionary("Englisch");
        assertNotNull(existing);

        newenglish.addCard(new Card("a", "b"));
        assertEquals(0, existing.getCards().size());
        assertEquals(1, newenglish.getCards().size());

        dm.replaceDictionary(newenglish);
        existing = dm.getDictionary("Englisch");
        assertEquals(existing, newenglish);

        assertEquals(1, existing.getCards().size());
    }

    @Test
    public void testIntegrateDictionary(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);

        dm.selectDictionary("Englisch");

        Dictionary newenglish = new Dictionary("Englisch");
        Dictionary existing = dm.getDictionary("Englisch");
        assertNotNull(existing);

        for(int i=0; i< existing.getCards().size(); i++){
            existing.deleteCard(i);
        }

        newenglish.addCard(new Card("a", "b"));
        newenglish.addCard(new Card("new", "english"));

        existing.addCard(new Card("c", "d"));
        dm.saveAll();

        assertEquals(1, existing.getCards().size());
        assertEquals(2, newenglish.getCards().size());

        dm.integrateDictionary(newenglish);

        assertEquals(3, existing.getCards().size());
    }

    @Test
    public void testRename(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        DictionaryManagement dm = DictionaryManagement.getInstance(activity);

        Dictionary newDict = new Dictionary("jupp");
        dm.addDictionaryObject(newDict);

        Dictionary found = dm.getDictionary("jupp");
        assertNotNull(found);

        boolean success = dm.renameDictionary("jupp", "juppie");
        assertTrue(success);
        assertNull(dm.getDictionary("jupp"));
        assertNotNull(dm.getDictionary("juppie"));
    }

}


