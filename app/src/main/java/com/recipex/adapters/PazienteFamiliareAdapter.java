package com.recipex.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.AppConstants;
import com.recipex.CircleTransform;
import com.recipex.R;
import com.recipex.activities.Home;
import com.recipex.activities.Profile;
import com.recipex.asynctasks.UpdateRelationInfoAT;
import com.recipex.taskcallbacks.UpdateRelationInfoTC;
import com.recipex.utilities.ConnectionDetector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sara on 07/05/2016.
 */

/**
 * adapter for patients and relatives
 */
public class PazienteFamiliareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int EMPTY_VIEW = 10;

    List<MainUserMainInfoMessage> pazienti;
    Home activity;
    UpdateRelationInfoTC taskCallback;
    CircularProgressView progressView;
    boolean pazientefamiliare;
    ConnectionDetector cd;
    Long user_id;
    RecipexServerApi apiHandler;

    //boolean: true patient, false familiar
    public PazienteFamiliareAdapter(List<MainUserMainInfoMessage> pazienti, Home activity, boolean pazientefamiliare,
                                    UpdateRelationInfoTC taskCallback, CircularProgressView progressView,
                                    Long user_id, RecipexServerApi apiHandler){
        if(pazienti != null)
            this.pazienti = pazienti;
        else
            this.pazienti = new ArrayList<MainUserMainInfoMessage>();
        this.activity=activity;
        this.pazientefamiliare=pazientefamiliare;
        this.taskCallback = taskCallback;
        this.progressView = progressView;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
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
            final MyViewHolderPaziente personViewHolder=(MyViewHolderPaziente) viewHolder;
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
            if(!pazientefamiliare) {
                personViewHolder.remove.setVisibility(View.VISIBLE);
                personViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                        // Add the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (AppConstants.checkNetwork(activity)) {

                                    new UpdateRelationInfoAT(user_id, pazienti.get(pos).getId(), AppConstants.FAMILIARE,
                                                activity.getWindow().getDecorView().getRootView(), activity, taskCallback,
                                                apiHandler).execute();

                                }
                            }
                        });
                        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });

                        String msg="familiari";

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage("Vuoi rimuovere " + personViewHolder.nome.getText().toString()
                                + " dai " + msg + "?")
                                .setTitle("Attenzione");

                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
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

    /**
     * class holding information about the patient or the familiar (the information showed is the same)
     */
    public static class MyViewHolderPaziente extends RecyclerView.ViewHolder {
        CardView cv;
        TextView nome;
        ImageView foto;
        ImageView remove;

        MyViewHolderPaziente(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            nome = (TextView)itemView.findViewById(R.id.nomePaziente);
            foto =(ImageView)itemView.findViewById(R.id.fotoPaziente);
            remove =(ImageView)itemView.findViewById(R.id.remove);
        }
    }

}
