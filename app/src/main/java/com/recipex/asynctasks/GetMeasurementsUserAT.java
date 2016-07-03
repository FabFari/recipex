package com.recipex.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.recipex.taskcallbacks.GetMeasurementsTC;

import java.io.IOException;

/**
 * Created by Sara on 11/05/2016.
 */

/**
 * gets measurements of the user
 */
public class GetMeasurementsUserAT extends AsyncTask<Void, Void, MainUserMeasurementsMessage> {
    long id;
    Context mContext;
    GetMeasurementsTC mCallback;
    RecipexServerApi apiHandler;
    int scroll;

    long idmes;

    public GetMeasurementsUserAT(long id, Context context, GetMeasurementsTC t, RecipexServerApi handler, int scroll,
                                 long idmes){
        this.id=id;
        mContext=context;
        mCallback=t;
        apiHandler=handler;
        this.scroll=scroll;
        this.idmes=idmes;
    }

    protected MainUserMeasurementsMessage doInBackground(Void... unused) {

        try {
            RecipexServerApi.User.GetMeasurements get = apiHandler.user().getMeasurements(id);
            if(scroll!=0){
                get.setFetch((long) scroll);
                if(idmes!=0) {
                    Log.d("GetMeasurement", ""+idmes);
                    get.setMeasurementId(idmes);
                }
                get.setReverse(true);
            }
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
