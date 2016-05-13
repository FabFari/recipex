package com.recipex.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.R;
import com.recipex.asynctasks.Register;
import com.recipex.taskcallbacks.TaskCallbackLogin;
import com.recipex.utilities.PlacesAutoCompleteAdapter;
import com.recipex.utilities.StreetAutoCompleteAdapter;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

/**
 * Created by Sara on 26/04/2016.
 */
public class Registration extends ActionBarActivity implements TaskCallbackLogin, View.OnClickListener{

    ImageView immagine;

    String nome, cognome, foto, email, bio, data, sesso, città, indirizzo, numeri, campoSpecializzazione, anniEsperienza,
    postoLavoro,numeriBusiness, disponibilità;
    EditText inserisciNome, inserisciCognome, inserisciEmail, inserisciBiografia, inserisciData, inserisciNumeri, inserisciSpecializzazione, inserisciAnni, inserisciPosto, inserisciNumeriBusiness,
    inserisciDisponibilità;

    Spinner inserisciSesso;
    AutoCompleteTextView inserisciCittà, inserisciIndirizzo;
    ArrayAdapter<CharSequence> sex_adapter;
    CoordinatorLayout coordinatorLayout;
    CircularProgressView progressView;
    int mDay, mMonth, mYear;

    SharedPreferences pref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_v2);

        immagine = (ImageView) findViewById(R.id.immagine);
        progressView = (CircularProgressView) findViewById(R.id.registration_progress_view);
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);

        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.registration_coordinator);

        inserisciEmail = (EditText) findViewById(R.id.insertEmail);
        inserisciEmail.setFocusable(false);
        inserisciEmail.setClickable(false);

        inserisciNome = (EditText) findViewById(R.id.insertNome);
        inserisciCognome = (EditText)findViewById(R.id.insertCognome);
        inserisciBiografia = (EditText) findViewById(R.id.insertBiografia);
        inserisciData = (EditText) findViewById(R.id.insertDataNascita);
        inserisciSesso = (Spinner) findViewById(R.id.insertSesso);
        inserisciCittà = (AutoCompleteTextView) findViewById(R.id.insertCittà);
        inserisciIndirizzo = (AutoCompleteTextView) findViewById(R.id.insertIndirizzo);
        inserisciNumeri=(EditText)findViewById(R.id.insertNumeri);

        inserisciSpecializzazione = (EditText)findViewById(R.id.insertCampo);
        inserisciAnni= (EditText)findViewById(R.id.insertAnni);
        inserisciPosto= (EditText)findViewById(R.id.insertPosto);
        inserisciNumeriBusiness=(EditText)findViewById(R.id.insertNumeriBusiness);
        inserisciDisponibilità = (EditText)findViewById(R.id.insertDisponibilità);

        sex_adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_type, /*android.R.layout.simple_spinner_item*/ R.layout.list_item);
        sex_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inserisciSesso.setAdapter(sex_adapter);

        inserisciCittà.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        inserisciCittà.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
            }
        });

        inserisciIndirizzo.setAdapter(new StreetAutoCompleteAdapter(this, R.layout.list_item));
        inserisciIndirizzo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
            }
        });

        inserisciData.setOnClickListener(this);

        //prendi i campi obbligatori passati dall'activity login
        Bundle extras = getIntent().getExtras();
        nome = extras.getString("nome");
        cognome = extras.getString("cognome");
        foto = extras.getString("foto");
        email = extras.getString("email");
        data = extras.getString("data");
        // sesso=extras.getString("sesso");

        inserisciNome.setText(nome);
        inserisciCognome.setText(cognome);
        inserisciEmail.setText(email);
        inserisciData.setText(data);
        // inserisciSesso.setText(sesso);

        Picasso.with(Registration.this).load(foto).into(immagine);


    }

    public void onBackPressed(){
        //do whatever you want the 'Back' button to do
        //as an example the 'Back' button is set to start a new Activity named 'NewActivity'
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("token", true).commit();
        this.finish();
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
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
            if(!inserisciIndirizzo.getText().toString().equals("")) {
                if(inserisciCittà.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Indirizzo inserito: inserire anche la città.", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return super.onOptionsItemSelected(item);
                }
            }

            if(!inserisciAnni.getText().toString().equals("") || !inserisciPosto.getText().toString().equals("") ||
                    !inserisciNumeriBusiness.getText().toString().equals("") || !inserisciBiografia.getText().toString().equals("") ||
                    !inserisciDisponibilità.getText().toString().equals("")) {
                if(inserisciSpecializzazione.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Attenzione! Campo obbligatorio \"Specializzazione\" vuoto!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return super.onOptionsItemSelected(item);
                }
            }

            if (inserisciNome.getText().length() > 1 && inserisciCognome.getText().length() > 1 &&
                    inserisciData.getText().length()>1) {

                nome=inserisciNome.getText().toString();
                cognome=inserisciCognome.getText().toString();
                email=inserisciEmail.getText().toString();
                data = inserisciData.getText().toString();
                sesso = inserisciSesso.getSelectedItem().toString();

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
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Attenzione! Uno o più campi obbligatori vuoti!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                return super.onOptionsItemSelected(item);
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
        // Toast.makeText(getApplicationContext(), "Login eseguito con successo!", Toast.LENGTH_LONG).show();

        //Log.d("LOGIN","done reg");

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("nome", nome);
        editor.putString("cognome", cognome);
        editor.putString("foto", foto);

        if(campoSpecializzazione != null)
            editor.putBoolean("caregiver", true);

        editor.commit();

        System.out.println(nome+" "+cognome);
        Intent myIntent = new Intent(Registration.this, Home.class);
        myIntent.putExtra("justRegistered", true);
        this.startActivity(myIntent);
        this.finish();
        /*}else{ //Login fallito perchè email non è registrata
            disconnetti();
            Toast.makeText(getApplicationContext(), "Login fallito! Devi registrarti!", Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        switch (v.getId()) {
            case R.id.insertDataNascita:
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                String dayOfMonthStr = null;
                                if(dayOfMonth < 10)
                                    dayOfMonthStr = "0" + dayOfMonth;
                                else
                                    dayOfMonthStr = "" + dayOfMonth;

                                String monthOfYearStr = null;
                                if(monthOfYear < 10)
                                    monthOfYearStr = "0" + (monthOfYear + 1);
                                else
                                    monthOfYearStr = "" + (monthOfYear + 1);

                                inserisciData.setText(year + "-" + monthOfYearStr + "-" + dayOfMonthStr);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
                break;
        }
    }
}
