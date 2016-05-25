package com.recipex.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.recipex.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Fabrizio on 18/05/2016.
 */
public class DateItemAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> integers;
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<String> orari_assunzioni;

    public DateItemAdapter(Context context, List<Integer> integers, ArrayList<String> orari_assunzioni) {
        this.context = context;
        this.integers = integers;
        this.orari_assunzioni = orari_assunzioni;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
    }

    @Override
    public int getCount() {
        return integers.size();
    }

    @Override
    public Object getItem(int position) {
        return integers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        convertView = mLayoutInflater.inflate(R.layout.aggiungi_terapia_date_item, null);
        TextView label = (TextView)convertView.findViewById(R.id.date_item_label);
        label.setText("Assunzione "+(position+1)+"°");
        final EditText ora = (EditText)convertView.findViewById(R.id.date_item_ora);
        ora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog tpd = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // Display Selected time in textbox
                                String myHour;
                                if(hourOfDay < 10)
                                    myHour = "0"+hourOfDay;
                                else
                                    myHour = ""+hourOfDay;

                                String myMin;
                                if(minute < 10)
                                    myMin = "0"+minute;
                                else
                                    myMin = ""+minute;

                                ora.setText(myHour + ":" + myMin);
                                orari_assunzioni.set(pos, myHour + ":" + myMin);
                            }
                        }, mHour, mMinute, false);
                tpd.show();
            }
        });

        return convertView;
    }

}
