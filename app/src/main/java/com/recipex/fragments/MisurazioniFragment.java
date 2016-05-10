package com.recipex.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recipex.R;
import com.recipex.adapters.RVAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 02/05/2016.
 */
public class MisurazioniFragment extends Fragment {

    List<String> date;

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
    }

}
