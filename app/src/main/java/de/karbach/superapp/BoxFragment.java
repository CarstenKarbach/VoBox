/**
 MoTAC - digital board for TAC board game
 Copyright (C) 2015-2016  Carsten Karbach

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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Created by Carsten on 29.12.2015.
 *
 * Show a list of boxes and allow access to tests, lists and practising
 * in each box.
 */
public class BoxFragment extends Fragment{

    protected int getCardNumberInBox(int box){
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();
        ArrayList<Card> boxCards = dict.getCardsForBox(box);

        return boxCards.size();
    }

    protected void startBoxTraining(int box, boolean realTest){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), "Keine Karten in der Box", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent  = new Intent(getActivity(), TestActivity.class);
        intent.putExtra(TestActivity.PARAMBOX, box);
        intent.putExtra(TestActivity.PARAMREALTEST, realTest);
        startActivity(intent);
    }

    protected void showList(int box){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), "Keine Karten in der Box", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent  = new Intent(getActivity(), CardListActivity.class);
        intent.putExtra(CardListActivity.PARAMBOX, box);
        startActivity(intent);
    }

    private Button getButtonForBox(int box, View rootView){
        if(rootView == null){
            return null;
        }

        if(box == 1){
            return (Button) rootView.findViewById(R.id.box1_button);
        }
        else if(box == 2) {
            return (Button) rootView.findViewById(R.id.box2_button);
        }
        else if(box == 3) {
            return (Button) rootView.findViewById(R.id.box3_button);
        }
        else if(box == 4) {
            return (Button) rootView.findViewById(R.id.box4_button);
        }
        else {
            return (Button) rootView.findViewById(R.id.box5_button);
        }
    }

    private TextView getCardsNumberTextViewForBox(int box, View rootView){
        if(rootView == null){
            return null;
        }

        if(box == 1){
            return (TextView) rootView.findViewById(R.id.box1_text);
        }
        else if(box == 2) {
            return (TextView) rootView.findViewById(R.id.box2_text);
        }
        else if(box == 3) {
            return (TextView) rootView.findViewById(R.id.box3_text);
        }
        else if(box == 4) {
            return (TextView) rootView.findViewById(R.id.box4_text);
        }
        else {
            return (TextView) rootView.findViewById(R.id.box5_text);
        }
    }

    protected void showPopup(int box){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), "Keine Karten in der Box", Toast.LENGTH_SHORT).show();
            return;
        }

        Button boxButton = getButtonForBox(box, getView());
        if(boxButton == null){
            return;
        }

        final int currentBox = box;

        PopupMenu popup = new PopupMenu(getActivity(), boxButton);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.boxpopup, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.box_list){
                    showList(currentBox);
                }
                if(item.getItemId() == R.id.box_test){
                    startBoxTraining(currentBox, true);
                }
                if(item.getItemId() == R.id.box_training){
                    startBoxTraining(currentBox, false);
                }

                return true;
            }
        });

        popup.show();//showing popup menu
    }


    public void updateCardNumbers(View root){
        if(root == null){
            return;
        }
        for(int i=1; i<=5; i++){
            TextView texti = getCardsNumberTextViewForBox(i, root);
            int cards = getCardNumberInBox(i);
            texti.setText(String.valueOf(cards));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.boxes_fragment, container, false);

        for(int i=1; i<=5; i++){
            Button boxi = getButtonForBox(i, result);
            final int currentBox = i;
            boxi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(currentBox);
                }
            });

            TextView texti = getCardsNumberTextViewForBox(i, result);
            texti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showList(currentBox);
                }
            });
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCardNumbers(getView());
    }
}
