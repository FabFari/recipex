package com.recipex.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainMeasurementInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.adapters.MisurazioniAdapter;
import com.recipex.asynctasks.GetMeasurementsUserAT;
import com.recipex.taskcallbacks.GetMeasurementsTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Misurazione;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * from user's own profile, if he is a caregiver, he can see measurements taken by his patients
 */
public class UserMeasurementsActivity extends AppCompatActivity implements GetMeasurementsTC {

    private static final String TAG = "USER_MEASUREMENTS_ACT";
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int EMPTY_VIEW = 10;

    private CoordinatorLayout coordinatorLayout;
    private FrameLayout frameLayout;
    private CircularProgressView progressView;
    private TextView emptyText;
    private Long profileId;
    FloatingActionMenu fab_menu;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;

    private ConnectionDetector cd;

    List<Misurazione> misurazioni=new LinkedList<>();;

    static RecyclerView curRecView;
    LinearLayoutManager llm;
    RecipexServerApi apiHandler;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 4;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private final int scrollnum=6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setContentView(R.layout.activity_user_measurements);
        else
            setContentView(R.layout.activity_user_measurements_lollipop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        profileId = intent.getLongExtra("profileId", 0L);

        bindActivity();

        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        Log.d(TAG, "Credential: " + credential);
        //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

        if (credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        else {
            if (profileId != 0 && AppConstants.checkNetwork(this)) {
                apiHandler = AppConstants.getApiServiceHandle(credential);

                //SCROLL listener
                //variables to use in listener
                final GetMeasurementsTC t=this;
                final Activity a=this;
                curRecView.setOnScrollListener(new RecyclerView.OnScrollListener() {
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

                            if(AppConstants.checkNetwork(a) && !misurazioni.isEmpty()){
                                Log.d(TAG, "scrollll");
                                Log.d(TAG, misurazioni.get(misurazioni.size()-1).data);
                                new GetMeasurementsUserAT(profileId, getApplicationContext(), t, apiHandler, scrollnum,
                                        misurazioni.get(misurazioni.size()-1).id).execute();
                            }

                            loading = true;
                        }

                    }
                });


                new GetMeasurementsUserAT(profileId, getApplicationContext(), this, apiHandler, scrollnum, 0).execute();
                progressView.startAnimation();
                progressView.setVisibility(View.VISIBLE);

            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout,
                                "Operazione fallita! Si è verificato un errore imprevisto!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }

    }

    /**
     * setup layout elements
     */
    private void bindActivity() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        progressView = (CircularProgressView) findViewById(R.id.home_progress_view);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.home_coordinator);
        emptyText = (TextView) findViewById(R.id.home_empty_message);
        RecyclerView rv = (RecyclerView)findViewById(R.id.my_recyclerview);
        curRecView=rv;
        llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        fab_menu = (FloatingActionMenu) findViewById(R.id.home_fab_menu_measurement);
        fab_menu.setVisibility(View.GONE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "Nell'if.");
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        //setSelectedAccountName(accountName);
                        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (AppConstants.checkNetwork(this)) {
                            new GetMeasurementsUserAT(profileId, this, this, apiHandler, scrollnum, 0).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            }
        }

    /**
     * sets name of the account which performs the operation
     * @param accountName
     */
    /*private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/


    /**
     * done from GetMeasurementsUserAT. Populates the list of measurements and sets the adapter
     * @param response from the server
     */
    public void done(MainUserMeasurementsMessage response){
        if((response!=null && response.getMeasurements()!=null) || !misurazioni.isEmpty()) {
            Log.e(TAG, "Nel done di getMisurazioni");
            List<MainMeasurementInfoMessage> lista;
            if(response!=null && response.getMeasurements() != null && !response.getMeasurements().isEmpty())
                lista = response.getMeasurements();
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

                misurazioni.add(mcur);
            }
            MisurazioniAdapter adapter = new MisurazioniAdapter(misurazioni, this, null, profileId, apiHandler);
            curRecView.setAdapter(adapter);
            Log.d(TAG, "itemCount: "+ adapter.getItemCount());
            if(adapter.getItemViewType(0) == EMPTY_VIEW) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout,
                                "Ancora nessuna misurazione?\nAggiungine subito una cliccando il bottone!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        else {
            MisurazioniAdapter adapter = new MisurazioniAdapter(new ArrayList<Misurazione>(), this, null, profileId, apiHandler);
            curRecView.setAdapter(adapter);
        }

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        //Toast.makeText(getActivity(), "You don't have prescriptions. Add one clicking on the button", Toast.LENGTH_LONG).show();
    }
}
