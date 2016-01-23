package de.karbach.superapp;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by Carsten on 29.12.2015.
 */
public class BoxActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new BoxFragment();
    }
}
