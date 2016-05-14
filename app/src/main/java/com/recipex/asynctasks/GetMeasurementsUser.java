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
    RecipexServerApi apiHandler;


    public GetMeasurementsUser(long id, Context context, TaskCallbackGetMeasurements t, RecipexServerApi handler){
        this.id=id;
        mContext=context;
        mCallback=t;
        apiHandler=handler;
    }

    protected MainUserMeasurementsMessage doInBackground(Void... unused) {

        try {
            RecipexServerApi.User.GetMeasurements get = apiHandler.user().getMeasurements(id);
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
