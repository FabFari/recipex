package com.recipex.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.utilities.Misurazione;

import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MyViewHolder>{

    List<Misurazione> misurazioni;

    public RVAdapter(List<Misurazione> data){
        misurazioni = data;
    }

    @Override
    public RVAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.misurazione_item, viewGroup, false);
        RVAdapter.MyViewHolder pvh = new RVAdapter.MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return misurazioni.size();
    }

    @Override
    public void onBindViewHolder(RVAdapter.MyViewHolder personViewHolder, int i) {
        Misurazione m=misurazioni.get(i);
        personViewHolder.data.setText(m.data);
        personViewHolder.titolo.setText(m.tipo);
        switch (m.tipo){
            case AppConstants.COLESTEROLO:
                personViewHolder.dato1.setText(m.datodouble);
                break;
            case AppConstants.FREQ_CARDIACA:
                personViewHolder.dato1.setText(m.dato1long);
                break;
            case AppConstants.PRESSIONE:
                personViewHolder.dato1.setText(m.dato1long);
                personViewHolder.dato2.setText(m.dato2long);
                break;
            case AppConstants.FREQ_RESPIRAZIONE:
                personViewHolder.dato1.setText(m.dato1long);
                break;
            case AppConstants.SPO2:
                personViewHolder.dato1.setText(m.datodouble);
                break;
            case AppConstants.GLUCOSIO:
                personViewHolder.dato1.setText(m.datodouble);
                break;
            case AppConstants.TEMP_CORPOREA:
                personViewHolder.dato1.setText(m.datodouble);
                break;
            case AppConstants.DOLORE:
                personViewHolder.dato1.setText(m.dato1long);
                break;
        }
        if(m.nota!=null)
            personViewHolder.nota.setText(m.nota);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView data;
        TextView titolo;
        TextView dato1;
        TextView dato2;
        TextView nota;

        MyViewHolder(View itemView) {
            super(itemView);
            data = (TextView)itemView.findViewById(R.id.data);
            titolo=(TextView)itemView.findViewById(R.id.titolo);
            dato1=(TextView)itemView.findViewById(R.id.dato1);
            dato2=(TextView)itemView.findViewById(R.id.dato2);
            nota=(TextView)itemView.findViewById(R.id.nota);
        }
    }
}
