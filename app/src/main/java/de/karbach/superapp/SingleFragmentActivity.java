package de.karbach.superapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by carsten on 02.10.15.
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.single_fragment);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);

        if(f==null){
            f = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, f).commit();
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
        }

        return false;
    }
}
