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
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 27.12.2015.
 *
 * This fragment is the entry screen. It allows to navigate to the
 * main activities (box, word list, add new vocabulary). Moreover,
 * the active dictionary can be selected.
 */
public class StarterFragment extends Fragment {

    public static  final int OPENFILECODE = 111;

    private DictSpinnerPresenter dictSpinnerPresenter;

    public void updateSelection(){
        if(dictSpinnerPresenter != null){
            dictSpinnerPresenter.updateSelectedDictionary();
        }
    }

    /**
     * Find spinner with dictionaries and set to the selected dictionary.
     * @param rootView root view of fragment
     */
    protected void fillDictionarySpinner(View rootView){
        if(dictSpinnerPresenter != null){
            dictSpinnerPresenter.freeMe();
        }
        final Spinner select = (Spinner) rootView.findViewById(R.id.language_selection);
        if(select != null){
            dictSpinnerPresenter = new DictSpinnerPresenter(getActivity(), select);
            dictSpinnerPresenter.updateSelectedDictionary();

            select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String value = select.getItemAtPosition(position).toString();
                    DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                    dm.selectDictionary(value);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.starter_fragment, container, false);

        Button newCardButton = (Button) result.findViewById(R.id.newcard_button);
        View.OnClickListener newCardOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CardActivity.class);
                startActivity(intent);
            }
        };
        if(newCardButton != null){
            newCardButton.setOnClickListener(newCardOnClick);
        }
        ImageButton newCardImage = (ImageButton) result.findViewById(R.id.newcard_imagebutton);
        if(newCardImage != null){
            newCardImage.setOnClickListener(newCardOnClick);
        }

        Button listButton = (Button) result.findViewById(R.id.list_button);
        View.OnClickListener listOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CardListActivity.class);
                startActivity(intent);
            }
        };
        if(listButton != null){
            listButton.setOnClickListener(listOnClick);
        }
        ImageButton listImage = (ImageButton) result.findViewById(R.id.list_imagebutton);
        if(listImage != null){
            listImage.setOnClickListener(listOnClick);
        }

        Button boxButton = (Button) result.findViewById(R.id.box_button);
        View.OnClickListener boxOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BoxActivity.class);
                startActivity(intent);
            }
        };
        if(boxButton != null){
            boxButton.setOnClickListener(boxOnClick);
        }
        ImageButton boxImage = (ImageButton) result.findViewById(R.id.box_imagebutton);
        if(boxImage != null){
            boxImage.setOnClickListener(boxOnClick);
        }

        final ImageButton actionsButton = (ImageButton) result.findViewById(R.id.actions_button);
        if(actionsButton != null){
            actionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(getActivity(), actionsButton);
                    popup.getMenuInflater().inflate(R.menu.dictionary_actions, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.dict_edit){
                                Intent intent = new Intent(getActivity(), DictionaryActivity.class);
                                intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.EDIT.ordinal());
                                startActivity(intent);
                                return true;
                            }
                            if(item.getItemId() == R.id.dict_export){
                                DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                                Dictionary selected = dm.getSelectedDictionary();
                                if(selected == null){
                                    return true;
                                }
                                selected.sendExportedDictionary(getActivity());
                            }
                            if(item.getItemId() == R.id.dict_import){
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("text/*");
                                startActivityForResult(intent, OPENFILECODE);
                            }
                            if(item.getItemId() == R.id.dict_new){
                                Intent intent = new Intent(getActivity(), DictionaryActivity.class);
                                intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.NEW.ordinal());
                                startActivity(intent);
                            }
                            if(item.getItemId() == R.id.dict_delete){
                                DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                                Dictionary selected = dm.getSelectedDictionary();
                                if(selected == null){
                                    return true;
                                }
                                String todelete = selected.getName();
                                dm.deleteDictionary(selected.getName());

                                Toast.makeText(getActivity(), getString(R.string.toast_deleted_dict, todelete), Toast.LENGTH_SHORT).show();

                                dictSpinnerPresenter.updateSelectedDictionary();
                            }

                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        return result;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(getView() != null) {
            fillDictionarySpinner(getView());
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == OPENFILECODE && resultCode == Activity.RESULT_OK && data.getData() != null){
            Intent intent = new Intent(getActivity(), StarterActivity.class);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setType("text/plain");
            intent.setData(data.getData());
            getActivity().finish();
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        this.dictSpinnerPresenter.freeMe();
        this.dictSpinnerPresenter = null;
    }
}
