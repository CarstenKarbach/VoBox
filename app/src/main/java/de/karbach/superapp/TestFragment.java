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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 *
 * Retained.
 *
 * The fragment to test and practise vocabularies in a box.
 * A list of cards is traversed. The user types in expected translation.
 * By clicking on the check button the solution is shown and whether
 * the user answered correctly.
 */
public class TestFragment extends Fragment {

    /**
     * Parameter for serialzed cards to test/practise. This contains a list of cards.
     */
    public final static String PARAMTESTCARDS = "de.karbach.superapp.TestFragment.CARDS";
    /**
     * Parameter indicating, whether a real test or only practis is conducted.
     */
    public final static String PARAMISREALTEST = "de.karbach.superapp.TestFragment.ISTEST";

    /**
     * The cards, which are tested
     */
    private List<Card> testcards = new ArrayList<Card>();
    /**
     * Stores for each card, if this card was already tested and the user clicked on the check button already
     */
    private List<Boolean> testCompleted = new ArrayList<Boolean>();
    /**
     * Holds the current position in the test card list
     */
    private int position = 0;

    /**
     * Base language of underlying dictionary
     */
    private String baseLanguage = "Deutsch";

    /**
     * The language, of which the clear text is shown, and which needs to be translated into lang2
     */
    private String lang1 = "Deutsch";
    /**
     * The language with the unknown word
     */
    private String lang2 = "Schwedisch";
    /**
     * Stores, whether this is a real test
     */
    private boolean realtest = false;
    /**
     * Stores, whether the check button was clicked already and the solution was shown already
     */
    private boolean answerShown = false;
    /**
     * Stores the background of the cardframe to restore in on next card load
     */
    private Drawable defaultBackground = null;

    public void switchLanguages(){
        String lang1tmp = lang1;
        lang1 = lang2;
        lang2 = lang1tmp;

        loadCard(getCurrentCard(), getView());
    }

    protected void setStatusText(Card card, View rootView){
        if(rootView ==null){
            return;
        }

        TextView status = (TextView) rootView.findViewById(R.id.testcard_statustext);
        String statusText = (position+1)+"/"+testcards.size();
        if(realtest){
            Boolean checked = testCompleted.get(position);
            if(checked != null && checked == true){
                statusText += " geprüft";
            }
        }
        status.setText(statusText);
    }

    protected void loadCard(Card card, View rootView){
        EditText lang1Text = (EditText) rootView.findViewById(R.id.testcard_lang1_text);
        EditText lang2Text = (EditText) rootView.findViewById(R.id.testcard_lang2_text);
        EditText solution = (EditText) rootView.findViewById(R.id.testcard_solution_text);

        lang1Text.setText("");
        lang2Text.setText("");

        solution.setText("");

        View solutionRow = rootView.findViewById(R.id.testcard_solution_row);
        solutionRow.setVisibility(View.GONE);

        View cardFrame = rootView.findViewById(R.id.testcard_cardframe);
        cardFrame.setBackgroundDrawable(defaultBackground);

        if(card != null){
            if(lang1.equals(baseLanguage)){
                lang1Text.setText(card.getLang1());
                solution.setText(card.getLang2());
            }
            else{
                lang1Text.setText(card.getLang2());
                solution.setText(card.getLang1());
            }
        }

        ImageView flag1 = (ImageView) rootView.findViewById(R.id.testcard_flag1);
        ImageView flag2 = (ImageView) rootView.findViewById(R.id.testcard_flag2);

        PictureHelper ph = new PictureHelper(getActivity());
        int r1 = ph.getDrawableResourceForLanguage(lang1);
        int r2 = ph.getDrawableResourceForLanguage(lang2);

        flag1.setImageResource(r1);
        flag2.setImageResource(r2);

        setStatusText(card, rootView);
    }

    protected void showSolution(){
        View rootView = getView();
        if(rootView == null){
            return;
        }

        View solutionRow = rootView.findViewById(R.id.testcard_solution_row);
        solutionRow.setVisibility(View.VISIBLE);
    }

    protected void checkSolution(){
        View rootView = getView();

        View solutionRow = rootView.findViewById(R.id.testcard_solution_row);
        if(solutionRow.getVisibility() == View.VISIBLE){
            Toast.makeText(getActivity(), "Schon geprüft", Toast.LENGTH_SHORT).show();
            return;
        }

        this.showSolution();

        EditText lang2Text = (EditText) rootView.findViewById(R.id.testcard_lang2_text);
        EditText solution = (EditText) rootView.findViewById(R.id.testcard_solution_text);

        View cardFrame = rootView.findViewById(R.id.testcard_cardframe);

        Card card = getCurrentCard();

        boolean solutionCorrect = lang2Text.getText().toString().trim().equals(solution.getText().toString().trim());
        if(! solutionCorrect){
            //Try to remove the brackets
            String longSolution = solution.getText().toString();
            int bOpen = longSolution.indexOf('(');
            String shortSolution = longSolution;
            if(bOpen != -1){
                shortSolution = longSolution.substring(0, bOpen);
                shortSolution = shortSolution.trim();
                solutionCorrect = lang2Text.getText().toString().trim().equals(shortSolution);
            }
        }
        if(solutionCorrect){
            //Correct solution

            cardFrame.setBackgroundColor(Color.GREEN);
            if(realtest) {
                Boolean isTestCompleted = testCompleted.get(position);
                if (isTestCompleted != null && isTestCompleted == false) {
                    boolean levelUp = card.boxUp();
                    testCompleted.set(position, true);
                    if(levelUp) {
                        Toast.makeText(getActivity(), "Karte hochgestuft", Toast.LENGTH_SHORT).show();
                    }
                    setStatusText(card, rootView);
                }
            }
        }
        else{
            cardFrame.setBackgroundColor(Color.RED);
            if(realtest) {
                Boolean isTestCompleted = testCompleted.get(position);
                if (isTestCompleted != null && isTestCompleted == false) {
                    boolean levelDown = card.boxDown();
                    testCompleted.set(position, true);
                    if(levelDown) {
                        Toast.makeText(getActivity(), "Karte abgestuft", Toast.LENGTH_SHORT).show();
                    }
                    setStatusText(card, rootView);
                }
            }
        }

        answerShown = true;
    }

    protected Card getCurrentCard(){
        if(testcards.size() == 0){
            return null;
        }
        else {
            Card card = testcards.get(position);
            return card;
        }
    }

    /**
     * Init the fragment with the new card after moving with next or back
     */
    protected void initAfterMovement(){
        Card card = getCurrentCard();
        loadCard(card, getView());
        answerShown = false;
        //Show keyboard
        View rootView = getView();
        if(rootView == null){
            return;
        }
        EditText lang2Text = (EditText) rootView.findViewById(R.id.testcard_lang2_text);
        if(lang2Text == null){
            return;
        }
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(lang2Text, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void goNext(){
        position++;
        if(position >= testcards.size()){
            position = 0;
        }

        initAfterMovement();
    }

    protected void goBack(){
        position--;
        if(position < 0){
            if(testcards != null && testcards.size() > 0){
                position = testcards.size()-1;
            }
            else{
                position = 0;
            }
        }

        initAfterMovement();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Serializable list = bundle.getSerializable(PARAMTESTCARDS);
            if (list != null) {
                List<Card> cardsToCopy = (List<Card>) list;
                testcards.addAll(cardsToCopy);

                //Mix the cards
                for(int i=0; i<testcards.size()*3; i++){
                    int switchWith = (int) (Math.random()*testcards.size());
                    Card tmpCard = testcards.get(switchWith);
                    testcards.set(switchWith, testcards.get(0));
                    testcards.set(0, tmpCard);
                }

                for(int i=0; i< testcards.size(); i++){
                    testCompleted.add(false);
                }
            }

            realtest = bundle.getBoolean(PARAMISREALTEST);
        }

        //Set second language
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary selected = dm.getSelectedDictionary();
        if(selected != null){
            if(selected.getLanguage() != null) {
                lang1 = selected.getBaseLanguage();
                lang2 = selected.getLanguage();
                baseLanguage = lang1;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.testcard_fragment, container, false);

        View cardFrame = result.findViewById(R.id.testcard_cardframe);
        defaultBackground = cardFrame.getBackground();

        Card card = getCurrentCard();
        loadCard(card, result);

        final Button checkButton = (Button) result.findViewById(R.id.testcard_check_button);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSolution();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null && getActivity().getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        Button nextButton = (Button) result.findViewById(R.id.testcard_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        Button backButton = (Button) result.findViewById(R.id.testcard_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        ImageButton switchButton = (ImageButton) result.findViewById(R.id.button_switch_lang);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLanguages();
            }
        });

        Button editButton = (Button) result.findViewById(R.id.testcard_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card currentCard = getCurrentCard();
                if(currentCard == null){
                    Toast.makeText(getActivity(), "Keine Karte zum Bearbeiten verfügbar.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent editIntent = new Intent(getActivity(), CardActivity.class);
                editIntent.putExtra(CardFragment.PARAMLANG1KEY, currentCard.getLang1());
                startActivityForResult(editIntent, CardFragment.REQUESTEDIT);
            }
        });

        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CardFragment.REQUESTEDIT && resultCode == Activity.RESULT_OK){
            String newlang1 = data.getStringExtra(CardFragment.PARAMLANG1KEY);
            if(newlang1 != null){
                Dictionary selected = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
                if(selected != null){
                    Card newcard = selected.getCardByLang1(newlang1);
                    if(newcard != null){
                        testcards.set(this.position, newcard);
                        this.testCompleted.set(position, false);
                        initAfterMovement();
                    }
                }
            }
        }
        else if(requestCode == CardFragment.REQUESTEDIT && resultCode == Activity.RESULT_CANCELED && data != null){
            boolean deleted = CardFragment.DELETEDVALUE.equals(data.getStringExtra(CardFragment.PARAMLANG1KEY));
            if(deleted) {
                //Card was deleted
                if (testcards.size() <= 1) {
                    getActivity().finish();
                } else {
                    testcards.remove(position);
                    testCompleted.remove(position);

                    if (position >= testcards.size()) {
                        position = 0;
                    }

                    initAfterMovement();
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if(answerShown){
            checkSolution();
        }
    }
}
