package de.karbach.superapp;

import android.app.Fragment;

/**
 * Created by Carsten on 29.12.2015.
 */
public class BoxActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new BoxFragment();
    }
}
