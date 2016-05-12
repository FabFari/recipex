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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.recipex.AppConstants;
import com.recipex.AppConstants.*;
import com.recipex.R;
import com.recipex.adapters.ContactAdapter;
import com.recipex.asynctasks.CheckUserRelationsAT;
import com.recipex.asynctasks.GetUserAT;
import com.recipex.asynctasks.SendRequestAT;
import com.recipex.fragments.ContactFragment;
import com.recipex.taskcallbacks.CheckUserRelationsTC;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.taskcallbacks.SendRequestTC;
import com.recipex.utilities.AlertDialogManager;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.ContactItem;
import com.squareup.picasso.Picasso;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, GetUserTC, CheckUserRelationsTC,
        SendRequestTC, View.OnClickListener {

    public static final String TAG = "PROFILE";
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private static final int EDIT_PROFILE = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;

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

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindActivity();

        mAppBarLayout.addOnOffsetChangedListener(this);

        myPrefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        user_id = myPrefs.getLong("userId", 0L);
        //user_id = 5705241014042624L;
        //user_id = 5724160613416960L;

        Bundle extras = getIntent().getExtras();
        //profile_id = 5724160613416960L;
        profile_id = extras.getLong("profileId", 0L);
        Log.d(TAG, "profile_id: " + profile_id);

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
        fab_relatives = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item1);
        fab_pc_physician = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item2);
        fab_visiting_nurse = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item3);
        fab_caregivers = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.profile_fab_menu_item4);
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
            mToolbar.inflateMenu(R.menu.menu_main);
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
                Log.d(TAG, "RESPONSE: " + response);
                if (response.getIsRelative()) {
                    fab_relatives.setEnabled(false);
                    if (!response.getIsRelativeRequest())
                        can_contact = true;
                }
                else
                    fab_relatives.setOnClickListener(this);
                if (response.getIsPcPhysician()) {
                    fab_pc_physician.setEnabled(false);
                    if (!response.getIsPcPhysicianRequest())
                        can_contact = true;
                }
                else
                    fab_pc_physician.setOnClickListener(this);
                if (response.getIsVisitingNurse()) {
                    fab_visiting_nurse.setEnabled(false);
                    if (!response.getIsVisitingNurseRequest())
                        can_contact = true;
                }
                else
                    fab_visiting_nurse.setOnClickListener(this);
                if (response.getIsCaregiver()) {
                    fab_caregivers.setEnabled(false);
                    if (!response.getIsCaregiverRequest())
                        can_contact = true;
                }
                else
                    fab_caregivers.setOnClickListener(this);

                if (can_contact)
                    mToolbar.inflateMenu(R.menu.menu_profile_visitor);
            }
            else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Operazione non riuscita: " + response.getResponse().getCode(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
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
                        .make(coordinatorLayout, "Richiesta inviata: " + response.getPayload(), Snackbar.LENGTH_SHORT);
                snackbar.show();
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
}