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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     *
     * @param box
     * @return number of cards in the box of the currently selected dirctionary
     */
    protected int getCardNumberInBox(int box){
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();
        ArrayList<Card> boxCards = dict.getCardsForBox(box);

        return boxCards.size();
    }

    /**
     * Init training for a given box. Start TestActivity.
     * @param box the box to train in
     * @param realTest if true, cards are put up and down the box numbers
     * @param askForLanguage2 if true, show base language (e.g. German) and ask for the other(e.g. English). If false ask for the base language
     */
    public void startBoxTraining(int box, boolean realTest, boolean askForLanguage2){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), getString(R.string.toast_nocards), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent  = new Intent(getActivity(), TestActivity.class);
        intent.putExtra(TestActivity.PARAMBOX, box);
        intent.putExtra(TestActivity.PARAMREALTEST, realTest);
        intent.putExtra(TestActivity.PARAMASKFORLANG2, askForLanguage2);
        startActivity(intent);
    }

    /**
     * Start card list activity for a box
     * @param box the box to show card list of
     */
    public void showList(int box){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), getString(R.string.toast_nocards), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent  = new Intent(getActivity(), CardListActivity.class);
        intent.putExtra(CardListActivity.PARAMBOX, box);
        startActivity(intent);
    }

    /**
     * Show popup menu for a box
     * @param box
     */
    public void showPopup(int box){
        int boxsize = getCardNumberInBox(box);
        if(boxsize == 0){
            Toast.makeText(getActivity(), getString(R.string.toast_nocards), Toast.LENGTH_SHORT).show();
            return;
        }

        BoxView bv = getView().findViewById(boxids[box-1]);

        final int currentBox = box;

        PopupMenu popup = new PopupMenu(getActivity(), bv);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.boxpopup, popup.getMenu());
        Menu menu = popup.getMenu();

        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();

        for(int i=0; i<menu.size(); i++){
            MenuItem menuItem = menu.getItem(i);
            if(menuItem.getItemId() == R.id.box_test1){
                menuItem.setTitle(menuItem.getTitle()+": "+dict.getBaseLanguage()+" -> ?");
            }
            if(menuItem.getItemId() == R.id.box_test2){
                menuItem.setTitle(menuItem.getTitle()+": "+dict.getLanguage()+" -> ?");
            }
            if(menuItem.getItemId() == R.id.box_training1){
                menuItem.setTitle(menuItem.getTitle()+": "+dict.getBaseLanguage()+" -> ?");
            }
            if(menuItem.getItemId() == R.id.box_training2){
                menuItem.setTitle(menuItem.getTitle()+": "+dict.getLanguage()+" -> ?");
            }
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.box_list){
                    showList(currentBox);
                }
                if(item.getItemId() == R.id.box_test1){
                    startBoxTraining(currentBox, true, true);
                }
                if(item.getItemId() == R.id.box_test2){
                    startBoxTraining(currentBox, true, false);
                }
                if(item.getItemId() == R.id.box_training1){
                    startBoxTraining(currentBox, false, true);
                }
                if(item.getItemId() == R.id.box_training2){
                    startBoxTraining(currentBox, false, false);
                }

                return true;
            }
        });

        popup.show();//showing popup menu
    }

    /**
     * IDs for all possible box views
     */
    public final static int[] boxids = new int[]{R.id.boxview1, R.id.boxview2, R.id.boxview3, R.id.boxview4, R.id.boxview5,
                                                 R.id.boxview6, R.id.boxview7, R.id.boxview8, R.id.boxview9, R.id.boxview10};

    /**
     * Place cards into box views
     * @param root root view of this fragment
     */
    public void updateBoxViews(View root){
        if(root == null){
            return;
        }
        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();

        BoxView.clearFlags();

        for(int box=1; box<=dict.getBoxcount(); box++){
            BoxView bv = root.findViewById(boxids[box-1]);
            bv.setLanguage1(dict.getBaseLanguage());
            bv.setLanguage2(dict.getLanguage());
            bv.setLevel(box);
            ArrayList<Card> boxCards = dict.getCardsForBox(box);
            bv.setCards(boxCards);
        }
    }

    /**
     * Get access to GestureListener for a box
     * @param box
     * @return GestureListener for box.
     */
    public GestureDetector.OnGestureListener getGestureListener(int box) {
        return mGestureListenerList.get(box-1);
    }

    /**
     * Stores GestureListener for each box
     */
    private Map<Integer, GestureDetector.OnGestureListener> mGestureListenerList = new HashMap<Integer, GestureDetector.OnGestureListener>();

    /**
     * Initialize GestureListener for boxview.
     * Start listening for tocuh and fling events.
     * @param boxview
     * @param currentbox
     */
    public void initGestureDetection(final BoxView boxview, final int currentbox){
        final GestureDetector.OnGestureListener listener = new GestureDetector.OnGestureListener(){
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
                boxview.setOffset( (int)(boxview.getOffset() + distanceX), false );
                boxview.fling(0);
                boxview.startScrollIndicator();
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
        };
        final GestureDetectorCompat mDetector = new GestureDetectorCompat(getActivity(),listener);
        mGestureListenerList.put(currentbox-1, listener);
        boxview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.boxes_fragment, container, false);

        DictionaryManagement dm = DictionaryManagement.getInstance(getActivity());
        Dictionary dict = dm.getSelectedDictionary();

        for(int i=1; i<=10; i++){
            BoxView boxi = result.findViewById(boxids[i-1]);
            if(i <= dict.getBoxcount()){
                final int currentBox = i;
                initGestureDetection(boxi, currentBox);
            }
            else{
                if(boxi.getParent() instanceof LinearLayout) {
                    LinearLayout boxesFrame = (LinearLayout) boxi.getParent();
                    boxesFrame.removeView(boxi);
                    if (boxesFrame.getChildCount() == 0 && boxesFrame.getParent() instanceof LinearLayout) {
                        LinearLayout globalParent = (LinearLayout) boxesFrame.getParent();
                        globalParent.removeView(boxesFrame);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBoxViews(getView());
    }
}
