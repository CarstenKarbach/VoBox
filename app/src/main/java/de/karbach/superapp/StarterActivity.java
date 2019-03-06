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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    final Context context = this;
                    alertDialog.setTitle(getString(R.string.import_dict));
                    alertDialog.setMessage(getString(R.string.question_how_import));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.label_as_new), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DictionaryManagement dm = DictionaryManagement.getInstance(context);
                            String loadedname = loaded.getName();
                            String newname = loadedname;
                            int trials = 2;
                            while( dm.dictionaryExists(newname) && trials<100 ){
                                newname = loadedname+trials;
                                trials++;
                            }
                            loaded.setName(newname);
                            boolean success = dm.addDictionaryObject(loaded);
                            if(! success){
                                Toast.makeText(context, getString(R.string.toast_dict_not_created), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else {
                                dm.saveAll();
                                dm.selectDictionary(newname);
                                Intent intent = new Intent(context, DictionaryActivity.class);
                                intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.EDIT.ordinal());
                                startActivity(intent);
                            }
                        }
                    });

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.label_integrate_dict), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Show dicionary selection dialog
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);
                            DictionarySelectionFragment dsf = new DictionarySelectionFragment();
                            dsf.setCallback(new DictionarySelectionFragment.OnDictionarySelected() {
                                @Override
                                public void onDictionarySelected(String selected) {
                                    DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                                    loaded.setName(selected);
                                    dm.integrateDictionary(loaded);
                                    dm.selectDictionary(selected);
                                    Toast.makeText(StarterActivity.this, getString(R.string.toast_integrated, selected), Toast.LENGTH_SHORT).show();

                                    FragmentManager fm = getFragmentManager();
                                    Fragment f = fm.findFragmentById(R.id.fragment_container);
                                    if(f != null && f instanceof StarterFragment){
                                        ((StarterFragment) f).updateSelection();
                                    }
                                }
                            });
                            dsf.show(ft, "dialog");
                        }});
                    alertDialog.show();
                }
                else{
                    Toast.makeText(this, getString(R.string.toast_error_import), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected boolean addOptions() {
        return false;
    }
}
