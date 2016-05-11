package com.recipex.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
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
import com.recipex.asynctasks.AddMeasurementAT;
import com.recipex.asynctasks.NdefReaderTask;
import com.recipex.taskcallbacks.AddMeasurementTC;
import com.recipex.taskcallbacks.NdefReaderTaskCallback;
import com.recipex.taskcallbacks.TaskCallbackCalendar;
import com.recipex.utilities.ConnectionDetector;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import com.recipex.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.angrybyte.numberpicker.view.ActualNumberPicker;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AddMeasurement extends AppCompatActivity
        implements TaskCallbackCalendar, EasyPermissions.PermissionCallbacks, AddMeasurementTC, me.angrybyte.numberpicker.listener.OnValueChangeListener,
        NdefReaderTaskCallback {

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static ProgressDialog mProgress;

    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int REQUEST_ACCOUNT_CALENDAR = 1000;


    //IMPORTANTISSIMO!
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    public static final String TAG = "ADD_MEASUREMENT";

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
    // Input
    // Long user_id = 5724160613416960L;
    private Long user_id;
    private SharedPreferences pref;
    String measurement_kind = AppConstants.COLESTEROLO;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

        pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        user_id = pref.getLong("userId", 0L);


        bindActivity();

        Intent intent = getIntent();
        NdefMessage msgs[];
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask(main_relative, this).execute(tag);
        }

        progressView.startAnimation();
        progressView.setVisibility(View.VISIBLE);

        //setupUI();

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

    /*
    @Override
    public void onBackPressed(){
        Intent myIntent = new Intent(getApplicationContext(), Home.class);
        startActivity(myIntent);
        this.finish();
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_measurement, menu);
        return true;
    }

    @Override
    public void onValueChanged(int oldValue, int newValue) {
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

    private void bindActivity() {
        picker1 = (SwipeNumberPicker) findViewById(R.id.measurement_number_picker_1);
        picker2 = (SwipeNumberPicker) findViewById((R.id.measurement_number_picker_2));
        picker_res1 = (TextView) findViewById(R.id.measurement_number_picker_res1);
        picker_res2 = (TextView) findViewById(R.id.measurement_number_picker_res2);
        float_picker_res = (TextView) findViewById(R.id.measurement_number_float_picker);
        if(measurement_kind.equals(AppConstants.SPO2))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_spo2_number_picker);
        if(measurement_kind.equals(AppConstants.GLUCOSIO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_hgt_number_picker);
        if(measurement_kind.equals(AppConstants.TEMP_CORPOREA))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_temp_number_picker);
        if(measurement_kind.equals(AppConstants.COLESTEROLO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_chl_number_picker);
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
            text_picker1.setText("Numero Battiti:");
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
            text_picker1.setText("HGT");
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
            text_picker1.setText("Colesterolo:");
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
            settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
            setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

            if(credential.getSelectedAccountName() == null)
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            else {
                RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                executeAddMeasurementAT(apiHandler);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Snackbar snackbar = Snackbar
                    .make(main_relative, "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
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
                        executeAddMeasurementAT(apiHandler);
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

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    private void executeAddMeasurementAT(RecipexServerApi apiHandler) {
        MainAddMeasurementMessage content = new MainAddMeasurementMessage();
        content.setKind(measurement_kind);
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
                content.setSpo2(float_value);
                break;
            case AppConstants.GLUCOSIO:
                //content.setHgt(Double.parseDouble(float_picker_res.getText().toString()));
                content.setHgt(float_value);
                break;
            case AppConstants.TEMP_CORPOREA:
                //String temp = float_picker_res.getText().toString();
                //content.setDegrees(Double.parseDouble(temp.substring(0,temp.length()-2)));
                content.setDegrees(float_value);
                break;
            case AppConstants.DOLORE:
                content.setNrs(Long.parseLong(picker_res1.getText().toString()));
                break;
            case AppConstants.COLESTEROLO:
                //content.setChlLevel(Double.parseDouble(float_picker_res.getText().toString()));
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

        if (checkNetwork()) {
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
            new AddMeasurementAT(this, this, main_relative, user_id, content, apiHandler).execute();
        }
    }

    @Override
    public void done(boolean resp, MainDefaultResponseMessage response) {
        if(response != null) {
            if(resp) {
                Snackbar snackbar = Snackbar
                        .make(main_relative, "Misurazione inserita: "+response.getPayload(), Snackbar.LENGTH_SHORT);
                snackbar.show();

                mProgress = new ProgressDialog(this);
                mProgress.setMessage("Sto inserendo nel calendario...");

                // Initialize credentials and service object.
                mCredential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());
                getResultsFromApi();
            }
            else {
                Snackbar snackbar = Snackbar
                        .make(main_relative, "Operazione non riuscita: "+response.getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.INVISIBLE);
    }

    //callback from Calendar
    public void done(boolean b){
        if(b){
            Intent i=new Intent(AddMeasurement.this, Home.class);
            startActivity(i);
            finish();
        }
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(AddMeasurement.this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            new AggiungiMisurazioneCalendar(mCredential, getApplicationContext(), this, measurement_kind).execute();
        }
    }

    @Override
    public void onResume() {
        Intent intent = getIntent();
        NdefMessage msgs[];
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask(main_relative, this).execute(tag);
            progressView.startAnimation();
            progressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void done(String result) {
        if(result != null)
            setupUI(result);

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
                        REQUEST_ACCOUNT_CALENDAR
                );
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

    private class AggiungiMisurazioneCalendar extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private TaskCallbackCalendar mCallback;
        private String tipo;

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public AggiungiMisurazioneCalendar(GoogleAccountCredential credential, Context context, TaskCallbackCalendar c,
                                       String tipo) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("RecipeX")
                    .build();
            this.context=context;
            this.mCallback=c;
            this.tipo=tipo;
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
                        .setSummary(tipo);

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

                String data=mYear+"-"+month+"-"+mDay+"T"+hourstr+":"+minutestr+":"+secondstr+"+02:00";

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

                String dataend=mYear+"-"+month+"-"+mDay+"T"+orepiùunostr+":"+minutestr+":"+secondstr+"+02:00";
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
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ECCEZIONE CALENDAR", e.getMessage());
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
            AddMeasurement.mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            AddMeasurement.REQUEST_AUTHORIZATION);
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
