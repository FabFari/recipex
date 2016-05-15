package com.recipex.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.recipex.CircleTransform;
import com.recipex.R;
import com.recipex.activities.Home;
import com.recipex.activities.Profile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sara on 07/05/2016.
 */
public class PazienteFamiliareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int EMPTY_VIEW = 10;

    List<MainUserMainInfoMessage> pazienti;
    Home activity;
    boolean pazientefamiliare;

    //boolean: true paziente, false familiare
    public PazienteFamiliareAdapter(List<MainUserMainInfoMessage> pazienti, Home activity, boolean pazientefamiliare){
        if(pazienti != null)
            this.pazienti = pazienti;
        else
            this.pazienti = new ArrayList<MainUserMainInfoMessage>();
        this.activity=activity;
        this.pazientefamiliare=pazientefamiliare;
    }

    @Override
    public int getItemCount() {
        if(pazienti != null && pazienti.size() > 0)
            return pazienti.size();
        else
            return 1;
    }
    @Override
    public int getItemViewType(int position) {
        if (pazienti == null || pazienti.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == EMPTY_VIEW) {
            if(pazientefamiliare)
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_pazienti_empty, viewGroup, false);
            else v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_familiare_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pazienti_item, viewGroup, false);
        MyViewHolderPaziente rvh = new MyViewHolderPaziente(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final int pos = i;
        if(viewHolder instanceof MyViewHolderPaziente) {
            MyViewHolderPaziente personViewHolder=(MyViewHolderPaziente) viewHolder;
            personViewHolder.nome.setText(String.format(Locale.getDefault(), "%s %s",
                    pazienti.get(i).getName(), pazienti.get(i).getSurname()));

            Picasso.with(activity.getApplicationContext()).load(pazienti.get(i).
                    getPic()).into(personViewHolder.foto);

            personViewHolder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                    myIntent.putExtra("profileId", pazienti.get(pos).getId());
                    activity.startActivity(myIntent);
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class MyViewHolderPaziente extends RecyclerView.ViewHolder {
        CardView cv;
        TextView nome;
        ImageView foto;

        MyViewHolderPaziente(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            nome = (TextView)itemView.findViewById(R.id.nomePaziente);
            foto =(ImageView)itemView.findViewById(R.id.fotoPaziente);
        }
    }
}
