package com.recipex.fragments;


import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.design.widget.FloatingActionButton;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;
import com.recipex.activities.Home;
import com.recipex.adapters.MisurazioniAdapter;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainMeasurementInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.recipex.asynctasks.DeleteEventsCalendarAT;
import com.recipex.asynctasks.GetMeasurementsUserAT;
import com.recipex.taskcallbacks.CalendarDeleteTC;
import com.recipex.taskcallbacks.DeleteMeasurementTC;
import com.recipex.taskcallbacks.GetMeasurementsTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Misurazione;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Sara on 02/05/2016.
 */

/**
 * fragment holding measurements of the user
 */
public class MisurazioniFragment extends Fragment implements GetMeasurementsTC,
        DeleteMeasurementTC, CalendarDeleteTC/*, Toolbar.OnMenuItemClickListener*/ {

    private final static String TAG = "MISURAZIONI_FRAGMENT";
    private final static int ADD_MEASUREMENT = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int EMPTY_VIEW = 10;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;
    private ConnectionDetector cd;


    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    public ArrayList<String> idEventi;
    static final int REQUEST_ACCOUNT_PICKER2 = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;


    private final int scrollnum=6;

    List<Misurazione> misurazioni=new LinkedList<>();

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 3;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    View mainView;

    private static final String SHOWCASE_ID_MAIN = "Showcase_single_use_main";

    List<String> date;
    FloatingActionMenu fab_menu;
    com.github.clans.fab.FloatingActionButton fab_pressione;
    com.github.clans.fab.FloatingActionButton fab_freq_cardiaca;
    com.github.clans.fab.FloatingActionButton fab_freq_respiratoria;
    com.github.clans.fab.FloatingActionButton fab_temperatura;
    com.github.clans.fab.FloatingActionButton fab_spo2;
    com.github.clans.fab.FloatingActionButton fab_diabete;
    com.github.clans.fab.FloatingActionButton fab_dolore;
    com.github.clans.fab.FloatingActionButton fab_colesterolo;

    private TextView emptyText;
    private CircularProgressView progressView;
    private CoordinatorLayout coordinatorLayout;
    ImageView delete;

    private Long userId;

    static RecyclerView curRecView;
    SharedPreferences pref;

    RecipexServerApi apiHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            rootView = inflater.inflate(R.layout.recyclerview, container, false);
        else
            rootView = inflater.inflate(R.layout.recyclerview2, container, false);

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
                    Log.d(TAG, "ADD_MEASUREMENT: RESULT_OK");
                    Snackbar snackbar = Snackbar
                            .make(getActivity().getWindow().getDecorView().getRootView(),
                                    "Misurazone aggiunta con successo!", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    credential = GoogleAccountCredential.usingAudience(getActivity(), AppConstants.AUDIENCE);
                    //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
                    accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());

                    if (credential.getSelectedAccountName() == null) {
                        Log.d(TAG, "AccountName == null: startActivityForResult.");
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    }
                    else {
                        if(AppConstants.checkNetwork(getActivity())) {
                            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                            new GetMeasurementsUserAT(userId, getContext(), this, apiHandler, scrollnum, 0).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            break;
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "Nell'if.");
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
                        if (AppConstants.checkNetwork(getActivity())) {
                            new GetMeasurementsUserAT(userId, getContext(), this, apiHandler,scrollnum, 0).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
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
     * setup layout elements and listeners on them
     * @param rootView of the fragment
     */
    private void initUI(View rootView) {
        progressView = (CircularProgressView) rootView.findViewById(R.id.home_progress_view);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.home_coordinator);
        emptyText = (TextView) rootView.findViewById(R.id.home_empty_message);
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        final LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        // GET FABs
        fab_menu = (FloatingActionMenu) rootView.findViewById(R.id.home_fab_menu_measurement);
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
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.PRESSIONE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_cardiaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_CARDIACA);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_respiratoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_RESPIRAZIONE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_temperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.TEMP_CORPOREA);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_spo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.SPO2);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_diabete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.GLUCOSIO);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_dolore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.DOLORE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_colesterolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.COLESTEROLO);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });

        pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        userId = pref.getLong("userId", 0L);

        settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(getActivity(), AppConstants.AUDIENCE);
        Log.d(TAG, "Credential: " + credential);
        //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());

        if (credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        else {
            if (userId != 0 && AppConstants.checkNetwork(getActivity())) {
                //final RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                Log.d(TAG, "entro");
                apiHandler = AppConstants.getApiServiceHandle(credential);

                final GetMeasurementsTC f=this;

                //SCROLL listener
                rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        visibleItemCount = curRecView.getChildCount();
                        totalItemCount = llm.getItemCount();
                        firstVisibleItem = llm.findFirstVisibleItemPosition();

                        Log.d(TAG, ""+visibleItemCount);
                        Log.d(TAG, ""+totalItemCount);
                        Log.d(TAG, ""+firstVisibleItem);

                        if (loading) {
                            if (totalItemCount > previousTotal) {
                                loading = false;
                                previousTotal = totalItemCount;
                            }
                        }
                        if (!loading && (totalItemCount - visibleItemCount)
                                <= (firstVisibleItem + visibleThreshold)) {
                            // End has been reached
                            Log.i(TAG, "end called");

                            if(AppConstants.checkNetwork(getActivity()) && !misurazioni.isEmpty()){
                                Log.d(TAG, "scrollll");
                                Log.d(TAG, misurazioni.get(misurazioni.size()-1).data);
                                new GetMeasurementsUserAT(userId, getContext(), f, apiHandler, scrollnum,
                                        misurazioni.get(misurazioni.size()-1).id).execute();
                            }
                            loading = true;
                        }
                    }
                });

                new GetMeasurementsUserAT(userId, getContext(), this, apiHandler, scrollnum, 0).execute();
                progressView.startAnimation();
                progressView.setVisibility(View.VISIBLE);
            }else {
                progressView.stopAnimation();
                progressView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * callback from GetMisurazioniUser
     * @param response from the server
     */
    public void done(MainUserMeasurementsMessage response){
        if((response!=null && response.getMeasurements()!=null) || !misurazioni.isEmpty() ) {
            Log.e(TAG, "Nel done di getMisurazioni");
            List<MainMeasurementInfoMessage> lista;
            if(response!=null && response.getMeasurements() != null && !response.getMeasurements().isEmpty()) {
                //new measurements retrieved
                lista = response.getMeasurements();
            }
            else
                lista = new ArrayList<MainMeasurementInfoMessage>();
            Iterator<MainMeasurementInfoMessage> i = lista.iterator();
            while (i.hasNext()) {
                MainMeasurementInfoMessage cur = i.next();
                Misurazione mcur=new Misurazione();
                switch (cur.getKind()) {
                    case AppConstants.COLESTEROLO:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getChlLevel()));
                        break;
                    case AppConstants.FREQ_CARDIACA:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), Long.toString(cur.getBpm()), "", "");
                        break;
                    case AppConstants.PRESSIONE:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), Long.toString(cur.getSystolic()),
                                Long.toString(cur.getDiastolic()), "");
                        break;
                    case AppConstants.FREQ_RESPIRAZIONE:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), Long.toString(cur.getRespirations()), "", "");
                        break;
                    case AppConstants.SPO2:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getSpo2()));
                        break;
                    case AppConstants.GLUCOSIO:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), "","", Double.toString(cur.getHgt()));
                        break;
                    case AppConstants.TEMP_CORPOREA:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(),"", "", Double.toString(cur.getDegrees()));
                        break;
                    case AppConstants.DOLORE:
                        mcur = new Misurazione(cur.getId(), cur.getKind(), cur.getDateTime(), Long.toString(cur.getNrs()), "", "");
                        break;
                }
                if(cur.getNote()!=null)
                    mcur.setNota(cur.getNote());

				mcur.setIdCalendar(cur.getCalendarId());
				mcur.setId(cur.getId());
                //each new measurement will be added to misurazioni, so the adapter can be updated.
				misurazioni.add(mcur);
				Log.d("MisIterator", mcur.data);
			}

            MisurazioniAdapter adapter = new MisurazioniAdapter(misurazioni, this.getActivity(), this, userId, apiHandler);
            curRecView.setAdapter(adapter);
            Log.d(TAG, "itemCount: "+ adapter.getItemCount());
            if(adapter.getItemViewType(0) == EMPTY_VIEW) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Ancora nessuna misurazione?\nAggiungine subito una cliccando il bottone!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
		else {
            //there are no measurements: set the adapter with an empty list
            MisurazioniAdapter adapter = new MisurazioniAdapter(new ArrayList<Misurazione>(), this.getActivity(), this, userId, apiHandler);
            curRecView.setAdapter(adapter);
        }

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
    }

    /**
     * sets name of the account which performs the operation
     * @param accountName
     */
    /*private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/


    /**
     * callback from DeleteMeasurementAT
     * @param res to check if it is all ok
     * @param calId of the calendar event to be deleted
     * @param response from the server
     */
    @Override
    public void done(boolean res, String calId, MainDefaultResponseMessage response) {
        /*In parallel, delete the event on the calendar if the measurement was deleted from the server correctly,
        and update the fragment*/
        if(res) {
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Misurazione rimossa con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            idEventi=new ArrayList<>();
            idEventi.add(calId);
            // Initialize credentials and service object.
            mCredential = GoogleAccountCredential.usingOAuth2(
                    getContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            getResultsFromApi();
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
                    new GetMeasurementsUserAT(userId, getContext(), this, apiHandler, scrollnum, 0).execute();
                }
            }
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
     * @param b to check if it is all ok
     * @param inutile to distinguish this from other done methods
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
