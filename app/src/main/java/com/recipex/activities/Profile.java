package com.recipex.activities;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.recipex.AppConstants;
import com.recipex.AppConstants.*;
import com.recipex.R;
import com.recipex.asynctasks.GetUserAT;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.utilities.AlertDialogManager;
import com.recipex.utilities.ConnectionDetector;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, GetUserTC {

    public static final String TAG = "PROFILE";
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private static final int REQUEST_ACCOUNT_PICKER = 2;

    private boolean mIsTheTitleVisible          = false;
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

    private Long user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindActivity();

        mAppBarLayout.addOnOffsetChangedListener(this);

        mToolbar.inflateMenu(R.menu.menu_profile);
        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        alert = new AlertDialogManager();

        Bundle extras = getIntent().getExtras();
        //user_id = (Long) extras.get("userId");
        user_id = 5705241014042624L;

        settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(this, AppConstants.AUDIENCE);
        Log.d(TAG, "Credential: "+credential);
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if(credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        else {
            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
            if (checkNetwork()) new GetUserAT(this, this, user_id, apiHandler).execute();
        }
    }

    private void bindActivity() {
        mToolbar        = (Toolbar) findViewById(R.id.main_toolbar);
        mTitle          = (TextView) findViewById(R.id.main_textview_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        mAppBarLayout   = (AppBarLayout) findViewById(R.id.main_appbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

            if(!mIsTheTitleVisible) {
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
            if(mIsTheTitleContainerVisible) {
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

    public static void startAlphaAnimation (View v, long duration, int visibility) {
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
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.commit();
                        // User is authorized
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if(checkNetwork()) new GetUserAT(this, this, user_id, apiHandler).execute();
                    }
                }
                break;
        }
    }

    @Override
    public void done(boolean res, final MainUserInfoMessage message) {
        /*
        if(res)
            Log.e(TAG, "User Info retrieved: " + message.getBirth());
        else
            Log.e(TAG, "Error.");
        */

        if(res)
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Toast.makeText(getApplicationContext(), "User Info retrived: "+ message.getBirth(), Toast.LENGTH_SHORT).show();
                }
            });
        else
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Toast.makeText(getApplicationContext(), "Errore. "+ message.getBirth(), Toast.LENGTH_SHORT).show();
                }
            });


    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: "+ accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            // Internet Connection is not present
            alert.showAlertDialog(Profile.this,
                    "Connessione Internet assente",
                    "Controlla la tua connessione internet!", false);
            // stop executing code by return
            return false;
        }
    }

}