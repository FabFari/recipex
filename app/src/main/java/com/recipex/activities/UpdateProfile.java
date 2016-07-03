package com.recipex.activities;

import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUpdateUserMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.asynctasks.UpdateUserAT;
import com.recipex.taskcallbacks.UpdateUserTC;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.PlacesAutoCompleteAdapter;
import com.recipex.utilities.StreetAutoCompleteAdapter;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity to update profile information.
 */
public class UpdateProfile extends AppCompatActivity implements View.OnClickListener, UpdateUserTC {

    private final static String TAG = "UPDATE_PROFILE";
    private static final int REQUEST_ACCOUNT_PICKER = 2;

    private CoordinatorLayout coordinatorLayout;
    private CardView userCard;
    private CardView caregiverCard;
    private CircularProgressView progressView;
    private ArrayAdapter<CharSequence> sex_adapter;
    private ConnectionDetector cd;
    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;

    // USER
    // Required
    private TextView name;
    private TextView surname;
    private ImageView pic;
    private TextView email;
    private TextView birth;
    private Spinner sex;
    // Not Required
    private AutoCompleteTextView city;
    private AutoCompleteTextView address;
    private TextView personal_num;
    // CAREGIVER
    // Required
    private TextView field;
    private TextView years_exp;
    private TextView place;
    private TextView available;
    private TextView business_num;
    private TextView bio;

    // INPUT VARIABLES
    private Long user_id;
    private Long profile_id;

    // INFO VARIABLES
    // USER
    // Required
    private String user_name;
    private String user_surname;
    private String user_pic;
    private String user_email;
    private String user_birth;
    private String user_sex;
    // Not Required
    private String user_city;
    private String user_address;
    private String user_phone;
    // CAREGIVER
    // Required
    private String crgv_field;
    // Not Required
    private Long crgv_years_exp;
    private String crgv_place;
    private String crgv_available;
    private String crgv_phone;
    private String crgv_bio;

    // Input
    int mDay, mMonth, mYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        // USER
        // Required
        user_id = extras.getLong("userId");
        user_name = extras.getString("userName");
        user_surname = extras.getString("userSurname");
        user_pic = extras.getString("userPic");
        user_email = extras.getString("userEmail");
        user_birth = extras.getString("userBirth");
        user_sex = extras.getString("userSex");
        // Not Required
        user_city = extras.getString("userCity", null);
        user_address = extras.getString("userAddress", null);
        user_phone = extras.getString("userPhone", null);
        // CAREGIVER
        // Required
        crgv_field = extras.getString("crgvField");
        if(crgv_field != null) {
            // Not Required
            crgv_years_exp = extras.getLong("crgvYears", 0L);
            crgv_place = extras.getString("crgvPlace", null);
            crgv_available = extras.getString("crgvAvailable", null);
            crgv_phone = extras.getString("crgvPhone", null);
            crgv_bio = extras.getString("crgvBio", null);
        }

        bindActivity();

        sex_adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_type, /*android.R.layout.simple_spinner_item*/ R.layout.list_item);
        sex_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex.setAdapter(sex_adapter);

        city.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
            }
        });

        address.setAdapter(new StreetAutoCompleteAdapter(this, R.layout.list_item));
        address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
            }
        });

        birth.setOnClickListener(this);

        setupUI();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.update_profile_confirm) {
            if(name.getText().toString().equals("") || surname.getText().toString().equals("") ||
                    birth.getText().toString().equals("") || sex.getSelectedItem().toString().equals("")) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Attenzione! Uno o più campi obbligatori vuoti!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                return super.onOptionsItemSelected(item);
            }
            if(!address.getText().toString().equals("")) {
                if(city.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Indirizzo inserito: inserire anche la città.", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return super.onOptionsItemSelected(item);
                }
            }
            if(crgv_field != null) {
                if(field.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Attenzione! Campo obbligatorio \"Specializzazione\" vuoto!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return super.onOptionsItemSelected(item);
                }
            }

            settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
            credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
            Log.d(TAG, "Credential: "+credential);
            //setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));
            accountName= AppConstants.setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null), credential, this);

            if(credential.getSelectedAccountName() == null) {
                Log.d(TAG, "AccountName == null: startActivityForResult.");
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
            else
                executeAsyncTask();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * setup layout elements
     */
    private void bindActivity() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.update_profile_coordinator);
        caregiverCard = (CardView) findViewById(R.id.update_profile_crgv_card);
        userCard = (CardView) findViewById(R.id.update_profile_user_card);
        progressView = (CircularProgressView) findViewById(R.id.update_profile_progress_view);
        // USER
        name = (TextView) findViewById(R.id.update_profile_user_name);
        surname = (TextView) findViewById(R.id.update_profile_user_surname);
        pic = (ImageView) findViewById(R.id.update_profile_user_pic);
        email = (TextView) findViewById(R.id.update_profile_user_email);
        birth = (TextView) findViewById(R.id.update_profile_user_birth);
        sex = (Spinner) findViewById(R.id.update_profile_user_sex);
        city = (AutoCompleteTextView) findViewById(R.id.update_profile_user_city);
        address = (AutoCompleteTextView) findViewById(R.id.update_profile_user_address);
        personal_num = (TextView) findViewById(R.id.update_profile_user_phone);
        // CAREGIVER
        field = (TextView) findViewById(R.id.update_profile_crgv_field);
        years_exp = (TextView) findViewById(R.id.update_profile_crgv_years);
        place = (TextView) findViewById(R.id.update_profile_crgv_place);
        available = (TextView) findViewById(R.id.update_profile_crgv_avlb);
        business_num = (TextView) findViewById(R.id.update_profile_crgv_phone);
        bio = (TextView) findViewById(R.id.update_profile_crgv_bio);
    }

    /**
     * complete textviews with values taken from extras in onCreate
     */
    private void setupUI() {
        // USER
        // REQUIRED
        Picasso.with(this).load(user_pic).into(pic);
        email.setText(user_email);
        name.setText(user_name);
        surname.setText(user_surname);
        birth.setText(user_birth);
        int sex_pos = sex_adapter.getPosition(user_sex);
        sex.setSelection(sex_pos);
        // NOT REQUIRED
        if(user_city != null)
            city.setText(user_city);
        if(user_address != null)
            address.setText(user_address);
        if(user_phone != null)
            personal_num.setText(user_phone);
        // CAREGIVER
        if(crgv_field != null) {
            field.setText(crgv_field);
            // NOT REQUIRED
            if(crgv_years_exp != 0L)
                years_exp.setText(crgv_years_exp.toString());
            if(crgv_place != null)
                place.setText(crgv_place);
            if(crgv_phone != null)
                business_num.setText(crgv_phone);
            if(crgv_available != null)
                available.setText(crgv_available);
            if(crgv_bio != null)
                bio.setText(crgv_bio);
            caregiverCard.setVisibility(View.VISIBLE);
        }
        userCard.setVisibility(View.VISIBLE);
        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
    }

    /**
     * call async task to save the modifications
     */
    private void executeAsyncTask() {
        MainUpdateUserMessage message = new MainUpdateUserMessage();
        message.setName(name.getText().toString());
        message.setSurname(surname.getText().toString());
        message.setSex(sex.getSelectedItem().toString());
        //if(!city.getText().toString().equals(""))
            message.setCity(city.getText().toString());
        //if(!address.getText().toString().equals(""))
            message.setAddress(address.getText().toString());
        //if(!personal_num.getText().toString().equals(""))
            message.setPersonalNum(personal_num.getText().toString());

        if(crgv_field != null) {
            message.setField(field.getText().toString());
            //if(!years_exp.getText().toString().equals(""))
                message.setYearsExp(Long.parseLong(years_exp.getText().toString()));
            //if(!place.getText().toString().equals(""))
                message.setPlace(place.getText().toString());
            //if(!available.getText().toString().equals(""))
                message.setAvailable(available.getText().toString());
            //if(!business_num.getText().toString().equals(""))
                message.setBusinessNum(business_num.getText().toString());
            //if(!bio.getText().toString().equals(""))
                message.setBio(bio.getText().toString());
        }
        progressView.startAnimation();
        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
        if (AppConstants.checkNetwork(this)) new UpdateUserAT(this, this, coordinatorLayout, user_id, message, apiHandler).execute();
    }

    /**
     * actions to be taken when changing user birth date
     * @param v
     */
    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        switch (v.getId()) {
            case R.id.update_profile_user_birth:
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

                                birth.setText(year + "-" + monthOfYearStr + "-" + dayOfMonthStr);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
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
        Log.d(TAG, "ACCOUNT NAME: "+ accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }*/

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
                        executeAsyncTask();
                    }
                }
                break;
        }
    }

    /**
     * done from UpdateUserAT
     * @param res to cehck if is all ok
     * @param response from the server
     */
    @Override
    public void done(boolean res, MainDefaultResponseMessage response) {
        if(response != null) {
            if(res) {
                this.setResult(RESULT_OK);
                this.finish();
            }
            else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Operazione non riuscita: "+response.getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.INVISIBLE);
    }
}
