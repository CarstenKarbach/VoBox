package de.karbach.superapp;

import android.app.Fragment;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 27.12.2015.
 */
public class StarterFragment extends Fragment {

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

        final Spinner select = (Spinner) result.findViewById(R.id.language_selection);
        if(select != null){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.languages_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            select.setAdapter(adapter);

            DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
            Dictionary selectedDict = dm.getSelectedDictionary();
            if(selectedDict != null) {
                for (int i = 0; i < select.getCount(); i++) {
                    String name = select.getItemAtPosition(i).toString();
                    if (name.equals(selectedDict.getName())) {
                        select.setSelection(i);
                        break;
                    }
                }
            }

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

        Button exporter = (Button) result.findViewById(R.id.button_export);
        if(exporter != null){
            exporter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
                    if(dm == null){
                        return;
                    }
                    Dictionary selected = dm.getSelectedDictionary();
                    if(selected == null){
                        return;
                    }
                    selected.sendExportedDictionary(getActivity());
                }
            });
        }

        return result;
    }
}
