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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import de.karbach.superapp.data.AutoTranslator;
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

    /**
     * Received translations
     */
    private List<String> translations = null;

    /**
     * Position in translations, which is currently shown
     */
    private int translationPos = -1;

    /**
     * language name, for which the edit box can be adapted
     */
    private String translationTargetLanguage=null;

    /**
     * Backup value for target text field of translation, which is changed during translations
     */
    private String backupBeforeTranslation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle arguments = getArguments();
        if(arguments != null) {
            lang1Key = arguments.getString(PARAMLANG1KEY);
        }
    }

    /**
     * Reset translations state. Make translation bar invisible
     * @param rootView current root view of the fragment
     */
    protected void clearTranslations(View rootView){
        final TableRow translationBar = rootView.findViewById(R.id.translation_bar);
        translationBar.setVisibility(View.GONE);
        translations = null;
        translationPos = -1;
        final ProgressBar lang1Progress = rootView.findViewById(R.id.lang1_translation_progress);
        final ProgressBar lang2Progress = rootView.findViewById(R.id.lang2_translation_progress);
        lang1Progress.setVisibility(View.INVISIBLE);
        lang2Progress.setVisibility(View.INVISIBLE);
        translationTargetLanguage=null;
        backupBeforeTranslation = null;
    }

    /**
     * Display currently selected translation
     */
    public void showCurrentTranslation(View rootView){
        final TableRow translationBar = rootView.findViewById(R.id.translation_bar);

        TextView transStats = rootView.findViewById(R.id.found_translations);
        if(translations != null) {
            transStats.setText( (translationPos+1) + "/" + translations.size());
            translationBar.setVisibility(View.VISIBLE);
        }
        else{
            transStats.setText("0/0");
            translationBar.setVisibility(View.GONE);
        }
        Dictionary cdict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
        if(cdict != null){
            TextView targetField = null;
            if(cdict.getBaseLanguage().equals(translationTargetLanguage)){
                targetField = rootView.findViewById(R.id.lang1_text);
            }
            else{
                targetField = rootView.findViewById(R.id.lang2_text);
            }
            if(translations == null) {
                targetField.setText("");
            }
            else {
                targetField.setText(translations.get(translationPos));
            }
        }
    }

    /**
     * Setup translationbar on creation of view
     */
    public void initTranslationBar(View rootView){
        if(translations == null){
            return;
        }
        Dictionary cdict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
        if(cdict != null){
            int targetPosition = 1;
            if(! cdict.getBaseLanguage().equals(translationTargetLanguage)){
                targetPosition = 2;
            }

            final TableRow translationBar = rootView.findViewById(R.id.translation_bar);
            final LinearLayout cardFrame = rootView.findViewById(R.id.cardframe);
            cardFrame.removeView(translationBar);
            cardFrame.addView(translationBar, targetPosition);
        }
        showCurrentTranslation(rootView);
    }

    /**
     * Start intent for translation isntead of translating yourself
     */
    public void startTranslationForward(String text, String sourceLang, String targetLang){
        AutoTranslator translator = new AutoTranslator();
        String url = translator.getUrlForTranslation(text, sourceLang, targetLang, null, null);

        Intent linkIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        linkIntent.setData(uri);
        startActivity(linkIntent);
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

        final Button translate = (Button) result.findViewById(R.id.translate_button);
        //Progress bars for translations
        final ProgressBar lang1Progress = result.findViewById(R.id.lang1_translation_progress);
        final ProgressBar lang2Progress = result.findViewById(R.id.lang2_translation_progress);
        final TableRow translationBar = result.findViewById(R.id.translation_bar);
        //Needed to position the translation bar
        final LinearLayout cardFrame = result.findViewById(R.id.cardframe);
        initTranslationBar(result);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTranslations(CardFragment.this.getView());

                AutoTranslator translator = new AutoTranslator();
                String word = lang1.getText().toString();
                Dictionary cdict = DictionaryManagement.getInstance(getActivity()).getSelectedDictionary();
                String sourceLang = "Englisch";
                String targetLang = "Deutsch";
                TextView ctarget = lang2;
                ProgressBar cprogress = lang2Progress;
                int ctranslationBarPosition = 2;
                backupBeforeTranslation = lang2.getText().toString();
                if(cdict != null){
                    sourceLang = cdict.getBaseLanguage();
                    targetLang = cdict.getLanguage();
                }
                if(lang2.hasFocus()){
                    word = lang2.getText().toString();
                    sourceLang = cdict.getLanguage();
                    targetLang = cdict.getBaseLanguage();
                    ctarget = lang1;
                    cprogress = lang1Progress;
                    ctranslationBarPosition = 1;
                    backupBeforeTranslation = lang1.getText().toString();
                }
                //Text field, where new translations are placed
                final TextView targetForTranslation = ctarget;
                //Progress bar for targetForTranslation
                final ProgressBar translationProgressbar = cprogress;
                //Position where to add the translation bar
                final int translationBarPosition = ctranslationBarPosition;

                translationTargetLanguage = targetLang;

                //Translation via intent
                //startTranslationForward(word, sourceLang, targetLang);

                translationProgressbar.setVisibility(View.VISIBLE);
                //Start asynch task and come back by callback
                translator.startTranslation(word, sourceLang, targetLang, new AutoTranslator.TranslationReceiver() {
                    @Override
                    public void receiveTranslation(List<String> translations, AutoTranslator.RETURN_CODES rc) {
                        translationProgressbar.setVisibility(View.INVISIBLE);
                        if(rc == AutoTranslator.RETURN_CODES.NO_NETWORK){
                            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(rc == AutoTranslator.RETURN_CODES.ERROR || translations == null || translations.size() == 0){
                            Toast.makeText(getActivity(), getString(R.string.no_translation_found), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        CardFragment.this.translations = translations;
                        translationPos = 0;

                        cardFrame.removeView(translationBar);
                        cardFrame.addView(translationBar, translationBarPosition);
                        translationBar.setVisibility(View.VISIBLE);
                        showCurrentTranslation(getView());
                    }
                });
            }
        });
        //Show next possible translation
        ImageButton nextTranslation = result.findViewById(R.id.translate_next);
        nextTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(translations == null){
                    return;
                }
                translationPos++;
                if(translationPos >= translations.size()){
                    translationPos = 0;
                }
                showCurrentTranslation(getView());
            }
        });
        //Show previous translation
        ImageButton backTranslation = result.findViewById(R.id.translate_back);
        backTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(translations == null){
                    return;
                }
                translationPos--;
                if(translationPos < 0){
                    translationPos = translations.size()-1;
                }
                showCurrentTranslation(getView());
            }
        });
        //Close translation bar
        ImageButton translationDone = result.findViewById(R.id.translate_done);
        translationDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(translations == null){
                    return;
                }
                clearTranslations(getView());
            }
        });
        //Dont translate
        ImageButton translationDecline = result.findViewById(R.id.translate_decline);
        translationDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(translations == null){
                    return;
                }
                translations.set(translationPos, backupBeforeTranslation);
                showCurrentTranslation(getView());
                clearTranslations(getView());
            }
        });

        TextWatcher translateVisibleWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int visible = View.VISIBLE;
                if( (lang1.getText() == null || lang1.getText().toString().length() == 0) &&
                        (lang2.getText() == null || lang2.getText().toString().length() == 0)){
                    visible = View.GONE;
                }
                translate.setVisibility(visible);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        lang1.addTextChangedListener(translateVisibleWatcher);
        lang2.addTextChangedListener(translateVisibleWatcher);

        if(card != null){
            if(lang1 != null){
                lang1.setText(card.getLang1());
            }
            if(lang2 != null){
                lang2.setText(card.getLang2());
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
                        Toast.makeText(getActivity(), getString(R.string.toast_newcard), Toast.LENGTH_LONG).show();
                    }
                    else{
                        //Modified card
                        Toast.makeText(getActivity(), getString(R.string.toast_savedchanges), Toast.LENGTH_LONG).show();
                        Intent result = new Intent();
                        result.putExtra(PARAMLANG1KEY, lang1Key);//Return the new identifier of the card
                        getActivity().setResult(Activity.RESULT_OK, result);
                    }

                    clearTranslations(getView());
                }
            });
        }


        return result;
    }
}
