package com.recipex.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.asynctasks.RimuoviTerapiaAT;
import com.recipex.asynctasks.UpdateRelationInfoAT;
import com.recipex.taskcallbacks.RimuoviTerapiaTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Terapia;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class TerapieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<Terapia> terapie;
    RecipexServerApi apiHandler;
    Fragment fragment;
    ConnectionDetector cd;
    RimuoviTerapiaTC taskCallbak;
    Long user_id;
    private static final String TAG = "TERAPIE_ADAPTER";
    private static final int EMPTY_VIEW = 10;

    public TerapieAdapter(List<Terapia> data, Fragment fragment, RecipexServerApi apiHandler,
                          RimuoviTerapiaTC taskCallback, Long user_id){
        if(data != null)
            this.terapie = data;
        else
            this.terapie = new ArrayList<Terapia>();
        this.fragment = fragment;
        this.apiHandler = apiHandler;
        this.taskCallbak =taskCallback;
        this.user_id = user_id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_terapie_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }
		
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.terapia_item, viewGroup, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        if(terapie != null && terapie.size() > 0)
            return terapie.size();
        else
            return 1;
    }


    @Override
    public int getItemViewType(int position) {
        if (terapie == null || terapie.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder instanceof MyViewHolder) {
            final MyViewHolder terapieViewHolder = (MyViewHolder) viewHolder;
            final int pos = i;
            if(terapie.get(i).tipo.equals(AppConstants.PILLOLA)) {
                terapieViewHolder.tipo.setText("Pillole");
                terapieViewHolder.icon.setImageResource(R.drawable.ic_pill);
            }
            else if(terapie.get(i).tipo.equals(AppConstants.BUSTINE)){
                terapieViewHolder.tipo.setText("Bustine");
                terapieViewHolder.icon.setImageResource(R.drawable.ic_sachet_dark);
            }
            else  if(terapie.get(i).tipo.equals(AppConstants.FIALA)) {
                terapieViewHolder.tipo.setText("Fiale");
                terapieViewHolder.icon.setImageResource(R.drawable.ic_vial);
            }
            else if(terapie.get(i).tipo.equals(AppConstants.CREMA)) {
                terapieViewHolder.tipo.setText("Crema");
                terapieViewHolder.icon.setImageResource(R.drawable.ic_cream_dark);
            }
            else {
                terapieViewHolder.tipo.setText("Altro");
                terapieViewHolder.icon.setImageResource(R.drawable.ic_other);
            }
            terapieViewHolder.nome.setText(terapie.get(i).nome);
            Log.d("TERAPIADOSE  ", i + " " + terapie.get(i).dose);
            terapieViewHolder.dose.setText(Long.toString(terapie.get(i).dose));
            terapieViewHolder.ricetta.setText((terapie.get(i).ricetta ? "SI" : "NO"));
            terapieViewHolder.ingrediente.setText(terapie.get(i).ingrediente);
            terapieViewHolder.unità.setText(terapie.get(i).unità);
            terapieViewHolder.quantità.setText(Long.toString(terapie.get(i).quantità));
            if (terapie.get(i).foglio != null && !terapie.get(i).foglio.equals("")) {
                terapieViewHolder.foglio_empty.setVisibility(View.GONE);
                terapieViewHolder.foglio.setVisibility(View.VISIBLE);
                Log.e(TAG, "foglio: "+ terapie.get(i).foglio);
                terapieViewHolder.foglio.setText(terapie.get(i).foglio);

                terapieViewHolder.foglio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = terapie.get(pos).foglio;
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        fragment.getActivity().startActivity(browserIntent);
                    }
                });
            }
            if (terapie.get(i).caregiver != null && terapie.get(i).caregiver.equals(""))
                terapieViewHolder.foglio.setText(terapie.get(i).caregiver);
            //else terapieViewHolder.foglio.setVisibility(View.INVISIBLE);
            terapieViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());

                    // Add the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(checkNetwork()) {
                                //Log.d(TAG, "ID: "+user_id);
                                //Log.d(TAG, "CAREGIVER ID: "+caregivers.get(pos).getId());
                                new RimuoviTerapiaAT(taskCallbak, fragment.getActivity(),
                                        fragment.getActivity().getWindow().getDecorView().getRootView(),
                                        terapie.get(pos).id, user_id, apiHandler).execute();
                            }
                        }
                    });
                    builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Vuoi rimuovere "+terapie.get(pos).nome+" dalle tue terapie?")
                            .setTitle("Attenzione");

                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView nome;
        TextView tipo;
        TextView dose;
        TextView ricetta;
        TextView ingrediente;
        TextView quantità;
        TextView unità;
        TextView foglio;
        TextView foglio_empty;
        TextView caregiver;
        ImageView remove;

        MyViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.tipo_ter);
            nome = (TextView)itemView.findViewById(R.id.nomeTerapiaCard);
            dose = (TextView)itemView.findViewById(R.id.doseCard);
            ricetta = (TextView)itemView.findViewById(R.id.ricettaCard);
            tipo = (TextView)itemView.findViewById(R.id.tipoCard);
            ingrediente = (TextView)itemView.findViewById(R.id.ingrCard);
            unità = (TextView) itemView.findViewById(R.id.unitCard);
            quantità = (TextView)itemView.findViewById(R.id.quantCard);
            foglio = (TextView)itemView.findViewById(R.id.foglioCard);
            foglio_empty = (TextView) itemView.findViewById(R.id.foglioCard_empty);
            caregiver = (TextView)itemView.findViewById(R.id.caregiverCard);
            remove = (ImageView)itemView.findViewById(R.id.remove);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(fragment.getActivity().getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Snackbar snackbar = Snackbar
                    .make(fragment.getActivity().getWindow().getDecorView().getRootView(),
                            "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ESCI", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fragment.getActivity().finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return false;
    }

}