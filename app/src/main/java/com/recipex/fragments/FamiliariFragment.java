package com.recipex.fragments;

import android.Manifest;
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
import android.support.annotation.NonNull;
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
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserRelationsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.Home;
import com.recipex.adapters.PazienteFamiliareAdapter;
import com.recipex.asynctasks.CheckUserRelationsAT;
import com.recipex.asynctasks.GetUserAT;
import com.recipex.asynctasks.GetUserRequestsAT;
import com.recipex.asynctasks.RemoveCalendarAccess;
import com.recipex.taskcallbacks.CalendarTC;
import com.recipex.taskcallbacks.CheckUserRelationsTC;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.taskcallbacks.UpdateRelationInfoTC;
import com.recipex.utilities.ConnectionDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Sara on 14/05/2016.
 */
public class FamiliariFragment extends Fragment implements GetUserTC, UpdateRelationInfoTC, CheckUserRelationsTC, CalendarTC, EasyPermissions.PermissionCallbacks {
    static RecyclerView curRecView;

    private SharedPreferences settings;
    SharedPreferences pref;
    private GoogleAccountCredential credential;
    private String accountName;

    RecipexServerApi apiHandler;

    //for calendar
    GoogleAccountCredential mCredential;
    String emailremove;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    private static final int REQUEST_ACCOUNT_PICKER = 2;

    private CircularProgressView progressView;
    private ConnectionDetector cd;
    private Long id;

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
        id = pref.getLong("userId", 0L);


        if(id!=0 && AppConstants.checkNetwork(getActivity())){
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
                    new GetUserAT(this, getActivity(), id, apiHandler).execute();
                }
            }
        }
        else {
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
    }

    /**
     * callback from GetUser
     * @param res
     * @param message
     */
    public void done(boolean res, final MainUserInfoMessage message) {

        //uso lo stesso adapter dei pazienti perchè ha tutti i campi che mi interessano
        if(message!=null && message.getRelatives()!=null && !message.getRelatives().isEmpty()) {
            List<MainUserMainInfoMessage> m = message.getRelatives();

            PazienteFamiliareAdapter adapter = new PazienteFamiliareAdapter(message.getRelatives(),
                    (Home)getActivity(), false, this, progressView, id, apiHandler);
            curRecView.setAdapter(adapter);
        }
        else {
            PazienteFamiliareAdapter adapter = new PazienteFamiliareAdapter(new ArrayList<MainUserMainInfoMessage>(),
                    (Home)getActivity(), false, this, progressView, id, apiHandler);
            curRecView.setAdapter(adapter);
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
    }
    // setSelectedAccountName definition
    /*private void setSelectedAccountName(String accountName) {
        settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d("Caregivers", "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/

    /**
     * callback from UpdateUserRelationsAT
     * @param resp
     * @param response
     */
    @Override
    public void done(boolean resp, MainDefaultResponseMessage response ) {
        if(resp) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Familiare rimosso con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            //check user relations with that caregiver, to see if I have to remove the access to the calendar
            Long familiareId= Long.parseLong(response.getPayload());

            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            Log.d("Familiari", pref.getString("calendar", ""));
            if (AppConstants.checkNetwork(this.getActivity()) && !pref.getString("calendar", "").equals("")) {
                new CheckUserRelationsAT(this, this.getActivity(), getActivity().getWindow().getDecorView().getRootView(), id, familiareId, apiHandler).execute();
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
                    new GetUserAT(this, getActivity(), id, apiHandler).execute();
                }
            }
        }

    }

    /**
     * callback from CheckUserRelationsAT
     * @param b
     * @param response
     */
    public void done(boolean b, MainUserRelationsMessage response){
        if(response!=null){
            if(b){
                Log.d("FamiliariFragment", "RESPONSE: " + response);

                if(!response.getIsCaregiver() && !response.getIsPcPhysician() && !response.getIsRelative() && !response.getIsVisitingNurse()){
                    Log.d("FamiliariFragment", "arrivo quaa" );
                    emailremove = response.getProfileMail();

                    mCredential = GoogleAccountCredential.usingOAuth2(
                            getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                            .setBackOff(new ExponentialBackOff());
                    getResultsFromApi();
                    /*if(!response.getIsPatient()) {
                        //remove access to both calendars

                        Log.d("CaregiverFragment", "Inizio calendario");
                        getResultsFromApi();
                        getResultsFromApiReverse();
                    }
                    else getResultsFromApi();*/
                }
                /*else if(response.getIsCaregiver() || response.getIsVisitingNurse() || response.getIsPcPhysician()){
                    if(!response.getIsPatient()) {
                        //remove access to both calendars
                        emailremove = response.getProfileMail();
                        calIdRemove=response.getProfileCalId();

                        mCredential = GoogleAccountCredential.usingOAuth2(
                                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                                .setBackOff(new ExponentialBackOff());
                        Log.d("CaregiverFragment", "Inizio calendario");
                        getResultsFromApiReverse();
                    }
                }*/
            }
        }
        else{
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Errore nella rimozione dell'accesso al calendario", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    /**
     * remove relative permission to see my calendar
     */
    public void getResultsFromApi() {
        if (! AppConstants.isGooglePlayServicesAvailable(getActivity())) {
            AppConstants.acquireGooglePlayServices(getActivity());
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount(true);
        } else if (! AppConstants.isDeviceOnline(getActivity())) {
            Toast.makeText(getActivity(), "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            new RemoveCalendarAccess(mCredential, getActivity().getApplicationContext(), this,
                    getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE).getString("calendar",""), emailremove).execute();
        }
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
    private void chooseAccount(boolean b) {
        if (EasyPermissions.hasPermissions(
                getActivity(), Manifest.permission.GET_ACCOUNTS)) {

            if (accountName != null) {
                Log.d("CALENDARcho", "account");

                mCredential.setSelectedAccountName(accountName);
                /*if(b)*/ getResultsFromApi();
                //else getResultsFromApiReverse();
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
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }



    /**
     * callback from RemoveCalendarAccess
     * @param b
     */
    public void done(boolean b){
        if(b){
            Log.d("CaregiverFragment", "DONE_TASKCALLBACK_CALENDAR");
        }
        else{
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Errore nella rimozione dell'accesso al calendario", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }
}

