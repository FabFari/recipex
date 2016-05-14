package com.recipex.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompatSideChannelService;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;
import com.recipex.activities.Home;
import com.recipex.adapters.PazienteFamiliareAdapter;
import com.recipex.adapters.RVAdapter;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainMeasurementInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainPrescriptionInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;
import com.recipex.activities.AggiungiTerapia;
import com.recipex.adapters.RVAdapter;
import com.recipex.adapters.TerapieAdapter;
import com.recipex.asynctasks.GetMeasurementsUser;
import com.recipex.asynctasks.GetTerapieUser;
import com.recipex.taskcallbacks.TaskCallbackGetMeasurements;
import com.recipex.utilities.Misurazione;
import com.recipex.utilities.Terapia;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 02/05/2016.
 */
public class MisurazioniFragment extends Fragment implements TaskCallbackGetMeasurements {

    private final static int ADD_MEASUREMENT = 1;

    View mainView;

    List<String> date;
    com.github.clans.fab.FloatingActionButton fab_pressione;
    com.github.clans.fab.FloatingActionButton fab_freq_cardiaca;
    com.github.clans.fab.FloatingActionButton fab_freq_respiratoria;
    com.github.clans.fab.FloatingActionButton fab_temperatura;
    com.github.clans.fab.FloatingActionButton fab_spo2;
    com.github.clans.fab.FloatingActionButton fab_diabete;
    com.github.clans.fab.FloatingActionButton fab_dolore;
    com.github.clans.fab.FloatingActionButton fab_colesterolo;

    static RecyclerView curRecView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        FloatingActionButton fabfragment=(FloatingActionButton)rootView.findViewById(R.id.fabfragment);
        fabfragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), AddMeasurement.class);
                startActivity(i);
                getActivity().finish();
            }
        });
        initUI(rootView);
        mainView = rootView;

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_MEASUREMENT:
                if(resultCode == Activity.RESULT_OK) {
                    Snackbar snackbar = Snackbar
                            .make(mainView, "Misurazone aggiunta con successo!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            break;
        }
    }

    private void initUI(View rootView) {
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

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
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_cardiaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_CARDIACA);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_respiratoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_RESPIRAZIONE);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_temperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.TEMP_CORPOREA);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_spo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.SPO2);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_diabete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.GLUCOSIO);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_dolore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.DOLORE);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_colesterolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.COLESTEROLO);
                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });

        SharedPreferences pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        long id=pref.getLong("userId", 0L);

        if(id!=0 && checkNetwork()) new GetMeasurementsUser(id, getContext(), this ).execute();
        else Toast.makeText(getActivity(), "Si è verificato un errore.", Toast.LENGTH_SHORT).show();
    }
    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
        if(isOnline) {
            return true;
        }else{
            new AlertDialog.Builder(getActivity())
                    .setTitle("Ops..qualcosa è andato storto!")
                    .setMessage("Sembra che tu non sia collegato ad internet! ")
                    .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
            return false;
        }
    }

    //callback from GetMisurazioniUser
    public void done(MainUserMeasurementsMessage response){
        if(response!=null && response.getMeasurements()!=null) {
            List<Misurazione> misurazioni=new LinkedList<>();
            List<MainMeasurementInfoMessage> lista = response.getMeasurements();
            Iterator<MainMeasurementInfoMessage> i = lista.iterator();
            while (i.hasNext()) {
                MainMeasurementInfoMessage cur = i.next();
                Misurazione mcur=new Misurazione();
                switch (cur.getKind()) {
                    case AppConstants.COLESTEROLO:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getChlLevel()));
                        break;
                    case AppConstants.FREQ_CARDIACA:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getBpm()), "", "");
                        break;
                    case AppConstants.PRESSIONE:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getSystolic()),
                                Long.toString(cur.getDiastolic()), "");
                        break;
                    case AppConstants.FREQ_RESPIRAZIONE:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getRespirations()), "", "");
                        break;
                    case AppConstants.SPO2:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getSpo2()));
                        break;
                    case AppConstants.GLUCOSIO:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "","", Double.toString(cur.getHgt()));
                        break;
                    case AppConstants.TEMP_CORPOREA:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(),"", "", Double.toString(cur.getDegrees()));
                        break;
                    case AppConstants.DOLORE:
                        mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getNrs()), "", "");
                        break;
                }
                if(cur.getNote()!=null)
                    mcur.setNota(cur.getNote());

                misurazioni.add(mcur);
            }
            RVAdapter adapter = new RVAdapter(misurazioni);
            curRecView.setAdapter(adapter);
        }
        else {
            RVAdapter adapter = new RVAdapter(null);
            curRecView.setAdapter(adapter);
        }
    }

}
