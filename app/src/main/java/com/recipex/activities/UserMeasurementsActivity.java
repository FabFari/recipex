package com.recipex.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.recipex.adapters.RVAdapter;
import com.recipex.asynctasks.GetMeasurementsUser;
import com.recipex.taskcallbacks.TaskCallbackGetMeasurements;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Misurazione;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UserMeasurementsActivity extends AppCompatActivity implements TaskCallbackGetMeasurements{

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

    static RecyclerView curRecView;
    RecipexServerApi apiHandler;

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
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if (credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        else {
            if (profileId != 0 && checkNetwork()) {
                apiHandler = AppConstants.getApiServiceHandle(credential);
                new GetMeasurementsUser(profileId, this, this, apiHandler, 0, null).execute();
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

    private void bindActivity() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        progressView = (CircularProgressView) findViewById(R.id.home_progress_view);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.home_coordinator);
        emptyText = (TextView) findViewById(R.id.home_empty_message);
        RecyclerView rv = (RecyclerView)findViewById(R.id.my_recyclerview);
        curRecView=rv;
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
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
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (checkNetwork()) {
                            new GetMeasurementsUser(profileId, this, this, apiHandler, 0, null).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            }
        }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout,
                            "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ESCI", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return false;
    }

    public void done(MainUserMeasurementsMessage response){
        if(response!=null && response.getMeasurements()!=null) {
            Log.e(TAG, "Nel done di getMisurazioni");
            List<Misurazione> misurazioni=new LinkedList<Misurazione>();
            List<MainMeasurementInfoMessage> lista;
            if(response.getMeasurements() != null)
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
            RVAdapter adapter = new RVAdapter(misurazioni, this, null, profileId, apiHandler);
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
            RVAdapter adapter = new RVAdapter(new ArrayList<Misurazione>(), this, null, profileId, apiHandler);
            curRecView.setAdapter(adapter);
        }

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        //Toast.makeText(getActivity(), "You don't have prescriptions. Add one clicking on the button", Toast.LENGTH_LONG).show();
    }
}
