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

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 28.12.2015.
 *
 * Activity showing a list of cards. Either a list for a box is shown (use of
 * PARAMBOX parameter) or a specific selection of serialized cards is shown (
 * CardListFragment.PARAMCARDS) or all cards of the selected dictionary are shown.
 * The shown cards are always updated on resume of this activity. This makes
 * sure that any changes to the dictionary are immediately reflected by the
 * list. This activity is configured to be searchable. It handles the search
 * intents and forwards the queries to the CardListFragment.
 */
public class CardListActivity extends SingleFragmentActivity {
    /**
     * Parameter for showing a list for a given box. An integer value is expected here.
     */
    public static final String PARAMBOX = "de.karbach.superapp.CardListActivity.box";

    private ArrayList<Card> getCardsToShow(){
        int box = getIntent().getIntExtra(PARAMBOX, -1);

        if(box != -1){
            Dictionary dict = DictionaryManagement.getInstance(this).getSelectedDictionary();
            return dict.getCardsForBox(box);
        }

        Serializable givenCards = getIntent().getSerializableExtra(CardListFragment.PARAMCARDS);

        ArrayList<Card> cards = new ArrayList<Card>();
        if(givenCards != null){
            cards = (ArrayList<Card>) givenCards;
        }
        else {
            //Get all cards from dictionary
            Dictionary dict = DictionaryManagement.getInstance(this).getSelectedDictionary();
            if (dict != null) {
                cards = dict.getCards();
            }
        }

        return cards;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(! addOptions()){
            return false;
        }

        MenuInflater inflater = getMenuInflater();
        if(inflater != null){
            inflater.inflate(R.menu.main_menu_forlist, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if(result){
            return result;
        }
        switch(item.getItemId()){
            case R.id.menu_item_search:
                boolean searching = onSearchRequested();
                return true;
            case R.id.menu_item_clearsearch:
                CardListFragment cardlist = getMyFragment();
                if(cardlist != null){
                    cardlist.search(null);
                }
                return true;
            case R.id.menu_item_training:
                CardListFragment myCardList = getMyFragment();
                if(myCardList != null){
                    myCardList.startTrainingWithShownCards();
                }
                return true;
            case R.id.menu_item_sort:
                DictionaryManagement dm = DictionaryManagement.getInstance(this);
                final Dictionary selected = dm.getSelectedDictionary();
                if(selected == null || selected.getBaseLanguage() == null){
                    return true;
                }
                if(selected.getLanguage() == null){
                    return true;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.sort));
                alertDialog.setMessage(getString(R.string.question_sort));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, selected.getBaseLanguage(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CardListFragment cardlist = getMyFragment();
                        if(cardlist != null){
                            cardlist.sortByLanguage(selected.getBaseLanguage());
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, selected.getLanguage(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CardListFragment cardlist = getMyFragment();
                        if(cardlist != null){
                            cardlist.sortByLanguage(selected.getLanguage());
                        }
                    }});
                alertDialog.show();
                return true;
        }

        return false;
    }

    @Override
    protected Fragment createFragment() {
        Fragment result = new CardListFragment();
        return result;
    }

    private void updateCardsInFragment(){
        CardListFragment cardlist = getMyFragment();
        if(cardlist != null){
            ArrayList<Card> cards = getCardsToShow();
            cardlist.updateCards(cards);
        }
    }

    /**
     * Get the CardListFragment, which is the main content of this activity.
     * The fragment is retained, so that it is sometimes useful to retrieve the
     * fragment. Not all changes can be done to the fragment during creation.
     * @return the active CardListFragment
     */
    private CardListFragment getMyFragment(){
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);
        if(f != null){
            return (CardListFragment) f;
        }
        else{
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCardsInFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CardListFragment.CARDCHANGERESULT){
            updateCardsInFragment();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(Intent.ACTION_SEARCH.equals(intent.getAction()) ){
            String query = intent.getStringExtra(SearchManager.QUERY);
            CardListFragment cardlist = getMyFragment();
            if(cardlist != null){
                cardlist.search(query);
            }
        }
    }
}
