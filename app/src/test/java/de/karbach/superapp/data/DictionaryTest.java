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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.R;
import de.karbach.superapp.StarterActivity;
import edu.emory.mathcs.backport.java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for a dictionary.
 */
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

        dict.deleteCard(77);
        Card nocard = dict.getCardByLang1(null);
        assertNull(nocard);
        int posnocard = dict.getPosForCard(nocard);
        assertEquals(-1, posnocard);

        boolean result = dict.saveToObj(null);
        assertFalse(result);
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
    public void testLoadExampleCSVFiles(){
        StarterActivity activity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        int[] rawExamples = new int[]{R.raw.example_de_en_csv, R.raw.example_de_po_csv, R.raw.example_sp_po_csv};

        for(int resid : rawExamples) {
            String content = activity.loadRawFile(resid);
            Dictionary dict = Dictionary.loadImported(content, true);
            assertTrue(dict.getCards().size() > 0);
        }

        String errorLoad = activity.loadRawFile(R.array.default_languages_dictionary_samples);
        assertNull(errorLoad);
    }

    @Test
    public void testSaveAndLoad(){
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

        card1.boxUp(null);
        card2.boxUp(null);
        assertEquals(0, dict.getCardsForBox(1).size());
        assertEquals(2, dict.getCardsForBox(2).size());
    }

    @Test
    public void testloadImportedDictionary(){
        Dictionary dict = Dictionary.loadImported("Englisch;\nDeutsch;\nboxcount:7\n[\"ja\",\"yes\",null,null,\"1\"]\n[\"nein\",\"no\",null,null,\"1\"]\n[\"danke\",\"thank you\",null,null,\"1\"]", true);
        assertEquals("Englisch", dict.getLanguage());
        assertEquals("Deutsch", dict.getBaseLanguage());
        assertEquals(3, dict.getCards().size());
        assertEquals(7, dict.getBoxcount());
    }

    @Test
    public void testSanitizeLanguages(){
        Dictionary dict = Dictionary.loadImported("Deutsch;\nDeutsch;\nboxcount:7\n[\"ja\",\"yes\",null,null,\"1\"]\n[\"nein\",\"no\",null,null,\"1\"]\n[\"danke\",\"thank you\",null,null,\"1\"]", true);
        assertNotEquals(dict.getBaseLanguage(), dict.getLanguage());

        dict.setBaseLanguage("Deutsch");
        dict.setLanguage("Deutsch");
        dict.sanitizeLanguagesToDiffer();
        assertNotEquals(dict.getBaseLanguage(), dict.getLanguage());

        dict.setBaseLanguage("Deutsch");
        dict.setLanguage("Englisch");
        dict.sanitizeLanguagesToDiffer();
        assertEquals("Deutsch", dict.getBaseLanguage());
        assertEquals("Englisch", dict.getLanguage());

        dict.setBaseLanguage("Französisch");
        dict.setLanguage("Spanisch");
        dict.sanitizeLanguagesToDiffer();
        assertEquals("Französisch", dict.getBaseLanguage());
        assertEquals("Spanisch", dict.getLanguage());

        dict.setBaseLanguage("Französisch");
        dict.setLanguage("Französisch");
        dict.sanitizeLanguagesToDiffer();
        assertNotEquals(dict.getBaseLanguage(), dict.getLanguage());
    }

    @Test
    public void testSanitizeWithAllowedValues(){
        Dictionary dict = new Dictionary("sanit");
        dict.setBaseLanguage("Abc");
        dict.setLanguage("DEF");
        List<String> allowed = Arrays.asList(new String[]{"Abc", "DEF"});
        dict.sanitizeLanguagesWithAllowedValues(allowed);
        assertEquals("Abc", dict.getBaseLanguage());
        assertEquals("DEF", dict.getLanguage());

        allowed = Arrays.asList(new String[]{"Deutsch", "Englisch"});
        dict.sanitizeLanguagesWithAllowedValues(allowed);
        assertNotEquals("Abc", dict.getBaseLanguage());
        assertNotEquals("DEF", dict.getLanguage());
    }
}
