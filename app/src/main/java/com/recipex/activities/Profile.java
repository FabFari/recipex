package com.recipex.activities;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainRequestSendMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserRelationsMessage;
import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.recipex.AppConstants;
import com.recipex.AppConstants.*;
import com.recipex.R;
import com.recipex.adapters.ContactAdapter;
import com.recipex.asynctasks.CheckUserRelationsAT;
import com.recipex.asynctasks.GetUserAT;
import com.recipex.asynctasks.GetUserRequestsAT;
import com.recipex.asynctasks.SendRequestAT;
import com.recipex.fragments.ContactFragment;
import com.recipex.taskcallbacks.CheckUserRelationsTC;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.taskcallbacks.SendRequestTC;
import com.recipex.taskcallbacks.TaskCallbackCalendar;
import com.recipex.utilities.AlertDialogManager;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.ContactItem;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Credentials;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Profile extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, GetUserTC, CheckUserRelationsTC,
        SendRequestTC, View.OnClickListener, TaskCallbackCalendar, EasyPermissions.PermissionCallbacks {

    public static final String TAG = "PROFILE";
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private static final int EDIT_PROFILE = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int ADD_THERAPY = 3;


    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;

    private ConnectionDetector cd;
    private AlertDialogManager alert;

    // UI STUFF
    private CoordinatorLayout coordinatorLayout;
    private CardView caregiverCard;
    private CardView userCard;
    private TextView tolbar_usr_name;
    private TextView crgv_subtitle;
    private CircularProgressView progressView;
    //private FloatingActionButton fab;

    // As a visitor
    // TODO Aggiungere un fab per aggiungere paziente se caregiver
    private FloatingActionMenu fab_menu;
    private com.github.clans.fab.FloatingActionButton fab_relatives;
    private com.github.clans.fab.FloatingActionButton fab_pc_physician;
    private com.github.clans.fab.FloatingActionButton fab_visiting_nurse;
    private com.github.clans.fab.FloatingActionButton fab_caregivers;

    // USER
    // Required
    private TextView name;
    private TextView surname;
    private CircleImageView pic;
    private TextView email;
    private TextView birth;
    private TextView sex;
    // Not Required
    private TextView city_lbl;
    private TextView city;
    private TextView address_lbl;
    private TextView address;
    private TextView personal_num_lbl;
    private TextView personal_num;
    // CAREGIVER
    // Required
    private TextView field;
    private TextView years_exp_lbl;
    private TextView years_exp;
    private TextView place_lbl;
    private TextView place;
    private TextView available_lbl;
    private TextView available;
    private TextView business_num_lbl;
    private TextView business_num;
    private TextView bio_lbl;
    private CardView bio_card;
    private TextView bio;

    SharedPreferences myPrefs;

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

    private boolean relations_checked = false;
    private boolean is_relative = false;
    private boolean is_pc_physician = false;
    private boolean is_visiting_nurse = false;
    private boolean is_caregiver = false;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private int fabPressed;
    private boolean utente_semplice;

    //per calendario
    GoogleAccountCredential mCredential;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int REQUEST_ACCOUNT_PICKER_CALENDAR = 1000;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private static final String PREF_ACCOUNT_NAME = "accountName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindActivity();

        mAppBarLayout.addOnOffsetChangedListener(this);

        myPrefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        user_id = myPrefs.getLong("userId", 0L);
        utente_semplice = myPrefs.getBoolean("utenteSemplice", true);
        //user_id = 5705241014042624L;
        //user_id = 5724160613416960L;

        Bundle extras = getIntent().getExtras();
        //profile_id = 5724160613416960L;
        profile_id = extras.getLong("profileId", 0L);
        Log.d(TAG, "profile_id: " + profile_id);

        if(user_id.equals(profile_id))
            fab_menu.setVisibility(View.GONE);

        //mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        Log.d(TAG, "Credential: " + credential);
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if (credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            progressView.startAnimation();
            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            if (checkNetwork()) {
                new GetUserAT(this, this, profile_id, apiHandler).execute();
                new CheckUserRelationsAT(this, this, coordinatorLayout, user_id, profile_id, apiHandler).execute();
            }
        }

    }

    private void bindActivity() {
        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        mTitle = (TextView) findViewById(R.id.profile_textview_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.profile_linearlayout_title);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.profile_appbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.profile_coordinator);
        caregiverCard = (CardView) findViewById(R.id.card_view_caregiver);
        userCard = (CardView) findViewById(R.id.card_view_user);
        tolbar_usr_name = (TextView) findViewById(R.id.profile_bar_usr_name);
        progressView = (CircularProgressView) findViewById(R.id.profile_progress_view);
        //fab = (FloatingActionButton) findViewById(R.id.profile_fab);
        crgv_subtitle = (TextView) findViewById(R.id.profile_crgv_subtitle);
        // USER
        name = (TextView) findViewById(R.id.profile_usr_name);
        surname = (TextView) findViewById(R.id.profile_usr_surname);
        pic = (CircleImageView) findViewById(R.id.profile_usr_pic);
        email = (TextView) findViewById(R.id.profile_usr_mail);
        birth = (TextView) findViewById(R.id.profile_usr_birth);
        sex = (TextView) findViewById(R.id.profile_usr_sex);
        city_lbl = (TextView) findViewById(R.id.profile_usr_city_lbl);
        city = (TextView) findViewById(R.id.profile_usr_city);
        address_lbl = (TextView) findViewById(R.id.profile_usr_address_lbl);
        address = (TextView) findViewById(R.id.profile_usr_address);
        personal_num_lbl = (TextView) findViewById(R.id.profile_usr_pers_num_lbl);
        personal_num = (TextView) findViewById(R.id.profile_usr_pers_num);
        // CAREGIVER
        field = (TextView) findViewById(R.id.profile_crgv_field);
        years_exp_lbl = (TextView) findViewById(R.id.profile_crgv_exp_lbl);
        years_exp = (TextView) findViewById(R.id.profile_crgv_exp);
        place_lbl = (TextView) findViewById(R.id.profile_crgv_place_lbl);
        place = (TextView) findViewById(R.id.profile_crgv_place);
        available_lbl = (TextView) findViewById(R.id.profile_crgv_avlb_lbl);
        available = (TextView) findViewById(R.id.profile_crgv_avlb);
        business_num_lbl = (TextView) findViewById(R.id.profile_crgv_bsns_num_lbl);
        business_num = (TextView) findViewById(R.id.profile_crgv_bsns_num);
        bio_lbl = (TextView) findViewById(R.id.profile_crgv_bio_lbl);
        bio_card = (CardView) findViewById(R.id.profile_card_view_crgv_bio);
        bio = (TextView) findViewById(R.id.profile_crgv_bio);
        // VISITOR
        fab_menu = (FloatingActionMenu) findViewById(R.id.profile_fab_menu_requests);
        fab_relatives = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item1);
        fab_pc_physician = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item2);
        fab_pc_physician.setEnabled(false);
        fab_visiting_nurse = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item3);
        fab_visiting_nurse.setEnabled(false);
        fab_caregivers = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item4);
        fab_caregivers.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (profile_id == null) {
            Bundle extras = getIntent().getExtras();
            profile_id = extras.getLong("profileId", 0L);
        }

        if (user_id.equals(profile_id))
            mToolbar.inflateMenu(R.menu.menu_profile);
        else
            mToolbar.inflateMenu(R.menu.menu_profile_visitor);
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);

    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "Nell'if.");
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
                        if (checkNetwork()) {
                            new GetUserAT(this, this, user_id, apiHandler).execute();
                            new CheckUserRelationsAT(this, this, coordinatorLayout, user_id, profile_id, apiHandler).execute();
                        }
                    }
                }
                break;
            case EDIT_PROFILE:
                if(resultCode == RESULT_OK) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Profilo Aggiornato con successo", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
                    credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);

                    setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

                    if (credential.getSelectedAccountName() == null)
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    else {
                        Log.d(TAG, "Nell'else.");
                        progressView.startAnimation();
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (checkNetwork())
                            new GetUserAT(this, this, profile_id, apiHandler).execute();
                    }
                }
                break;
            case ADD_THERAPY:
                if(resultCode == RESULT_OK) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Terapia aggiunta con successo!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(Profile.this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER_CALENDAR:
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

    @Override
    public void done(boolean res, final MainUserInfoMessage message) {
        /*
        if(res)
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    //Toast.makeText(getApplicationContext(), "User Info retrived: "+ message.getBirth(), Toast.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "User Info retrived: "+ message.getBirth(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        else
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    //Toast.makeText(getApplicationContext(), "Errore. "+ message.getBirth(), Toast.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Errore", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        */

        // USER
        // Required
        user_pic = message.getPic();
        Picasso.with(this).load(message.getPic()).into(pic);
        pic.setVisibility(View.VISIBLE);
        tolbar_usr_name.setText(String.format("%s %s", message.getName(), message.getSurname()));
        tolbar_usr_name.setVisibility(View.VISIBLE);
        mTitle.setText(String.format("%s %s", message.getName(), message.getSurname()));
        mTitle.setVisibility(View.VISIBLE);
        user_name = message.getName();
        name.setText(message.getName());
        user_surname = message.getSurname();
        surname.setText(message.getSurname());
        user_email = message.getEmail();
        email.setText(message.getEmail());
        user_birth = message.getBirth();
        birth.setText(message.getBirth());
        user_sex = message.getSex();
        sex.setText(message.getSex());
        // Not Required
        if (message.getCity() != null) {
            user_city = message.getCity();
            city.setText(message.getCity());
            city_lbl.setVisibility(View.VISIBLE);
            city.setVisibility(View.VISIBLE);
        }
        else {
            city_lbl.setVisibility(View.GONE);
            city.setVisibility(View.GONE);
        }
        if (message.getAddress() != null) {
            user_address = message.getAddress();
            address.setText(message.getAddress());
            address_lbl.setVisibility(View.VISIBLE);
            address.setVisibility(View.VISIBLE);
        }
        else {
            address_lbl.setVisibility(View.GONE);
            address.setVisibility(View.GONE);
        }
        if (message.getPersonalNum() != null) {
            user_phone = message.getPersonalNum();
            personal_num.setText(message.getPersonalNum());
            personal_num_lbl.setVisibility(View.VISIBLE);
            personal_num.setVisibility(View.VISIBLE);
        }
        else {
            personal_num_lbl.setVisibility(View.GONE);
            personal_num.setVisibility(View.GONE);
        }
        userCard.setVisibility(View.VISIBLE);
        // CAREGIVER
        // Required
        if (message.getField() != null) {
            if(!relations_checked) {
                fab_pc_physician.setEnabled(true);
                fab_visiting_nurse.setEnabled(true);
                fab_caregivers.setEnabled(true);
            }
            else {
                if(!is_pc_physician)
                    fab_pc_physician.setEnabled(true);
                if(!is_visiting_nurse)
                    fab_visiting_nurse.setEnabled(true);
                if(!is_caregiver)
                    fab_caregivers.setEnabled(true);
            }
            crgv_field = message.getField();
            field.setText(message.getField());
            crgv_subtitle.setVisibility(View.VISIBLE);

            if (message.getYearsExp() != null) {
                crgv_years_exp = message.getYearsExp();
                // years_exp.setText(String.format(Locale.getDefault(), "%d", message.getYearsExp()));
                if (message.getYearsExp() > 20)
                    years_exp.setText("Più di 20 anni");
                else if (message.getYearsExp() > 10)
                    years_exp.setText("Trai 10 e i 20 anni");
                else if (message.getYearsExp() > 5)
                    years_exp.setText("Trai 5 e i 10 anni");
                else
                    years_exp.setText("Meno di 5 anni");
                years_exp_lbl.setVisibility(View.VISIBLE);
                years_exp.setVisibility(View.VISIBLE);
            }
            else {
                years_exp_lbl.setVisibility(View.GONE);
                years_exp.setVisibility(View.GONE);
            }
            if (message.getPlace() != null) {
                crgv_place = message.getPlace();
                place.setText(message.getPlace());
                place_lbl.setVisibility(View.VISIBLE);
                place.setVisibility(View.VISIBLE);
            }
            else {
                place_lbl.setVisibility(View.GONE);
                place.setVisibility(View.GONE);
            }
            if (message.getAvailable() != null) {
                crgv_available = message.getAvailable();
                available.setText(message.getAvailable());
                available_lbl.setVisibility(View.VISIBLE);
                available.setVisibility(View.VISIBLE);
            }
            else {
                available_lbl.setVisibility(View.GONE);
                available.setVisibility(View.GONE);
            }
            if (message.getBusinessNum() != null) {
                crgv_phone = message.getBusinessNum();
                business_num.setText(message.getBusinessNum());
                business_num_lbl.setVisibility(View.VISIBLE);
                business_num.setVisibility(View.VISIBLE);
            }
            else {
                business_num_lbl.setVisibility(View.GONE);
                business_num.setVisibility(View.GONE);
            }
            if (message.getBio() != null) {
                crgv_bio = message.getBio();
                bio.setText(message.getBio());
                bio_lbl.setVisibility(View.VISIBLE);
                bio.setVisibility(View.VISIBLE);
                bio_card.setVisibility(View.VISIBLE);
            }
            else {
                bio_lbl.setVisibility(View.GONE);
                bio.setVisibility(View.GONE);
                bio_card.setVisibility(View.GONE);
            }
            caregiverCard.setVisibility(View.VISIBLE);
        }

        //if(!user_id.equals(profile_id))
        //fab.setVisibility(View.VISIBLE);

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);

    }

    @Override
    public void done(boolean res, final MainUserRelationsMessage response) {
        if (response != null) {
            if (res) {
                boolean can_contact = false;
                boolean can_add_therapy = false;
                Log.d(TAG, "RESPONSE: " + response);
                if (response.getIsRelative()) {
                    fab_relatives.setEnabled(false);
                    if (!response.getIsRelativeRequest()) {
                        can_contact = true;
                        is_relative = true;
                    }
                }
                else
                    fab_relatives.setOnClickListener(this);

                if (response.getIsPcPhysician()) {
                    fab_pc_physician.setEnabled(false);
                    if (!response.getIsPcPhysicianRequest()) {
                        can_contact = true;
                        is_pc_physician = true;
                        can_add_therapy = true;
                    }
                }
                else
                    fab_pc_physician.setOnClickListener(this);

                if (response.getIsVisitingNurse()) {
                    fab_visiting_nurse.setEnabled(false);
                    if (!response.getIsVisitingNurseRequest()) {
                        can_contact = true;
                        is_visiting_nurse = true;
                        can_add_therapy = true;
                    }
                }
                else
                    fab_visiting_nurse.setOnClickListener(this);

                if (response.getIsCaregiver()) {
                    fab_caregivers.setEnabled(false);
                    if (!response.getIsCaregiverRequest()) {
                        can_contact = true;
                        is_caregiver = true;
                        can_add_therapy = true;
                    }
                }
                else
                    fab_caregivers.setOnClickListener(this);

                if (can_contact)
                    mToolbar.getMenu().findItem(R.id.profile_contact).setVisible(true);

                Log.d(TAG, "utente semplice: "+ utente_semplice);

                if(can_add_therapy && !utente_semplice)
                    mToolbar.getMenu().findItem(R.id.profile_add_therapy).setVisible(true);
            }
            else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Operazione non riuscita: " + response.getResponse().getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        relations_checked = true;
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
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
    public void onClick(View v) {
        MainRequestSendMessage content = new MainRequestSendMessage();
        content.setSender(user_id);
        SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);
        fabPressed = v.getId();
        // TODO Aggiungere Dialog per inserimento messaggio
        switch (v.getId()) {
            case R.id.profile_fab_menu_item1:
                content.setKind(AppConstants.FAMILIARE);
                break;
            case R.id.profile_fab_menu_item2:
                content.setKind(AppConstants.MEDICO_BASE);
                content.setRole(AppConstants.ASSISTITO);
                break;
            case R.id.profile_fab_menu_item3:
                content.setKind(AppConstants.INF_DOMICILIARE);
                content.setRole(AppConstants.ASSISTITO);
                break;
            case R.id.profile_fab_menu_item4:
                content.setKind(AppConstants.CAREGIVER);
                content.setRole(AppConstants.ASSISTITO);
                break;
        }

        progressView.startAnimation();
        progressView.setVisibility(View.VISIBLE);
        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
        if (checkNetwork())
            new SendRequestAT(this, this, coordinatorLayout, profile_id, content, apiHandler).execute();

    }

    @Override
    public void done(boolean resp, MainDefaultResponseMessage response) {
        if (response != null) {
            if (resp) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Richiesta inviata con successo!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                com.github.clans.fab.FloatingActionButton fab_pressed = (com.github.clans.fab.FloatingActionButton)
                        findViewById(fabPressed);
                fab_pressed.setEnabled(false);
                SharedPreferences pref=getSharedPreferences("MyPref", MODE_PRIVATE);

                //aggiungo caregiver al mio calendario
                if(!pref.getString("calendar", "").equals("")) {
                    mCredential = GoogleAccountCredential.usingOAuth2(
                            getApplicationContext(), Arrays.asList(SCOPES))
                            .setBackOff(new ExponentialBackOff());
                    Log.d(TAG, "Inizio calendario");
                    getResultsFromApi();
                }
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Operazione non riuscita: " + response.getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        progressView.stopAnimation();
        progressView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.profile_user_edit:
                Intent intent = new Intent(Profile.this, UpdateProfile.class);
                // User
                // Required
                intent.putExtra("userId", user_id);
                intent.putExtra("userPic", user_pic);
                intent.putExtra("userName", user_name);
                intent.putExtra("userSurname", user_surname);
                intent.putExtra("userEmail", user_email);
                intent.putExtra("userBirth", user_birth);
                intent.putExtra("userSex", user_sex);
                // Not Required
                if (user_city != null)
                    intent.putExtra("userCity", user_city);
                if (user_address != null)
                    intent.putExtra("userAddress", user_address);
                if (user_phone != null)
                    intent.putExtra("userPhone", user_phone);
                // Caregiver
                if (crgv_field != null) {
                    intent.putExtra("crgvField", crgv_field);
                    if (crgv_years_exp != null)
                        intent.putExtra("crgvYears", crgv_years_exp);
                    if (crgv_place != null)
                        intent.putExtra("crgvPlace", crgv_place);
                    if (crgv_available != null)
                        intent.putExtra("crgvAvailable", crgv_available);
                    if (crgv_bio != null)
                        intent.putExtra("crgvBio", crgv_bio);
                }
                this.startActivityForResult(intent, EDIT_PROFILE);
            break;
            case R.id.profile_contact:
                final AlertDialog.Builder contactDialog = new AlertDialog.Builder(Profile.this);
                final LayoutInflater inflater = getLayoutInflater();
                final View convertView = (View) inflater.inflate(R.layout.contact_dialog, null);
                contactDialog.setView(convertView);

                List<ContactItem> myList = new ArrayList<ContactItem>();
                myList.add(new ContactItem(R.drawable.ic_email, "E-mail"));
                if(user_phone != null || crgv_phone != null)
                    myList.add(new ContactItem(R.drawable.ic_phone, "Telefono"));
                else
                    myList.add(new ContactItem(R.drawable.ic_phone_off, "Telefono"));
                if(user_phone != null || crgv_phone != null)
                    myList.add(new ContactItem(R.drawable.ic_sms, "SMS"));
                else
                    myList.add(new ContactItem(R.drawable.ic_sms_off, "SMS"));

                final ContactAdapter contactAdapter = new ContactAdapter(myList, this, user_email, user_phone, crgv_phone);
                final RecyclerView contactRecycler = (RecyclerView) convertView.findViewById(R.id.contact_recyclerview);
                final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                contactRecycler.setLayoutManager(layoutManager);
                contactRecycler.setHasFixedSize(true);
                contactRecycler.setAdapter(contactAdapter);

                contactDialog.create();
                contactDialog.show();
                /*
                Bundle bundle = new Bundle();
                bundle.putString("user_mail", user_email);
                if(user_phone != null)
                    bundle.putString("user_phone", user_phone);
                if(crgv_phone != null)
                    bundle.putString("crgv_phone", crgv_phone);
                mFragmentManager = getSupportFragmentManager();
                ContactFragment dialogFragment = new ContactFragment();
                dialogFragment.setArguments(bundle);
                dialogFragment.show(mFragmentManager, "Contact fragment");
                break;
                */
                break;
            case R.id.profile_add_therapy:
                Intent addTerapyIntent = new Intent(Profile.this, AggiungiTerapia.class);
                addTerapyIntent.putExtra("caregiverId", user_id);
                this.startActivityForResult(addTerapyIntent, ADD_THERAPY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("CALENDARgetres", "account");
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(Profile.this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("CALENDARgetres", "task");
            new AggiungiCaregiverCalendar(mCredential, getApplicationContext(), this,
                    getSharedPreferences("MyPref", MODE_PRIVATE).getString("calendar",""), email.getText().toString()).execute();
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
                        REQUEST_ACCOUNT_PICKER_CALENDAR);
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
                Profile.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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

    //callback from Calendar
    public void done(boolean b){
        if(b){
            Log.d(TAG, "DONE_TASKCALLBACK_CALENDAR");
        }
    }


    private class AggiungiCaregiverCalendar extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private TaskCallbackCalendar mCallback;
        private String idCalendar;
        private String emailCaregiver;

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public AggiungiCaregiverCalendar(GoogleAccountCredential credential, Context context, TaskCallbackCalendar c,
                                         String id, String e) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("RecipeX")
                    .build();
            this.context=context;
            this.mCallback=c;
            this.idCalendar=id;
            this.emailCaregiver=e;
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

            try {
                Log.d(TAG, idCalendar);
                // Create access rule with associated scope
                AclRule rule = new AclRule();
                AclRule.Scope scope = new AclRule.Scope();
                scope.setType("user").setValue(emailCaregiver);
                rule.setScope(scope).setRole("writer");

                // Insert new access rule
                AclRule createdRule = mService.acl().insert(idCalendar, rule).execute();
                System.out.println(createdRule.getId());
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                //Log.d("ECCEZIONE CALENDAR", e.getCause().toString());
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