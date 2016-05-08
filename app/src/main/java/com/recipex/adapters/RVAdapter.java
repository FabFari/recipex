package com.recipex.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.recipex.R;

import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MyViewHolder>{

    List<String> data;

    public RVAdapter(List<String> data){
        this.data = data;
    }

    @Override
    public RVAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.misurazione_item, viewGroup, false);
        RVAdapter.MyViewHolder pvh = new RVAdapter.MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(RVAdapter.MyViewHolder personViewHolder, int i) {
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
