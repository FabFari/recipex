package com.recipex.fragments;

/**
 * Created by Sara on 24/04/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.recipex.R;
import com.recipex.*;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;


public class PazientiFragment extends Fragment {

    List<String> pazienti;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        pazienti=new LinkedList<String>();
        pazienti.add("Billo");
        pazienti.add("Ciccio");
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        RVAdapterPaziente adapter = new RVAdapterPaziente(pazienti);
        rv.setAdapter(adapter);
    }

}
class RVAdapterPaziente extends RecyclerView.Adapter<RVAdapterPaziente.MyViewHolderPaziente>{

    List<String> pazienti;

    RVAdapterPaziente(List<String> pazienti){
        this.pazienti = pazienti;
    }

    @Override
    public MyViewHolderPaziente onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pazienti_item, viewGroup, false);
        MyViewHolderPaziente pvh = new MyViewHolderPaziente(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return pazienti.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolderPaziente personViewHolder, int i) {
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
