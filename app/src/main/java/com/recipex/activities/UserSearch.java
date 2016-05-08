package com.recipex.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserListOfUsersMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.adapters.UsersAdapter;
import com.recipex.asynctasks.GetUsersAT;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.taskcallbacks.GetUsersTC;
import com.recipex.utilities.ConnectionDetector;

import java.util.ArrayList;
import java.util.List;

public class UserSearch extends AppCompatActivity implements GetUsersTC {

    private final static String TAG = "USER_SEARCH";

    private CoordinatorLayout coordinator;
    private RecyclerView recycler;
    private UsersAdapter adapter;
    private LinearLayoutManager layoutManager;
    private EditText searchBar;
    private TextView emptyText;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private ConnectionDetector cd;
    private CircularProgressView progressView;

    private ArrayList<MainUserMainInfoMessage> partialUsers = new ArrayList<MainUserMainInfoMessage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindActivity();

        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if(credential.getSelectedAccountName() == null)
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        else {
            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            if(checkNetwork()) new GetUsersAT(this, this, coordinator, apiHandler).execute();
        }

    }

    private void bindActivity() {
        coordinator = (CoordinatorLayout) findViewById(R.id.user_search_coordinator);
        recycler = (RecyclerView) findViewById(R.id.user_search_recycler);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        searchBar = (EditText) findViewById(R.id.user_search_seachbar);
        progressView = (CircularProgressView) findViewById(R.id.user_search_progress_view);
        emptyText = (TextView) findViewById(R.id.user_search_empty_message);
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

    @Override
    public void done(boolean res, final MainUserListOfUsersMessage response) {
        if (response != null) {
            if (res) {
                adapter = new UsersAdapter(response.getUsers(), this, progressView);
                recycler.setAdapter(adapter);

                searchBar.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                        emptyText.setVisibility(View.GONE);
                        AlterAdapterUsers(response.getUsers());
                    }

                    // Not used for this program
                    @Override
                    public void afterTextChanged(Editable arg0) {

                    }

                    // Not uses for this program
                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                    }

                });

            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinator, "Operazione non riuscita: " + response.getResponse().getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
    }

    @UiThread
    private void AlterAdapterUsers(List<MainUserMainInfoMessage> users) {
        if (searchBar.getText().toString().isEmpty()) {
            partialUsers.clear();
            adapter.setDataset(users);
            adapter.notifyDataSetChanged();
        }
        else {
            partialUsers.clear();
            for (MainUserMainInfoMessage user: users) {
                String complete_name = user.getName() + " " + user.getSurname();
                if (complete_name.toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                    partialUsers.add(user);
            }
            adapter.setDataset(partialUsers);
            adapter.notifyDataSetChanged();
        }

        if(adapter.getItemCount()==0)
            emptyText.setVisibility(View.VISIBLE);

    }

}
