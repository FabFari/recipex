package com.recipex.activities;

/**
 * Created by Sara on 24/04/2016.
 */
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.asynctasks.RegisterAT;
import com.recipex.taskcallbacks.LoginTC;

/**
 * Login activity
 */
public class Login extends AppCompatActivity implements LoginTC, OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final String TAG = "LoginActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnContinua;
    private TextView accedi;
    private CircularProgressView progressView;
    private RelativeLayout mainRelative;

    GoogleSignInResult result;

    String nome;
    String cognome;
    String email;
    String personPhotoUrl;
    String sesso;
    String birth;

    SharedPreferences pref;
    boolean token=false;
    private boolean tokenLogin = false;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressView = (CircularProgressView) findViewById(R.id.login_progress_view);
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        accedi = (TextView) findViewById(R.id.login);
        mainRelative = (RelativeLayout) findViewById(R.id.login_mainRelative);

        btnSignIn.setOnClickListener(this);
        accedi.setOnClickListener(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        Intent i = getIntent();
        boolean hasLogOut = i.getBooleanExtra("hasLogOut", false);


        // Change Fabrizio
        Long id = pref.getLong("userId", 0L);

        String e = pref.getString("email",null);
        String n=pref.getString("nome", null);
        String c=pref.getString("cognome", null);
        String f=pref.getString("foto", null);


        //if I already have data of the user, I do not need Google+.
        if(e!=null && n!=null && c!=null && f!=null && !id.equals(0L))
            avviaHome();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(AppConstants.WEB_CLIENT_ID)
                .requestServerAuthCode(AppConstants.WEB_CLIENT_ID)
                .requestProfile().requestEmail().requestScopes(
                        Plus.SCOPE_PLUS_LOGIN,
                        Plus.SCOPE_PLUS_PROFILE,
                        new Scope("https://www.googleapis.com/auth/plus.profile.emails.read"))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();

        if(hasLogOut) {
            Snackbar snackbar = Snackbar
                    .make(mainRelative, "Logout eseguito con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    /**
     * go to home activity
     */
    private void avviaHome(){
        Intent myIntent = new Intent(Login.this, Home.class);
        this.startActivity(myIntent);
        myIntent.putExtra("justRegistered", false);
        this.finish();
    }

    /**
     * start google api client
     */
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * when client is connected
     * @param arg0
     */
    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        token = pref.getBoolean("token",false);

        //if token is false, I want to disconnect
        if(token){
            disconnetti();
        }else{
            System.out.println("OnConnected");
            getProfileInformation();
        }

    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * disconnect google client
     */
    public void disconnetti(){

        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Log.e(TAG, "Provo a disconnettermi!!");
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            //mGoogleApiClient.disconnect();
            signOut();
            pref.edit().putBoolean("token",false).commit();
            mGoogleApiClient.connect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked)
                resolveSignInError();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Activity Res", "" + requestCode);

        switch(requestCode) {
            case RC_SIGN_IN:
                Log.e(TAG, "Nell'onActivityResult!!!");
                result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                getProfileInformation();
                break;
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
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (AppConstants.checkNetwork(this)) executeAsyncTask(apiHandler);
                    }
                }
                break;
            }
        }

    /**
     * calls async task RegisterAT that checks if user is already registered
     * @param apiHandler for Google App Engine api
     */
    private void executeAsyncTask(RecipexServerApi apiHandler) {
        Log.e(TAG, "Lancio Async Task");
        new RegisterAT(getApplicationContext(), email, nome, cognome, personPhotoUrl, "", birth,
                sesso, "", "", "", "", (long) 0, "", "", "", this, apiHandler, false).execute();
    }

    /**
     * launch registration activity
     */
    private void lauchRegisterActivity() {
        Intent myIntent = new Intent(Login.this, Registration.class);
        myIntent.putExtra("nome", nome);
        myIntent.putExtra("cognome", cognome);
        myIntent.putExtra("email", email);
        myIntent.putExtra("foto", personPhotoUrl);
        myIntent.putExtra("data", birth);
        myIntent.putExtra("sesso", sesso);
        signOut();
        this.startActivity(myIntent);
        this.finish();
    }

    /**
     * retrieve data of the user from Google+
     */
    private void getProfileInformation() {
        Log.e(TAG, "Result: "+ result.isSuccess());
        Log.e(TAG, "Result: "+ result.getStatus());
        Log.e(TAG, "Result: "+ result.toString());

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e(TAG, "Account Name: "+ acct.getDisplayName());
            Log.e(TAG, "Account Email: "+acct.getEmail());
            email = acct.getEmail();
            // SET DEFAULT ACCOUNT
            accountName = email;
            SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
            editor.commit();

            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                //Person currentPerson = personBuffer.get(0);
                personPhotoUrl = currentPerson.getImage().getUrl();
                cognome = currentPerson.getName().getFamilyName();
                nome = currentPerson.getName().getGivenName();
                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 6);

                int sex = currentPerson.getGender();
                sesso = "";
                if (sex == 1)
                    sesso = "Donna";
                else sesso = "Uomo";

                birth = currentPerson.getBirthday();

                Log.e(TAG, "personPhotoUrl: "+personPhotoUrl);
                Log.e(TAG, "name: "+nome);
                Log.e(TAG, "surname: "+cognome);
                Log.e(TAG, "sesso: "+sesso);
                Log.e(TAG, "birth: "+birth);

                if (tokenLogin) { //login button was clicked
                    tokenLogin = false;
                    if (AppConstants.checkNetwork(this)) {
                        //check if user is present in the database: if not, it registers him

                        if (birth == null) {
                            //debug date, not used.
                            birth = "1994-01-12";
                        }

                        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
                        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
                        Log.d(TAG, "Credential: " + credential);
                        //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
                        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

                        if (credential.getSelectedAccountName() == null) {
                            Log.d(TAG, "AccountName == null: startActivityForResult.");
                            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                        } else {
                            progressView.startAnimation();
                            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                            if (AppConstants.checkNetwork(this)) executeAsyncTask(apiHandler);
                        }
                    }

                } else
                    lauchRegisterActivity();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        progressView.startAnimation();
        progressView.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.sign_in_button:
                tokenLogin = false;
                //signInWithGplus();
                latestGooglePlus();
                break;

            case R.id.login:
                tokenLogin = true;
                //signInWithGplus();
                latestGooglePlus();
                break;
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * callback from RegisterAT task.
     * @param canLogIn
     * @param email
     * @param idCalendar
     */
    @Override
    public void done(boolean canLogIn, String email, String idCalendar) {
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        if(canLogIn) {
            Log.d(TAG,"done login");
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("email", email);
            editor.putString("nome", nome);
            editor.putString("cognome", cognome);
            editor.putString("foto", personPhotoUrl);

            if(idCalendar!=null) {
                editor.putString("calendar", idCalendar);
                Log.d(TAG, "idCalendar "+idCalendar);
            }

            boolean utenteSemplice=pref.getBoolean("utenteSemplice", false);
            Log.d("UTENTESEMPLICE DONE", " "+utenteSemplice);

            editor.commit();
            signOut();
            Intent i=new Intent(Login.this, Home.class);
            i.putExtra("justRegistered", false);
            startActivity(i);
            this.finish();
        }
        else{ //Login failed because email was not registered
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putLong("userId", 0L);
            editor.putString("email", null);
            editor.putString("nome", null);
            editor.putString("cognome", null);
            editor.putString("foto", null);
            Log.d(TAG, "Lancio disconnetti!");
            disconnetti();
            Snackbar snackbar = Snackbar
                    .make(mainRelative, "Login fallito! Utente non registrato!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    /**
     * latest sign in method
     */
    private void latestGooglePlus() {

        Log.e(TAG, "Using: "+ AppConstants.GOOGLE_SIGN_WEB_CLIENT);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        Log.e(TAG, "Credentials.getSelectedAccountName: "+credential.getSelectedAccountName());
        this.accountName = accountName;
    }*/

    /**
     * sign out
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        Log.e(TAG, "Signout eseguito!");
                        // [END_EXCLUDE]
                    }
                });
    }


    private void revokeAccess() {
        Log.e(TAG, "Provo a revocare l'accesso");
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.e(TAG, "Acceso revocato!!!");
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }

}