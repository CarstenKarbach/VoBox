package de.karbach.superapp;

/**
 * Created by Carsten on 28.12.2015.
 */
public class PictureHelper {

    public static int getDrawableResourceForLanguage(String language){
        if(language == null){
            return R.drawable.flag_german;
        }
        if(language.equals("Spanisch")){
            return R.drawable.flag_spanish;
        }
        if(language.equals("Schwedisch")){
            return R.drawable.flag_swedish;
        }
        if(language.equals("Englisch")){
            return R.drawable.flag_english;
        }
        return R.drawable.flag_german;
    }

}
