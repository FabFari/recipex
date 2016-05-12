package com.recipex.fragments;

/**
 * Created by Sara on 24/04/2016.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recipex.R;
import com.recipex.adapters.RVAdapterPaziente;

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
