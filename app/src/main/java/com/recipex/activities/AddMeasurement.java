package com.recipex.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
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

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddMeasurementMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.recipex.AppConstants;
import com.recipex.asynctasks.AddMeasurementAT;
import com.recipex.asynctasks.NdefReaderTask;
import com.recipex.taskcallbacks.AddMeasurementTC;
import com.recipex.taskcallbacks.NdefReaderTaskCallback;
import com.recipex.utilities.ConnectionDetector;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import com.recipex.R;

import java.util.Locale;

import me.angrybyte.numberpicker.view.ActualNumberPicker;

public class AddMeasurement extends AppCompatActivity
        implements AddMeasurementTC, me.angrybyte.numberpicker.listener.OnValueChangeListener,
        NdefReaderTaskCallback {

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
}
