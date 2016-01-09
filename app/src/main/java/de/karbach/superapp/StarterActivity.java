package de.karbach.superapp;

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
            Uri dataUri = intent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(dataUri.getScheme(), "file") || TextUtils.equals(dataUri.getScheme(), "content")) {
                final Dictionary loaded = Dictionary.loadFromUri(dataUri, false, this);
                if(loaded != null){
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Wörterbuch importieren");
                    alertDialog.setMessage("Wie soll das Wörterbuch importiert werden?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "integrieren", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                            dm.integrateDictionary(loaded);
                            Toast.makeText(StarterActivity.this, "Integration in Wörterbuch "+loaded.getLanguage()+" abgeschlossen", Toast.LENGTH_SHORT).show();
                        }
                    });

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "abbrechen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }});

                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ersetzen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DictionaryManagement dm = DictionaryManagement.getInstance(StarterActivity.this);
                            dm.replaceDictionary(loaded);
                            Toast.makeText(StarterActivity.this, "Wörterbuch "+loaded.getLanguage()+" ersetzt", Toast.LENGTH_SHORT).show();
                        }
                    });

                    alertDialog.show();
                }
            }
        }
    }
}
