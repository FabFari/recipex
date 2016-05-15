package com.recipex.activities;

/**
 * Created by Sara on 24/04/2016.
 */
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
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
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.asynctasks.GetTerapieUser;
import com.recipex.asynctasks.Register;
import com.recipex.taskcallbacks.TaskCallbackGetTerapie;
import com.recipex.taskcallbacks.TaskCallbackLogin;


public class Login extends AppCompatActivity implements TaskCallbackLogin, OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

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


        if(e!=null && n!=null && c!=null && f!=null && !id.equals(0L))
            avviaHome();

        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();
        */
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity ,
                        this  OnConnectionFailedListener )
                        this  OnConnectionFailedListener )
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();*/

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

        // Sign out from previous sessions
        //signOut();

    }

    private void avviaHome(){
        Intent myIntent = new Intent(Login.this, Home.class);
        this.startActivity(myIntent);
        myIntent.putExtra("justRegistered", false);
        this.finish();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        token = pref.getBoolean("token",false);

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

    /*
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                System.out.println("Response not ok");
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }
    */

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
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (checkNetwork()) executeAsyncTask(apiHandler);
                    }
                }
                break;
            }
        }

    private void executeAsyncTask(RecipexServerApi apiHandler) {
        Log.e(TAG, "Lancio Async Task");
        new Register(getApplicationContext(), email, nome, cognome, personPhotoUrl, "", birth,
                sesso, "", "", "", "", (long) 0, "", "", "", this, apiHandler, false).execute();
    }

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

    private void getProfileInformation() {
        Log.e(TAG, "Result: "+ result.isSuccess());
        Log.e(TAG, "Result: "+ result.getStatus());
        Log.e(TAG, "Result: "+ result.toString());

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            //acct.getPhotoUrl();
            //acct.getId();
            Log.e(TAG, "Account Name: "+ acct.getDisplayName());
            Log.e(TAG, "Account Email: "+acct.getEmail());
            email = acct.getEmail();
            // SET DEFAULT ACCOUNT
            accountName = email;
            SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
            editor.commit();
                    /*
                    Plus.PeopleApi.load(mGoogleApiClient, "signed_in_user_account_id")
                        .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                            @Override
                            public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                                Log.e(TAG, "Nell'onResult!!");
                                PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
                                Log.e(TAG, "personBuffer: "+personBuffer);
                                if (personBuffer != null && personBuffer.getCount() > 0) {
                                    Log.e(TAG, "Prendo le info!!");
                                    */
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

                if (tokenLogin) { //E' stato cliccato il bottone per effettuare il Login
                    tokenLogin = false;
                    if (checkNetwork()) {
                        //metto come vuoti i campi già registrati, non li posso recuperare dalla classe Person.
                        //Register registra se l'email non è presente nel db, altrimenti restituisce true e fa il login
                        if (birth == null) {
                            //data default: tanto in register non viene contata se l'utente è giù registrato
                            birth = "1994-01-12";
                        }

                        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
                        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
                        Log.d(TAG, "Credential: " + credential);
                        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

                        if (credential.getSelectedAccountName() == null) {
                            Log.d(TAG, "AccountName == null: startActivityForResult.");
                            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                        } else {
                            progressView.startAnimation();
                            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                            if (checkNetwork()) executeAsyncTask(apiHandler);
                        }
                    }

                } else
                    lauchRegisterActivity();
            }
                                /*
                                }
                            }
                        });
                        */
        }
    }

    /*
    private void getProfileInformation() {
        progressView.startAnimation();
        progressView.setVisibility(View.VISIBLE);
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                personPhotoUrl = currentPerson.getImage().getUrl();
                email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                cognome = currentPerson.getName().getFamilyName();
                nome = currentPerson.getName().getGivenName();

                String birth=currentPerson.getBirthday();

                int sex=currentPerson.getGender();
                String sesso = "";
                if (sex == 1)
                    sesso = "F";
                else sesso = "M";

                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 6);

                if (tokenLogin) { //E' stato cliccato il bottone per effettuare il Login
                    tokenLogin = false;
                    //Devo verificare che la mail con cui l'utente ha effettuato l'accesso è presente nel nostro DB come artista
                    if (checkNetwork()) {
                        /*metto come vuoti i campi già registrati, non li posso recuperare dalla classe Person.
                        Register registra se l'email non è presente nel db, altrimenti restituisce true e fa il login

                        if (birth == null) {
                            //data default: tanto in register non viene contata se l'utente è giù registrato
                            birth = "1994-01-12";
                        }

                        new Register(getApplicationContext(), email, nome, cognome, personPhotoUrl, "", birth, sesso, "", "",
                                "", "", (long) 0, "", "", "", this, false).execute();
                    }

                } else {
                    Intent myIntent = new Intent(Login.this, Registration.class);
                    myIntent.putExtra("nome", nome);
                    myIntent.putExtra("cognome", cognome);
                    myIntent.putExtra("email", email);
                    myIntent.putExtra("foto", personPhotoUrl);
                    myIntent.putExtra("data", birth);
                    myIntent.putExtra("sesso", sesso);
                    this.startActivity(myIntent);
                    this.finish();
                }
            }else{

                    // SOLO PER DEBUG
                    nome = "Sara";
                    cognome = "Veterini";
                    email = "saraveterini@gmail.com";
                    String personPhotoUrl = "http://www.dis.uniroma1.it/sites/default/files/pictures/picture-1521-1424796678.jpg";
                    String birth="1994-01-12";
                    System.out.println("SONO QUI");
                    /*Intent myIntent = new Intent(Login.this, Home.class);
                    myIntent.putExtra("nome", nome);
                    myIntent.putExtra("cognome", cognome);
                    myIntent.putExtra("email", email);
                    myIntent.putExtra("foto", personPhotoUrl);
                    this.startActivity(myIntent);
                    Toast.makeText(getApplicationContext(), "Login in debug mode", Toast.LENGTH_LONG).show();
                    this.finish();
                if (tokenLogin) { //E' stato cliccato il bottone per effettuare il Login
                    tokenLogin = false;
                    //Devo verificare che la mail con cui l'utente ha effettuato l'accesso è presente nel nostro DB come artista
                    if (checkNetwork())
                        /*metto come vuoti i campi già registrati, non li posso recuperare dalla classe Person.
                        Register registra se l'email non è presente nel db, altrimenti restituisce true e fa il login
                        new Register(getApplicationContext(), email, nome, cognome, personPhotoUrl, "", birth, "", "", "",
                                new ArrayList<String>(), "", (long)0, "", new ArrayList<String>(), "", this).execute();
                } else { /* E' stato cliccato il bottone per la registrazione
                    Intent myIntent = new Intent(Login.this, Registration.class);
                    myIntent.putExtra("nome", nome);
                    myIntent.putExtra("cognome", cognome);
                    myIntent.putExtra("email", email);
                    myIntent.putExtra("foto", personPhotoUrl);
                    myIntent.putExtra("data", birth);
                    this.startActivity(myIntent);
                    this.finish();
                }

                Toast.makeText(getApplicationContext(), "Non è stato possibile effetture il login. Riprovare in un secondo momento", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

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


/*
    public void onBackPressed(){
        //System.exit(0);
        //return;
        if(progressView.getVisibility() == View.VISIBLE) {
            progressView.stopAnimation();
            progressView.setVisibility(View.GONE);
        }
        else
            super.onBackPressed();
    }
*/

    @Override
    public void done(boolean canLogIn, String email) {
        //if(x){ //Utente può accedere
        //Toast.makeText(getApplicationContext(), "Login eseguito con successo!", Toast.LENGTH_LONG).show();
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        if(canLogIn) {
            //System.out.println("DONE LOGIN");
            Log.d(TAG,"done login");
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("email", email);
            editor.putString("nome", nome);
            editor.putString("cognome", cognome);
            editor.putString("foto", personPhotoUrl);

            boolean utenteSemplice=pref.getBoolean("utenteSemplice", false);
            Log.d("UTENTESEMPLICE DONE", " "+utenteSemplice);

            editor.commit();
            Intent i=new Intent(Login.this, Home.class);
            i.putExtra("justRegistered", false);
            startActivity(i);
            this.finish();
        }
        else{ //Login fallito perchè email non è registrata
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
            //signOut();
        }
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
        if(isOnline) {
            return true;
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Ops..qualcosa è andato storto!")
                    .setMessage("Sembra che tu non sia collegato ad internet! ")
                    .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
            return false;
        }
    }

    private void latestGooglePlus() {

        Log.e(TAG, "Using: "+ AppConstants.GOOGLE_SIGN_WEB_CLIENT);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        Log.e(TAG, "Credentials.getSelectedAccountName: "+credential.getSelectedAccountName());
        this.accountName = accountName;
    }

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
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
    // [END revokeAccess]

}