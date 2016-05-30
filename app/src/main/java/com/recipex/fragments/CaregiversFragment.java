package com.recipex.fragments;

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
import com.appspot.recipex_1281.recipexServerApi.model.MainMeasurementInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;
import com.recipex.activities.Home;
import com.recipex.adapters.CaregiverAdapter;
import com.recipex.adapters.RVAdapter;
import com.recipex.adapters.UsersAdapter;
import com.recipex.asynctasks.CheckUserRelationsAT;
import com.recipex.asynctasks.GetMeasurementsUser;
import com.recipex.asynctasks.GetUserAT;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.taskcallbacks.UpdateRelationInfoTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Misurazione;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Sara on 12/05/2016.
 */
public class CaregiversFragment extends Fragment implements GetUserTC, UpdateRelationInfoTC{
    static RecyclerView curRecView;

    private SharedPreferences settings;
    SharedPreferences pref;
    private GoogleAccountCredential credential;
    private String accountName;

    CircularProgressView progressView;
    RecipexServerApi apiHandler;
    ConnectionDetector cd;
    Long id;

    private static final int REQUEST_ACCOUNT_PICKER = 2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            rootView = inflater.inflate(R.layout.recyclerview, container, false);
        else
            rootView = inflater.inflate(R.layout.recyclerview2, container, false);

        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        FloatingActionButton fab=(FloatingActionButton)rootView.findViewById(R.id.fabfragment);
        fab.setVisibility(View.GONE);

        FloatingActionMenu fabhome=(FloatingActionMenu) rootView.findViewById(R.id.home_fab_menu_measurement);
        fabhome.setVisibility(View.GONE);

        progressView = (CircularProgressView) rootView.findViewById(R.id.home_progress_view);
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        id=pref.getLong("userId", 0L);


        if(id!=0 && checkNetwork()){
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
                    new GetUserAT(this, getActivity(), id, apiHandler).execute();
                }
            }
        }
        else Toast.makeText(getActivity(), "Si è verificato un errore.", Toast.LENGTH_SHORT).show();
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

    //callback from GetUser
    public void done(boolean res, final MainUserInfoMessage message) {
        if(message!=null && ((message.getCaregivers()!=null && !message.getCaregivers().isEmpty()) || message.getPcPhysician()!=null
        || message.getVisitingNurse()!=null)) {
            Log.d("CaregiversFragment", "setto adapter");

            List<MainUserMainInfoMessage> m=new LinkedList<>();
            Set<String> emailcaregivers=new HashSet<>();

            if(message.getPcPhysician()!=null) {
                emailcaregivers.add(message.getPcPhysician().getEmail());
            }
            else if (message.getVisitingNurse()!=null) {
                emailcaregivers.add(message.getVisitingNurse().getEmail());
            }
            m=message.getCaregivers();

            if(m!=null && !m.isEmpty()) {
                Iterator<MainUserMainInfoMessage> i = m.iterator();
                while (i.hasNext()) {
                    MainUserMainInfoMessage cur = (MainUserMainInfoMessage) i.next();
                    emailcaregivers.add(cur.getEmail());
                }


            }
            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet("emailcaregivers", emailcaregivers);
            editor.commit();
            Log.d("CaregiversFragment", " "+emailcaregivers.size());

            CaregiverAdapter adapter = new CaregiverAdapter(message.getCaregivers(), message.getPcPhysician(),
                    message.getVisitingNurse(), (Home) getActivity(), null, this, id, apiHandler);
            curRecView.setAdapter(adapter);
        }
        else {
            CaregiverAdapter adapter = new CaregiverAdapter(null, null, null, (Home)getActivity(),
                    null, this, id, apiHandler);
            curRecView.setAdapter(adapter);
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
        Log.d("Caregivers", "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    @Override
    public void done(boolean resp, MainDefaultResponseMessage response) {
        if(resp) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Caregiver rimosso con successo!", Snackbar.LENGTH_SHORT);
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
                    new GetUserAT(this, getActivity(), id, apiHandler).execute();
                }
            }
        }

    }
}
