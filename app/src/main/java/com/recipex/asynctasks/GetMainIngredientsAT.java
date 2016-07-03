package com.recipex.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientsMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.ActiveIngredientsTC;

import java.io.IOException;

/**
 * Created by Sara on 08/05/2016.
 */

/**
 * get list of ingredients for medicines used in prescriptions
 */
public class GetMainIngredientsAT extends AsyncTask<Void, Void, MainActiveIngredientsMessage> {
    ActiveIngredientsTC mCallback;
    Activity activity;
    RecipexServerApi apihandler;
    View view;

    public GetMainIngredientsAT(RecipexServerApi apihandler, Activity a, ActiveIngredientsTC t, View v){
        mCallback=t;
        activity=a;
        this.apihandler=apihandler;
        view=v;
    }


    protected MainActiveIngredientsMessage doInBackground(Void... unused) {

        try {
            RecipexServerApi.ActiveIngredient.GetActiveIngredients get = apihandler.activeIngredient().getActiveIngredients();
            MainActiveIngredientsMessage response = get.execute();

            return response;
        } catch (Exception e) {
            Log.d("GetMainIngredientsAT", e.getMessage());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Snackbar snackbar = Snackbar
                                    .make(view, "Operazione non riuscita!", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    });
                }
            });
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