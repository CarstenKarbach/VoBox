package de.karbach.superapp;

import android.app.Fragment;

/**
 * The wrapper activity for the list generate fragment. The
 * activity is started directly from the StarterFragment.
 */
public class ListGeneratorActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ListGeneratorFragment();
    }
}
