package com.recipex.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.utilities.Misurazione;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<Misurazione> misurazioni;
    private static final int EMPTY_VIEW = 10;

    public RVAdapter(List<Misurazione> data){
        if(data == null)
            misurazioni = new ArrayList<Misurazione>();
        misurazioni = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_misurazione_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.misurazione_item, viewGroup, false);
        RVAdapter.MyViewHolder pvh = new RVAdapter.MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        if(misurazioni != null && misurazioni.size() > 0)
            return misurazioni.size();
        else
            return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (misurazioni == null || misurazioni.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder instanceof MyViewHolder) {
            MyViewHolder personViewHolder = (MyViewHolder) viewHolder;
            Misurazione m = misurazioni.get(i);
            String[] data_ora = m.data.split(" ");
            personViewHolder.data.setText(data_ora[0]);
            personViewHolder.ora.setText(data_ora[1]);
            switch (m.tipo) {
                case AppConstants.COLESTEROLO:
                    personViewHolder.icon.setImageResource(R.drawable.ic_cholesterol_dark);
                    personViewHolder.titolo.setText("Colesterolo");
                    personViewHolder.dato1_lbl.setText("Colesterolo: ");
                    personViewHolder.dato1.setText(m.datodouble);
                    break;
                case AppConstants.FREQ_CARDIACA:
                    personViewHolder.icon.setImageResource(R.drawable.ic_heart_rate_dark);
                    personViewHolder.titolo.setText("Frequenza cardiaca");
                    personViewHolder.dato1_lbl.setText("Numero battiti: ");
                    personViewHolder.dato1.setText(m.dato1long);
                    break;
                case AppConstants.PRESSIONE:
                    personViewHolder.icon.setImageResource(R.drawable.ic_pressure_dark);
                    personViewHolder.titolo.setText("Pressione arteriosa");
                    personViewHolder.dato1_lbl.setText("Sistolica: ");
                    personViewHolder.dato1.setText(m.dato1long);
                    personViewHolder.dato2_lbl.setVisibility(View.VISIBLE);
                    personViewHolder.dato2_lbl.setText("Distolica: ");
                    personViewHolder.dato2.setText(m.dato2long);
                    personViewHolder.dato2.setVisibility(View.VISIBLE);
                    break;
                case AppConstants.FREQ_RESPIRAZIONE:
                    personViewHolder.icon.setImageResource(R.drawable.ic_respirations_dark);
                    personViewHolder.titolo.setText("Frequenza respiratoria");
                    personViewHolder.dato1_lbl.setText("Numero respiri: ");
                    personViewHolder.dato1.setText(m.dato1long);
                    break;
                case AppConstants.SPO2:
                    personViewHolder.icon.setImageResource(R.drawable.ic_spo2_dark);
                    personViewHolder.titolo.setText("Ossigenazione sanguigna");
                    personViewHolder.dato1_lbl.setText("SpO2: ");
                    personViewHolder.dato1.setText(m.datodouble+"%");
                    break;
                case AppConstants.GLUCOSIO:
                    personViewHolder.icon.setImageResource(R.drawable.ic_diabetes_dark);
                    personViewHolder.titolo.setText("Glicemia");
                    personViewHolder.dato1_lbl.setText("HGT: ");
                    personViewHolder.dato1.setText(m.datodouble);
                    break;
                case AppConstants.TEMP_CORPOREA:
                    personViewHolder.icon.setImageResource(R.drawable.ic_temperature_dark);
                    personViewHolder.titolo.setText("Temperatura corporea");
                    personViewHolder.dato1_lbl.setText("Temperatura: ");
                    personViewHolder.dato1.setText(m.datodouble);
                    break;
                case AppConstants.DOLORE:
                    personViewHolder.icon.setImageResource(R.drawable.ic_pain_dark);
                    personViewHolder.titolo.setText("Dolore");
                    personViewHolder.dato1_lbl.setText("Dolore: ");
                    personViewHolder.dato1.setText(m.dato1long);
                    break;
            }
            if (m.nota != null)
                personViewHolder.nota.setText(m.nota);
        }

    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView data;
        TextView ora;
        TextView titolo;
        TextView dato1;
        TextView dato1_lbl;
        TextView dato2;
        TextView dato2_lbl;
        TextView nota;
        ImageView icon;

        MyViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView)itemView.findViewById(R.id.tipo_mes);
            data = (TextView)itemView.findViewById(R.id.data_value);
            ora = (TextView)itemView.findViewById(R.id.ora_value);
            titolo = (TextView)itemView.findViewById(R.id.titolo);
            dato1 = (TextView)itemView.findViewById(R.id.dato1_value);
            dato1_lbl =(TextView)itemView.findViewById(R.id.dato1);
            dato2  =(TextView)itemView.findViewById(R.id.dato2_value);
            dato2_lbl = (TextView)itemView.findViewById(R.id.dato2);
            nota = (TextView)itemView.findViewById(R.id.nota_value);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

}
