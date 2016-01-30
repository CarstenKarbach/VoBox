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
 */
public class CardListFragment extends ListFragment{

    public static final String PARAMCARDS = "de.karbach.superapp.CardListFragment.cards";

    public static final int CARDCHANGERESULT = 1;

    private class CardAdapter extends ArrayAdapter<Card> {

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

        private String lang1 = "Deutsch";
        private String lang2 = "Schwedisch";

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

            int r1 = PictureHelper.getDrawableResourceForLanguage(lang1);
            int r2 = PictureHelper.getDrawableResourceForLanguage(lang2);
            if(flag1 != null) {
                flag1.setImageResource(r1);
            }
            if(flag2 != null) {
                flag2.setImageResource(r2);
            }

            Card c = getItem(position);

            if(langtext1 != null) {
                if(lang1.equals("Deutsch")) {
                    langtext1.setText(c.getLang1());
                }
                else {
                    langtext1.setText(c.getLang2());
                }
            }

            if(langtext2 != null) {
                if(lang2.equals("Deutsch")) {
                    langtext2.setText(c.getLang1());
                }
                else {
                    langtext2.setText(c.getLang2());
                }
            }

            return convertView;
        }
    }

    private List<Card> cards = new ArrayList<Card>();
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
        language2 = dict.getLanguage();

        CardAdapter adapter = new CardAdapter(cardsAfterSearch);
        adapter.setLang1(language1);
        adapter.setLang2(language2);

        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        if(result == null){
            return null;
        }
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
        int position = info.position;

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

    public void updateCards(List<Card> newcards){
        this.cards.clear();
        this.cards.addAll(newcards);
        cardsAfterSearch.clear();
        cardsAfterSearch.addAll(newcards);

        if(lastSearch != null){
            search(lastSearch);
        }

        CardAdapter adapter = (CardAdapter)getListAdapter();
        adapter.notifyDataSetChanged();
    }

    private String lastSearch = null;

    public void search(String search){
        lastSearch = search;
        cardsAfterSearch.clear();
        if(search == null || search.equals("")){//Clear search
            cardsAfterSearch.addAll(cards);
        }
        else{
            String simpleSearch = Card.toSimpleString(search);
            for(Card card: cards){
                if(card.matchesSearch(simpleSearch)){
                    cardsAfterSearch.add(card);
                }
            }
        }

        CardAdapter adapter = (CardAdapter)getListAdapter();
        adapter.notifyDataSetChanged();
    }

    public void sortByLanguage(String language){
        Comparator<Card> lang1Comparator = new Comparator<Card>() {
            @Override
            public int compare(Card lhs, Card rhs) {
                if(lhs == null){
                    return -1;
                }
                if(rhs == null){
                    return 1;
                }
                if(lhs.getLang1() == null){
                    return -1;
                }
                String lang1 = Card.toSimpleString(lhs.getLang1());
                String lang2 = Card.toSimpleString(rhs.getLang1());
                if(lang1 == null){
                    return -1;
                }
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
                if(lhs.getLang1() == null){
                    return -1;
                }
                String lang1 = Card.toSimpleString(lhs.getLang2());
                String lang2 = Card.toSimpleString(rhs.getLang2());
                if(lang1 == null){
                    return -1;
                }
                return lang1.compareTo(lang2);
            }
        };

        if(language.equals("Deutsch")){
            Collections.sort(cards, lang1Comparator);
            Collections.sort(cardsAfterSearch, lang1Comparator);
        }
        else{
            Collections.sort(cards, lang2Comparator);
            Collections.sort(cardsAfterSearch, lang2Comparator);
        }

        CardAdapter adapter = (CardAdapter)getListAdapter();
        adapter.notifyDataSetChanged();
    }
}
