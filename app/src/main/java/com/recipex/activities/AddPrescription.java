package com.recipex.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientsMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddPrescriptionMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUpdateUserMessage;
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
import com.recipex.adapters.DateItemAdapter;
import com.recipex.asynctasks.AddPrescriptionAT;
import com.recipex.asynctasks.DeleteEventsCalendarAT;
import com.recipex.asynctasks.GetMainIngredientsAT;
import com.recipex.asynctasks.RegistraCalendarioAT;
import com.recipex.taskcallbacks.ActiveIngredientsTC;
import com.recipex.taskcallbacks.AddPrescriptionTC;
import com.recipex.taskcallbacks.CalendarAddTC;
import com.recipex.taskcallbacks.CalendarDeleteTC;
import com.recipex.taskcallbacks.RegisterCalendarTC;
import com.recipex.utilities.ConnectionDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/* This activity adds a prescription for a user, by showing all fields that must be filled for the prescription. Then it automatically
 * adds the prescription to the user's calendar, if some optional fields are filled
 */
public class AddPrescription extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, AddPrescriptionTC,
        ActiveIngredientsTC, CalendarAddTC, CalendarDeleteTC, RegisterCalendarTC,
        AdapterView.OnItemSelectedListener, View.OnClickListener{

    private final static String TAG = "AGGIUNGI_TERAPIA";

    GoogleAccountCredential mCredential;
    public static ProgressDialog mProgress;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;

    static final int REQUEST_ACCOUNT_PICKER2 = 2;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    //IMPORTANTISSIMO!
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    public ArrayList<String> idEventi;

    private CoordinatorLayout coordinatorLayout;
    private Long caregiverId, patientId;
    private String idPatientCalendar;

    private Toolbar toolbar;

    private int mDay, mMonth, mYear;

    boolean fatto=false;

    List<String> nomiIngredienti;
    List<Long> idsIngredienti;

    Spinner spinner;

    // Added Fabrizio
    ImageView add_date_item;
    ListView date_item_listview;
    DateItemAdapter date_item_adapter;
    List<Integer> integers;
    int count = 1;
    private ConnectionDetector cd;
    private ArrayList<String> orari_assunzioni = new ArrayList<>();
    private AutoCompleteTextView princ_attivo;

    String nome, ingrediente, tipo, dose, unità, quantità, ricetta, foglio, caregiver, numerocadenza, cadenza, inizio;
    long dose2;
    int quanto;

    boolean recipe;
    long ingredienteID;

    EditText inserisciNome, inserisciDose, inserisciUnità, inserisciQuantità,
            inserisciFoglio, inserisciInizio;

    static final int MAXNUMEROCADENZA=30;
    static final int ORE =24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_terapia);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aggiungi Terapia");

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        caregiverId = intent.getLongExtra("caregiverId", 0L);
        patientId = intent.getLongExtra("patientId", 0L);
        if(intent.getStringExtra("idProfileCalendar")!=null){
            idPatientCalendar=intent.getStringExtra("idProfileCalendar");
            Log.d(TAG, idPatientCalendar);
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.lay_aggiungiterapia);
        Spinner numerocadenza=(Spinner)findViewById(R.id.numerocadenzaspin);

        String[] np = new String[MAXNUMEROCADENZA];
        for(int i=1;i<=MAXNUMEROCADENZA;i++){
            np[i-1]=Integer.toString(i);
        }

        ArrayAdapter <String> _aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,np);//array holding min and max pages
        numerocadenza.setAdapter(_aa);
        numerocadenza.setOnItemSelectedListener(this);

        Spinner cadenzaspin=(Spinner)findViewById(R.id.cadenzaspin);
        ArrayAdapter<CharSequence> adapterc = ArrayAdapter.createFromResource(this,
                R.array.cadenze, android.R.layout.simple_spinner_item);
        cadenzaspin.setAdapter(adapterc);
        cadenzaspin.setOnItemSelectedListener(this);

        add_date_item = (ImageView)findViewById(R.id.date_item_add);
        add_date_item.setOnClickListener(this);
        date_item_listview = (ListView)findViewById(R.id.date_item_listview);
        integers = new ArrayList<Integer>();
        integers.add(new Integer(count));
        orari_assunzioni.add("");
        date_item_adapter = new DateItemAdapter(this, integers, orari_assunzioni);
        date_item_listview.setAdapter(date_item_adapter);


        Spinner tipi=(Spinner) findViewById(R.id.tipi);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipoterapia, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        tipi.setAdapter(adapter);
        tipi.setOnItemSelectedListener(this);

        Spinner ricettaSINO=(Spinner) findViewById(R.id.ricettaSINO);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.sino, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        ricettaSINO.setAdapter(adapter2);
        ricettaSINO.setOnItemSelectedListener(this);

        inserisciNome= (EditText)findViewById(R.id.insertNomeTerapia);
        inserisciDose= (EditText)findViewById(R.id.insertDose);
        inserisciUnità=(EditText)findViewById(R.id.insertUnità);
        inserisciQuantità=(EditText)findViewById(R.id.insertQuantità);
        inserisciFoglio=(EditText)findViewById(R.id.insertFoglio);
        inserisciInizio=(EditText)findViewById(R.id.insertInizio);
        inserisciInizio.setOnClickListener(this);

        //call async task to retrieve ingredients
        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

        if(credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER2);
        }
        else {
            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            if (AppConstants.checkNetwork(this))
                new GetMainIngredientsAT(apiHandler, this, this, coordinatorLayout).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbar.inflateMenu(R.menu.menu_registration);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //when you confirm the data inserted
        if (id == R.id.registrati) {
            //check mandatory fileds are filled
            if (inserisciNome.getText().length() > 1 && fatto &&
                    inserisciDose.getText().length()>0 &&
                    inserisciUnità.getText().length()>1 && inserisciQuantità.getText().length()>0 ) {

                nome=inserisciNome.getText().toString();

                dose = inserisciDose.getText().toString();

                dose2= Long.parseLong(dose);

                unità = inserisciUnità.getText().toString();
                quantità = inserisciQuantità.getText().toString();

                quanto=Integer.parseInt(quantità);

                foglio = inserisciFoglio.getText().toString();
                //caregiver = inserisciCaregiver.getText().toString();

                inizio=inserisciInizio.getText().toString();

                Log.d("REGISTRAZIONE ", "Sono qui");

                if(AppConstants.checkNetwork(this)){
                    /*if the start date field is filled, it means you want to add the prescription to the calendar,
                     even if you did not set notifications' timing */
                    if(!inizio.equals("")) {
                        mProgress = new ProgressDialog(this);
                        mProgress.setMessage("Sto inserendo nel calendario...");

                        // Initialize credentials and service object.
                        mCredential = GoogleAccountCredential.usingOAuth2(
                                getApplicationContext(), Arrays.asList(SCOPES))
                                .setBackOff(new ExponentialBackOff());
                        getResultsFromApi();
                    }
                    else {
                        Log.d(TAG, "non voglio il calendario");
                        done(true, new ArrayList<String>());
                    }
                }

            }
            else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Attenzione! Uno o più campi obbligatori vuoti!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * calls asynctask that adds the events on the calendar
     */
    private void getResultsFromApi() {
        if (! AppConstants.isGooglePlayServicesAvailable(this)) {
            AppConstants.acquireGooglePlayServices(this);
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount();
        } else if (! AppConstants.isDeviceOnline(this)) {
            Toast.makeText(AddPrescription.this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            SharedPreferences pref=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            if(idPatientCalendar!=null && !idPatientCalendar.equals(""))
                new AggiungiTerapiaCalendar(mCredential, getApplicationContext(), idPatientCalendar, this, nome, numerocadenza, cadenza,
                        inizio).execute();
            else new AggiungiTerapiaCalendar(mCredential, getApplicationContext(), pref.getString("calendar", ""), this, nome, numerocadenza, cadenza,
                    inizio).execute();
        }
    }

    /**
     *  callback from AggiungiTerapiaCalendar
      * @param b boolean to check it is all ok
     * @param idEventiC ids of the events created on the calendar
     */
    public void done(boolean b, ArrayList<String> idEventiC){
        SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);

        //if a new calendar was created, I have to save its id in our server.
        if(pref.getBoolean("nuovocalendario", false) && !pref.getString("calendar", "").equals("")){
            settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

            if(credential.getSelectedAccountName() == null) {
                Log.d(TAG, "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER2);
            }
            else {
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                MainUpdateUserMessage m=new MainUpdateUserMessage();
                m.setCalendarId(pref.getString("calendar", ""));
                if(AppConstants.checkNetwork(this)) new RegistraCalendarioAT(this, this, coordinatorLayout, pref.getLong("userId", 0L), m, apiHandler).execute();
            }
        }

        //if the prescription was correctly saved in the calendar, I add it in the server.
        if(b){

            settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

            if(credential.getSelectedAccountName() == null) {
                Log.d(TAG, "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER2);
            }
            else {
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);

                idEventi=idEventiC;
                MainAddPrescriptionMessage reg = new MainAddPrescriptionMessage();

                //Mandatory fields
                reg.setName(nome);
                reg.setActiveIngredient(ingredienteID);
                reg.setKind(tipo);
                reg.setDose(dose2);
                reg.setUnits(unità);
                reg.setQuantity((long) quanto);
                Log.d("AggiungiATricetta", ""+ricetta );
                reg.setRecipe(recipe);

                //Not mandatory
                reg.setPil(foglio);
                if(!idEventi.isEmpty())
                    reg.setCalendarIds(idEventi);

                if(caregiverId != 0){
                    Log.d(TAG, ""+caregiverId);
                    reg.setCaregiver(caregiverId);
                }


                Long id;
                if(patientId != 0)
                    id = patientId;
                else
                    id = pref.getLong("userId", 0L);
                if (AppConstants.checkNetwork(this)) {new AddPrescriptionAT(id, apiHandler, reg, this).execute();}
            }
        }

        else{
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Operazione non riuscita", Snackbar.LENGTH_SHORT);
            snackbar.show();
            Intent i =new Intent(AddPrescription.this, Home.class);
            startActivity(i);
            this.finish();
        }
    }

    /**
     * callback from RegistraClaendarioAT
     * @param b boolean to check it is all ok
     * @param i useless parameter to distinguish this from other done methods
     */
    public void done(boolean b, int i){
        if(b){
            SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor=pref.edit();
            //ho registrato il calendario.
            editor.putBoolean("nuovocalendario", false);
            editor.commit();
        }
    }

    /**
     * callback from AddPrescriptionAT
     * @param b boolean to check it is all ok
     * @param response from server
     */
    public void done(boolean b, MainDefaultResponseMessage response){
        if(response != null) {
            if(b) {

                Log.d(TAG, "aggiunta");

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Terapia inserita con successo!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                idEventi.clear();

                Intent i = new Intent(AddPrescription.this, Home.class);
                this.setResult(RESULT_OK);
                this.finish();
                startActivity(i);
                finish();

            }

        }
        //if the operation was not performed, I have to delete the event on the calendar, if it was added, for consistency.
        else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Operazione non riuscita", Snackbar.LENGTH_SHORT);
            snackbar.show();

            if(!idEventi.isEmpty()) {
                if (AppConstants.checkNetwork(this)) {
                    new DeleteEventsCalendarAT(mCredential, this.getApplicationContext(), this, idEventi).execute();
                }
            }
            else{
                Intent i =new Intent(AddPrescription.this, Home.class);
                startActivity(i);
                this.finish();
            }
        }
    }

    /**
     * callback from DeleteEventsCalendarAT
     * @param b boolean to check it is all ok
     * @param inutile to distinguish this from other done methods
     */
    public void done(boolean b, String inutile){
        idEventi.clear();
        if(!b){
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Errore: aggiunto evento sul calendario" +
                            "che non corrisponde a una misurazione.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        Intent i =new Intent(AddPrescription.this, Home.class);
        startActivity(i);
        this.finish();
    }


    /**
     * callback from GetIngredients
     * @param m response from server
     */
    public void done(MainActiveIngredientsMessage m){

        nomiIngredienti=new LinkedList<>();
        idsIngredienti=new LinkedList<>();

        List<MainActiveIngredientMessage> ingredientimex=m.getActiveIngredients();
        Iterator<MainActiveIngredientMessage> i=ingredientimex.iterator();
        while(i.hasNext()){
            MainActiveIngredientMessage cur=(MainActiveIngredientMessage)i.next();
            nomiIngredienti.add(cur.getName());
            idsIngredienti.add(cur.getId());
        }

        princ_attivo = (AutoCompleteTextView) findViewById(R.id.ingredienti);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, nomiIngredienti);
        // Apply the adapter to the spinner
        princ_attivo.setAdapter(adapter);
        princ_attivo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                ingrediente = (String)parent.getItemAtPosition(position);
                Log.d(TAG, "posizione nome "+position);
                Log.d("INGREDIENTE ", ingrediente);

                //prendo l'id effettivo: la posizione è diversa
                int pos2=nomiIngredienti.indexOf(ingrediente);
                Log.d(TAG, "posizione id "+pos2);
                ingredienteID = idsIngredienti.get(pos2);
                Log.d("INGREDIENTEID ", " "+ingredienteID);
            }
        });

        //mi serve per aspettare prima che l'utente mandi i dati
        fatto=true;
        Log.d("FATTO", "fatto");
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if(parent.getId()==R.id.ingredienti) {
            ingrediente = (String) parent.getItemAtPosition(pos);
            Log.d("INGREDIENTE ", ingrediente);
            ingredienteID = idsIngredienti.get(pos);
            Log.d("INGREDIENTEID ", " "+ingredienteID);
        }
        else if(parent.getId()==R.id.tipi){
            //tipo=(String) parent.getItemAtPosition(pos);
            switch (pos) {
                case 0:
                    tipo = AppConstants.PILLOLA;
                    break;
                case 1:
                    tipo = AppConstants.BUSTINE;
                    break;
                case 2:
                    tipo = AppConstants.FIALA;
                    break;
                case 3:
                    tipo = AppConstants.CREMA;
                    break;
                case 4:
                    tipo = AppConstants.ALTRO;
            }
        }
        else if(parent.getId()==R.id.ricettaSINO){
            ricetta=(String)parent.getItemAtPosition(pos);
            recipe=(ricetta.equals("SI"))? true: false;
            Log.d("RICETTA ", " "+recipe);
        }
        else if(parent.getId()==R.id.numerocadenzaspin){
            numerocadenza=(String)parent.getItemAtPosition(pos);
        }
        else if(parent.getId()==R.id.cadenzaspin){
            cadenza=(String)parent.getItemAtPosition(pos);
            Log.d("CADENZA", cadenza);
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
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
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {

            /*
            String accountName = getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            */
            String accountName = getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE)
                    .getString(AppConstants.DEFAULT_ACCOUNT, null);
            if (accountName != null) {
                Log.d("CALENDARcho", "account");

                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                Log.d("CALENDARcho", "choose");

                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            Log.d("CALENDARcho", "noperm");

            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(AddPrescription.this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER2:
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
                        SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);
                        // User is authorized
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        MainUpdateUserMessage m=new MainUpdateUserMessage();
                        m.setCalendarId(pref.getString("calendar", ""));
                        if(AppConstants.checkNetwork(this)) new RegistraCalendarioAT(this, this, coordinatorLayout, pref.getLong("userId", 0L), m, apiHandler).execute();
                    }
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    Log.d("CALENDARres", "entro in res");

                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getSharedPreferences(AppConstants.PREFS_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        //editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Log.d("CALENDARres", accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case AppConstants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * Async task to add an event to the user's calendar
     */
    private class AggiungiTerapiaCalendar extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private CalendarAddTC mCallback;
        private String nome;
        private String numerocadenza;
        private String cadenza;
        private String inizio;
        private String idCalendar;

        //insert here the ids of the events I create
        private ArrayList<String> idEventiCalendar;

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public AggiungiTerapiaCalendar(GoogleAccountCredential credential, Context context, String idCalendar, CalendarAddTC c,
                                       String nome, String numerocadenza, String cadenza, String inizio) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("RecipeX")
                    .build();
            this.idCalendar=idCalendar;
            this.context=context;
            this.mCallback=c;
            this.nome=nome;
            this.numerocadenza=numerocadenza;
            this.cadenza=cadenza;
            this.inizio=inizio;
            idEventiCalendar=new ArrayList<>();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

            try {
                //String idCalendar=pref.getString("calendar", "");
                //create a calendar if there is not
                if(idCalendar.equals("")) {
                    Log.d("CALENDAR", "creo nuovo calendario");
                    // Create a new calendar
                    com.google.api.services.calendar.model.Calendar calendar = new Calendar().setSummary("Terapie");
                    calendar.setTimeZone("Europe/Rome");

                    // Insert the new calendar
                    Calendar createdCalendar = mService.calendars().insert(calendar).execute();
                    System.out.println("New Calendar "+createdCalendar.getId());
                    idCalendar=createdCalendar.getId();
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putString("calendar", createdCalendar.getId());
                    //nel done mi serve per capire se registrare il calendario o no.
                    editor.putBoolean("nuovocalendario", true);
                    editor.commit();

                    //if I create a new calendar I have to publish it to all my caregivers.
                    if(pref.getStringSet("emailcaregivers",null)!=null) {
                        Set<String> emailcaregivers = pref.getStringSet("emailcaregivers", null);

                        Iterator<String> i = emailcaregivers.iterator();
                        while (i.hasNext()) {
                            String emailcur = (String) i.next();

                            Log.d("CALENDAR", emailcur);
                            // Create access rule with associated scope
                            AclRule rule = new AclRule();
                            AclRule.Scope scope = new AclRule.Scope();
                            scope.setType("user").setValue(emailcur);
                            rule.setScope(scope).setRole("writer");

                            // Insert new access rule
                            AclRule createdRule = mService.acl().insert(idCalendar, rule).execute();
                            System.out.println(createdRule.getId());
                        }

                    }
                }
                //add prescription
                System.out.println("arrivo");
                Iterator<String> iteratore=orari_assunzioni.iterator();

                while(iteratore.hasNext()) {
                    String o=(String)iteratore.next();
                    String oreminuti=o.substring(0, 5);
                    String ore=o.substring(0, 2);
                    if(!ore.equals("")){
                        Event event = new Event()
                                .setSummary(nome);

                        Log.d("STARTTIME ", inizio + "T" + oreminuti + ":00+02:00");
                        DateTime startDateTime = new DateTime(inizio + "T" + oreminuti + ":00+02:00");

                        EventDateTime startEvento = new EventDateTime()
                                .setDateTime(startDateTime)
                                .setTimeZone("Europe/Rome");
                        event.setStart(startEvento);

                        int orepiùunoint = Integer.parseInt(ore) + 1;
                        String orepiùuno = Integer.toString(orepiùunoint);
                        if (Integer.parseInt(orepiùuno) <= 9)
                            orepiùuno = "0" + orepiùuno;

                        DateTime endDateTime = new DateTime(inizio + "T" + orepiùuno + o.substring(2, 5)+":00+02:00");
                        Log.d("DATAEND", inizio + "T" + orepiùuno + ":00+02:00");

                        EventDateTime endEvento = new EventDateTime()
                                .setDateTime(endDateTime)
                                .setTimeZone("Europe/Rome");
                        event.setStart(startEvento);
                        event.setEnd(endEvento);

                        String ruledef = "";
                        if (cadenza.equals("giorno"))
                            ruledef = "DAILY";
                        else if (cadenza.equals("settimana"))
                            ruledef = "WEEKLY";
                        else if (cadenza.equals("mese"))
                            ruledef = "MONTHLY";
                        else if (cadenza.equals("anno"))
                            ruledef = "YEARLY";

                        String[] recurrence = new String[]{"RRULE:FREQ=" + ruledef + ";INTERVAL=" + numerocadenza};

                        event.setRecurrence(Arrays.asList(recurrence));
                        Log.d("RECURRENCE ", recurrence[0]);

                        EventReminder[] reminderOverrides = new EventReminder[]{
                                new EventReminder().setMethod("email").setMinutes(24 * 60),
                                new EventReminder().setMethod("popup").setMinutes(10),
                        };
                        Event.Reminders reminders = new Event.Reminders()
                                .setUseDefault(false)
                                .setOverrides(Arrays.asList(reminderOverrides));
                        event.setReminders(reminders);

                        event = mService.events().insert(idCalendar, event).execute();
                        idEventiCalendar.add(event.getId());
                        System.out.printf("Event created: %s\n", event.getId());
                    }
                }
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ECCEZIONE CALENDAR", e.getCause().toString());
                mLastError = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if(response){
                Log.d(TAG, ""+idEventiCalendar.size());
                mCallback.done(response, idEventiCalendar);
            }
            else{
                Toast.makeText(context, "Operazione non riuscita", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            AddPrescription.mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    AppConstants.showGooglePlayServicesAvailabilityErrorDialog((AddPrescription)context,
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            AppConstants.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(context, "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("ERRORE CALENDAR", mLastError.getMessage());
                }
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * actions to be done when clicking on the textview where start date is inserted, and when selecting notifications' timing
     * @param v
     */
    @Override
    public void onClick(View v) {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        switch (v.getId()) {
            case R.id.insertInizio:
                mYear = c.get(java.util.Calendar.YEAR);
                mMonth = c.get(java.util.Calendar.MONTH);
                mDay = c.get(java.util.Calendar.DAY_OF_MONTH);

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

                                inserisciInizio.setText(year + "-" + monthOfYearStr + "-" + dayOfMonthStr);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
                break;
            case R.id.date_item_add:
                integers.add(new Integer(count++));
                date_item_adapter.notifyDataSetChanged();
                setListViewHeightBasedOnItems(date_item_listview);
                orari_assunzioni.add("");
                break;
        }
    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

}
