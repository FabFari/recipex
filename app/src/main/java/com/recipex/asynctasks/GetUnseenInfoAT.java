package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserUnseenInfoMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.GetUnseenInfoTC;
import com.recipex.taskcallbacks.GetUserTC;
import com.recipex.utilities.AlertDialogManager;

import java.io.IOException;

/**
 * Created by Sara on 06/07/2016.
 */

/**
 * async task to retrieve info for the user, as for example people have removed a relation with him
 */
public class GetUnseenInfoAT extends AsyncTask<Void, Void, MainUserUnseenInfoMessage> {
    public static String TAG = "GET_USER_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    GetUnseenInfoTC taskCallback;
    Long user_id;
    AlertDialogManager alert = new AlertDialogManager();

    public GetUnseenInfoAT(GetUnseenInfoTC taskCallback, Activity activity, Long user_id, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
    }

    protected MainUserUnseenInfoMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.HasUnseenInfo get = apiHandler.user().hasUnseenInfo(user_id);
            Log.d(TAG, "Get Well formed.");
            MainUserUnseenInfoMessage response = get.execute();
            Log.d(TAG, "Get Executed.");
            return response;
        }
        catch(IOException e) {
            Log.d(TAG, "IOException: "+ e.getCause());
            Log.d(TAG, "IOException: "+ e.toString());
            taskCallback.done(null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainUserUnseenInfoMessage response) {
        if(response != null && response.getResponse().getCode().equals(AppConstants.OK))
            taskCallback.done(response);
        else {
            alert.showAlertDialog(activity,
                    "Attenzione!",
                    "Si è verificato un problema. Riprovare!", false);
            taskCallback.done(null);
        }

    }
}
