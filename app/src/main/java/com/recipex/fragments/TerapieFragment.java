package com.recipex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recipex.R;
import com.recipex.activities.AggiungiTerapia;
import com.recipex.adapters.RVAdapter;
import com.recipex.adapters.TerapieAdapter;
import com.recipex.taskcallbacks.TaskCallbackAggiungiTerapia;
import com.recipex.utilities.Terapia;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 03/05/2016.
 */
public class TerapieFragment extends Fragment{

    List<Terapia> t;
    static RecyclerView r;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        initUI(rootView);
        return rootView;
    }

    private void initUI(View rootView) {

        t=new LinkedList<>();
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        r=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        Bundle args=getArguments();
        if(args!=null && args.get("nomeTerapia")!=null){
            String nomeTerapia=(String)args.get("nomeTerapia");
            int doseTerapia=(int)args.get("doseTerapia");
            String tipoTerapia=(String)args.get("tipoTerapia");
            boolean ricettaTerapia=(boolean)args.get("ricettaTerapia");
            args.clear();
            TerapieAdapter adapter = new TerapieAdapter(new Terapia(nomeTerapia, doseTerapia, tipoTerapia, ricettaTerapia));
            rv.setAdapter(adapter);
        }
        else{
            TerapieAdapter adapter = new TerapieAdapter(t);
            rv.setAdapter(adapter);
        }
    }


    public static void onDataChanged(Terapia t){
        TerapieAdapter adapter = new TerapieAdapter(t);
        r.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}