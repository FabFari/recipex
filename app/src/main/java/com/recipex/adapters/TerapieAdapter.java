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

    public TerapieAdapter(Terapia newdata){
        if(terapie==null) {
            Log.d("Terapie ", "null");
            terapie = new LinkedList<>();
        }
        else{
            Log.d("SIZE ", " "+terapie.size());
        }
        Log.d("Terapia ", newdata.nome);
        terapie.add(newdata);
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
        Log.d("LETTERA I", " "+i);
        Log.d("terapia posizione ", terapie.get(0).nome);
        personViewHolder.nome.setText(terapie.get(i).nome);
        personViewHolder.dose.setText(Integer.toString(terapie.get(i).dose));
        personViewHolder.tipo.setText(terapie.get(i).tipo);
        personViewHolder.ricetta.setText(terapie.get(i).ricetta? "SI": "NO");

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

        MyViewHolder(View itemView) {
            super(itemView);
            nome = (TextView)itemView.findViewById(R.id.nomeTerapiaCard);
            dose = (TextView)itemView.findViewById(R.id.doseCard);
            ricetta=(TextView)itemView.findViewById(R.id.ricettaCard);
            tipo=(TextView)itemView.findViewById(R.id.tipoCard);
        }
    }


}