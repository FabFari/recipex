package com.recipex;

/**
 * Created by Sara on 24/04/2016.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.model.people.Person;


public class Login extends AppCompatActivity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "LoginActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnContinua;
    private TextView accedi;

    SharedPreferences pref;
    boolean token=false;
    private boolean tokenLogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        accedi = (TextView) findViewById(R.id.login);

        btnSignIn.setOnClickListener(this);
        accedi.setOnClickListener(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();


        /*String x = pref.getString("email",null);
        if(x!=null) {
            System.out.println("Email "+x);
            avviaHome();
        }*/


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity ,
                        this  OnConnectionFailedListener )
                        this  OnConnectionFailedListener )
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();*/
    }

    private void avviaHome(){
        Intent myIntent = new Intent(Login.this, Home.class);
        this.startActivity(myIntent);
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
            System.out.println("Disconnetti");
            mGoogleApiClient.disconnect();
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


    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                String cognome = currentPerson.getName().getFamilyName();
                String nome = currentPerson.getName().getGivenName();

                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 6);

                /*if(tokenLogin){ E' stato cliccato il bottone per effettuare il Login
                    tokenLogin=false;
                    Devo verificare che la mail con cui l'utente ha effettuato l'accesso è presente nel nostro DB come artista
                    if(checkNetwork()) new CheckLogin(getApplicationContext(),email,this).execute();

                    Il risultato della chiamata CheckLogin lo trovo in done(boolean,string) */
                //}else { /* E' stato cliccalto il bottone per la registrazione */
                    Intent myIntent = new Intent(Login.this, Home.class);
                    myIntent.putExtra("nome", nome);
                    myIntent.putExtra("cognome", cognome);
                    myIntent.putExtra("email", email);
                    myIntent.putExtra("foto", personPhotoUrl);
                    this.startActivity(myIntent);
                    this.finish();
                //}
            } else {
                // SOLO PER DEBUG
                String nome = "nome";
                String cognome = "cognome";
                String email = "ciao@ciao.it";
                String personPhotoUrl = "http://www.francescocucari.it/artist.jpg";

                System.out.println("SONO QUI");

                Intent myIntent = new Intent(Login.this, Home.class);
                myIntent.putExtra("nome",nome);
                myIntent.putExtra("cognome",cognome);
                myIntent.putExtra("email",email);
                myIntent.putExtra("foto",personPhotoUrl);

                this.startActivity(myIntent);
                Toast.makeText(getApplicationContext(), "Errore nel Login", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        switch (v.getId()) {
            case R.id.sign_in_button:
                signInWithGplus();
                break;

            case R.id.login:
                tokenLogin = true;
                signInWithGplus();
                break;
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }



    public void onBackPressed(){
        System.exit(0);
        return;
    }

}
