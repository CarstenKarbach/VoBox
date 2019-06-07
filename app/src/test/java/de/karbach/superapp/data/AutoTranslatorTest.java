package de.karbach.superapp.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test auto translation
 */
@RunWith(RobolectricTestRunner.class)
public class AutoTranslatorTest {

    @Test
    public void testTranslation(){
        AutoTranslator translator = new AutoTranslator();
        final List<String> foundtranslations = new ArrayList<String>();
        AutoTranslator.TranslationReceiver receiver = new AutoTranslator.TranslationReceiver() {
            @Override
            public void receiveTranslation(List<String> translations) {
                foundtranslations.clear();
                if(translations != null){
                    foundtranslations.addAll(translations);
                }
            }
        };
        translator.startTranslation("test", "englisch", "deutsch", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertFalse(foundtranslations.isEmpty() );

        translator.startTranslation("Universität", "deutsch", "englisch", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertFalse(foundtranslations.isEmpty() );

        translator.startTranslation("buey", "spanisch", "deutsch", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertFalse(foundtranslations.isEmpty() );

        translator.startTranslation("Universität", "deutsch", "spanisch", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertFalse(foundtranslations.isEmpty() );

        translator.startTranslation("laskdkasodjjgnsnja", "deutsch", "englisch", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertTrue(foundtranslations.isEmpty() );

        translator.startTranslation("test", "sprach", "sprach2", receiver);
        Robolectric.flushBackgroundThreadScheduler();
        assertTrue(foundtranslations.isEmpty() );
    }

}
