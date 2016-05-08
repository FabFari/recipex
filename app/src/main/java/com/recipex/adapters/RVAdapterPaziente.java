package com.recipex.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.recipex.CircleTransform;
import com.recipex.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class RVAdapterPaziente extends RecyclerView.Adapter<RVAdapterPaziente.MyViewHolderPaziente>{

    List<String> pazienti;

    public RVAdapterPaziente(List<String> pazienti){
        this.pazienti = pazienti;
    }

    @Override
    public RVAdapterPaziente.MyViewHolderPaziente onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pazienti_item, viewGroup, false);
        RVAdapterPaziente.MyViewHolderPaziente pvh = new RVAdapterPaziente.MyViewHolderPaziente(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return pazienti.size();
    }

    @Override
    public void onBindViewHolder(RVAdapterPaziente.MyViewHolderPaziente personViewHolder, int i) {
        personViewHolder.nome.setText(pazienti.get(i));
        String personPhotoUrl = "http://www.dis.uniroma1.it/sites/default/files/pictures/picture-1521-1424796678.jpg";
        Picasso.with(personViewHolder.context).load(personPhotoUrl).transform(new CircleTransform()).into(personViewHolder.foto);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MyViewHolderPaziente extends RecyclerView.ViewHolder {
        CardView cv;
        TextView nome;
        ImageView foto;
        Context context;

        MyViewHolderPaziente(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            nome = (TextView)itemView.findViewById(R.id.nomePaziente);
            foto =(ImageView)itemView.findViewById(R.id.fotoPaziente);
            context=itemView.getContext();
        }
    }
}
