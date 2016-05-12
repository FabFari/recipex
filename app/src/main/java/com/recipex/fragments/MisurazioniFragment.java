package com.recipex.fragments;

import android.app.Activity;
import android.content.Intent;
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

import com.google.android.gms.plus.model.people.Person;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 02/05/2016.
 */
public class MisurazioniFragment extends Fragment {

    List<String> date;
    com.github.clans.fab.FloatingActionButton fab_pressione;
    com.github.clans.fab.FloatingActionButton fab_freq_cardiaca;
    com.github.clans.fab.FloatingActionButton fab_freq_respiratoria;
    com.github.clans.fab.FloatingActionButton fab_temperatura;
    com.github.clans.fab.FloatingActionButton fab_spo2;
    com.github.clans.fab.FloatingActionButton fab_diabete;
    com.github.clans.fab.FloatingActionButton fab_dolore;
    com.github.clans.fab.FloatingActionButton fab_colesterolo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        date=new LinkedList<String>();
        date.add("12-01-16");
        date.add("24-3-16");
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(date);
        rv.setAdapter(adapter);

        // GET FABs
        fab_pressione = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item1);
        fab_freq_cardiaca = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item2);
        fab_freq_respiratoria = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item3);
        fab_temperatura = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item4);
        fab_spo2 = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item5);
        fab_diabete = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item6);
        fab_dolore = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item7);
        fab_colesterolo = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item8);

        // SET CLICK LISTENERS
        fab_pressione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.PRESSIONE);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_freq_cardiaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_CARDIACA);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_freq_respiratoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_RESPIRAZIONE);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_temperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.TEMP_CORPOREA);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_spo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.SPO2);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_diabete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.GLUCOSIO);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_dolore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.DOLORE);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });
        fab_colesterolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.COLESTEROLO);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
            }
        });

    }

}
class RVAdapter extends RecyclerView.Adapter<RVAdapter.MyViewHolder>{

    List<String> data;

    RVAdapter(List<String> data){
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.misurazione_item, viewGroup, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder personViewHolder, int i) {
        personViewHolder.data.setText(data.get(i));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView data;

        MyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            data = (TextView)itemView.findViewById(R.id.data);
        }
    }


}