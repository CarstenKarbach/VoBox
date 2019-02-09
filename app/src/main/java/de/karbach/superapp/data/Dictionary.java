/**
 MoTAC - digital board for TAC board game
 Copyright (C) 2015-2016  Carsten Karbach

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
     * The language for the second language
     */
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the dictionary for identification of the dictionary.
     */
    private String name;

    public Dictionary(String name){
        cards = new ArrayList<Card>();
        this.name = name;
        this.setBaseLanguage("Deutsch");
    }

    public void addCard(Card card){
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

    public void deleteCard(int pos){
        if(pos< 0 || pos >= cards.size()){
            return;
        }
        cards.remove(pos);
    }

    public void deleteCard(Card card){
        cards.remove(card);
    }

    public ArrayList<Card> getCards(){
        return cards;
    }

    /**
     * Use language 1 value as key to search for a card
     * @param lang1 the value for language 1
     * @return the Card found, or null if not
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

    public String getBaseLanguage() {
        return baseLanguage;
    }

    public void setBaseLanguage(String language) {
        this.baseLanguage = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFilenameForStore(){
        return getFilenameForStore("txt");
    }

    public String getFilenameForStore(String ending){
        String filename = "dictionary."+ending;
        if(name != null){
            filename = name+"."+ending;
        }
        return filename;
    }

    public boolean dictionaryFileExists(Context context){
        String filename = this.getFilenameForStore();
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    public void loadIfPossible(Context context){
        if(dictionaryFileExists(context)){
            load(context);
        }
    }

    public void deleteFile(Context context){
        String filename = this.getFilenameForStore();
        File file = context.getFileStreamPath(filename);
        if(file.exists()){
            file.delete();
        }
    }

    public boolean save(Context context){
        File result = exportToFile(getFilenameForStore(), context, false);
        return result != null;
    }

    /**
     * Save this board data to file
     * @param context needed context to save data.
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

    private void initWithDictionary(Dictionary other){
        if(other == null){
            return;
        }

        cards = other.cards;
        baseLanguage = other.baseLanguage;
        language = other.language;
        name = other.name;
    }

    public void load(Context context){
        String filename = getFilenameForStore();
        File toLoad = context.getFileStreamPath(filename);
        Uri uri = Uri.fromFile(toLoad);
        Dictionary loaded = loadFromUri(uri, true, context);

        initWithDictionary(loaded);
    }

    /**
     * Load last saved board from file.
     * Save board into this instance
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

    public ArrayList<Card> getCardsForBox(int box){
        ArrayList<Card> result = new ArrayList<Card>();
        for(Card card: cards){
            if(card.getBox() == box){
                result.add(card);
            }
        }
        return result;
    }


    public String export(){
        StringBuilder result = new StringBuilder();
        result.append(language);
        result.append("\n");
        result.append(baseLanguage);
        result.append("\n");
        for(Card card: cards){
            String exportedCard = card.export();
            result.append(exportedCard);
            result.append("\n");
        }
        return result.toString();
    }

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

    public static Dictionary loadImported(String dictExportString, boolean loadAll){
        String[] lines = dictExportString.split("\n");
        Dictionary result = null;
        for(int i=0; i<=1; i++) {
            String language = "";
            if (lines.length > i) {
                language = lines[i];
                language = language.trim();
                if (language.endsWith(";")) {
                    language = language.substring(0, language.length() - 1);
                    language = language.trim();
                }
            }
            if(i == 0) {
                result = new Dictionary(language);
                result.setLanguage(language);
            }
            else{
                result.setBaseLanguage(language);
            }
        }
        for(int i=2; i<lines.length; i++){
            Card card = Card.loadJSONOrCSV(lines[i], loadAll);
            if(card != null) {
                result.addCard(card);
            }
        }

        return result;
    }

    public void sendExportedDictionary(Context context){
        File exportedFile = exportToFile(getFilenameForStore("txt"), context, true);
        if(exportedFile == null){
            return;
        }
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        Uri uri = Uri.fromFile(exportedFile);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(intent);
    }

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
