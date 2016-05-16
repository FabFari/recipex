package com.recipex.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.adapters.RequestsAdapter;
import com.recipex.asynctasks.AnswerRequestAT;
import com.recipex.asynctasks.GetUserRequestsAT;
import com.recipex.taskcallbacks.AnswerRequestTC;
import com.recipex.taskcallbacks.GetUserRequestsTC;
import com.recipex.taskcallbacks.SendRequestTC;
import com.recipex.taskcallbacks.TaskCallbackCalendar;
import com.recipex.utilities.ConnectionDetector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserRequests extends AppCompatActivity implements GetUserRequestsTC, AnswerRequestTC{

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
        userId = myPrefs.getLong("userId", 0L);
        //userId = 5705241014042624L;
        Log.d(TAG, "userId: "+ userId);

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
