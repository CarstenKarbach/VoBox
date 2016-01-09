package de.karbach.superapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 28.12.2015.
 */
public class CardFragment extends Fragment {

    public static final String PARAMLANG1KEY = "de.karbach.superapp.CardFragment";

    private String lang1Key = null;//Key to use for modification

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if(arguments != null) {
            lang1Key = arguments.getString(PARAMLANG1KEY);
        }
    }

    private void setCheckedInRadiogroup(RadioGroup group, String value){
        for(int i=0; i<group.getChildCount(); i++){
            View childView = group.getChildAt(i);
            if(childView instanceof LinearLayout){
                LinearLayout linlayout = (LinearLayout)childView;
                for(int j=0; j<linlayout.getChildCount(); j++){
                    View child = linlayout.getChildAt(j);
                    if(child instanceof  RadioButton) {
                        RadioButton childRadio = (RadioButton) child;
                        String childvalue = childRadio.getText().toString();
                        if (value != null && value.equals(childvalue)) {
                            childRadio.setChecked(true);
                            return;
                        }
                    }
                }
            }
            else if(childView instanceof RadioButton){
                RadioButton child = (RadioButton) childView;
                String childvalue = child.getText().toString();
                if(value != null && value.equals(childvalue)){
                    child.setChecked(true);
                    return;
                }
            }
        }
    }

    private String getCheckedValueInRadiogroup(RadioGroup group){
        for(int i=0; i<group.getChildCount(); i++){
            View childView = group.getChildAt(i);
            if(childView instanceof LinearLayout){
                LinearLayout linlayout = (LinearLayout)childView;
                for(int j=0; j<linlayout.getChildCount(); j++){
                    View child = linlayout.getChildAt(j);
                    if(child instanceof  RadioButton) {
                        RadioButton childRadio = (RadioButton) child;
                        if(childRadio.isChecked()){
                            return childRadio.getText().toString();
                        }
                    }
                }
            }
            else if(childView instanceof RadioButton){
                RadioButton child = (RadioButton) childView;
                if(child.isChecked()){
                    return child.getText().toString();
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.card_fragment, container, false);

        ImageView flag2 = (ImageView) result.findViewById(R.id.flag2);

        Dictionary dict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
        Card card = null;
        if(dict != null){
            if(flag2 != null){
                int resource = PictureHelper.getDrawableResourceForLanguage(dict.getLanguage());
                flag2.setImageResource(resource);
            }
            if(lang1Key != null) {
                card = dict.getCardByLang1(lang1Key);
            }
        }

        final TextView lang1 = (TextView) result.findViewById(R.id.lang1_text);
        final TextView lang2 = (TextView) result.findViewById(R.id.lang2_text);

        final RadioGroup lessons = (RadioGroup) result.findViewById(R.id.lesson_selection);
        final RadioGroup types = (RadioGroup) result.findViewById(R.id.type_selection);

        if(card != null){
            if(lang1 != null){
                lang1.setText(card.getLang1());
            }
            if(lang2 != null){
                lang2.setText(card.getLang2());
            }
            if(lessons != null){
                String lesson = card.getLesson();
                if(lesson != null){
                    setCheckedInRadiogroup(lessons, lesson);
                }
            }
            if(types != null){
                String type = card.getType();
                setCheckedInRadiogroup(types, type);
            }

            Button delete = (Button) result.findViewById(R.id.delete_button);
            if(delete != null){
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dictionary cdict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
                        if(cdict != null){
                            Card cCard = cdict.getCardByLang1(lang1Key);
                            if(cCard != null){
                                cdict.deleteCard(cCard);
                                getActivity().finish();
                            }
                        }
                    }
                });
            }
        }

        Button save = (Button) result.findViewById(R.id.save_button);
        if(save != null){
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dictionary dict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
                    if(dict != null && lang1 != null && lang2 != null){
                        Card newCard = new Card(lang1.getText().toString(), lang2.getText().toString());
                        if(lessons != null){
                            String lesson = getCheckedValueInRadiogroup(lessons);
                            newCard.setLesson(lesson);
                        }
                        if(types != null){
                            String type = getCheckedValueInRadiogroup(types);
                            newCard.setType(type);
                        }

                        dict.addCard(newCard);//Might modify an existing card

                        if(lang1Key != null && !lang1Key.equals(newCard.getLang1())){
                            Card oldCard = dict.getCardByLang1(lang1Key);
                            newCard.setBox( oldCard.getBox() );
                            dict.deleteCard(oldCard);
                        }
                    }
                    if(lang1Key != null){
                       lang1Key = lang1.getText().toString();
                    }
                    if(lang1Key == null) {
                        lang1.setText("");
                        lang2.setText("");
                        lang1.requestFocus();
                    }
                    //Tell user about action
                    if(lang1Key == null){
                        //New card added
                        Toast.makeText(getActivity(), "Neue Karteikarte eingefügt", Toast.LENGTH_LONG).show();
                    }
                    else{
                        //Modified card
                        Toast.makeText(getActivity(), "Änderungen gespeichert.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        return result;
    }
}
