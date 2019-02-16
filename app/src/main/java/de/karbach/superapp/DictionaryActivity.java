/**
 MoTAC - digital board for TAC board game
 Copyright (C) 2015-2016  Carsten Karbach

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


import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Carsten on 28.12.2015.
 *
 * Activity showing a single card. It is used for inserting new cards
 * and for editing existing cards.
 */
public class DictionaryActivity extends SingleFragmentSaveOnPauseActivity {
    @Override
    protected Fragment createFragment() {
        DictionaryFragment result =  new DictionaryFragment();

        int modeKey = getIntent().getIntExtra(DictionaryFragment.PARAMMODE, -1);
        if(modeKey != -1){
            Bundle arguments = new Bundle();
            arguments.putInt(DictionaryFragment.PARAMMODE, modeKey);
            result.setArguments(arguments);
        }

        return result;
    }

}
