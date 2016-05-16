package com.recipex.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientsMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
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
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.asynctasks.AggiungiTerapiaAT;
import com.recipex.asynctasks.GetMainIngredientsAT;
import com.recipex.taskcallbacks.TaskCallbackActiveIngredients;
import com.recipex.taskcallbacks.TaskCallbackAggiungiTerapia;
import com.recipex.taskcallbacks.TaskCallbackCalendar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AggiungiTerapia extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, TaskCallbackAggiungiTerapia,
        TaskCallbackActiveIngredients, TaskCallbackCalendar, AdapterView.OnItemSelectedListener, View.OnClickListener{

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    //IMPORTANTISSIMO!
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    private CoordinatorLayout coordinatorLayout;
    private Long caregiverId, patientId;

    private Toolbar toolbar;

    private int mDay, mMonth, mYear;

    boolean fatto=false;

    List<String> nomiIngredienti;
    List<Long> idsIngredienti;

    Spinner spinner;

    String nome, ingrediente, tipo, dose, unità, quantità, ricetta, foglio, caregiver, numerocadenza, cadenza, ore, inizio;

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

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.lay_aggiungiterapia);
        Spinner numerocadenza=(Spinner)findViewById(R.id.numerocadenzaspin);
        Spinner orespin=(Spinner)findViewById(R.id.orespin);

        String[] np = new String[MAXNUMEROCADENZA];
        for(int i=1;i<=MAXNUMEROCADENZA;i++){
            np[i-1]=Integer.toString(i);
        }

        String[] np2 = new String[ORE];
        for(int i=0;i<ORE;i++){
            np2[i]=Integer.toString(i);
        }
        ArrayAdapter <String> _aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,np);//array holding min and max pages
        numerocadenza.setAdapter(_aa);

        ArrayAdapter <String> _aa2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,np2);//array holding min and max pages
        orespin.setAdapter(_aa2);

        numerocadenza.setOnItemSelectedListener(this);
        orespin.setOnItemSelectedListener(this);

        Spinner cadenzaspin=(Spinner)findViewById(R.id.cadenzaspin);
        ArrayAdapter<CharSequence> adapterc = ArrayAdapter.createFromResource(this,
                R.array.cadenze, android.R.layout.simple_spinner_item);
        cadenzaspin.setAdapter(adapterc);
        cadenzaspin.setOnItemSelectedListener(this);

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

        if(checkNetwork()) new GetMainIngredientsAT(getApplicationContext(), this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_registration, menu);
        toolbar.inflateMenu(R.menu.menu_registration);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.registrati) {
            if (inserisciNome.getText().length() > 1 && fatto &&
                    inserisciDose.getText().length()>0 &&
                    inserisciUnità.getText().length()>1 && inserisciQuantità.getText().length()>0 ) {

                nome=inserisciNome.getText().toString();

                dose = inserisciDose.getText().toString();

                long dose2= Long.parseLong(dose);

                unità = inserisciUnità.getText().toString();
                quantità = inserisciQuantità.getText().toString();

                int quanto=Integer.parseInt(quantità);

                foglio = inserisciFoglio.getText().toString();
                //caregiver = inserisciCaregiver.getText().toString();

                inizio=inserisciInizio.getText().toString();

                Log.d("REGISTRAZIONE ", "Sono qui");

                if (checkNetwork()) {
                    if(!caregiverId.equals(0L)) {
                        new AggiungiTerapiaAT(getApplicationContext(), nome, ingredienteID, tipo, dose2, unità,
                                quanto, recipe, foglio, caregiverId, patientId, this).execute();
                    }
                    else {
                        new AggiungiTerapiaAT(getApplicationContext(), nome, ingredienteID, tipo, dose2, unità,
                                quanto, recipe, foglio, null, null, this).execute();
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


    //callback from AggiungiTerapiaAT
    public void done(boolean b, MainDefaultResponseMessage response){
        if(response != null) {
            if(b) {
                //Toast.makeText(this, "Terapia inserita "+response.getMessage(), Toast.LENGTH_LONG).show();

                //aggiungo al calendario
                if(!inizio.equals("")) {
                    mProgress = new ProgressDialog(this);
                    mProgress.setMessage("Sto inserendo nel calendario...");


                    // Initialize credentials and service object.
                    mCredential = GoogleAccountCredential.usingOAuth2(
                            getApplicationContext(), Arrays.asList(SCOPES))
                            .setBackOff(new ExponentialBackOff());
                    getResultsFromApi();
                }
            }
            else {
                //Toast.makeText(this, "Operazione non riuscita", Toast.LENGTH_LONG).show();
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Operazione non riuscita", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Operazione non riuscita", Snackbar.LENGTH_SHORT);
            snackbar.show();

        }
    }

    //callback from Calendar
    public void done(boolean b){
        if(b){
            Intent i=new Intent(AggiungiTerapia.this, Home.class);
            this.setResult(RESULT_OK);
            this.finish();
            startActivity(i);
            finish();
        }
    }

    //callback from GetIngredients
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

        spinner = (Spinner) findViewById(R.id.ingredienti);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, nomiIngredienti);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

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
            boolean recipe=(ricetta.equals("SI"))? true: false;
            Log.d("RICETTA ", " "+recipe);
        }
        else if(parent.getId()==R.id.numerocadenzaspin){
            numerocadenza=(String)parent.getItemAtPosition(pos);
        }
        else if(parent.getId()==R.id.cadenzaspin){
            cadenza=(String)parent.getItemAtPosition(pos);
            Log.d("CADENZA", cadenza);
        }
        else if(parent.getId()==R.id.orespin){
            ore=(String)parent.getItemAtPosition(pos);
            Log.d("ORE", ore);
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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


    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(AggiungiTerapia.this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            new AggiungiTerapiaCalendar(mCredential, getApplicationContext(), this, nome, numerocadenza, cadenza,
                    ore, inizio).execute();
        }
    }
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

            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
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
                    Toast.makeText(AggiungiTerapia.this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();

                } else {
                    getResultsFromApi();
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
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Log.d("CALENDARres", accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
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
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                AggiungiTerapia.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class AggiungiTerapiaCalendar extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private TaskCallbackCalendar mCallback;
        private String nome;
        private String numerocadenza;
        private String cadenza;
        private String ore;
        private String inizio;

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public AggiungiTerapiaCalendar(GoogleAccountCredential credential, Context context, TaskCallbackCalendar c,
                                       String nome, String numerocadenza, String cadenza, String ore, String inizio) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("RecipeX")
                    .build();
            this.context=context;
            this.mCallback=c;
            this.nome=nome;
            this.numerocadenza=numerocadenza;
            this.cadenza=cadenza;
            this.ore=ore;
            this.inizio=inizio;
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

            try {
                String idCalendar=pref.getString("calendar", "");
                //creo calendario se non c'è
                if(idCalendar.equals("")) {
                    Log.d("CALENDAR", "creo nuovo calendario");
                    // Create a new calendar
                    com.google.api.services.calendar.model.Calendar calendar = new Calendar().setSummary("Terapie");
                    calendar.setTimeZone("Europe/Rome");

                    // Insert the new calendar
                    Calendar createdCalendar = mService.calendars().insert(calendar).execute();
                    System.out.println("New Calendar "+createdCalendar.getId());
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putString("calendar", createdCalendar.getId());
                    editor.commit();
                }
                //aggiungo la terapia
                System.out.println("arrivo");
                Event event = new Event()
                        .setSummary(nome);

                if(Integer.parseInt(ore)<=9)
                    ore="0"+ore;
                Log.d("STARTTIME ", inizio+"T"+ore+":00:00+02:00");
                DateTime startDateTime = new DateTime(inizio+"T"+ore+":00:00+02:00");

                EventDateTime startEvento = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("Europe/Rome");
                event.setStart(startEvento);

                //event.setEndTimeUnspecified(true);


                int orepiùunoint = Integer.parseInt(ore) + 1;
                String orepiùuno = Integer.toString(orepiùunoint);
                if(Integer.parseInt(orepiùuno)<=9)
                    orepiùuno="0"+orepiùuno;

                DateTime endDateTime=new DateTime(inizio+"T"+orepiùuno+":00:00+02:00");
                Log.d("DATAEND", inizio+"T"+orepiùuno+":00:00+02:00");

                EventDateTime endEvento = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone("Europe/Rome");
                event.setStart(startEvento);
                event.setEnd(endEvento);

                String ruledef="";
                if(cadenza.equals("giorno"))
                    ruledef="DAILY";
                else if(cadenza.equals("settimana"))
                    ruledef="WEEKLY";
                else if(cadenza.equals("mese"))
                    ruledef="MONTHLY";
                else if(cadenza.equals("anno"))
                    ruledef="YEARLY";

                String[] recurrence = new String[] {"RRULE:FREQ="+ruledef+";INTERVAL="+numerocadenza};

                event.setRecurrence(Arrays.asList(recurrence));
                Log.d("RECURRENCE ",recurrence[0]);

                EventReminder[] reminderOverrides = new EventReminder[] {
                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                        new EventReminder().setMethod("popup").setMinutes(10),
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);

                event = mService.events().insert(idCalendar, event).execute();
                System.out.printf("Event created: %s\n", event.getHtmlLink());
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
                mCallback.done(response);
            }
            else{
                Toast.makeText(context, "Operazione non riuscita", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            AggiungiTerapia.mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            AggiungiTerapia.REQUEST_AUTHORIZATION);
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
        }
    }

}
