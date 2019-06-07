package de.karbach.superapp.data;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Translates with the use of dict.leo.org.
 * Only translations between German and other languages is possible here, but in both directions.
 * Runs get request on dict.leo.org and parses the output html.
 */
public class AutoTranslator {

    public enum RETURN_CODES{
        SUCCESS,//Translations found
        NO_NETWORK, //No network connection
        ERROR //Some other error occurred
    }

    /**
     * Callback interface for finished translations
     */
    public static interface TranslationReceiver{
        public void receiveTranslation(List<String> translations, RETURN_CODES rc);
    }

    /**
     * Asynch task for running get request and translating
     */
    public static class RequestTranslationTask extends AsyncTask<String, String, List<String>> {
        /**
         * Callback when translation is received
         */
        private TranslationReceiver receiver;
        //If this is true, translation is searched on the right side of the table, otherwise on the left side
        private boolean translationIsRight=true;

        // Stores return code for the task
        private RETURN_CODES rc;

        /**
         *
         * @param receiver receiver for retrieved translations
         * @param translationIsRight if true, translation is on the right side of the result table, otherwise left
         */
        public RequestTranslationTask(TranslationReceiver receiver, boolean translationIsRight){
            this.receiver = receiver;
            this.translationIsRight = translationIsRight;
        }

        /**
         * Read result HTML from get request. Parse result into list of translations
         * @param in input stream containing result of GET request
         * @return list of parsed translations
         */
        private List<String> readTranslations(InputStream in){
            List<String> result = new ArrayList<String>();
            // create an InputSource object from /res/raw
            InputSource inputSrc = new InputSource(new BufferedReader(new InputStreamReader(in)));
            // query XPath instance, this is the parser
            XPath xpath = XPathFactory.newInstance().newXPath();
            // specify the xpath expression
            String expression = "//samp";
            // list of nodes queried
            NodeList nodes = null;
            try {
                nodes = (NodeList) xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
            }
            catch(XPathExpressionException exception){
                rc = RETURN_CODES.ERROR;
                return null;
            }

            String[] remove = new String[]{"der", "die", "das"};

            if(nodes != null && nodes.getLength() > 0) {
                int len = nodes.getLength();
                for(int i = 0; i < len; i+=2) {
                    // query value
                    Node sourceNode = nodes.item(translationIsRight ? i : i+1);
                    Node targetNode = nodes.item(translationIsRight ? i+1 : i);
                    if(sourceNode != null && targetNode != null){
                        String source = sourceNode.getTextContent();
                        String target = targetNode.getTextContent();
                        for(String toremove: remove){
                            target = target.replace(toremove, "");
                        }
                        int kplindex = target.indexOf("kein Pl.");
                        if(kplindex != -1){
                            target = target.substring(0, kplindex);
                        }
                        int plindex = target.indexOf("Pl.");
                        if(plindex != -1){
                            target = target.substring(0, plindex);
                        }
                        target = target.trim();
                        if(! result.contains(target)) {
                            result.add(target);
                        }
                    }
                }
            }

            return result;
        }

        @Override
        protected List<String> doInBackground(String... params) {
            String endpoint = params[0];
            URL url = null;
            try{
                url = new URL(endpoint);
            }
            catch(MalformedURLException exception){
                rc = RETURN_CODES.ERROR;
                return null;
            }
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                rc = RETURN_CODES.SUCCESS;
                List<String> result = readTranslations(in);
                return result;
            }
            catch(IOException exception){
                rc = RETURN_CODES.NO_NETWORK;
                if(exception instanceof FileNotFoundException){
                    rc = RETURN_CODES.ERROR;
                }
                return null;
            } finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            receiver.receiveTranslation(result, rc);
        }
    }

    /**
     * Init background task to run get request and parse for translations.
     * Once translations are found, the receiver is informed. On error null is handed back to the receiver.
     * This function is only needed for testing. Use shorter version for normal usage, see below.
     * @param text the source to translate
     * @param sourceLanguage the source language, e.g. deutsch
     * @param targetLanguage the target language, e.g. englisch
     * @param receiver translation receiver callback
     * @param encoding url encoding charset
     * @param urlPrefix prefix for translation page
     */
    public void startTranslation(String text, String sourceLanguage, String targetLanguage, TranslationReceiver receiver, String encoding, String urlPrefix){
        sourceLanguage = sourceLanguage.toLowerCase();
        targetLanguage = targetLanguage.toLowerCase();
        boolean translationIsRight = true;
        String sideParam = "side=left";
        if(sourceLanguage.equals("deutsch")){//
            sourceLanguage = targetLanguage;
            targetLanguage = "deutsch";
            translationIsRight = false;
            sideParam = "side=right";
        }
        String url = urlPrefix+sourceLanguage+"-"+targetLanguage+"/"+text+"?"+sideParam;
        try {
            url = urlPrefix + sourceLanguage + "-" + targetLanguage + "/" + URLEncoder.encode(text, encoding)+"?"+sideParam;
        }
        catch(UnsupportedEncodingException exception){
        }
        System.out.println(url);
        RequestTranslationTask task = new RequestTranslationTask(receiver, translationIsRight);
        task.execute(url);
    }

    /**
     * Init background task to run get request and parse for translations.
     * Once translations are found, the receiver is informed. On error null is handed back to the receiver.
     * @param text the source to translate
     * @param sourceLanguage the source language, e.g. deutsch
     * @param targetLanguage the target language, e.g. englisch
     * @param receiver translation receiver callback
     */
    public void startTranslation(String text, String sourceLanguage, String targetLanguage, TranslationReceiver receiver){
        this.startTranslation(text, sourceLanguage, targetLanguage, receiver, "UTF-8", "https://dict.leo.org/");
    }
}
