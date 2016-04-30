package com.recipex.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.utilities.AlertDialogManager;

import java.io.IOException;

public class GetUserAT extends AsyncTask<Void, Void, MainUserInfoMessage> {
    public static String TAG = "GETUSERAT";
    RecipexServerApi apiHandler;
    Activity activity;
    GetUserTC taskCallback;
    Long user_id;
    AlertDialogManager alert = new AlertDialogManager();

    public GetUserAT(GetUserTC taskCallback, Activity activity, Long user_id, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
    }

    protected MainUserInfoMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.GetUser get = apiHandler.user().getUser(user_id);
            Log.d(TAG, "Get Well formed.");
            MainUserInfoMessage response = get.execute();
            Log.d(TAG, "Get Executed.");
            return response;
        }
        catch(IOException e) {
            Log.d(TAG, "IOException: "+ e.getCause());
            Log.d(TAG, "IOException: "+ e.toString());
            taskCallback.done(false, null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainUserInfoMessage response) {
        if(response != null && response.getResponse().getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else {
            alert.showAlertDialog(activity,
                    "Attenzione!",
                    "Si è verificato un problema. Riprovare!", false);
            taskCallback.done(false, null);
        }

    }
}