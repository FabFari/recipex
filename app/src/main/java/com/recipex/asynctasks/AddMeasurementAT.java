package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddMeasurementMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.recipex.AppConstants;
import com.recipex.activities.AddMeasurement;
import com.recipex.taskcallbacks.AddMeasurementTC;
import com.recipex.utilities.AlertDialogManager;

import java.io.IOException;

/**
 * Created by Fabrizio on 04/05/2016.
 */
public class AddMeasurementAT extends AsyncTask<Void, Void, MainDefaultResponseMessage>{

    public static String TAG = "GET_USER_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    AddMeasurementTC taskCallback;
    Long user_id;
    MainAddMeasurementMessage content;
    AlertDialogManager alert = new AlertDialogManager();

    public AddMeasurementAT(AddMeasurementTC taskCallback, Activity activity, Long user_id,
                            MainAddMeasurementMessage content, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.user_id = user_id;
        this.content = content;
        this.apiHandler = apiHandler;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.Measurement.AddMeasurement post = apiHandler.measurement().addMeasurement(user_id, content);
            MainDefaultResponseMessage response = post.execute();
            return response;
        }
        catch(IOException e) {
            taskCallback.done(false, null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainDefaultResponseMessage response) {
        if(response != null && response.getCode().equals(AppConstants.CREATED))
            taskCallback.done(true, response);
        else {
            activity.runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  alert.showAlertDialog(activity,
                                          "Attenzione!",
                                          "Si è verificato un problema. Riprovare!", false);
                              }
            });
            taskCallback.done(false, null);
        }

    }

}
