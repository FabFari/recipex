package com.recipex.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.recipex.R;
import com.recipex.utilities.Terapia;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class TerapieAdapter extends RecyclerView.Adapter<TerapieAdapter.MyViewHolder>{

    List<Terapia> terapie;

    public TerapieAdapter(List<Terapia> data){
        this.terapie = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.terapia_item, viewGroup, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return terapie.size();
    }



    @Override
    public void onBindViewHolder(MyViewHolder personViewHolder, int i) {
        personViewHolder.nome.setText(terapie.get(i).nome);
        Log.d("TERAPIADOSE  ", i+" "+terapie.get(i).dose);
        personViewHolder.dose.setText("Dose "+Long.toString(terapie.get(i).dose));
        personViewHolder.tipo.setText("Tipo "+terapie.get(i).tipo);
        personViewHolder.ricetta.setText("Ricetta "+(terapie.get(i).ricetta? "SI": "NO"));
        personViewHolder.ingrediente.setText("Ingrediente "+terapie.get(i).ingrediente);
        personViewHolder.unità.setText("Unità "+terapie.get(i).unità);
        personViewHolder.quantità.setText("Quantità "+Long.toString(terapie.get(i).quantità));
        if(!terapie.get(i).foglio.equals(""))
            personViewHolder.foglio.setText("Foglio "+terapie.get(i).foglio);
        else personViewHolder.foglio.setVisibility(View.INVISIBLE);
        if(!terapie.get(i).caregiver.equals(""))
            personViewHolder.foglio.setText("Caregiver "+terapie.get(i).caregiver);
        else personViewHolder.foglio.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nome;
        TextView dose;
        TextView ricetta;
        TextView tipo;
        TextView ingrediente;
        TextView quantità;
        TextView unità;
        TextView foglio;
        TextView caregiver;

        MyViewHolder(View itemView) {
            super(itemView);
            nome = (TextView)itemView.findViewById(R.id.nomeTerapiaCard);
            dose = (TextView)itemView.findViewById(R.id.doseCard);
            ricetta=(TextView)itemView.findViewById(R.id.ricettaCard);
            tipo=(TextView)itemView.findViewById(R.id.tipoCard);
            ingrediente=(TextView)itemView.findViewById(R.id.ingrCard);
            unità=(TextView) itemView.findViewById(R.id.unitCard);
            quantità=(TextView)itemView.findViewById(R.id.quantCard);
            foglio=(TextView)itemView.findViewById(R.id.foglioCard);
            caregiver=(TextView)itemView.findViewById(R.id.caregiverCard);
        }
    }


}