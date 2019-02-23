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

import android.support.v4.widget.TextViewCompat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class CardTest {

    @Test
    public void cardCreation(){
        Card card = new Card("Wort", "word");
        assertEquals("Wort", card.getLang1());
        assertEquals("word", card.getLang2());
    }

    @Test
    public void cardAdditions(){
        Card card = new Card("Wort", "word");
        card.setLang1("Wort");card.setLang2("word");card.setLesson("Kapitel 1");card.setBox(3);card.setType("Adjektiv");
        assertEquals("Wort", card.getLang1());
        assertEquals("word", card.getLang2());
        assertEquals("Kapitel 1", card.getLesson());
        assertEquals("Adjektiv", card.getType());
        assertEquals(3, card.getBox());

        card.setLang1("Wort2");
        card.setLang1("Wort3");
    }

    @Test
    public void boxStatus(){
        Card card = new Card("Wort", "word");
        card.setBox(1);
        for(int i=1; i<=5; i++){
            assertEquals(i, card.getBox());
            card.boxUp();
        }
        assertEquals(5, card.getBox());
        card.boxDown();
        for(int i=4; i>=1; i--){
            assertEquals(i, card.getBox());
            card.boxDown();
        }
        assertEquals(1, card.getBox());
    }

    @Test
    public void exportImportCards(){
        Card card = new Card("Wort", "word");
        card.setType("a type");card.setLesson("a lesson");
        String json = card.export();
        assertTrue(json.contains("Wort"));
        assertTrue(json.contains("word"));
        Card reimported = Card.loadImported(json, true);
        assertNotEquals(card, reimported);
        assertEquals(card.getLang1(), reimported.getLang1());
        assertEquals(card.getLang2(), reimported.getLang2());
        assertEquals(card.getBox(), reimported.getBox());

        Card reimported2 = Card.loadJSONOrCSV(json, true);
        assertNotEquals(card, reimported2);
        assertEquals(card.getLang1(), reimported2.getLang1());
        assertEquals(card.getLang2(), reimported2.getLang2());
        assertEquals(card.getBox(), reimported2.getBox());
    }

    @Test
    public void loadFromCSV(){
        String csv = "Wort;word;Typ;3;2";
        Card fromcsv = Card.loadImportedCSV(csv, true);
        assertEquals("Wort", fromcsv.getLang1());
        assertEquals("word", fromcsv.getLang2());
        assertEquals(2, fromcsv.getBox());

        Card fromcsv2 = Card.loadJSONOrCSV(csv, true);
        assertEquals("Wort", fromcsv2.getLang1());
        assertEquals("word", fromcsv2.getLang2());
        assertEquals(2, fromcsv2.getBox());

        Card loadedfromempty=Card.loadImported("", true);
        assertNull(loadedfromempty);
        loadedfromempty=Card.loadImportedCSV(null, true);
        assertNull(loadedfromempty);
    }

    @Test
    public void searchMatch(){
        String csv = "Wort;word;Typ;3;2";
        Card fromcsv = Card.loadImportedCSV(csv, true);
        assertFalse(fromcsv.matchesSearch("öäü"));
        assertFalse(fromcsv.matchesSearch("Quatsch"));
        assertTrue(fromcsv.matchesSearch("word"));
        assertTrue(fromcsv.matchesSearch("wort"));
        assertTrue(fromcsv.matchesSearch("3"));
        assertTrue(fromcsv.matchesSearch("typ"));
        assertFalse(fromcsv.matchesSearch(null));

        assertNull( Card.toSimpleString(null) );
    }
}

