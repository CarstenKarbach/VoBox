package de.karbach.superapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
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
 */
public class CardListActivity extends SingleFragmentActivity {

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
        }

        return false;
    }

    private Bundle getArgumentsBundle(){
        ArrayList<Card> cards = getCardsToShow();

        Bundle arguments = new Bundle();
        arguments.putSerializable(CardListFragment.PARAMCARDS, cards);
        return arguments;
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
