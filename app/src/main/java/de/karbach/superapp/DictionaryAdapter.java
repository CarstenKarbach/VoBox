package de.karbach.superapp;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.karbach.superapp.data.Dictionary;
import de.karbach.superapp.data.DictionaryManagement;

/**
 * Adapter for dicationary selection.
 * Shows two flags and dictionary name in every item.
 */
public class DictionaryAdapter extends BaseAdapter {

    /**
     * List of available dictionary names
     */
    private String[] dictNames;
    /**
     * Actvity context for inflating view layout.
     */
    private Activity activity;

    /**
     *
     * @param dictNames List of available dictionary names
     * @param activity context
     */
    public DictionaryAdapter(String[] dictNames, Activity activity) {
        this.dictNames = dictNames;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return dictNames.length;
    }

    @Override
    public Object getItem(int i) {
        return dictNames[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = activity.getLayoutInflater().inflate(R.layout.flagdict_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.dict_name);
        ImageView flag1 = (ImageView) convertView.findViewById(R.id.flagview1);
        ImageView flag2 = (ImageView) convertView.findViewById(R.id.flagview2);

        String nameValue = dictNames[position];

        DictionaryManagement dm = DictionaryManagement.getInstance(activity);
        Dictionary dict = dm.getDictionary(nameValue);

        if(dict != null) {
            PictureHelper ph = new PictureHelper(activity);
            int r1 = ph.getDrawableResourceForLanguage(dict.getBaseLanguage());
            if (flag1 != null) {
                flag1.setImageResource(r1);
            }
            int r2 = ph.getDrawableResourceForLanguage(dict.getLanguage());
            if (flag2 != null) {
                flag2.setImageResource(r2);
            }

            name.setText(nameValue);
        }

        return convertView;
    }

}
