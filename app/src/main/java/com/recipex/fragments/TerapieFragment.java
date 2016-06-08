package com.recipex.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainPrescriptionInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AggiungiTerapia;
import com.recipex.activities.Home;
import com.recipex.adapters.RVAdapter;
import com.recipex.adapters.TerapieAdapter;
import com.recipex.asynctasks.GetTerapieUser;
import com.recipex.taskcallbacks.RimuoviTerapiaTC;
import com.recipex.taskcallbacks.TaskCallbackAggiungiTerapia;
import com.recipex.taskcallbacks.TaskCallbackGetTerapie;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Terapia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 03/05/2016.
 */
public class TerapieFragment extends Fragment implements TaskCallbackGetTerapie, RimuoviTerapiaTC{

    static RecyclerView curRecView;

    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private SharedPreferences settings;
    private SharedPreferences pref;
    private GoogleAccountCredential credential;
    private String accountName;
    private RecipexServerApi apiHandler;
    private Long id;

    private CoordinatorLayout coordinatorLayout;
    private CircularProgressView progressView;

    private ConnectionDetector cd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            rootView = inflater.inflate(R.layout.recyclerview, container, false);
        else
            rootView = inflater.inflate(R.layout.recyclerview2, container, false);

        FloatingActionMenu fab=(FloatingActionMenu) rootView.findViewById(R.id.home_fab_menu_measurement);
        fab.setVisibility(View.GONE);

        FloatingActionButton fabfragment=(FloatingActionButton)rootView.findViewById(R.id.fabfragment);
        // Fabrizio Change
        fabfragment.setVisibility(View.VISIBLE);
        fabfragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), AggiungiTerapia.class);
                startActivity(i);
            }
        });
        initUI(rootView);
        return rootView;
    }

    private void initUI(View rootView) {
        coordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.home_coordinator);
        progressView = (CircularProgressView) rootView.findViewById(R.id.home_progress_view);
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        /*if(args!=null && args.get("nomeTerapia")!=null){
            String nomeTerapia=(String)args.get("nomeTerapia");
            int doseTerapia=(int)args.get("doseTerapia");
            String tipoTerapia=(String)args.get("tipoTerapia");
            boolean ricettaTerapia=(boolean)args.get("ricettaTerapia");
            args.clear();
            TerapieAdapter adapter = new TerapieAdapter(new Terapia(nomeTerapia, doseTerapia, tipoTerapia, ricettaTerapia));
            rv.setAdapter(adapter);
        }*/
        /*if(args!=null && args.get("terapie")!=null){
            String terapie=(String)args.get("terapie");
            String[] terapiearray=terapie.split(" ");
            for(int i=0; i<terapiearray.length; i++){
                t.add(new Terapia(terapiearray[i], 0, "", false));
            }
        }*/
        SharedPreferences pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        id=pref.getLong("userId", 0L);



        if(id!=0 && checkNetwork()){
            //new GetTerapieUser(id, getContext(), this ).execute();
            settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(getContext(), AppConstants.AUDIENCE);
            Log.d("Caregivers", "Credential: " + credential);
            setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

            if(credential.getSelectedAccountName() == null)
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            else {
                apiHandler = AppConstants.getApiServiceHandle(credential);
                new GetTerapieUser(id, getContext(), this, apiHandler).execute();
            }
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
        }
        else {
            //Toast.makeText(getActivity(), "Si è verificato un errore.", Toast.LENGTH_SHORT).show();
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Si è verificato un errore.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ESCI", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return false;
    }

    //callback from GetTerapieUser
    public void done(boolean res, MainUserPrescriptionsMessage response){
        if(res) {
            List<Terapia> terapie=new LinkedList<>();
            if(response.getPrescriptions()!=null){
                List<MainPrescriptionInfoMessage> lista = response.getPrescriptions();
                Iterator<MainPrescriptionInfoMessage> i = lista.iterator();
                while (i.hasNext()) {
                    MainPrescriptionInfoMessage cur = i.next();
                    Terapia tcur;
                    if(cur.getCaregiverName()==null) {
                        Log.d("TERAPIEFRAGMENT", "caregiver null");
                        tcur = new Terapia(cur.getName(), cur.getDose(), cur.getKind(), cur.getRecipe(),
                                cur.getActiveIngrName(), cur.getUnits(), cur.getQuantity(), cur.getPil(), "",cur.getId());
                    }
                    else
                        tcur = new Terapia(cur.getName(), cur.getDose(), cur.getKind(), cur.getRecipe(),
                            cur.getActiveIngrName(), cur.getUnits(), cur.getQuantity(), cur.getPil(), cur.getCaregiverName(), cur.getId());
                    terapie.add(tcur);

                    Log.d("TERAPIEFRAGMENT", cur.getActiveIngrName());

                }
                TerapieAdapter adapter = new TerapieAdapter(terapie, this, apiHandler, this, id);
                Log.d("TERAPIEFRAGMENT", "size " + terapie.size());
                curRecView.setAdapter(adapter);
            }
            else {
                TerapieAdapter adapter = new TerapieAdapter(new ArrayList<Terapia>(), this, apiHandler, this, id);
                Log.d("TERAPIEFRAGMENT", "size " + terapie.size());
                curRecView.setAdapter(adapter);
                //Toast.makeText(getActivity(), "You don't have prescriptions. Add one clicking on the button", Toast.LENGTH_LONG).show();
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Non hai terapie al momento.\nAggiungine una cliccando sul bottone.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        else{
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Si è verificato un errore.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        apiHandler = AppConstants.getApiServiceHandle(credential);
                        new GetTerapieUser(id, getContext(), this, apiHandler).execute();
                    }
                }
                break;
        }
    }

    @Override
    public void done(boolean res, MainDefaultResponseMessage response) {
        if(res) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Terapia rimossa con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Operazione non riuscita!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

        if(checkNetwork()){
            settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(getContext(), AppConstants.AUDIENCE);
            Log.d("Caregivers", "Credential: " + credential);
            setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

            if (credential.getSelectedAccountName() == null) {
                Log.d("Caregivers", "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } else {
                apiHandler = AppConstants.getApiServiceHandle(credential);
                if (checkNetwork()) {
                    progressView.startAnimation();
                    progressView.setVisibility(View.VISIBLE);
                    new GetTerapieUser(id, getContext(), this, apiHandler).execute();
                }
            }
        }
    }
}