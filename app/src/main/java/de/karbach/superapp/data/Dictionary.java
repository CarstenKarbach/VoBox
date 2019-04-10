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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Carsten on 27.12.2015.
 *
 * A dictionary is in principle a list of cards, where the second language of each card
 * belongs to the language of the dictionary. A dictionary can be stored to file and
 * exported to other apps. Wrapper functions for adding, removing, searching and
 * filtering cards are the main functions of a dictionary. Especially, the boxes are
 * filled with cards by using the function getCardsForBox. The same could be done to
 * retrieve a list of all verbs or adjectives and so forth.
 */
public class Dictionary implements Serializable {

    /**
     * The cards belonging to this dictionary
     */
    private ArrayList<Card> cards;

    /**
     * Base language, usually German, but can be changed on a per dictionary basis
     */
    private String baseLanguage;

    /**
     * The language for the second language, which the user is learning
     */
    private String language;

    /**
     *
     * @return maximum value for cards' boxes in this dictionary
     */
    public int getBoxcount() {
        return boxcount;
    }

    /**
     * Set maximum value for cards' boxes in this dictionary
     * @param boxcount maximum value for cards' boxes in this dictionary
     */
    public void setBoxcount(int boxcount) {
        this.boxcount = boxcount;
    }

    /**
     * maximum value for cards' boxes in this dictionary
     */
    private int boxcount = 5;

    /**
     *
     * @return The name of the dictionary for identification of the dictionary.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name The name of the dictionary for identification of the dictionary.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the dictionary for identification of the dictionary.
     */
    private String name;

    /**
     * Default base language is set to Deutsch here
     * @param name The name of the dictionary for identification of the dictionary.
     */
    public Dictionary(String name){
        cards = new ArrayList<Card>();
        this.name = name;
        this.setBaseLanguage("Deutsch");
    }

    /**
     * Add a card to this dictionary or modify an existing card, if a car with the same lang1 value already exists.
     * @param card the added card
     */
    public void addCard(Card card){
        if(card == null){
            return;
        }
        String keyLang = card.getLang1();
        if(keyLang == null){
            return;
        }
        for(Card c: cards){
            if(keyLang.equals(c.getLang1())){
                //Modify existing card
                c.setType(card.getType());
                c.setLesson(card.getLesson());
                c.setLang2(card.getLang2());
                return;
            }
        }
        //card not yet included => add
        cards.add(card);
    }

    /**
     * Delete a card at a given position
     * @param pos the position of the card in the stack
     */
    public void deleteCard(int pos){
        if(pos< 0 || pos >= cards.size()){
            return;
        }
        cards.remove(pos);
    }

    /**
     * Delete a card object from the dictionary
     * @param card
     */
    public void deleteCard(Card card){
        cards.remove(card);
    }

    /**
     * Allows read/write access to the cards of the dictionary.
     * @return the cards in the dictionary
     */
    public ArrayList<Card> getCards(){
        return cards;
    }

    /**
     * Use language 1 value as key to search for a card
     * @param lang1 the value for language 1
     * @return the card found, or null if not
     */
    public Card getCardByLang1(String lang1){
        if(lang1 == null){
            return null;
        }
        for(Card card: cards){
            if(lang1.equals(card.getLang1())){
                return card;
            }
        }
        return null;
    }

    /**
     *
     * @param card
     * @return -1 if not found
     */
    public int getPosForCard(Card card){
        for(int i=0; i<cards.size(); i++){
            Card c = cards.get(i);
            if(c == card){
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @return Base language, usually German, but can be changed on a per dictionary basis
     */
    public String getBaseLanguage() {
        return baseLanguage;
    }

    /**
     *
     * @param language Base language, usually German, but can be changed on a per dictionary basis
     */
    public void setBaseLanguage(String language) {
        this.baseLanguage = language;
    }

    /**
     *
     * @return The language for the second language, which the user is learning
     */
    public String getLanguage() {
        return language;
    }

    /**
     *
     * @param language The language for the second language, which the user is learning
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     *
     * @return string filename used to store this dictionary, e.g. Englisch.txt
     */
    public String getFilenameForStore(){
        return getFilenameForStore("txt");
    }

    /**
     *
     * @param ending e.g. "txt"
     * @return name+"."+ending as string
     */
    public String getFilenameForStore(String ending){
        String filename = "dictionary."+ending;
        if(name != null){
            filename = name+"."+ending;
        }
        return filename;
    }

    /**
     * Check if file for this dictionary exists already
     * @param context
     * @return true if file exists, false otherwise
     */
    public boolean dictionaryFileExists(Context context){
        String filename = this.getFilenameForStore();
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    /**
     * If file exists, load data (cards, boxcount, language) from file
     * @param context
     */
    public void loadIfPossible(Context context){
        if(dictionaryFileExists(context)){
            load(context);
        }
    }

    /**
     * Delete the file for the dictionary, if it exists.
     * @param context
     */
    public void deleteFile(Context context){
        String filename = this.getFilenameForStore();
        File file = context.getFileStreamPath(filename);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * Save this dictionary to file
     * @param context
     * @return true on success, false on error
     */
    public boolean save(Context context){
        File result = exportToFile(getFilenameForStore(), context, false);
        return result != null;
    }

    /**
     * Save this dictionary to file, use object serialization.
     * @param context needed context to save data.
     * @return true on success, false on error
     */
    public boolean saveToObj(Context context){
        String filename = this.getFilenameForStore("obj");
        try {
            ObjectOutputStream oOut = new ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE));
            oOut.writeObject(this);
            oOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy all data from the other dictionary to this dictionary.
     * @param other e.g. a loaded dictionary
     */
    private void initWithDictionary(Dictionary other){
        if(other == null){
            return;
        }

        cards = other.cards;
        baseLanguage = other.baseLanguage;
        language = other.language;
        name = other.name;
        boxcount = other.boxcount;
    }

    /**
     * Load dictionary from file ointo this dictionary.
     * @param context
     */
    public void load(Context context){
        String filename = getFilenameForStore();
        File toLoad = context.getFileStreamPath(filename);
        Uri uri = Uri.fromFile(toLoad);
        Dictionary loaded = loadFromUri(uri, true, context);

        initWithDictionary(loaded);
    }

    /**
     * Load last saved dictionary from file.
     * Save dictionary into this instance.
     * Uses object serialization.
     * @param context needed context to load data from.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws StreamCorruptedException
     * @throws ClassNotFoundException
     */
    public void loadFromObj(Context context) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException{
        String filename = this.getFilenameForStore("obj");

        ObjectInputStream oIn = new ObjectInputStream(context.openFileInput(filename));
        Dictionary loaded = (Dictionary) oIn.readObject();

        initWithDictionary(loaded);
    }

    /**
     * Get all cards in a given box
     * @param box the box for the cards
     * @return list of cards in this box
     */
    public ArrayList<Card> getCardsForBox(int box){
        ArrayList<Card> result = new ArrayList<Card>();
        for(Card card: cards){
            if(card.getBox() == box){
                result.add(card);
            }
        }
        return result;
    }

    /**
     *
     * @return string representation of this card
     */
    public String export(){
        StringBuilder result = new StringBuilder();
        result.append(language);
        result.append("\n");
        result.append(baseLanguage);
        result.append("\n");
        result.append("boxcount:"+boxcount);
        result.append("\n");
        for(Card card: cards){
            String exportedCard = card.export();
            result.append(exportedCard);
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Load dictionary expecting format generated by export.
     * @param uri file location
     * @param loadAll if true, load boxes for cards, too
     * @param context
     * @return the loaded dictionary, null on error
     */
    public static Dictionary loadFromUri(Uri uri, boolean loadAll, Context context){
        if (! TextUtils.equals(uri.getScheme(), "file") && ! TextUtils.equals(uri.getScheme(), "content")) {
            return null;
        }

        Scanner scanner = null;

        try {
            if (TextUtils.equals(uri.getScheme(), "file")) {
                String fileName = uri.getPath();
                // Create a File object for this filename
                File file = new File(fileName);
                scanner = new Scanner(file);
            } else {
                scanner = new Scanner(context.getContentResolver().openInputStream(uri));
            }
        }
        catch(FileNotFoundException exception){
            return null;
        }

        StringBuilder dict = new StringBuilder();
        while (scanner.hasNext()) {
           dict.append(scanner.nextLine());
           dict.append("\n");
        }
        return loadImported(dict.toString(), loadAll);
    }

    /**
     * Read single value cell from csv line, e.g. "boxcount:3;   " results in "boxcount:3"
     * @param line a line from csv file
     * @return single value of first cell
     */
    protected static String sanitizeSingleValueInLine(String line){
        if(line == null){
            return null;
        }
        line = line.trim();
        line = line.replace("\uFEFF", "");//Handle BOM for csv import
        for (String pd : Card.delimiters) {
            if (line.endsWith(pd)) {
                line = line.substring(0, line.length() - 1);
                line = line.trim();
            }
        }
        return line;
    }

    /**
     * Try to parse a line in this format: "Deutsch;Englisch"
     * @param line the line to parse
     * @return array with two languages, first is base language, second is the language to learn, e.g. ["Deutsch", "Englisch"]
     */
    protected static String[] getTwoLanguagesFromFirstLine(String line){
        if(line == null){
            return null;
        }
        line = line.trim();
        //Find delimiter
        String delimiter = ";";
        //Find the used delimiter in this string
        for (String pd : Card.delimiters) {
            if (line.contains(pd) && !line.endsWith(pd)) {
                delimiter = pd;
                break;
            }
        }
        if (line.endsWith(delimiter)) {
            line = line.substring(0, line.length() - 1);
            line = line.trim();
        }
        if(line.indexOf(delimiter) == -1){
            return null;
        }

        String[] languages = line.split(delimiter);
        if(languages.length == 2 && languages[0].length() > 0 && languages[1].length() > 0){
            return languages;
        }

        return null;
    }

    /**
     * Make sure, that base language and language differ and are not empty.
     */
    public void sanitizeLanguagesToDiffer(){
        if(baseLanguage == null || baseLanguage.length() == 0){
            baseLanguage = "Deutsch";
        }
        if(language == null || language.length() == 0){
            language = "Englisch";
        }
        if(baseLanguage.equals(language)){
            if(baseLanguage.equals("Deutsch")){
                setLanguage("Englisch");
            }
            else{
                setLanguage("Deutsch");
            }
        }
    }

    /**
     * If baseLanguage or language cannot be found in allowedLanguages, set them to default values.
     *
     * @param allowedLanguages list of allowed languages
     */
    public void sanitizeLanguagesWithAllowedValues(List<String> allowedLanguages){
        if(! allowedLanguages.contains(baseLanguage)){
            baseLanguage = "Deutsch";
        }
        if(! allowedLanguages.contains(language)){
            language = "Deutsch";
        }
    }

    /**
     * Load dictionary directly from a string
     * @param dictExportString format generated by export()
     * @param loadAll if true, load boxes for cards, too
     * @return the loaded dictionary, null on error
     */
    public static Dictionary loadImported(String dictExportString, boolean loadAll){
        String[] lines = dictExportString.split("\n");
        Dictionary result = null;
        boolean languagesFoundInFirstLine = false;
        //Try to parese both languages from the first line
        if(lines.length > 0) {
            String[] singleLineLanguages = getTwoLanguagesFromFirstLine(lines[0]);
            if(singleLineLanguages != null){
                String baseLanguage = sanitizeSingleValueInLine(singleLineLanguages[0]);
                String learnLanguage = sanitizeSingleValueInLine(singleLineLanguages[1]);
                result = new Dictionary(learnLanguage);
                result.setLanguage(learnLanguage);
                result.setBaseLanguage(baseLanguage);
                languagesFoundInFirstLine = true;
            }
        }
        if(! languagesFoundInFirstLine) {
            for (int i = 0; i <= 1; i++) {
                String language = "";
                if (lines.length > i) {
                    language = lines[i];
                    language = sanitizeSingleValueInLine(language);
                }
                if (i == 0) {
                    result = new Dictionary(language);
                    result.setLanguage(language);
                } else {
                    result.setBaseLanguage(language);
                }
            }
        }
        //Make sure that language and baselanguage differ
        if(result != null){
            result.sanitizeLanguagesToDiffer();
        }
        int cardStartIndex = languagesFoundInFirstLine ? 1 : 2;
        for(int i=cardStartIndex; i<lines.length; i++){
            String boxcountKey = "boxcount:";
            String boxcountcellValue = sanitizeSingleValueInLine(lines[i]);
            if(boxcountcellValue.indexOf(boxcountKey) == 0 ){
                int boxCountParsedNumber = Integer.valueOf(boxcountcellValue.substring(boxcountKey.length()).trim());
                result.setBoxcount(boxCountParsedNumber);
                continue;
            }
            Card card = Card.loadJSONOrCSV(lines[i], loadAll);
            if(card != null) {
                result.addCard(card);
            }
        }

        return result;
    }

    /**
     * Start intent to send / share the exported dictionary.
     * Dictionary is exported with the export() function.
     * @param context
     */
    public void sendExportedDictionary(Context context){
        File exportedFile = exportToFile(getFilenameForStore("txt"), context, true);
        if(exportedFile == null){
            return;
        }
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("text/plain");
        Uri uri = FileProvider.getUriForFile(context, "de.karbach.superapp.fileprovider", exportedFile);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(intent);
    }

    /**
     * Export dictionary with export() function and store that string to the given file.
     * Example call: exportToFile(getFilenameForStore("txt"), context, true)
     * @param filename e.g. "Englisch.txt"
     * @param context
     * @param external if true try to store on SD card like, otherwise in internal filesystem
     * @return file handle on success
     */
    public File exportToFile(String filename, Context context, boolean external){
        File result = null;
        if(!external){
            result = new File(context.getFilesDir(), filename);
        }
        else {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
            if (!mExternalStorageWriteable) {
                return null;
            }

            result = new File(context.getExternalFilesDir(null), filename);
        }
        try {
            FileOutputStream fout = new FileOutputStream(result);
            Writer writer = new OutputStreamWriter(fout);
            try {
                writer.write(export());
                writer.close();
            } catch (IOException e) {
                return null;
            }
            return result;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
