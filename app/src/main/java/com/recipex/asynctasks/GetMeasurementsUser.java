package com.recipex.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.TaskCallbackGetMeasurements;
import com.recipex.taskcallbacks.TaskCallbackGetTerapie;

import java.io.IOException;

/**
 * Created by Sara on 11/05/2016.
 */
public class GetMeasurementsUser extends AsyncTask<Void, Void, MainUserMeasurementsMessage> {
    long id;
    Context mContext;
    TaskCallbackGetMeasurements mCallback;

    GoogleAccountCredential credential;
    SharedPreferences settings;
    SharedPreferences pref;


    public GetMeasurementsUser(long id, Context context, TaskCallbackGetMeasurements t){
        this.id=id;
        mContext=context;
        mCallback=t;
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        settings = mContext.getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("account", accountName);
        editor.commit();
        credential.setSelectedAccountName(accountName);
    }

    protected MainUserMeasurementsMessage doInBackground(Void... unused) {

        // Retrieve service handle.
        credential = GoogleAccountCredential.usingAudience(mContext,
                AppConstants.AUDIENCE);
        pref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String email = pref.getString("email", "");
        setSelectedAccountName(email);

        RecipexServerApi apiServiceHandle = AppConstants.getApiServiceHandle(credential);

        try {
            RecipexServerApi.User.GetMeasurements get = apiServiceHandle.user().getMeasurements(id);
            MainUserMeasurementsMessage response = get.execute();

            Log.d("RESPONSE TERAPIE", response.getResponse().getMessage());
            return response;
        } catch (IOException e) {
            Looper.prepare();
            Toast.makeText(mContext, "Exception during API call! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainUserMeasurementsMessage response) {
        if(response != null) {
            Log.d("RESPONSE TASK", "CIAO");
            mCallback.done(response);
        }
        else{
            Log.d("RESPONSE TASK", "NONO");
            mCallback.done(null);
        }

    }
}
