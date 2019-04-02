/*
 VoBox - train your vocabulary
 Copyright (C) 2015-2019  Carsten Karbach

 Contact by mail carstenkarbach@gmx.de
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.superapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Carsten on 28.12.2015.
 *
 * A helper class for all functions related to pictures.
 * Especially, helps to map languages to their flag images.
 */
public class PictureHelper {

    /**
     * all available language IDs in German
     */
    private List<String> allLanguages;
    /**
     * Resource IDs for flags to display.
     * allFlags[0] corresponds to language allLanguages[0]
     * allFlags[1] corresponds to language allLanguages[1]
     * ...
     */
    private List<Integer> allFlags;
    /**
     * Language to display to the user according to localization.
     * E.g. this contains German for English localization, but Deutsch for German localization
     */
    private List<String> allDisplayLanguages;

    /**
     * If no display name is found for a language, this name is returned
     * Is loaded in constructur
     */
    private String defaultDisplayLanguage;

    public PictureHelper(Context context){
        allLanguages = Arrays.asList( context.getResources().getStringArray(R.array.all_languages_array) );
        allDisplayLanguages = Arrays.asList( context.getResources().getStringArray(R.array.all_languages_display_array) );
        TypedArray flagarray = context.getResources().obtainTypedArray(R.array.flags);
        allFlags = new ArrayList<Integer>();
        for(int i = 0; i< flagarray.length(); i++){
            allFlags.add(flagarray.getResourceId(i, R.drawable.flag_german));
        }
        defaultDisplayLanguage = context.getResources().getString(R.string.lang_german);
    }

    /**
     * Get drawable resource id for a language id
     * @param language language id in German
     * @return resource ID for flag to draw for a language
     */
    public int getDrawableResourceForLanguage(String language){
        int index = allLanguages.indexOf(language);
        if(index == -1) {
            return R.drawable.flag_german;
        }
        return allFlags.get(index);
    }

    /**
     * Localizes language
     * @param language German language ID
     * @return corresponding localized display text for language
     */
    public String getDisplaynameForLanguage(String language){
        int index = allLanguages.indexOf(language);
        if(index == -1) {
            return defaultDisplayLanguage;
        }
        return allDisplayLanguages.get(index);
    }

}
