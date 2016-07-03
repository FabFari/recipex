package com.recipex.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.recipex.taskcallbacks.GetPrescriptionsTC;

import java.io.IOException;

/**
 * Created by Sara on 08/05/2016.
 */

/**
 * gets prescriptions of user
 */
public class GetPrescriptionsUserAT extends AsyncTask<Void, Void, MainUserPrescriptionsMessage> {
    long id;
    Context mContext;
    GetPrescriptionsTC mCallback;
    RecipexServerApi apiHandler;

    public GetPrescriptionsUserAT(long id, Context context, GetPrescriptionsTC t, RecipexServerApi apiHandler){
        this.id=id;
        mContext=context;
        mCallback=t;
        this.apiHandler = apiHandler;
    }

    protected MainUserPrescriptionsMessage doInBackground(Void... unused) {

        try {
            RecipexServerApi.User.GetPrescriptions get = apiHandler.user().getPrescriptions(id);
            MainUserPrescriptionsMessage response = get.execute();

            Log.d("RESPONSE TERAPIE", response.getResponse().getMessage());
            return response;
        } catch (IOException e) {
            Looper.prepare();
            Toast.makeText(mContext, "Exception during API call! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainUserPrescriptionsMessage response) {
        if(response != null) {
            Log.d("RESPONSE TASK", "CIAO");
            mCallback.done(true, response);
        }
        else{
            Log.d("RESPONSE TASK", "NONO");
            mCallback.done(false, null);
        }

    }
}
