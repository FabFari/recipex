package com.recipex.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddPrescriptionMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.AddPrescriptionTC;

import java.util.List;

/**
 * Created by Sara on 03/05/2016.
 */

/**
 * adds a prescription to the user
 */
public class AddPrescriptionAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    RecipexServerApi apihandler;
    MainAddPrescriptionMessage reg;
    Long id;
    AddPrescriptionTC mCallback;


    public AddPrescriptionAT(Long id, RecipexServerApi apihandler, MainAddPrescriptionMessage reg, AddPrescriptionTC mCallback)
    {
        this.apihandler=apihandler;
        this.reg=reg;
        this.id=id;
        this.mCallback = mCallback;
    }


    protected MainDefaultResponseMessage doInBackground(Void... unused) {

        try {
            Log.d("AggiungiAT", "pre execute");

            RecipexServerApi.Prescription.AddPrescription post = apihandler.prescription().addPrescription(id, reg);

            MainDefaultResponseMessage response = post.execute();
            return response;
        } catch (Exception e) {
            Log.d("AggiungiAT", e.getMessage());
        }
        return null;
    }


    protected void onPostExecute(MainDefaultResponseMessage response) {
        if(response != null && response.getCode().equals(AppConstants.CREATED))
            mCallback.done(true, response);
        else
            mCallback.done(false, null);

    }
}