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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GestureDetectorCompat;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowPopupMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;
import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

import static android.view.MotionEvent.ACTION_DOWN;
import static org.junit.Assert.*;

/**
 * Start all activities you can find. Run basic behaviour on each activity.
 */
@RunWith(RobolectricTestRunner.class)
public class StartActivitiesTest {

    @Test
    public void startActivityTest() {
        StarterActivity activity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        assertFalse(activity.showUpButton());
    }

    @Test
    public void startDictionaryActivity(){
        Dictionary mydict = new Dictionary("MySuperDict");
        mydict.setBaseLanguage("Deutsch");
        mydict.setLanguage("Englisch");
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.addDictionaryObject(mydict);
        dm.selectDictionary("MySuperDict");

        DictionaryActivity activity = Robolectric.buildActivity(DictionaryActivity.class).setup().get();
        assertTrue(activity.showUpButton());

        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.EDIT.ordinal());
        ActivityController<DictionaryActivity> actController = Robolectric.buildActivity(DictionaryActivity.class);
        actController.get().setIntent(intent);
        actController.create();
        activity = actController.get();
        Button saveButton = activity.findViewById(R.id.save_button);
        EditText nameview = activity.findViewById(R.id.dictionary_name);
        nameview.setText("AnotherSuperDict");
        saveButton.performClick();
        assertNotNull(dm.getDictionary("AnotherSuperDict"));
        assertNull(dm.getDictionary("MySuperDict"));

        nameview.setText("Juppdidu");
        saveButton.performClick();
        assertNotNull(dm.getDictionary("Juppdidu"));

        nameview.setText("");
        assertNull(dm.getDictionary(""));
        saveButton.performClick();
        assertNull(dm.getDictionary(""));

        nameview.setText("Juppdidu");

        Button deleteButton = activity.findViewById(R.id.delete_button);
        deleteButton.performClick();
        assertNull(dm.getDictionary("Juppdidu"));

        //Start with dictionary with unknown language
        mydict.setBaseLanguage("KenntNiemand");
        mydict.setLanguage("Gibtsauchnicht");
        mydict.setName("FinalTestDict");
        dm.addDictionaryObject(mydict);
        dm.selectDictionary("FinalTestDict");
        intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.EDIT.ordinal());
        actController = Robolectric.buildActivity(DictionaryActivity.class);
        actController.get().setIntent(intent);
        actController.create().get();

        //Start with intent for new dictionary, try to save on already taken name
        intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(DictionaryFragment.PARAMMODE, DictionaryFragment.Mode.NEW.ordinal());
        actController = Robolectric.buildActivity(DictionaryActivity.class);
        actController.get().setIntent(intent);
        activity = actController.create().get();
        saveButton = activity.findViewById(R.id.save_button);
        nameview = activity.findViewById(R.id.dictionary_name);
        Dictionary oldenglish = dm.getDictionary("Englisch");
        assertNotNull(oldenglish);
        nameview.setText("Englisch");
        saveButton.performClick();
        assertEquals(oldenglish, dm.getDictionary("Englisch"));
        nameview.setText("Gibtsjanicht");
        saveButton.performClick();
        assertNotNull(dm.getDictionary("Gibtsjanicht"));
    }

    @Test
    public void testClickInfoButton() {
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        MenuItem infobutton = new RoboMenuItem(R.id.menu_item_info);
        ShadowActivity shadow = Shadows.shadowOf(starteractivity);
        shadow.clearNextStartedActivities();
        assertNull(shadow.getNextStartedActivity());
        starteractivity.onOptionsItemSelected(infobutton);

        assertNotNull(shadow.getNextStartedActivity());
    }

    @Test
    public void testIntentIsClearedOnDialogClick(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        Intent intent = new Intent(starteractivity,StarterActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary dict = dm.getDictionary("Englisch");

        assertNotNull(dict);
        File savedFile = dict.exportToFile("mystartintent.txt", starteractivity, false);
        Uri uri = Uri.fromFile(savedFile);
        intent.setData(uri);
        ActivityController<StarterActivity> actController = Robolectric.buildActivity(StarterActivity.class);
        actController.get().setIntent(intent);
        StarterActivity intendedActivity = actController.create().get();

        assertNotNull( intendedActivity.getIntent().getAction() );

        //Click on buttons of alert dialog
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        assertNull( intendedActivity.getIntent().getAction() );
    }

    @Test
    public void testStartStarterActivityWithIntents(){
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary dict = dm.getDictionary("Englisch");

        assertNotNull(dict);
        File savedFile = dict.exportToFile("mystartintent.txt", starteractivity, false);
        assertTrue(savedFile.exists());

        Intent intent = new Intent(starteractivity,StarterActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(savedFile);
        intent.setData(uri);
        ActivityController<StarterActivity> actController = Robolectric.buildActivity(StarterActivity.class);
        actController.get().setIntent(intent);
        StarterActivity intendedActivity = actController.create().get();

        //Click on buttons of alert dialog
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();

        FragmentManager fm = intendedActivity.getFragmentManager();
        Fragment dialogFragment = fm.findFragmentByTag("dialog");

        assertNotNull(dialogFragment);
        assertTrue(dialogFragment instanceof DictionarySelectionFragment);

        DictionarySelectionFragment selectDictFragment = (DictionarySelectionFragment) dialogFragment;
        selectDictFragment.getView().findViewById(R.id.dictionary_selected_button).performClick();

        dialogFragment = fm.findFragmentByTag("dialog");
        assertNull(dialogFragment);

        //Test dictionary which cannot be read
        intent = new Intent(starteractivity,StarterActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        savedFile.delete();
        uri = Uri.fromFile(savedFile);
        intent.setData(uri);
        actController = Robolectric.buildActivity(StarterActivity.class);
        actController.get().setIntent(intent);
        actController.create().pause();
    }

    @Test
    public void testOptionsMenuStarter(){
        ActivityController<DictionaryActivity> controller = Robolectric.buildActivity(DictionaryActivity.class).setup();
        DictionaryActivity activity = controller.get();

        DictionaryManagement dm = DictionaryManagement.getInstance(activity);
        dm.selectDictionary("Englisch");

        int[] ids = new int[]{R.id.menu_item_box, R.id.menu_item_list, R.id.menu_item_newword, android.R.id.home, R.id.delete_button};

        for(int id: ids) {
            MenuItem boxlist = new RoboMenuItem(id);
            activity.onOptionsItemSelected(boxlist);
        }

        controller.visible().pause();
    }

    @Test
    public void startCardActivity(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary selected = dm.getSelectedDictionary();
        Card exampleCard = new Card("Beispiel", "example");
        assertEquals("example", exampleCard.getLang2());
        selected.addCard(exampleCard);

        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(CardFragment.PARAMLANG1KEY, "Beispiel");
        ActivityController<CardActivity> actController = Robolectric.buildActivity(CardActivity.class);
        actController.get().setIntent(intent);
        actController.create();

        Dictionary dict = dm.getSelectedDictionary();
        assertNotNull(dict.getBaseLanguage());
        System.out.println(dict.getBaseLanguage());

        //Button clicks
        CardActivity ca = actController.get();
        final TextView lang2 = (TextView) ca.findViewById(R.id.lang2_text);
        lang2.setText("example_test");
        Button save = (Button) ca.findViewById(R.id.save_button);
        save.performClick();
        selected = dm.getSelectedDictionary();
        exampleCard = selected.getCardByLang1("Beispiel");
        assertEquals("example_test", exampleCard.getLang2());

        //Change lang1
        final TextView lang1 = (TextView) ca.findViewById(R.id.lang1_text);
        lang1.setText("AnotherExample");

        save.performClick();
        exampleCard = selected.getCardByLang1("AnotherExample");
        assertNotNull(exampleCard);


        Button delete = (Button) ca.findViewById(R.id.delete_button);
        delete.performClick();
        exampleCard = selected.getCardByLang1("Beispiel");
        assertNull(exampleCard);
    }

    @Test
    public void startCardActivityForNewCard(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        Intent intent = new Intent(starteractivity,CardActivity.class);
        ActivityController<CardActivity> actController = Robolectric.buildActivity(CardActivity.class);
        actController.get().setIntent(intent);
        actController.create();

        //Button clicks
        CardActivity ca = actController.get();
        Button save = (Button) ca.findViewById(R.id.save_button);
        save.performClick();

        Button delete = (Button) ca.findViewById(R.id.delete_button);
        delete.performClick();
    }

    @Test
    public void startListGenActivity(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.selectDictionary("Englisch");

        Dictionary dict = dm.getSelectedDictionary();
        dict.addCard(new Card("eins", "one"));
        dict.addCard(new Card("zwei", "two"));
        dict.addCard(new Card("drei", "three"));
        dict.addCard(new Card("test", "ey1(-et"));
        dict.addCard(new Card("test2", "ey2(-en"));

        ListGeneratorActivity activity = Robolectric.buildActivity(ListGeneratorActivity.class).setup().get();
        Button show = activity.findViewById(R.id.button_showlist);
        show.performClick();

        CheckBox ettCheck = activity.findViewById(R.id.ettCheck);
        CheckBox enCheck = activity.findViewById(R.id.enCheck);
        ettCheck.setChecked(true);
        enCheck.setChecked(true);
        show.performClick();
    }

    /**
     * Add a dictionary wioth a test name and select it
     * @param newName name of the dictionary
     */
    private void initAndSelectDictionary(String newName, int box){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary dict = new Dictionary(newName);
        dict.setBaseLanguage("Deutsch");
        dict.setLanguage("Englisch");
        dict.addCard(new Card("eins", "one("));
        dict.addCard(new Card("zwei", "two"));
        dict.addCard(new Card("drei", "three"));
        dict.addCard(new Card("dreiklammer", "three(bc"));
        for(Card card: dict.getCards()){
            card.setBox(box);
        }
        dm.addDictionaryObject(dict);
        dm.selectDictionary(newName);
    }

    @Test
    public void startTestActivityCardDown() {
        initAndSelectDictionary("startTestActivityCardDown", 2);

        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(TestActivity.PARAMREALTEST, true);
        intent.putExtra(TestActivity.PARAMBOX, 2);
        ActivityController<TestActivity> actController = Robolectric.buildActivity(TestActivity.class);
        actController.get().setIntent(intent);
        actController.create();
        TestActivity activity = actController.visible().get();
        Button checkButton = activity.findViewById(R.id.testcard_check_button);
        checkButton.performClick();

        //Test that no error occurs when no cards are given
        intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(TestActivity.PARAMREALTEST, true);
        intent.putExtra(TestActivity.PARAMBOX, 3);
        actController = Robolectric.buildActivity(TestActivity.class);
        actController.get().setIntent(intent);
        actController.create();
        activity = actController.visible().get();
        Button editButton = activity.findViewById(R.id.testcard_edit);
        editButton.performClick();

        Button backButton = activity.findViewById(R.id.testcard_back_button);
        backButton.performClick();
    }

    @Test
    public void testRealTestForTestFragment(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(TestActivity.PARAMREALTEST, false);
        ActivityController<TestActivity> actController = Robolectric.buildActivity(TestActivity.class);
        actController.get().setIntent(intent);
        actController.create();
        TestActivity activity = actController.visible().get();

        Button editButton = activity.findViewById(R.id.testcard_edit);
        editButton.performClick();
    }

    @Test
    public void startTestActivity(){
        initAndSelectDictionary("startTestActivity",1 );

        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        Dictionary selected = dm.getSelectedDictionary();

        Intent intent = new Intent(starteractivity,CardActivity.class);
        intent.putExtra(TestActivity.PARAMREALTEST, true);
        ActivityController<TestActivity> actController = Robolectric.buildActivity(TestActivity.class);
        actController.get().setIntent(intent);
        actController.create();
        TestActivity activity = actController.visible().get();
        EditText lang2Text = activity.findViewById(R.id.testcard_lang2_text);
        EditText solutionText = activity.findViewById(R.id.testcard_solution_text);
        Button checkButton = activity.findViewById(R.id.testcard_check_button);
        checkButton.performClick();
        //Solution correct
        lang2Text.setText(solutionText.getText());
        System.out.println("Solution correct");
        checkButton.performClick();

        Button nextButton = activity.findViewById(R.id.testcard_next_button);
        nextButton.performClick();
        //Solution wrong
        lang2Text.setText(solutionText.getText()+"Wrong");
        checkButton.performClick();

        nextButton.performClick();
        //Solution with bracket
        solutionText.setText("mysolution(");
        lang2Text.setText("mysolution");
        checkButton.performClick();

        Button backButton = activity.findViewById(R.id.testcard_back_button);
        backButton.performClick();

        Button editButton = activity.findViewById(R.id.testcard_edit);
        editButton.performClick();

        nextButton.performClick();
        nextButton.performClick();//End of testing cards reached

        FragmentManager fm = activity.getFragmentManager();
        TestFragment testf = (TestFragment)fm.findFragmentById(R.id.fragment_container);
        testf.onActivityResult(CardFragment.REQUESTEDIT, Activity.RESULT_OK, new Intent().putExtra(CardFragment.PARAMLANG1KEY, "eins"));

        checkButton.performClick();
        testf.onResume();

        for(int i = 0; i<10; i++) {
            testf.onActivityResult(CardFragment.REQUESTEDIT, Activity.RESULT_CANCELED, new Intent().putExtra(CardFragment.PARAMLANG1KEY, CardFragment.DELETEDVALUE));
        }

        dm.selectDictionary(null);

        testf.switchLanguages();

        editButton = activity.findViewById(R.id.testcard_edit);
        editButton.performClick();

        initAndSelectDictionary("moretestsplease", 1);
        actController = Robolectric.buildActivity(TestActivity.class).setup();
        activity = actController.get();
        nextButton = activity.findViewById(R.id.testcard_next_button);
        backButton = activity.findViewById(R.id.testcard_back_button);

        for(int i=0; i<10; i++){
            backButton.performClick();
        }
        for(int i=0; i<10; i++){
            nextButton.performClick();
        }

        checkButton = activity.findViewById(R.id.testcard_check_button);
        checkButton.performClick();
    }

    @Test
    public void testPictureHelperNull(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        PictureHelper ph = new PictureHelper(starteractivity);
        int res = ph.getDrawableResourceForLanguage(null);
        assertEquals(R.drawable.flag_german, res);
        assertEquals(starteractivity.getResources().getString(R.string.lang_german), ph.getDisplaynameForLanguage(null));
    }

    @Test @Config(qualifiers = "land")
    public void startBoxActivityLandscape() {
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.selectDictionary("Englisch");

        ActivityController<BoxActivity> controller = Robolectric.buildActivity(BoxActivity.class);
        controller.create().start().postCreate(null).resume();
    }

    @Test
    public void startBoxActivity(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.selectDictionary("Englisch");
        //Put some cards in all the boxes
        Dictionary dict = dm.getSelectedDictionary();
        for(int i=0; i<4; i++){
            for(int j=0; j<10; j++){
                Card card = new Card(String.valueOf(i)+"//"+String.valueOf(j), String.valueOf(i)+"//"+String.valueOf(j+j));
                card.setBox(i+1);
                dict.addCard(card);
            }
        }

        Card longcard = new Card("kjaskdlasldaskfjasjfkaskaslf", "saldalsflaskfjafjakslalasaggdsgs saa ");
        longcard.setBox(3);
        dict.addCard(longcard);

        ActivityController<BoxActivity> controller = Robolectric.buildActivity(BoxActivity.class);

        controller.create().start().postCreate(null).resume();

        BoxActivity ba = controller.get();
        ShadowActivity shadow = Shadows.shadowOf(ba);

        controller.visible();

        for(int box=1; box<=5; box++) {
            BoxView bv = ba.findViewById(BoxFragment.boxids[box - 1]);
            Canvas canvas = new Canvas(Shadow.newInstanceOf(Bitmap.class));

            bv.drawBox(canvas);

            bv.startScrollIndicator();
            bv.fling(77);
            for(int offset= -10; offset<10; offset++){
                bv.setOffset(offset*78, false);
                bv.drawBox(canvas);
            }
            bv.drawBox(canvas);

            if(box == 1){
                //Test scroll bar indicator alpha change
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                }
                bv.drawBox(canvas);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                bv.drawBox(canvas);
            }

            assertNotNull(bv);
            bv.fling(10000);
            bv.fling(-10000);

            MotionEvent touchEvent = MotionEvent.obtain(200, 300, MotionEvent.ACTION_MOVE, 15.0f, 10.0f, 0);
            Shadows.shadowOf(bv).getOnTouchListener().onTouch(bv, touchEvent);

            FragmentManager fm = ba.getFragmentManager();
            Fragment f = fm.findFragmentById(R.id.fragment_container);
            assertTrue(f instanceof BoxFragment);

            BoxFragment bf = (BoxFragment) f;
            bf.showPopup(box);
            bf.updateBoxViews(null);
            bf.showPopup(1000);

            PopupMenu popMenu = ShadowPopupMenu.getLatestPopupMenu();
            MenuItem boxlist = new RoboMenuItem(R.id.box_list);
            MenuItem boxtest = new RoboMenuItem(R.id.box_test1);
            MenuItem boxtraining = new RoboMenuItem(R.id.box_training1);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(boxlist);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(boxtest);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(boxtraining);

            boxtest = new RoboMenuItem(R.id.box_test2);
            boxtraining = new RoboMenuItem(R.id.box_training2);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(boxtest);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(boxtraining);


            bf.startBoxTraining(box, false, true);

            Intent expectedIntentTest = new Intent(ba, TestActivity.class);
            Intent expectedIntentList = new Intent(ba, CardListActivity.class);
            Intent actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.startBoxTraining(box, false, false);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.startBoxTraining(box, true, true);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.startBoxTraining(box, true, false);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentTest.getComponent());

            bf.showList(box);
            actual = shadow.getNextStartedActivity();
            assertEquals(actual.getComponent(), expectedIntentList.getComponent());

            GestureDetector.OnGestureListener gestureListener = bf.getGestureListener(box);
            gestureListener.onDown(null);
            gestureListener.onShowPress(null);
            gestureListener.onSingleTapUp(null);
            gestureListener.onScroll(null, null, 10,10);
            gestureListener.onLongPress(null);
            gestureListener.onFling(null, null, 20, 20);
        }
    }

    @Test
    public void testBuildConfig(){
        assertTrue( BuildConfig.DEBUG == true || BuildConfig.DEBUG==false);
    }

    @Test
    public void testCardlistActivityWithSerializedCards(){
        Dictionary dict = new Dictionary("Englisch");
        dict.addCard(new Card("eins", "one"));
        dict.addCard(new Card("zwei", "two"));
        dict.addCard(new Card("drei", "three"));

        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        Intent intent = new Intent(starteractivity,CardListActivity.class);
        intent.putExtra(CardListFragment.PARAMCARDS, dict.getCards());
        ActivityController<CardListActivity> actController = Robolectric.buildActivity(CardListActivity.class);
        actController.get().setIntent(intent);
        CardListActivity activity = actController.setup().get();
    }

    @Test
    public void testCardlistActivitySearchBeforeCreate(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        ActivityController<CardListActivity> actController = Robolectric.buildActivity(CardListActivity.class);

        Intent searchintent = new Intent(starteractivity,CardListActivity.class);
        searchintent.setAction(Intent.ACTION_SEARCH);
        actController.newIntent(searchintent);
        // Foreces getMyFragment to return null, because fragment was not yet created
    }

    @Test
    public void testCardlistActivity(){
        Dictionary mydict = new Dictionary("MyCardlistDict");
        mydict.setBaseLanguage("Deutsch");
        mydict.setLanguage("Englisch");
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();
        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);
        dm.addDictionaryObject(mydict);

        dm.selectDictionary("MyCardlistDict");
        Dictionary dict = dm.getSelectedDictionary();
        dict.addCard(new Card("eins", "one"));
        dict.addCard(new Card("zwei", "two"));
        dict.addCard(new Card("drei", "three"));
        dict.addCard(new Card("aaa", "aaa"));
        dict.addCard(new Card("ccc", "ccc"));
        dict.addCard(new Card("bbb", "bbb"));
        dict.addCard(new Card("", ""));

        CardListActivity activity = Robolectric.buildActivity(CardListActivity.class).setup().get();

        //Start with box intent
        Intent intent = new Intent(starteractivity,CardListActivity.class);
        intent.putExtra(CardListActivity.PARAMBOX, 1);
        ActivityController<CardListActivity> actController = Robolectric.buildActivity(CardListActivity.class);
        actController.get().setIntent(intent);
        activity = actController.setup().get();

        int[] optionIds = new int[]{R.id.menu_item_search, R.id.menu_item_clearsearch, R.id.menu_item_training, R.id.menu_item_sort};

        for(int resource: optionIds) {
            MenuItem menuItem = new RoboMenuItem(resource);
            boolean res = activity.onOptionsItemSelected(menuItem);
            assertTrue(res);
        }

        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_sort));
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_sort));
        dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();

        //Sort although no dictionary is selected
        dm.selectDictionary(null);
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_sort));
        dm.selectDictionary("MyCardlistDict");

        //Search with intent
        Intent searchintent = new Intent(starteractivity,CardListActivity.class);
        searchintent.setAction(Intent.ACTION_SEARCH);
        actController.newIntent(searchintent);

        searchintent = new Intent(starteractivity,CardListActivity.class);

        searchintent.setAction(Intent.ACTION_SEARCH);
        actController.newIntent(searchintent);

        //Click on a card in list, onResult
        FragmentManager fm = activity.getFragmentManager();
        CardListFragment cardlistfragment = (CardListFragment) fm.findFragmentById(R.id.fragment_container);
        cardlistfragment.onListItemClick(null, null, 0,0);
        Shadows.shadowOf(activity).receiveResult(
                new Intent(activity, CardActivity.class),
                Activity.RESULT_OK,
                new Intent());

        cardlistfragment.sortByLanguage("Deutsch");
        cardlistfragment.sortByLanguage("Englisch");

        MenuItem menuItem = new RoboMenuItem(R.id.menu_card_delete);
        cardlistfragment.onContextItemSelected(menuItem);

        cardlistfragment.search("a");
        cardlistfragment.search("b");
        cardlistfragment.search("drei");

        CardListFragment.CardAdapter ca = (CardListFragment.CardAdapter) cardlistfragment.getListAdapter();
        assertEquals( "Deutsch", ca.getLang1());
        assertEquals( "Englisch", ca.getLang2());

        ListView lv = cardlistfragment.getListView();
        View itemview = lv.getChildAt(0);
        itemview.performLongClick();

        ArrayList<Card> nullCards = new ArrayList<Card>();
        nullCards.add(new Card("", ""));
        nullCards.add(new Card("aaa", "aaa"));
        nullCards.add(new Card("ccc", "ccc"));
        nullCards.add(null);
        nullCards.add(new Card("bbb", "bbb"));
        nullCards.add(new Card("", ""));
        nullCards.add(new Card("arg", ""));
        nullCards.add(new Card("aarg", ""));
        cardlistfragment.updateCards(nullCards);
        cardlistfragment.sortByLanguage("Deutsch");
        cardlistfragment.updateCards(nullCards);
        cardlistfragment.sortByLanguage("Englisch");
        cardlistfragment.updateCards(nullCards);

        cardlistfragment.search("XYZabc");
        cardlistfragment.startTrainingWithShownCards();

        //Test with dictionary where base language is not Deutsch
        Dictionary mytwisteddict = new Dictionary("TwistedDict");
        mytwisteddict.setLanguage("Deutsch");
        mytwisteddict.setBaseLanguage("Englisch");
        mytwisteddict.addCard(new Card("english", "englisch"));
        mytwisteddict.addCard(new Card("nice", "nett"));
        dm.addDictionaryObject(mytwisteddict);
        dm.selectDictionary("TwistedDict");
        activity = Robolectric.buildActivity(CardListActivity.class).setup().visible().get();

        //Start with bundled cards
        Bundle bundle = new Bundle();
        bundle.putSerializable(CardListFragment.PARAMCARDS, nullCards);
        activity = Robolectric.buildActivity(CardListActivity.class).setup(bundle).visible().get();
    }

    @Test
    public void testStarterFragment(){
        StarterActivity starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        DictionaryManagement dm = DictionaryManagement.getInstance(starteractivity);

        dm.selectDictionary(null);
        ImageButton actionsButton = starteractivity.findViewById(R.id.actions_button);
        actionsButton.performClick();
        PopupMenu popMenu = ShadowPopupMenu.getLatestPopupMenu();
        int[] ids = new int[]{R.id.dict_edit,R.id.dict_export,R.id.dict_import, R.id.dict_new, R.id.dict_delete};
        for(int id: ids) {
            MenuItem menuitem = new RoboMenuItem(id);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(menuitem);
        }

        Dictionary dict = new Dictionary("newtesttest");
        dict.setBaseLanguage("Deutsch");
        dict.setLanguage("Englisch");
        dict.addCard(new Card("eins", "one"));
        dict.addCard(new Card("zwei", "two"));
        dict.addCard(new Card("drei", "three"));
        dm.addDictionaryObject(dict);
        dm.selectDictionary("newtesttest");

        starteractivity = Robolectric.buildActivity(StarterActivity.class).setup().get();

        Button newCardButton = starteractivity.findViewById(R.id.newcard_button);
        newCardButton.performClick();
        Button listButton = starteractivity.findViewById(R.id.list_button);
        listButton.performClick();
        Button boxButton = starteractivity.findViewById(R.id.box_button);
        boxButton.performClick();

        Robolectric.buildActivity(StarterActivity.class).setup().pause().destroy();

        actionsButton = starteractivity.findViewById(R.id.actions_button);
        actionsButton.performClick();
        popMenu = ShadowPopupMenu.getLatestPopupMenu();
        for(int id: ids) {
            MenuItem menuitem = new RoboMenuItem(id);
            Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(menuitem);
        }

        Spinner select = starteractivity.findViewById(R.id.language_selection);
        Shadows.shadowOf(select).getItemSelectedListener().onNothingSelected(select);

        //Test on activity result of starterfragment
        FragmentManager fm = starteractivity.getFragmentManager();
        StarterFragment starterfragment = (StarterFragment) fm.findFragmentById(R.id.fragment_container);
        starterfragment.updateSelection();
        MenuItem menuitem = new RoboMenuItem(R.id.dict_import);
        Shadows.shadowOf(popMenu).getOnMenuItemClickListener().onMenuItemClick(menuitem);

        File savedFile = dict.exportToFile("mystartintent.txt", starteractivity, false);
        assertTrue(savedFile.exists());
        Uri uri = Uri.fromFile(savedFile);;
        Intent data = new Intent();
        data.setData(uri);
        starterfragment.onActivityResult(StarterFragment.OPENFILECODE, Activity.RESULT_OK, data);

        //Try to resume twice
        starterfragment.onResume();
        Spinner dictSpinner = starterfragment.getView().findViewById(R.id.language_selection);
        String firstLanguage = dictSpinner.getSelectedItem().toString();
        starterfragment.onResume();
        dictSpinner = starterfragment.getView().findViewById(R.id.language_selection);
        String secondLanguage = dictSpinner.getSelectedItem().toString();
        assertEquals(firstLanguage, secondLanguage);
    }


}
