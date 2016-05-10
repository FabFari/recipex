package com.recipex.activities;

/**
 * Created by Sara on 24/04/2016.
 */
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.recipex.R;
import com.recipex.asynctasks.GetTerapieUser;
import com.recipex.asynctasks.Register;
import com.recipex.taskcallbacks.TaskCallbackGetTerapie;
import com.recipex.taskcallbacks.TaskCallbackLogin;


public class Login extends AppCompatActivity implements TaskCallbackLogin, OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "LoginActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnContinua;
    private TextView accedi;

    String nome;
    String cognome;
    String email;
    String personPhotoUrl;

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
        editor.putLong("id", 5719238044024832L);
        editor.commit();

        String e = pref.getString("email",null);
        String n=pref.getString("nome", null);
        String c=pref.getString("cognome", null);
        String f=pref.getString("foto", null);


        if(e!=null && n!=null && c!=null && f!=null)
            avviaHome();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
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
                         */
                        if (birth == null) {
                            //data default: tanto in register non viene contata se l'utente è giù registrato
                            birth = "1994-01-12";
                        }

                        new Register(getApplicationContext(), email, nome, cognome, personPhotoUrl, "", birth, sesso, "", "",
                                "", "", (long) 0, "", "", "", this).execute();
                    }

                } else { /* E' stato cliccato il bottone per la registrazione */
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
                /*
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
                */
                Toast.makeText(getApplicationContext(), "Non è stato possibile effetture il login. Riprovare in un secondo momento", Toast.LENGTH_LONG).show();
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

    @Override
    public void done(boolean x, String email) {
        //if(x){ //Utente può accedere
        Toast.makeText(getApplicationContext(), "Login eseguito con successo!", Toast.LENGTH_LONG).show();
        System.out.println("DONE LOGIN");
        Log.d("LOGIN","done login");
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("nome", nome);
        editor.putString("cognome", cognome);
        editor.putString("foto", personPhotoUrl);
        editor.putLong("id", 5719238044024832L);

        boolean utenteSemplice=pref.getBoolean("utenteSemplice", false);
        Log.d("UTENTESEMPLICE DONE", " "+utenteSemplice);

        editor.commit();
        Intent i=new Intent(Login.this, Home.class);
        startActivity(i);

        /*}else{ //Login fallito perchè email non è registrata
            disconnetti();
            Toast.makeText(getApplicationContext(), "Login fallito! Devi registrarti!", Toast.LENGTH_LONG).show();
        }*/
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
}


