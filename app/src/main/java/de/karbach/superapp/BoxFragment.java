/**
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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

    protected void showPopup(int box){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), "Keine Karten in der Box", Toast.LENGTH_SHORT).show();
            return;
        }

        BoxView bv = getView().findViewById(boxids[box-1]);
        if(bv == null){
            return;
        }

        final int currentBox = box;

        PopupMenu popup = new PopupMenu(getActivity(), bv);
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

    private static int[] boxids = new int[]{R.id.boxview1, R.id.boxview2, R.id.boxview3, R.id.boxview4, R.id.boxview5};

    public void updateBoxViews(View root){
        if(root == null){
            return;
        }
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();

        for(int box=1; box<=5; box++){
            BoxView bv = root.findViewById(boxids[box-1]);
            bv.setLanguage1(dict.getBaseLanguage());
            bv.setLanguage2(dict.getLanguage());
            bv.setLevel(box);
            ArrayList<Card> boxCards = dict.getCardsForBox(box);
            bv.setCards(boxCards);
        }
    }

    public void initGestureDetection(final BoxView boxview, final int currentbox){
        final GestureDetectorCompat mDetector = new GestureDetectorCompat(getActivity(),
                new GestureDetector.OnGestureListener(){
                    @Override
                    public boolean onDown(MotionEvent e) {
                        boxview.fling(0);
                        return true;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {
                    }
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        showPopup(currentbox);
                        return true;
                    }
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        boxview.setOffset( (int)(boxview.getOffset() + distanceX) );
                        boxview.fling(0);
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        boxview.fling(velocityX);
                        return true;
                    }
                }
        );
        boxview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.boxes_fragment, container, false);

        for(int i=1; i<=5; i++){
            BoxView boxi = result.findViewById(boxids[i-1]);
            final int currentBox = i;
            initGestureDetection(boxi, currentBox);
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        //updateBoxViews(getView());
    }
}
