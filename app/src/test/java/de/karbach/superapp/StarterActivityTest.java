package de.karbach.superapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class StarterActivityTest {

    @Test
    public void startActivityTest(){
        StarterActivity activity = Robolectric.setupActivity(StarterActivity.class);
        assertFalse(activity.showUpButton());
    }

}
