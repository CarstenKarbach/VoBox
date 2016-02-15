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

package de.karbach.superapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Entry point for this app. It shows the selected dictionary and the main activities
 * navigate go to.
 * It also handles importing txt files as dictionaries.
 * A format like the following is accepted for import:
 *
 Schwedisch;
 grüßen;hälsa (-r, -de, -t)
 Guten Tag;God dag.
 gut;god (gott, goda)
 *
 * The entire data must be encoded with UTF-8. The first line contains the name of the
 * language to import into. Allowed values are Schwedisch, Spanisch, Englisch (see
 * values/languages.xml). Afterwards every line contains at first the German word followed
 * by ; and then followed by the translated word. If there is no language line on top
 * the user is asked, whether the import should be done into the currently selected dictionary.
 * You can also export a dictionary into a text-file and share it with others. The export format
 * looks like this:
 *
 Schwedisch
 ["Blitzstart","rivstart (-en, -er, -erna)",null,null,"3"]
 ["Kapitel","kapitel (kapitlet, kapitlen)",null,null,"4"]
 *
 * Here the first line only contains the language without a semicolon. Every following line is
 * each for a single card. Each card is encoded as a JSON array.
 * There are two ways to import an txt file: it can be integrated into the existing
 * dictionary or replace the entire dictionary. Integrating means that new cards are added and
 * existing cards are only modified with possible changes. Replacing means to clear all existing
 * cards and replace them with all cards of the new imported dictionary.
 *
 */
public class StarterActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new StarterFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();

        DictionaryManagement dm = DictionaryManagement.getInstance(this);
        dm.saveAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleViewIntent();
    }

    @Override
    protected boolean showUpButton() {
        return false;
    }

    private void showImportDictDialog(final Dictionary newDict, final Uri dataUri){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Wörterbuch importieren");
        alertDialog.setMessage("Wie soll das Wörterbuch importiert werden?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "integrieren", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                dm.integrateDictionary(newDict);
                Toast.makeText(StarterActivity.this, "Integration in Wörterbuch "+newDict.getLanguage()+" abgeschlossen", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }});

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ersetzen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Dictionary allLoaded = Dictionary.loadFromUri(dataUri, true, StarterActivity.this);
                allLoaded.setLanguage(newDict.getLanguage());
                allLoaded.setName(newDict.getName());
                DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                dm.replaceDictionary(allLoaded);
                Toast.makeText(StarterActivity.this, "Wörterbuch "+allLoaded.getLanguage()+" ("+allLoaded.getCards().size()+" Wörter)"+" ersetzt", Toast.LENGTH_SHORT).show();

                Dictionary sel = dm.getSelectedDictionary();
            }
        });

        alertDialog.show();
    }

    /*
         * Called from onNewIntent() for a SINGLE_TOP Activity
         * or onCreate() for a new Activity. For onNewIntent(),
         * remember to call setIntent() to store the most
         * current Intent
         *
         */
    private void handleViewIntent() {
        // Get the Intent action
        Intent intent = getIntent();
        String action = intent.getAction();
        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent
            final Uri dataUri = intent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(dataUri.getScheme(), "file") || TextUtils.equals(dataUri.getScheme(), "content")) {
                final Dictionary loaded = Dictionary.loadFromUri(dataUri, false, this);
                if(loaded != null){
                    String dictInteract = loaded.getLanguage();
                    DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                    Dictionary otherDict = dm.getDictionary(dictInteract);
                    final Dictionary selected = dm.getSelectedDictionary();
                    if(otherDict == null && selected != null){
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Wörterbuch importieren");
                        alertDialog.setMessage("Kein passendes Wörterbuch gefunden. Vokabeln mit aktuellem Wörterbuch ("+selected.getLanguage()+") abgleichen?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loaded.setName(selected.getName());
                                loaded.setLanguage(selected.getLanguage());
                                showImportDictDialog(loaded, dataUri);
                            }
                        });

                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "abbrechen", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }});
                        alertDialog.show();
                    }
                    else{
                        if(otherDict != null) {
                            showImportDictDialog(loaded, dataUri);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean addOptions() {
        return false;
    }
}
