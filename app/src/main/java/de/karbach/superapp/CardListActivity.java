package de.karbach.superapp;

import android.app.Fragment;
import android.os.Bundle;

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

    private Fragment createdFragment;

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

    private Bundle getArgumentsBundle(){
        ArrayList<Card> cards = getCardsToShow();

        Bundle arguments = new Bundle();
        arguments.putSerializable(CardListFragment.PARAMCARDS, cards);
        return arguments;
    }

    @Override
    protected Fragment createFragment() {
        Fragment result = new CardListFragment();

        createdFragment = result;

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(createdFragment != null){
            CardListFragment cardlist = (CardListFragment)createdFragment;

            ArrayList<Card> cards = getCardsToShow();
            cardlist.updateCards(cards);
        }
    }
}
