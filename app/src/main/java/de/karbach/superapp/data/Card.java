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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;

/**
 * Created by Carsten on 27.12.2015.
 *
 * Stores one card in the cardbox. A card is a pair of two words in
 * two different languages. The first language is the root language,
 * which is the user's mother tongue. The second language is the translation.
 * A card can have different other attributes, by which cards can be grouped.
 * E.g. a card has a box, is assigned to a lesson and to a word type.
 * The box is most important for the learning functionality.
 * A card starts in box 1 and is moved into higher boxes, if they are
 * successfully tested. If the user cannot translate the card successfully
 * the card is moved back into lower boxes.
 */
public class Card implements Serializable {

    /**
     * Vocabulary in first language
     */
    private String lang1;
    /**
     * Vocabulary in second language
     */
    private String lang2;

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }

    /**
     * Box level, starting from 1 going up
     */
    private int box = 1;
    /**
     * The type of the word, e.g. adjective, verb, noun
     */
    private String type = null;

    /**
     * Name of the lesson, to which this card belongs to
     */
    private String lesson = null;

    public String getLesson() {
        return lesson;
    }

    public void setLesson(String lesson) {
        this.lesson = lesson;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLang2() {
        return lang2;
    }

    public void setLang2(String lang2) {
        this.lang2 = lang2;
    }

    public String getLang1() {
        return lang1;
    }

    public void setLang1(String lang1) {
        this.lang1 = lang1;
    }

    public Card(String lang1, String lang2) {
        this.lang1 = lang1;
        this.lang2 = lang2;
    }

    public boolean boxUp() {
        box++;
        if (box <= 5) {
            return true;
        } else {
            box = 5;
            return false;
        }
    }

    public boolean boxDown() {
        box--;
        if (box < 1) {
            box = 1;
            return false;
        } else {
            return true;
        }
    }

    public String export() {
        JSONArray array = new JSONArray();
        String[] values = new String[]{lang1, lang2, type, lesson, String.valueOf(box)};

        for (String value : values) {
            array.put(value);
        }

        return array.toString();
    }

    public static Card loadJSONOrCSV(String anyString, boolean loadAll){
        if(anyString.startsWith("[") ){
            return loadImported(anyString, loadAll);
        }
        else{
            return loadImportedCSV(anyString, loadAll);
        }
    }

    public static Card loadImportedCSV(String csvString, boolean loadAll) {
        if(csvString == null ){
            return null;
        }
        csvString = csvString.trim();
        String[] delimiters = new String[]{";", ",", "|"};
        String delimiter = ";";
        //Find the used delimiter in this string
        for (String pd : delimiters) {
            if (csvString.contains(pd)) {
                delimiter = pd;
                break;
            }
        }

        String[] array = csvString.split(delimiter);

        String lang1 = null;
        if (array.length > 0) {
            lang1 = array[0];
            lang1 = lang1.trim();
        }
        String lang2 = null;
        if (array.length > 1) {
            lang2 = array[1];
            lang2 = lang2.trim();
        }
        String type = "Substantiv";
        if (array.length > 2) {
            type = array[2];
        }
        String lesson = "1";
        if (array.length > 3) {
            lesson = array[3];
        }

        Card result = new Card(lang1, lang2);
        result.setType(type);
        result.setLesson(lesson);

        if (loadAll) {
            int box = 1;
            if (array.length > 4) {
                box = Integer.parseInt(array[4]);
            }
            result.setBox(box);
        }

        return result;
    }

    public static Card loadImported(String jsonString, boolean loadAll) {
        try {
            JSONArray array = new JSONArray(jsonString);

            String lang1 = null;
            if (array.length() > 0) {
                lang1 = array.getString(0);
            }
            String lang2 = null;
            if (array.length() > 1) {
                lang2 = array.getString(1);
            }
            String type = null;
            if (array.length() > 2 && !array.isNull(2)) {
                type = array.getString(2);
            }
            String lesson = null;
            if (array.length() > 3 && !array.isNull(3)) {
                lesson = array.getString(3);
            }

            Card result = new Card(lang1, lang2);
            result.setType(type);
            result.setLesson(lesson);

            if (loadAll) {
                int box = 1;
                if (array.length() > 4 && !array.isNull(4)) {
                    box = Integer.parseInt(array.getString(4));
                }
                result.setBox(box);
            }

            return result;

        } catch (JSONException e) {
            return null;
        }
    }

    public static String toSimpleString(String input){
        if(input == null){
            return null;
        }
        input = input.toLowerCase();

        input = input.replace('ä', 'a');
        input = input.replace('å', 'a');
        input = input.replace('ö', 'o');
        input = input.replace('ü', 'u');
        input = input.replace(" ", "");

        return input;
    }

    public boolean matchesSearch(String simpleSearch){
        if(simpleSearch == null){
            return false;
        }

        if(type != null){
            String check = toSimpleString(type);
            if(check.equals(simpleSearch)){
                return true;
            }
        }
        if(lesson != null){
            String check = toSimpleString(lesson);
            if(check.equals(simpleSearch)){
                return true;
            }
        }
        if(lang1 != null){
            String check = toSimpleString(lang1);
            if(check.contains(simpleSearch)){
                return true;
            }
        }
        if(lang2 != null){
            String check = toSimpleString(lang2);
            if(check.contains(simpleSearch)){
                return true;
            }
        }

        return false;
    }
}
