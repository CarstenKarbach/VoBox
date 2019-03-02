package de.karbach.superapp;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import de.karbach.superapp.data.Card;
import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

public class DictionarySelectionFragment extends DialogFragment {

    public interface OnDictionarySelected{
        public void onDictionarySelected(String selected);
    }

    public void setCallback(OnDictionarySelected callback) {
        this.callback = callback;
    }

    private OnDictionarySelected callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.dictionary_selection_fragment, container, false);

        final Spinner select = (Spinner) result.findViewById(R.id.dictionary_language_selection);
        if(select != null) {
            DictSpinnerPresenter dictSpinnerPresenter = new DictSpinnerPresenter(getActivity(), select);
            dictSpinnerPresenter.updateSelectedDictionary();
            dictSpinnerPresenter.freeMe();
        }

        final Button okbutton = result.findViewById(R.id.dictionary_selected_button);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDict = select.getSelectedItem().toString();
                callback.onDictionarySelected(selectedDict);
                setCallback(null);
                dismiss();
            }
        });

        return result;
    }

}
