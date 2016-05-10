package com.recipex.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientsMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.TaskCallbackActiveIngredients;
import com.recipex.taskcallbacks.TaskCallbackGetTerapie;

import java.io.IOException;

/**
 * Created by Sara on 08/05/2016.
 */
public class GetMainIngredientsAT extends AsyncTask<Void, Void, MainActiveIngredientsMessage> {
    Context mContext;
    TaskCallbackActiveIngredients mCallback;

    GoogleAccountCredential credential;
    SharedPreferences settings;
    SharedPreferences pref;


    public GetMainIngredientsAT(Context context, TaskCallbackActiveIngredients t){
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

    protected MainActiveIngredientsMessage doInBackground(Void... unused) {

        // Retrieve service handle.
        credential = GoogleAccountCredential.usingAudience(mContext,
                AppConstants.AUDIENCE);
        pref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String email = pref.getString("email", "");
        setSelectedAccountName(email);

        RecipexServerApi apiServiceHandle = AppConstants.getApiServiceHandle(credential);

        try {
            RecipexServerApi.ActiveIngredient.GetActiveIngredients get = apiServiceHandle.activeIngredient().getActiveIngredients();
            MainActiveIngredientsMessage response = get.execute();

            return response;
        } catch (IOException e) {
            Looper.prepare();
            Toast.makeText(mContext, "Exception during API call! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainActiveIngredientsMessage response) {
        if(response != null)
            mCallback.done(response);
        else
            mCallback.done(null);

    }
}