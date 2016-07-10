package com.recipex;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.recipex.utilities.ConnectionDetector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import javax.annotation.Nullable;


public class AppConstants extends AppCompatActivity{
    /**
     * Your WEB CLIENT ID from the API Access screen of the Developer Console for your project. This
     * is NOT the Android client id from that screen.
     *
     * @see <a href="https://developers.google.com/console">https://developers.google.com/console</a>
     */
    public static final String WEB_CLIENT_ID = "1077668244667-v42n91q6av4tlub6rh3dffbdqa0pncj0.apps.googleusercontent.com";

    public static final String GOOGLE_SIGN_WEB_CLIENT = "1077668244667-ffc86ise0qgj4ogq4m2ev68cqjsqpqan.apps.googleusercontent.com";

    /**
     * The audience is defined by the web client id, not the Android client id.
     */
    public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

    /**
     * Constants for app
      */
    public static final String DEFAULT_ACCOUNT = "PREF_ACCOUNT_NAME";
    public static final String PREFS_NAME = "com.recipex.RecipeXPrefs";

    /**
     * code for the calendar operations
     */
    public static final int REQUEST_AUTHORIZATION = 1001;


    /**
     * Codes for server
     */

    public static final String CREATED = "201 Created";
    public static final String OK = "200 OK";
    public static final String PRECONDITION_FAILED = "412 Precondition Failed";
    public static final String BAD_REQUEST = "400 Bad Request";
    public static final String NOT_FOUND = "404 Not Found";

    // Tipi di Misurazione
    public static final String PRESSIONE = "BP";
    public static final String FREQ_CARDIACA = "HR";
    public static final String FREQ_RESPIRAZIONE = "RR";
    public static final String SPO2 = "SpO2";
    public static final String GLUCOSIO = "HGT";
    public static final String TEMP_CORPOREA = "TMP";
    public static final String DOLORE = "PAIN";
    public static final String COLESTEROLO = "CHL";

    // Tipi di Richiesta
    public static final String FAMILIARE = "RELATIVE";
    public static final String CAREGIVER = "CAREGIVER";
    public static final String MEDICO_BASE = "PC_PHYSICIAN";
    public static final String INF_DOMICILIARE = "V_NURSE";

    // Tipo di Ruolo nella Richiesta
    public static final String ASSISTITO = "PATIENT";
    public static final String ASSISTENTE = "CAREGIVER";

    // Tipo di Prescrizione
    public static final String PILLOLA = "PILL";
    public static final String BUSTINE = "SACHET";
    public static final String FIALA = "VIAL";
    public static final String CREMA = "CREAM";
    public static final String ALTRO = "OTHER";

    /**
     * Class instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


    /**
     * Retrieve a RecipexServerApi api service handle to access the API.
     */

    public static RecipexServerApi getApiServiceHandle(@Nullable GoogleAccountCredential credentials) {
        // Use a builder to help formulate the API request.
        RecipexServerApi.Builder recipexServerApi = new RecipexServerApi.Builder(AppConstants.HTTP_TRANSPORT,
                                                                           AppConstants.JSON_FACTORY,
                                                                           credentials);

        recipexServerApi.setRootUrl("https://recipex-1281.appspot.com/_ah/api");
        return recipexServerApi.build();
    }


    /**
     * Count Google accounts on the device.
     */
    public static int countGoogleAccounts(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts == null || accounts.length < 1) {
            return 0;
        } else {
            return accounts.length;
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    public static boolean isDeviceOnline(Activity a) {
        ConnectivityManager connMgr =
                (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    public static boolean isGooglePlayServicesAvailable(Activity a) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(a);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    public static void acquireGooglePlayServices(Activity a) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(a);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            AppConstants.showGooglePlayServicesAvailabilityErrorDialog(a,connectionStatusCode);
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     */
    public static boolean checkGooglePlayServicesAvailable(Activity activity) {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
            return false;
        }
        return true;
    }

    /**
     * Called if the device does not have Google Play Services installed.
     */
    public static void showGooglePlayServicesAvailabilityErrorDialog(final Activity activity,
                                                                     final int connectionStatusCode) {
        final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, activity, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    /**
     * this method checks if there is network available, to avoid calling asynctasks that require network if there is no network
     */
    public static boolean checkNetwork(final Activity a) {
        ConnectionDetector cd = new ConnectionDetector(a.getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Log.d("AppConstants", a.getWindow().getDecorView().getRootView().toString() );
            Snackbar snackbar = Snackbar
                    .make(a.getWindow().getDecorView().getRootView(), "Nessuna connessione a internet!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ESCI", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            a.finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return false;
    }

    /**
     * sets name of the account which performs the operation
     * @param accountName
     */
    public static String setSelectedAccountName(String accountName, GoogleAccountCredential credential, Activity a) {
        SharedPreferences settings = a.getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        credential.setSelectedAccountName(accountName);
        return accountName;
    }

    /**
     * returns type of measurement from its symbol
     * @param s
     * @return
     */
    public static String getTipo(String s){
        if(s.equals(PRESSIONE))
            return "Pressione arteriosa";
        else if(s.equals(FREQ_CARDIACA))
            return "Frequenza cardiaca";
        else if(s.equals(FREQ_RESPIRAZIONE))
            return "Frequenza respiratoria";
        else if(s.equals(SPO2))
            return "Ossigenazione sanguigna";
        else if(s.equals(GLUCOSIO))
            return "Glicemia";
        else if(s.equals(TEMP_CORPOREA))
            return "Temperatura corporea";
        else if(s.equals(DOLORE))
            return "Scala del dolore";
        else if(s.equals(COLESTEROLO))
            return "Colesterolo";
        return "";
    }
}
