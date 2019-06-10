package de.karbach.superapp.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowWifiManager;

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
            public void receiveTranslation(List<String> translations, AutoTranslator.RETURN_CODES rc) {
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

    @Test
    public void testExceptions(){
        AutoTranslator translator = new AutoTranslator();
        AutoTranslator.TranslationReceiver receiver = new AutoTranslator.TranslationReceiver() {
            @Override
            public void receiveTranslation(List<String> translations, AutoTranslator.RETURN_CODES rc) {
            }
        };
        translator.startTranslation("test", "deutsch", "englisch", receiver, "UTUT", "https://dict.leo.org/");

        //Test network connection disabled

        //Malformed url
        translator.startTranslation("malformed", "deutsch", "englisch", receiver, "UTUT", "file...");
    }

    @Test
    public void getTranslationURLTest(){
        AutoTranslator translator = new AutoTranslator();
        String url = translator.getUrlForTranslation("test", "englisch", "deutsch", null, null);
        assertNotNull(url);

        url = translator.getUrlForTranslation("kohle", "deutsch", "englisch", null, null);
        assertNotNull(url);

        System.out.println(url);
    }
}
