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
import android.content.Intent;
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
 *
 * Retained.
 *
 * Fragment for a single card for adding a new card or editing an existing card.
 * If the parameter PARAMLANG1KEY is given, the fragment tries to edit the corresponding
 * card. If there is no parameter or the card cannot be found, a new card is inserted.
 */
public class CardFragment extends Fragment {
    /**
     * Parameter for the German word, which should be edited
     */
    public static final String PARAMLANG1KEY = "de.karbach.superapp.CardFragment";

    /**
     * Request code for editing the current card.
     * Call this in combination with parameter CardFragment.PARAMLANG1KEY
     */
    public final static int REQUESTEDIT = 1;

    /**
     * This string is used to indicate, that a the card was deleted
     */
    public static final String DELETEDVALUE = "**DELETED**";

    /**
     * Key to use for modification.
     * The corresponding card where the first language equals this key is searched and edited.
     */
    private String lang1Key = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

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
        ImageView flag1 = (ImageView) result.findViewById(R.id.flag1);

        Dictionary dict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
        Card card = null;
        if(dict != null){
            PictureHelper ph = new PictureHelper(getActivity());
            if(flag1 != null){
                int resource = ph.getDrawableResourceForLanguage(dict.getBaseLanguage());
                flag1.setImageResource(resource);
            }
            if(flag2 != null){
                int resource = ph.getDrawableResourceForLanguage(dict.getLanguage());
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

                                Intent result = new Intent();
                                result.putExtra(PARAMLANG1KEY, DELETEDVALUE);//Return deleted value
                                getActivity().setResult(Activity.RESULT_CANCELED, result);

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
                        Card newCard = new Card(lang1.getText().toString().trim(), lang2.getText().toString().trim());
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
                            if(oldCard != null) {
                                newCard.setBox(oldCard.getBox());
                                dict.deleteCard(oldCard);
                            }
                        }
                    }
                    if(lang1Key != null){
                       lang1Key = lang1.getText().toString().trim();
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
                        Intent result = new Intent();
                        result.putExtra(PARAMLANG1KEY, lang1Key);//Return the new identifier of the card
                        getActivity().setResult(Activity.RESULT_OK, result);
                    }
                }
            });
        }


        return result;
    }
}
