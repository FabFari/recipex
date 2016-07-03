package com.recipex.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddMeasurementMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUpdateUserMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
import com.recipex.asynctasks.AddMeasurementAT;
import com.recipex.asynctasks.DeleteEventsCalendarAT;
import com.recipex.asynctasks.NdefReaderAT;
import com.recipex.asynctasks.RegistraCalendarioAT;
import com.recipex.taskcallbacks.AddMeasurementTC;
import com.recipex.taskcallbacks.NdefReaderTC;
import com.recipex.taskcallbacks.CalendarAddTC;
import com.recipex.taskcallbacks.CalendarDeleteTC;
import com.recipex.taskcallbacks.RegisterCalendarTC;
import com.recipex.utilities.ConnectionDetector;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import com.recipex.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.angrybyte.numberpicker.view.ActualNumberPicker;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * This activity adds a measurement for a user, by showing all fields that must be filled for the measurement. Then it automatically
 * adds the measurement to the user's calendar
 */
public class AddMeasurement extends AppCompatActivity
        implements CalendarAddTC, EasyPermissions.PermissionCallbacks, AddMeasurementTC,
        me.angrybyte.numberpicker.listener.OnValueChangeListener, CalendarDeleteTC, RegisterCalendarTC,
        NdefReaderTC {

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int REQUEST_ACCOUNT_CALENDAR = 1000;

    static final int REQUEST_ACCOUNT_PICKER2 = 3;


    //IMPORTANTISSIMO!
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    public static final String TAG = "ADD_MEASUREMENT";
    public ArrayList<String> idEventi;

    private SwipeNumberPicker picker1;
    private SwipeNumberPicker picker2;
    private ActualNumberPicker float_picker;
    private ImageView icon;
    private TextView title;
    private LinearLayout linearLayout;
    private TextView text_picker1;
    private TextView text_picker2;
    private EditText note;
    private RelativeLayout main_relative;
    private RelativeLayout relativeLayout2;

    private TextView picker_res1;
    private TextView picker_res2;
    private TextView float_picker_res;
    private double float_value;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private ConnectionDetector cd;

    private CircularProgressView progressView;

    private Long user_id;
    private boolean has_change = false;

    private SharedPreferences pref;
    private String measurement_kind;

    private NfcAdapter mNfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

        pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        user_id = pref.getLong("userId", 0L);

        Intent intent = getIntent();
        measurement_kind = intent.getStringExtra("kind");

        bindActivity();

        //Intent intent = getIntent();
        //NdefMessage msgs[];
        //super.onResume();
        /*
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Log.d(TAG, "Sono nell'OnCreate!!!");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderAT(main_relative, this).execute(tag);
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
        }
        else {
        */
        if(measurement_kind != null) {
            bind_specific();
            setupUI(measurement_kind);
        }
        //}

        picker1.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
                boolean isValueOk = (newValue & 1) == 0;
                if (isValueOk) {
                    picker_res1.setText(Integer.toString(newValue));
                }

                return isValueOk;
            }
        });

        picker2.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
                boolean isValueOk = (newValue & 1) == 0;
                if (isValueOk) {
                    picker_res2.setText(Integer.toString(newValue));
                }

                return isValueOk;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_measurement, menu);
        return true;
    }

    /**
     *
     * @param oldValue selected
     * @param newValue selected
     */
    @Override
    public void onValueChanged(int oldValue, int newValue) {
        has_change = true;
        if(measurement_kind.equals(AppConstants.SPO2)) {
            float_value = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*100.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f%%", float_value));
        }
        else if(measurement_kind.equals(AppConstants.GLUCOSIO)){
            float_value = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*600.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f", float_value));
        }
        else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)){
            float_value = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*15.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1fC°", float_value));
        }
        else {
            float_value = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*800.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f", float_value));
        }
    }

    /**
     * calculate final value of float picker
     */
    private void get_float_value() {
        if(measurement_kind.equals(AppConstants.SPO2)) {
            float_value = ((double) float_picker.getValue() / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*100.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f%%", float_value));
        }
        else if(measurement_kind.equals(AppConstants.GLUCOSIO)){
            float_value = ((double) float_picker.getValue() / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*600.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f", float_value));
        }
        else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)){
            float_value = ((double) float_picker.getValue() / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*15.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1fC°", float_value));
        }
        else {
            float_value = ((double) float_picker.getValue() / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*800.0;
            float_value = (double)Math.round(float_value * 10d) / 10d;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f", float_value));
        }
    }

    /**
     * set layouts elements
     */
    private void bindActivity() {
        picker1 = (SwipeNumberPicker) findViewById(R.id.measurement_number_picker_1);
        picker2 = (SwipeNumberPicker) findViewById((R.id.measurement_number_picker_2));
        picker_res1 = (TextView) findViewById(R.id.measurement_number_picker_res1);
        picker_res2 = (TextView) findViewById(R.id.measurement_number_picker_res2);
        float_picker_res = (TextView) findViewById(R.id.measurement_number_float_picker);
        icon = (ImageView) findViewById(R.id.measurement_icon);
        title = (TextView) findViewById(R.id.measurement_title);
        linearLayout = (LinearLayout) findViewById(R.id.measurement_linearlayout);
        text_picker1 = (TextView) findViewById(R.id.measurement_number_picker_text1);
        text_picker2 = (TextView) findViewById(R.id.measurement_number_picker_text2);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.measurement_relative2);
        progressView = (CircularProgressView) findViewById(R.id.measurement_progress_view);
        note = (EditText) findViewById((R.id.measurement_crgv_bio));
        main_relative = (RelativeLayout) findViewById(R.id.measurement_main_relative);
    }

    /**
     * specific elements of layout
     */
    private void bind_specific() {
        if(measurement_kind.equals(AppConstants.SPO2))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_spo2_number_picker);
        if(measurement_kind.equals(AppConstants.GLUCOSIO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_hgt_number_picker);
        if(measurement_kind.equals(AppConstants.TEMP_CORPOREA))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_temp_number_picker);
        if(measurement_kind.equals(AppConstants.COLESTEROLO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_chl_number_picker);
    }

    /**
     * set ui depending on measurement kind selected
     * @param measurement_kind
     */
    private void setupUI(String measurement_kind) {
        if(measurement_kind.equals(AppConstants.PRESSIONE)) {
            text_picker2.setVisibility(View.VISIBLE);
            relativeLayout2.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.FREQ_CARDIACA)) {
            text_picker1.setText("Numero battiti:");
            picker1.setMinValue(0);
            picker1.setValue(80, true);
            picker1.setMaxValue(400);
            picker_res1.setText("80");
            icon.setImageResource(R.drawable.ic_heart_rate_dark);
            title.setText("Frequenza cardiaca");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.FREQ_RESPIRAZIONE)) {
            text_picker1.setText("Atti Respiri:");
            picker1.setMinValue(0);
            picker1.setValue(16, true);
            picker1.setMaxValue(200);
            picker_res1.setText("16");
            icon.setImageResource(R.drawable.ic_respirations_dark);
            title.setText("Frequenza respiratoria");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.SPO2)) {
            text_picker1.setText("Concentrazione di ossigeno:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("98%");
            icon.setImageResource(R.drawable.ic_spo2_dark);
            title.setText("Ossigenazione sanguigna");
            title.setTextSize(20);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.GLUCOSIO)) {
            text_picker1.setText("HGT:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("150");
            icon.setImageResource(R.drawable.ic_diabetes_dark);
            title.setText("Glicemia");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)) {
            text_picker1.setText("Temperatura:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("36.5C°");
            icon.setImageResource(R.drawable.ic_temperature_dark);
            title.setText("Temperatura corporea");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.DOLORE)) {
            text_picker1.setText("Dolore:");
            picker1.setMinValue(0);
            picker1.setValue(0, true);
            picker1.setMaxValue(10);
            picker_res1.setText("0");
            float_picker_res.setText("36.5C°");
            icon.setImageResource(R.drawable.ic_pain_dark);
            title.setText("Scala del dolore");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else {
            text_picker1.setText("Colesterolo totale:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("150");
            icon.setImageResource(R.drawable.ic_cholesterol_dark);
            title.setText("Colesterolo");
            linearLayout.setVisibility(View.VISIBLE);
        }
        progressView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.measurement_confirm) {
            //When the measurement is confirmed, it will be first added in the calendar.

            // Initialize credentials and service object.
            mCredential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            Log.d(TAG, "Inizio calendario");
            if(AppConstants.checkNetwork(this)) getResultsFromApi();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * calls asynctask that adds the event on the calendar
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            //setto come account la mail con cui ho fatto il login (c'è per forza)
            mCredential.setSelectedAccountName(getSharedPreferences("MyPref", MODE_PRIVATE).getString("email", ""));
            Log.d("CALENDARgetres", "account");
            new AggiungiMisurazioneCalendar(mCredential, getApplicationContext(), this, measurement_kind,
                    getDescription(measurement_kind)).execute();
            //chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(AddMeasurement.this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            new AggiungiMisurazioneCalendar(mCredential, getApplicationContext(), this, measurement_kind,
                    getDescription(measurement_kind)).execute();
        }
    }

    /**
     * gets value of the measurement, as a description of the event to be put on the calendar
     * @param tipo
     * @return
     */
    private String getDescription(String tipo){
        String description=new String();
        switch(measurement_kind) {
            case AppConstants.PRESSIONE:
                description = "Sistolica: " + picker_res1.getText().toString() + "\n" +
                        "Diastolica: " + picker_res2.getText().toString();
                break;
            case AppConstants.FREQ_CARDIACA:
                description = "Numero battiti: " + picker_res1.getText().toString();
                break;
            case AppConstants.FREQ_RESPIRAZIONE:
                description = "Atti Respiri: " + picker_res1.getText().toString();
                break;
            case AppConstants.SPO2:
                if (!has_change)
                    get_float_value();
                description = "Concentrazione di ossigeno: " + float_value;
                break;
            case AppConstants.GLUCOSIO:
                if (!has_change)
                    get_float_value();
                description = "HGT: " + float_value;
                break;
            case AppConstants.TEMP_CORPOREA:
                if (!has_change)
                    get_float_value();
                description = "Temperatura corporea: " + float_value;
                break;
            case AppConstants.DOLORE:
                description = "Dolore: " + picker_res1.getText().toString();
                break;
            case AppConstants.COLESTEROLO:
                if (!has_change)
                    get_float_value();
                description = "Colesterolo totale: " + float_value;
                break;
            default:
                Snackbar snackbar = Snackbar
                        .make(main_relative, "Tipo misurazione non esistente!!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                break;
        }
        return description;
    }

    /**
     * callback from calendar
     * @param b boolean to check it is all ok
     * @param s ids of the events created on the calendar (for the measurement just one)
     */
    public void done(boolean b, ArrayList<String> s){
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
                if(AppConstants.checkNetwork(this)) new RegistraCalendarioAT(this, this, main_relative, pref.getLong("userId", 0L), m, apiHandler).execute();
            }
        }

        //if the measurement was correctly saved in the calendar, I add it in the server.
        if(b){
            idEventi=s;

            settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

            if(credential.getSelectedAccountName() == null) {
                Log.d(TAG, "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
            else {
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                executeAddMeasurementAT(apiHandler);
            }
        }
    }

    /**
     * calls asynctask to add measurement on the server, with values depending on the measurement kind
     * @param apiHandler parameter for AppEngine server
     */
    private void executeAddMeasurementAT(RecipexServerApi apiHandler) {
        MainAddMeasurementMessage content = new MainAddMeasurementMessage();
        content.setKind(measurement_kind);

        //there will be always an event for the measurement in the calendar. The measure is always added to the calendar
        if(!idEventi.isEmpty()) content.setCalendarId(idEventi.get(0));
        switch(measurement_kind) {
            case AppConstants.PRESSIONE:
                content.setSystolic(Long.parseLong(picker_res1.getText().toString()));
                content.setDiastolic(Long.parseLong(picker_res2.getText().toString()));
                break;
            case AppConstants.FREQ_CARDIACA:
                content.setBpm(Long.parseLong(picker_res1.getText().toString()));
                break;
            case AppConstants.FREQ_RESPIRAZIONE:
                content.setRespirations(Long.parseLong(picker_res1.getText().toString()));
                break;
            case AppConstants.SPO2:
                //String spo2 = float_picker_res.getText().toString();
                //content.setSpo2(Double.parseDouble(spo2.substring(0,spo2.length()-1)));
                if(!has_change)
                    get_float_value();
                content.setSpo2(float_value);
                break;
            case AppConstants.GLUCOSIO:
                //content.setHgt(Double.parseDouble(float_picker_res.getText().toString()));
                if(!has_change)
                    get_float_value();
                content.setHgt(float_value);
                break;
            case AppConstants.TEMP_CORPOREA:
                //String temp = float_picker_res.getText().toString();
                //content.setDegrees(Double.parseDouble(temp.substring(0,temp.length()-2)));
                if(!has_change)
                    get_float_value();
                content.setDegrees(float_value);
                break;
            case AppConstants.DOLORE:
                content.setNrs(Long.parseLong(picker_res1.getText().toString()));
                break;
            case AppConstants.COLESTEROLO:
                //content.setChlLevel(Double.parseDouble(float_picker_res.getText().toString()));
                if(!has_change)
                    get_float_value();
                content.setChlLevel(float_value);
                break;
            default:
                Snackbar snackbar = Snackbar
                        .make(main_relative, "Tipo misurazione non esistente!!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                break;
        }

        if(note.getText().toString().length() > 0)
            content.setNote(note.getText().toString());

        if (AppConstants.checkNetwork(this)) {
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
            new AddMeasurementAT(this, this, main_relative, user_id, content, apiHandler).execute();
        }
    }


    /**
     * callback register calendar
     * @param b boolean to check it is all ok
     * @param i to distinguish from other done methods
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
     * callback from AddMeasurementAT
     * @param resp boolean to check it is all ok
     * @param response from the server
     */
    @Override
    public void done(boolean resp, MainDefaultResponseMessage response) {
        if(response != null) {
            if(resp) {
                Intent i=new Intent(AddMeasurement.this, Home.class);
                startActivity(i);
                this.setResult(RESULT_OK);
                idEventi.clear();
                finish();
            }
        }
        //if the operation was not performed, I have to delete the event on the calendar, for consistency.
        else {
            Snackbar snackbar = Snackbar
                    .make(main_relative, "Operazione non riuscita", Snackbar.LENGTH_SHORT);
            snackbar.show();

            //delete from the calendar
            if(AppConstants.checkNetwork(this)){
                new DeleteEventsCalendarAT( mCredential, this.getApplicationContext(), this, idEventi).execute();
            }
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.INVISIBLE);
    }


    /**
     * callback elimina eventi calendar
     * @param b boolean from the server
     * @param inutile to distinguish from other done methods
     */
    public void done(boolean b, String inutile){
        if(!b){
            Snackbar snackbar = Snackbar
                    .make(main_relative, "Errore: aggiunto evento sul calendario" +
                            "che non corrisponde a una misurazione.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        Intent i =new Intent(AddMeasurement.this, Home.class);
        startActivity(i);
        this.finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
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
                        executeAddMeasurementAT(apiHandler);
                    }
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
                        if(AppConstants.checkNetwork(this)) new RegistraCalendarioAT(this, this, main_relative, pref.getLong("userId", 0L), m, apiHandler).execute();
                    }
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(AddMeasurement.this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_CALENDAR:
                Log.d(TAG, "Request account calendar");
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    Log.d("CALENDARres", "entro in res");

                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Log.d("CALENDARres", accountName);
                        getResultsFromApi();
                    }
                }
                else{
                    Log.d("CALENDARres", "errore");
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


    @Override
    public void onResume() {
        Intent intent = getIntent();
        //NdefMessage msgs[];
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Log.d(TAG, "Sono nell'OnResume!!!");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderAT(main_relative, this).execute(tag);
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * callback from NdefReaderAT
     * @param result measurement kind associated with a nfc
     */
    @Override
    public void done(String result) {
        if(result != null) {
            measurement_kind = result;
            bind_specific();
            setupUI(measurement_kind);
        }

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
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

            String accountName = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
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
                        REQUEST_ACCOUNT_CALENDAR);
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
                AddMeasurement.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }



    /**
     * Async task to add an event to the user's calendar
     */
    private class AggiungiMisurazioneCalendar extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private CalendarAddTC mCallback;
        private String tipo;
        private String descrizione;

        //insert here the ids of the events I create (in this case one)
        private ArrayList<String> idEventiCalendar;

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public AggiungiMisurazioneCalendar(GoogleAccountCredential credential, Context context, CalendarAddTC c,
                                           String tipo, String descrizione) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("RecipeX")
                    .build();
            this.context=context;
            this.mCallback=c;
            this.tipo=tipo;
            this.descrizione=descrizione;
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
                String idCalendar=pref.getString("calendar", "");
                //create a calendar if there is not
                if(idCalendar.equals("")) {
                    Log.d("CALENDAR", "creo nuovo calendario");
                    // Create a new calendar
                    com.google.api.services.calendar.model.Calendar calendar = new Calendar().setSummary("Terapie");
                    calendar.setTimeZone("Europe/Rome");

                    // Insert the new calendar
                    Calendar createdCalendar = mService.calendars().insert(calendar).execute();
                    idCalendar=createdCalendar.getId();
                    System.out.println("New Calendar "+createdCalendar.getId());
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putString("calendar", createdCalendar.getId());
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
                //add measurement

                Event event = new Event()
                        .setSummary(AppConstants.getTipo(tipo)).setDescription(descrizione);

                java.util.Calendar c = java.util.Calendar.getInstance();
                int second = c.get(java.util.Calendar.SECOND);
                String secondstr="";
                if(second<=9)
                    secondstr="0"+second;
                else secondstr=Integer.toString(second);

                int minute = c.get(java.util.Calendar.MINUTE);
                String minutestr="";
                if(minute<=9)
                    minutestr="0"+minute;
                else minutestr=Integer.toString(minute);

                //24 hour format
                int hourofday = c.get(java.util.Calendar.HOUR_OF_DAY);
                String hourstr="";
                if(hourofday<=9)
                    hourstr="0"+hourofday;
                else hourstr=Integer.toString(hourofday);

                int mYear = c.get(java.util.Calendar.YEAR);
                int mMonth = c.get(java.util.Calendar.MONTH);
                String month=Integer.toString(mMonth+1);
                if(mMonth<=9)
                    month="0"+month;

                int mDay = c.get(java.util.Calendar.DAY_OF_MONTH);
                String day=Integer.toString(mDay);
                if(mDay<=9)
                    day="0"+day;

                String data=mYear+"-"+month+"-"+day+"T"+hourstr+":"+minutestr+":"+secondstr+"+02:00";

                DateTime startDateTime=new DateTime(data);

                Log.d("STARTTIME ", data);

                EventDateTime startEvento = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("Europe/Rome");
                event.setStart(startEvento);

                int orepiùuno=hourofday+1;
                String orepiùunostr="";
                if(orepiùuno<=9)
                    orepiùunostr="0"+orepiùuno;
                else orepiùunostr=Integer.toString(orepiùuno);

                String dataend=mYear+"-"+month+"-"+day+"T"+orepiùunostr+":"+minutestr+":"+secondstr+"+02:00";
                DateTime endDateTime=new DateTime(dataend);
                Log.d("DATAEND", dataend);

                EventDateTime endEvento = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone("Europe/Rome");
                event.setStart(startEvento);
                event.setEnd(endEvento);

                EventReminder[] reminderOverrides = new EventReminder[] {
                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                        new EventReminder().setMethod("popup").setMinutes(10),
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);


                event = mService.events().insert(idCalendar, event).execute();
                idEventiCalendar.add(event.getId());
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean response) {
            if(response){
                mCallback.done(response, idEventiCalendar);
            }
            else{
                Toast.makeText(context, "Operazione non riuscita", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
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

}
