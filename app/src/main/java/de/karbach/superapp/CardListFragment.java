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

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 28.12.2015.
 *
 * Retained.
 *
 * A listfragment, which shows a list of cards. Depending on the language of
 * the selected dictionary other flags are shown next to the words. Mainly
 * the German word and the corresponding translation is shown in each row.
 * By tapping on a card entry, the CardActivity is called for editing the card.
 * The card list can be sorted and filtered.
 */
public class CardListFragment extends ListFragment{
    /**
     * Parameter name for the parameter holding all cards to show.
     * This fragment simply needs a list of cards to show. It does not
     * much care about dictionary, boxes or any filtering. This is all handled
     * by the calls to its functions, but the fragment does not know about
     * these semantics. It simply shows a list of cards.
     */
    public static final String PARAMCARDS = "de.karbach.superapp.CardListFragment.cards";
    /**
     * The code, with which the CardListActivity is started for result.
     * On return from the activity the list is updated.
     */
    public static final int CARDCHANGERESULT = 1;

    /**
     * Generate views for all cards in the list
     */
    public class CardAdapter extends ArrayAdapter<Card> {

        public String getLang1() {
            return lang1;
        }

        public void setLang1(String lang1) {
            this.lang1 = lang1;
        }

        public String getLang2() {
            return lang2;
        }

        public void setLang2(String lang2) {
            this.lang2 = lang2;
        }
        public void setBaseLanguage(String baseLanguage) {
            this.baseLanguage = baseLanguage;
        }

        public String getBaseLanguage() {
            return baseLanguage;
        }

        private String baseLanguage = getString(R.string.lang_german);

        private String lang1 = getString(R.string.lang_german);
        private String lang2 = getString(R.string.lang_swedish);

        public CardAdapter(List<Card> cards) {
            super(getActivity(), 0,  cards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.card_item, parent, false);
            }

            TextView langtext1 = (TextView) convertView.findViewById(R.id.carditem_lang1);
            TextView langtext2 = (TextView) convertView.findViewById(R.id.carditem_lang2);

            ImageView flag1 = (ImageView) convertView.findViewById(R.id.carditem_flag1);
            ImageView flag2 = (ImageView) convertView.findViewById(R.id.carditem_flag2);

            PictureHelper ph = new PictureHelper(getActivity());
            int r1 = ph.getDrawableResourceForLanguage(lang1);
            int r2 = ph.getDrawableResourceForLanguage(lang2);
            if(flag1 != null) {
                flag1.setImageResource(r1);
            }
            if(flag2 != null) {
                flag2.setImageResource(r2);
            }

            Card c = getItem(position);

            if(langtext1 != null) {
                if(lang1 != null && lang1.equals(baseLanguage)) {
                    langtext1.setText(c.getLang1());
                }
                else {
                    langtext1.setText(c.getLang2());
                }
            }

            if(langtext2 != null) {
                if(lang2 != null && lang2.equals(baseLanguage)) {
                    langtext2.setText(c.getLang1());
                }
                else {
                    langtext2.setText(c.getLang2());
                }
            }

            return convertView;
        }
    }

    /**
     * The entire original list of cards to be shown.
     */
    private List<Card> cards = new ArrayList<Card>();
    /**
     * The actual list of cards shown. This might be a subset of cards,
     * when the search function was called.
     */
    private List<Card> cardsAfterSearch = new ArrayList<Card>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle bundle = getArguments();
        if(bundle != null) {
            Serializable list = bundle.getSerializable(PARAMCARDS);
            if (list != null) {
                List<Card> cardsToCopy = (List<Card>) list;
                cards.addAll(cardsToCopy);
                cardsAfterSearch.addAll(cards);
            }
        }

        String language1 = "Deutsch";
        String language2 = "Schwedisch";
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();
        language1 = dict.getBaseLanguage();
        language2 = dict.getLanguage();

        CardAdapter adapter = new CardAdapter(cardsAfterSearch);
        adapter.setLang1(language1);
        adapter.setLang2(language2);
        adapter.setBaseLanguage(dict.getBaseLanguage());

        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        ListView listview = (ListView) result.findViewById(android.R.id.list);
        registerForContextMenu(listview);

        return result;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.cardlist_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info == null ? 0 : info.position;

        if(item.getItemId() == R.id.menu_card_delete){
            Card card = cardsAfterSearch.get(position);

            if(card != null) {
                DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                Dictionary dict = dm.getSelectedDictionary();

                dict.deleteCard(card);
                cards.remove(card);
                cardsAfterSearch.remove(card);

                CardAdapter adapter = (CardAdapter)getListAdapter();
                adapter.notifyDataSetChanged();
            }

        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Card card = cardsAfterSearch.get(position);
        if(card != null){
            String lang1Key = card.getLang1();
            Intent intent  = new Intent(getActivity(), CardActivity.class);
            intent.putExtra(CardFragment.PARAMLANG1KEY, lang1Key);
            getActivity().startActivityForResult(intent, CARDCHANGERESULT);
        }
    }

    /**
     * Set the cards to show. cardsAfterSearch is updated, too.
     * @param newcards
     */
    public void updateCards(List<Card> newcards){
        this.cards.clear();
        this.cards.addAll(newcards);
        cardsAfterSearch.clear();
        cardsAfterSearch.addAll(newcards);

        if(lastSearch != null){
            search(lastSearch);
        }
        if(lastSort != null){
            sortByLanguage(lastSort);
        }

        CardAdapter adapter = (CardAdapter)getListAdapter();
        adapter.notifyDataSetChanged();
    }

    /**
     * Used for retaining the search query while the fragment is retained.
     * If this was not used, a configuration change would cause the
     * CardListActivity to update the shown cards, then the search query
     * would be lost. (See updateCards for how it is used)
     */
    private String lastSearch = null;

    /**
     * Filter cards by the search string.
     * @param search the search string to match for
     */
    public void search(String search){
        lastSearch = search;
        cardsAfterSearch.clear();
        if(search == null || search.equals("")){//Clear search
            cardsAfterSearch.addAll(cards);
        }
        else{
            String simpleSearch = Card.toSimpleString(search);
            for(Card card: cards){
                if(card == null){
                    continue;
                }
                if(card.matchesSearch(simpleSearch)){
                    cardsAfterSearch.add(card);
                }
            }
        }

        CardAdapter adapter = (CardAdapter)getListAdapter();
        adapter.notifyDataSetChanged();

        getActivity().setTitle(search);
    }

    /**
     * Same story as for lastSearch. It stores the last used sort argument
     * to retain the sorting throughout configuration changes. (See updateCards for how it is used)
     */
    private String lastSort = null;

    /**
     * Sort cards shown by the given language.
     * @param language Language by which to sort the cards
     */
    public void sortByLanguage(String language){
        lastSort = language;
        Comparator<Card> lang1Comparator = new Comparator<Card>() {
            @Override
            public int compare(Card lhs, Card rhs) {
                if(lhs == null){
                    return -1;
                }
                if(rhs == null){
                    return 1;
                }
                if(lhs.getLang1() == null || "".equals(lhs.getLang1())){
                    return -1;
                }
                String lang1 = Card.toSimpleString(lhs.getLang1());
                String lang2 = Card.toSimpleString(rhs.getLang1());
                return lang1.compareTo(lang2);
            }
        };

        Comparator<Card> lang2Comparator = new Comparator<Card>() {
            @Override
            public int compare(Card lhs, Card rhs) {
                if(lhs == null){
                    return -1;
                }
                if(rhs == null){
                    return 1;
                }
                if(lhs.getLang1() == null || "".equals(lhs.getLang1())){
                    return -1;
                }
                String lang1 = Card.toSimpleString(lhs.getLang2());
                String lang2 = Card.toSimpleString(rhs.getLang2());
                if(lang1 == null || "".equals(lang1)){
                    return -1;
                }
                return lang1.compareTo(lang2);
            }
        };

        CardAdapter adapter = (CardAdapter)getListAdapter();

        if(language.equals(adapter.getBaseLanguage())){
            Collections.sort(cards, lang1Comparator);
            Collections.sort(cardsAfterSearch, lang1Comparator);
        }
        else{
            Collections.sort(cards, lang2Comparator);
            Collections.sort(cardsAfterSearch, lang2Comparator);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Start training activity with the currently shown cards
     */
    public void startTrainingWithShownCards(){
        if(cardsAfterSearch == null || cardsAfterSearch.size()==0){
            return;
        }

        ArrayList<Card> toTrain = new ArrayList<Card>( cardsAfterSearch );

        Intent training = new Intent(this.getActivity(), TestActivity.class);
        training.putExtra( TestActivity.PARAMDIRECTCARDS, toTrain );
        training.putExtra( TestActivity.PARAMREALTEST, Boolean.FALSE);

        startActivity(training);
    }
}
