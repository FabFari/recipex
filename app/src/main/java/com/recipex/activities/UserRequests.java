package com.recipex.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserRequestsMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.AclRule;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.adapters.RequestsAdapter;
import com.recipex.asynctasks.AnswerRequestAT;
import com.recipex.asynctasks.GetUserRequestsAT;
import com.recipex.taskcallbacks.AnswerRequestTC;
import com.recipex.taskcallbacks.GetUserRequestsTC;
import com.recipex.taskcallbacks.SendRequestTC;
import com.recipex.utilities.ConnectionDetector;

public class UserRequests extends AppCompatActivity implements GetUserRequestsTC, AnswerRequestTC {

    private final static String TAG = "USER_REQUESTS";

    private CoordinatorLayout coordinator;
    private RecyclerView recycler;
    private RequestsAdapter adapter;
    private LinearLayoutManager layoutManager;

    private SharedPreferences myPrefs;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private ConnectionDetector cd;
    private CircularProgressView progressView;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_requests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myPrefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        // userId = myPrefs.getLong("userId", 0L);
        userId = 5705241014042624L;

        bindActivity();

        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if(credential.getSelectedAccountName() == null)
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        else {
            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            if(checkNetwork()) new GetUserRequestsAT(this, this, coordinator, userId, apiHandler).execute();
        }
    }

    private void bindActivity() {
        coordinator = (CoordinatorLayout) findViewById(R.id.requests_coordinator);
        recycler = (RecyclerView) findViewById(R.id.requests_recyclerview);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        progressView = (CircularProgressView) findViewById(R.id.requests_progress_view);
    }

    @Override
    public void done(boolean res, MainUserRequestsMessage response) {
        if (response != null) {
            if (res) {
                adapter = new RequestsAdapter(response.getRequests(), this, progressView);
                recycler.setAdapter(adapter);
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinator, "Operazione non riuscita: " + response.getResponse().getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if(checkNetwork()) new GetUserRequestsAT(this, this, coordinator, userId, apiHandler).execute();
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
                    .make(coordinator, "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
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

    public void executeAsyncTask(Long requestId, Boolean answer) {
        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
        if(checkNetwork()) new AnswerRequestAT(this, this, coordinator, userId, requestId, answer, apiHandler).execute();
    }

    @Override
    public void done(boolean res, MainDefaultResponseMessage response) {
        if (response != null) {
            if (res) {
                Snackbar snackbar = Snackbar
                        .make(coordinator, "Risposta inviata con successo!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);

                SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);
                if(pref.getString("email", "").equals("")) {

                    // Dò accesso al calendario dell'utente
                    com.google.api.services.calendar.Calendar mService = new com.google.api.services.calendar.Calendar.Builder(
                            AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                            .setApplicationName("RecipeX")
                            .build();
                    AclRule rule = new AclRule();
                    AclRule.Scope scope = new AclRule.Scope();
                    scope.setType("user").setValue(pref.getString("email", ""));
                    rule.setScope(scope).setRole("writer");

                    try {
                        // Insert new access rule
                        AclRule createdRule = mService.acl().insert(response.getPayload(), rule).execute();
                        System.out.println(createdRule.getId());
                    }
                    catch(Exception e){e.printStackTrace();}

                }
                else{
                    Toast.makeText(UserRequests.this, "Si è verificato un errore nella condivisione del calendario dell'utente.",
                            Toast.LENGTH_LONG).show();
                }
                
                if(checkNetwork()) new GetUserRequestsAT(this, this, coordinator, userId, apiHandler).execute();

            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinator, "Operazione non riuscita: " + response.getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
                progressView.stopAnimation();
                progressView.setVisibility(View.GONE);
            }
        }
    }
}
