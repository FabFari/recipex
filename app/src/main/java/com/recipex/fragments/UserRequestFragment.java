package com.recipex.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import com.recipex.utilities.ConnectionDetector;

/**
 * Created by Sara on 14/05/2016.
 */

/**
 * fragment holding pending requests for the user
 */
public class UserRequestFragment extends Fragment implements GetUserRequestsTC, AnswerRequestTC {

    private SharedPreferences settings;
    private SharedPreferences pref;
    private GoogleAccountCredential credential;
    private String accountName;


    private final static String TAG = "USER_REQUESTS";
    private static final int REQUEST_ACCOUNT_PICKER = 2;

    private CoordinatorLayout coordinator;
    private RecyclerView recycler;
    private RequestsAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ConnectionDetector cd;
    private CircularProgressView progressView;

    private Long userId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.activity_user_requests, container, false);

        initUI(rootView);

        return rootView;
    }

    /**
     * setup layout elements
     * @param rootView
     */
    private void initUI(View rootView) {
        coordinator = (CoordinatorLayout) rootView.findViewById(R.id.requests_coordinator);
        recycler = (RecyclerView) rootView.findViewById(R.id.requests_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        progressView = (CircularProgressView) rootView.findViewById(R.id.requests_progress_view);

        pref = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        userId = pref.getLong("userId", 0L);

        Log.e(TAG, "userId: "+userId);

        if(userId!=0 && AppConstants.checkNetwork(getActivity())){
            settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(getContext(), AppConstants.AUDIENCE);
            Log.d("Caregivers", "Credential: " + credential);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());

            if(credential.getSelectedAccountName() == null)
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            else {
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                //retrieve user's pending requests
                if(AppConstants.checkNetwork(getActivity())) new GetUserRequestsAT(this, this.getActivity(), coordinator, userId, apiHandler).execute();
            }
        }
        else {
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
    }

    /**
     * callback from GetUserRequests
     * @param res to check it is all ok
     * @param response from server, containing user's requests
     */
    public void done(boolean res, MainUserRequestsMessage response) {
        if (response != null) {
            if (res) {
                adapter = new RequestsAdapter(response.getRequests(), this, progressView);
                recycler.setAdapter(adapter);
            } else {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Operazione non riuscita: " + response.getResponse().getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
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
        Log.d("Caregivers", "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/

    /**
     * calls async task to confirm or reject a request. It is called from the adapter.
     * @param requestId
     * @param answer
     */
    public void executeAsyncTask(Long requestId, Boolean answer) {
        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
        if(AppConstants.checkNetwork(getActivity())) new AnswerRequestAT(this, this.getActivity(), coordinator, userId, requestId, answer, apiHandler).execute();
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
                        //setSelectedAccountName(accountName);
                        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this.getActivity());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if(AppConstants.checkNetwork(getActivity())) new GetUserRequestsAT(this, this.getActivity(), coordinator, userId, apiHandler).execute();
                    }
                }
                break;
        }
    }

    /**
     * callback from AnswerRequestAT
     * @param res to check it is all ok
     * @param response from the server
     */
    @Override
    public void done(boolean res, MainDefaultResponseMessage response) {
        if (response != null) {
            if (res) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Risposta inviata con successo!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);

                SharedPreferences pref = getActivity().getSharedPreferences("MyPref", Activity.MODE_PRIVATE);
                /*if(pref.getString("email", "").equals("")) {

                    // if the request is accepted, the calendar is published to the new caregiver
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
                    snackbar = Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),
                            "Operazione non riuscita: " + response.getCode(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }*/

                //Update user's requests
                if(AppConstants.checkNetwork(getActivity())) new GetUserRequestsAT(this, this.getActivity(), coordinator, userId, apiHandler).execute();

            } else {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Operazione non riuscita: " + response.getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
                progressView.stopAnimation();
                progressView.setVisibility(View.GONE);
            }
        }
    }

}