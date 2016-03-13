package de.karbach.superapp;

import android.app.Fragment;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**^^
 * This fragment allows to select cards from the entire dictionary.
 * The cards can be selected by special attributes.
 * The fragment then simply calls the card list fragment to
 * display the cards found.
 */
public class ListGeneratorFragment extends Fragment {

    private ArrayList<Card> getAllCards(){
        Dictionary selected = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
        if(selected == null){
            return null;
        }
        ArrayList<Card> all = selected.getCards();
        return all;
    }

    private ArrayList<Card> getFilteredDictionary(List<String> matches){
        if(matches == null ||matches.size()==0){
            return new ArrayList<Card>();
        }

        ArrayList<Card> all = getAllCards();
        if(all == null){
            return null;
        }

        ArrayList<Card> result = new ArrayList<Card>();

        for(Card c: all){
            for(String filter: matches) {
                if (c.matchesSearch(filter)) {
                    result.add(c);
                    break;
                }
            }
        }

        return result;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.list_generator, container, false);

        final CheckBox ettCheck = (CheckBox) result.findViewById(R.id.ettCheck);
        final CheckBox enCheck = (CheckBox) result.findViewById(R.id.enCheck);

        Button showListButton = (Button) result.findViewById(R.id.button_showlist);
        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> matches = new ArrayList<String>();
                if(ettCheck.isChecked()){
                    matches.add("(-et");
                    matches.add("(-t");
                    matches.add("( -et");
                    matches.add("( -t");
                }
                if(enCheck.isChecked()){
                    matches.add("(-en");
                    matches.add("(-n");
                    matches.add("( -en");
                    matches.add("( -n");
                }

                ArrayList<Card> selectedCards = getFilteredDictionary(matches);
                if(selectedCards == null || selectedCards.size() == 0){
                    Toast.makeText(getActivity(), "Keine Karten gefunden", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent listIntent = new Intent(getActivity(), CardListActivity.class);
                listIntent.putExtra(CardListFragment.PARAMCARDS, selectedCards);
                startActivity( listIntent );
            }
        });

        return result;
    }
}
