package com.recipex.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        return rootView;
    }

    private void initUI(View rootView) {
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

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

    //callback from GetTerapieUser
    public void done(MainUserMeasurementsMessage response){
        if(response!=null) {
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
            Toast.makeText(getActivity(), "You don't have prescriptions. Add one clicking on the button", Toast.LENGTH_LONG).show();
        }
    }

}
