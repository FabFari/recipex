package com.recipex.fragments;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainPrescriptionInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddPrescription;
import com.recipex.activities.Home;
import com.recipex.adapters.TerapieAdapter;
import com.recipex.asynctasks.DeleteEventsCalendarAT;
import com.recipex.asynctasks.GetPrescriptionsUserAT;
import com.recipex.taskcallbacks.DeletePrescriptionTC;
import com.recipex.taskcallbacks.CalendarDeleteTC;
import com.recipex.taskcallbacks.GetPrescriptionsTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Prescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Sara on 03/05/2016.
 */

/**
 * fragment holding prescriptions of the user
 */
public class TerapieFragment extends Fragment implements GetPrescriptionsTC, DeletePrescriptionTC, CalendarDeleteTC {

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

    public ArrayList<String> idEventi;
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    static final int REQUEST_ACCOUNT_PICKER2 = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;


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
                Intent i=new Intent(getActivity(), AddPrescription.class);
                startActivity(i);
            }
        });
        initUI(rootView);
        return rootView;
    }

    /**
     * setup layout elements
     * @param rootView
     */
    private void initUI(View rootView) {
        coordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.home_coordinator);
        progressView = (CircularProgressView) rootView.findViewById(R.id.home_progress_view);
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        SharedPreferences pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        id=pref.getLong("userId", 0L);

        if(id!=0 && AppConstants.checkNetwork(getActivity())){
            settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(getContext(), AppConstants.AUDIENCE);
            Log.d("Caregivers", "Credential: " + credential);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());

            if(credential.getSelectedAccountName() == null)
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            else {
                apiHandler = AppConstants.getApiServiceHandle(credential);
                new GetPrescriptionsUserAT(id, getContext(), this, apiHandler).execute();
            }
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
        }
        else {
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
    }


    /**
     * callback from GetPrescriptionsUserAT
     * @param res boolean to check it is all ok
     * @param response from the server
     */
    public void done(boolean res, MainUserPrescriptionsMessage response){
        if(res) {
            List<Prescription> terapie=new LinkedList<>();
            if(response.getPrescriptions()!=null){
                List<MainPrescriptionInfoMessage> lista = response.getPrescriptions();
                Iterator<MainPrescriptionInfoMessage> i = lista.iterator();
                while (i.hasNext()) {
                    MainPrescriptionInfoMessage cur = i.next();
                    Prescription tcur;
                    if(cur.getCaregiverName()==null) {
                        Log.d("TERAPIEFRAGMENT", "caregiver null");
                        tcur = new Prescription(cur.getName(), cur.getDose(), cur.getKind(), cur.getRecipe(),
                                cur.getActiveIngrName(), cur.getUnits(), cur.getQuantity(), cur.getPil(), "",cur.getId());
                    }
                    else
                        tcur = new Prescription(cur.getName(), cur.getDose(), cur.getKind(), cur.getRecipe(),
                            cur.getActiveIngrName(), cur.getUnits(), cur.getQuantity(), cur.getPil(), cur.getCaregiverName(), cur.getId());
                    if(cur.getCalendarIds()!=null)
                        tcur.setIdCalendar(cur.getCalendarIds());
                    //add all prescriptions to list terapie, so adapter can be setup.
                    terapie.add(tcur);

                    Log.d("TERAPIEFRAGMENT", cur.getActiveIngrName());

                }
                TerapieAdapter adapter = new TerapieAdapter(terapie, this, apiHandler, this, id);
                Log.d("TERAPIEFRAGMENT", "size " + terapie.size());
                curRecView.setAdapter(adapter);
            }
            else {
                //if there is no prescription, set adapter with an empty list.
                TerapieAdapter adapter = new TerapieAdapter(new ArrayList<Prescription>(), this, apiHandler, this, id);
                Log.d("TERAPIEFRAGMENT", "size " + terapie.size());
                curRecView.setAdapter(adapter);
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

    /**
     * sets name of the account which performs the operation
     * @param accountName
     */
    /*private void setSelectedAccountName(String accountName) {
        settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/

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
                        //setSelectedAccountName(accountName);
                        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        apiHandler = AppConstants.getApiServiceHandle(credential);
                        new GetPrescriptionsUserAT(id, getContext(), this, apiHandler).execute();
                    }
                }
                break;
            case REQUEST_ACCOUNT_PICKER2:
                if (resultCode == getActivity().RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    Log.d("CALENDARres", "entro in res");

                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getContext().getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        //editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Log.d("CALENDARres", accountName);
                        getResultsFromApi();
                    }
                }
                break;
        }
    }

    /**
     * callback from DeletePrescriptionAT
     * @param res to check it is all ok
     * @param ids of events created on the calendar(may be empty)
     * @param response from the server
     */
    @Override
    public void done(boolean res, ArrayList<String> ids, MainDefaultResponseMessage response) {
        /*In parallel, delete the events on the calendar (if there are some) if the prescription was deleted from the server correctly,
        and update the fragment*/
        if(res) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Terapia rimossa con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            if(!ids.isEmpty()) {
                idEventi = ids;
                // Initialize credentials and service object.
                mCredential = GoogleAccountCredential.usingOAuth2(
                        getContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());
                getResultsFromApi();
            }
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Operazione non riuscita!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

        if(AppConstants.checkNetwork(getActivity())){
            settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(getContext(), AppConstants.AUDIENCE);
            Log.d("Caregivers", "Credential: " + credential);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());

            if (credential.getSelectedAccountName() == null) {
                Log.d("Caregivers", "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } else {
                apiHandler = AppConstants.getApiServiceHandle(credential);
                if (AppConstants.checkNetwork(getActivity())) {
                    progressView.startAnimation();
                    progressView.setVisibility(View.VISIBLE);
                    new GetPrescriptionsUserAT(id, getContext(), this, apiHandler).execute();
                }
            }
        }
        else{
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);

        }
    }

    /**
     * calls async task to delete events from the calendar
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Connessione assente!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            Log.d("CALENDARgetres", "task");
            new DeleteEventsCalendarAT(mCredential, getContext(), this, idEventi).execute();
        }
    }
    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.GET_ACCOUNTS)) {

            /*
            String accountName = getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            */
            String accountName = getContext().getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE)
                    .getString(AppConstants.DEFAULT_ACCOUNT, null);
            if (accountName != null) {
                Log.d("CALENDARcho", "account");

                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                Log.d("CALENDARcho", "choose");

                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER2);
            }
        } else {
            Log.d("CALENDARcho", "noperm");

            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getActivity());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    /**
     * callback from DeleteEventsCalendarAT
     * @param b boolean to check it is all ok
     * @param inutile to distinguish from other done methods
     */
    public void done(boolean b, String inutile){
        idEventi.clear();
        if(!b){
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Errore: aggiunto evento sul calendario" +
                            "che non corrisponde a una misurazione.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        Intent i =new Intent(getActivity(), Home.class);
        startActivity(i);
        getActivity().finish();
    }
}