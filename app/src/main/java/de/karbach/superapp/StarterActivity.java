package de.karbach.superapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

public class StarterActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new StarterFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();

        DictionaryManagement dm = DictionaryManagement.getInstance(this);
        dm.saveAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleViewIntent();
    }

    private void showImportDictDialog(final Dictionary newDict, final Uri dataUri){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Wörterbuch importieren");
        alertDialog.setMessage("Wie soll das Wörterbuch importiert werden?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "integrieren", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                dm.integrateDictionary(newDict);
                Toast.makeText(StarterActivity.this, "Integration in Wörterbuch "+newDict.getLanguage()+" abgeschlossen", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }});

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ersetzen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Dictionary allLoaded = Dictionary.loadFromUri(dataUri, true, StarterActivity.this);
                allLoaded.setLanguage(newDict.getLanguage());
                allLoaded.setName(newDict.getName());
                DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                dm.replaceDictionary(allLoaded);
                Toast.makeText(StarterActivity.this, "Wörterbuch "+allLoaded.getLanguage()+" ("+allLoaded.getCards().size()+" Wörter)"+" ersetzt", Toast.LENGTH_SHORT).show();

                Dictionary sel = dm.getSelectedDictionary();
            }
        });

        alertDialog.show();
    }

    /*
         * Called from onNewIntent() for a SINGLE_TOP Activity
         * or onCreate() for a new Activity. For onNewIntent(),
         * remember to call setIntent() to store the most
         * current Intent
         *
         */
    private void handleViewIntent() {
        // Get the Intent action
        Intent intent = getIntent();
        String action = intent.getAction();
        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent
            final Uri dataUri = intent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(dataUri.getScheme(), "file") || TextUtils.equals(dataUri.getScheme(), "content")) {
                final Dictionary loaded = Dictionary.loadFromUri(dataUri, false, this);
                if(loaded != null){
                    String dictInteract = loaded.getLanguage();
                    DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                    Dictionary otherDict = dm.getDictionary(dictInteract);
                    final Dictionary selected = dm.getSelectedDictionary();
                    if(otherDict == null && selected != null){
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Wörterbuch importieren");
                        alertDialog.setMessage("Kein passendes Wörterbuch gefunden. Vokabeln mit aktuellem Wörterbuch ("+selected.getLanguage()+") abgleichen?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loaded.setName(selected.getName());
                                loaded.setLanguage(selected.getLanguage());
                                showImportDictDialog(loaded, dataUri);
                            }
                        });

                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "abbrechen", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }});
                        alertDialog.show();
                    }
                    else{
                        if(otherDict != null) {
                            showImportDictDialog(loaded, dataUri);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean addOptions() {
        return false;
    }
}
