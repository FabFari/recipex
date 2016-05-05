package com.recipex.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.recipex.R;
import com.recipex.asynctasks.Register;
import com.recipex.utilities.TaskCallbackLogin;
import com.squareup.picasso.Picasso;

/**
 * Created by Sara on 26/04/2016.
 */
public class Registration extends ActionBarActivity implements TaskCallbackLogin {

    ImageView immagine;

    String nome, cognome, foto, email, bio, data, sesso, città, indirizzo, numeri, campoSpecializzazione, anniEsperienza,
    postoLavoro,numeriBusiness, disponibilità;
    EditText inserisciNome, inserisciCognome, inserisciEmail, inserisciBiografia, inserisciData, inserisciSesso, inserisciCittà,
    inserisciIndirizzo, inserisciNumeri, inserisciSpecializzazione, inserisciAnni, inserisciPosto, inserisciNumeriBusiness,
    inserisciDisponibilità;

    SharedPreferences pref;

    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        immagine = (ImageView) findViewById(R.id.immagine);


        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        inserisciEmail = (EditText) findViewById(R.id.insertEmail);
        inserisciEmail.setFocusable(false);
        inserisciEmail.setClickable(false);

        inserisciNome = (EditText) findViewById(R.id.insertNome);
        inserisciCognome = (EditText)findViewById(R.id.insertCognome);
        inserisciBiografia = (EditText) findViewById(R.id.insertBiografia);
        inserisciData = (EditText) findViewById(R.id.insertDataNascita);
        inserisciSesso = (EditText)findViewById(R.id.insertSesso);
        inserisciCittà = (EditText)findViewById(R.id.insertCittà);
        inserisciIndirizzo = (EditText)findViewById(R.id.insertIndirizzo);
        inserisciNumeri=(EditText)findViewById(R.id.insertNumeri);

        inserisciSpecializzazione = (EditText)findViewById(R.id.insertCampo);
        inserisciAnni= (EditText)findViewById(R.id.insertAnni);
        inserisciPosto= (EditText)findViewById(R.id.insertPosto);
        inserisciNumeriBusiness=(EditText)findViewById(R.id.insertNumeriBusiness);
        inserisciDisponibilità = (EditText)findViewById(R.id.insertDisponibilità);

        //prendi i campi obbligatori passati dall'activity login
        Bundle extras = getIntent().getExtras();
        nome = extras.getString("nome");
        cognome = extras.getString("cognome");
        foto = extras.getString("foto");
        email = extras.getString("email");
        data = extras.getString("data");
        sesso=extras.getString("sesso");

        inserisciNome.setText(nome);
        inserisciCognome.setText(cognome);
        inserisciEmail.setText(email);
        inserisciData.setText(data);
        inserisciSesso.setText(sesso);

        Picasso.with(Registration.this).load(foto).into(immagine);


    }


    public void onBackPressed(){
        //do whatever you want the 'Back' button to do
        //as an example the 'Back' button is set to start a new Activity named 'NewActivity'

        this.startActivity(new Intent(Registration.this,Login.class));
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("token", true).commit();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.registrati) {
            if (inserisciNome.getText().length() > 1 && inserisciCognome.getText().length() > 1 &&
                    inserisciData.getText().length()>1 && inserisciSesso.getText().length()>1) {

                nome=inserisciNome.getText().toString();
                cognome=inserisciCognome.getText().toString();
                email=inserisciEmail.getText().toString();
                data = inserisciData.getText().toString();
                sesso = inserisciSesso.getText().toString();

                bio = inserisciBiografia.getText().toString();
                città = inserisciCittà.getText().toString();
                indirizzo = inserisciIndirizzo.getText().toString();
                numeri = inserisciNumeri.getText().toString();
                campoSpecializzazione = inserisciSpecializzazione.getText().toString();
                anniEsperienza = inserisciAnni.getText().toString();
                if(anniEsperienza.equals("")) anniEsperienza="0";
                postoLavoro = inserisciPosto.getText().toString();
                numeriBusiness = inserisciNumeriBusiness.getText().toString();
                disponibilità = inserisciDisponibilità.getText().toString();

                Log.d("CAMPI ", campoSpecializzazione+"-"+anniEsperienza+"-"+postoLavoro+"-"+bio+"-"+disponibilità);

                Log.d("REGISTRAZIONE ", "Sono qui");
                if (checkNetwork()) new Register(getApplicationContext(), email, nome, cognome, foto,bio, data, sesso,
                        città, indirizzo, numeri, campoSpecializzazione, Long.parseLong(anniEsperienza), postoLavoro, numeriBusiness,
                        disponibilità, this).execute();

            }
            else {
                Toast.makeText(getApplicationContext(),"Compilare i campi obbligatori", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void done(boolean x, String email) {
        //if(x){ //Utente può accedere
        Toast.makeText(getApplicationContext(), "Login eseguito con successo!", Toast.LENGTH_LONG).show();

        Log.d("LOGIN","done reg");

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("nome", nome);
        editor.putString("cognome", cognome);
        editor.putString("foto", foto);
        editor.commit();

        System.out.println(nome+" "+cognome);
        Intent myIntent = new Intent(Registration.this, Home.class);
        this.startActivity(myIntent);
        this.finish();
        /*}else{ //Login fallito perchè email non è registrata
            disconnetti();
            Toast.makeText(getApplicationContext(), "Login fallito! Devi registrarti!", Toast.LENGTH_LONG).show();
        }*/
    }
}
