package de.karbach.superapp;

import android.app.Fragment;
import android.content.Context;
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
 */
public class TestFragment extends Fragment {

    public final static String PARAMTESTCARDS = "de.karbach.superapp.TestFragment.CARDS";
    public final static String PARAMISREALTEST = "de.karbach.superapp.TestFragment.ISTEST";

    private List<Card> testcards = new ArrayList<Card>();
    private List<Boolean> testCompleted = new ArrayList<Boolean>();
    private int position = 0;

    private String lang1 = "Deutsch";
    private String lang2 = "Schwedisch";

    private boolean realtest = false;

    private boolean answerShown = false;

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
            if(lang1.equals("Deutsch")){
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

        int r1 = PictureHelper.getDrawableResourceForLanguage(lang1);
        int r2 = PictureHelper.getDrawableResourceForLanguage(lang2);

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

        if(lang2Text.getText().toString().equals(solution.getText().toString()) ){
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

    protected void goNext(){
        position++;
        if(position >= testcards.size()){
            position = 0;
        }

        Card card = getCurrentCard();
        loadCard(card, getView());
        answerShown = false;
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
                lang2 = selected.getLanguage();
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
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        });

        Button nextButton = (Button) result.findViewById(R.id.testcard_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        ImageButton switchButton = (ImageButton) result.findViewById(R.id.button_switch_lang);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLanguages();
            }
        });

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(answerShown){
            checkSolution();
        }
    }
}
