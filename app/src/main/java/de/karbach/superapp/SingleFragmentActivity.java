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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by carsten on 02.10.15.
 *
 * An activity with only a single fragment as content.
 * Takes care of the main navigation to the main activities: BoxActivity, CardListActivity and CardActivity.
 * Navigation is done by clearing all activities except the starter activity and
 * then launching the desired activity on top.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    /**
     *
     * @return Fragment shown for this acitivity
     */
    protected abstract Fragment createFragment();

    /**
     *
     * @return true, if options for navigation should be added, false, if not
     */
    protected boolean addOptions(){
        return true;
    }

    /**
     *
     * @return true, if activity should create the up button. False if this is not required.
     */
    protected boolean showUpButton(){
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.single_fragment);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);

        if(f==null){
            f = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, f).commit();
        }

        if(showUpButton()){
            if( getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result =  super.onCreateOptionsMenu(menu);
        if(! addOptions()){
            return result;
        }

        MenuInflater inflater = getMenuInflater();
        if(inflater != null){
            inflater.inflate(R.menu.main_menu, menu);
        }

        return true;
    }

    private void goToActivity(Class actClass){
        Intent toMainIntent = new Intent(this, StarterActivity.class);
        toMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        toMainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(toMainIntent);

        Intent onTop = new Intent(this, actClass);
        startActivity(onTop);

        if(! (this instanceof StarterActivity)){
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_box:
                goToActivity(BoxActivity.class);

                return true;
            case R.id.menu_item_list:
                goToActivity(CardListActivity.class);

                return true;
            case R.id.menu_item_newword:
                goToActivity(CardActivity.class);

                return true;
            case android.R.id.home://Up button
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return false;
    }
}
