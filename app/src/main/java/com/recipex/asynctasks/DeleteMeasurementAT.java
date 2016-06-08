package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.DeleteMeasurementTC;

import java.io.IOException;

/**
 * Created by Fabrizio on 03/06/2016.
 */
public class DeleteMeasurementAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    public static String TAG = "SEND_REQUEST_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    DeleteMeasurementTC taskCallback;
    Long id;
    Long user_id;
    String calId;

    public DeleteMeasurementAT(DeleteMeasurementTC taskCallback, Activity activity, View mainView, Long id,
                            Long user_id, String calId, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.id = id;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
        this.calId=calId;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.Measurement.DeleteMeasurement delete = apiHandler.measurement().deleteMeasurement(id, user_id);
            MainDefaultResponseMessage response = delete.execute();
            return response;
        }
        catch(IOException e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Snackbar snackbar = Snackbar
                                    .make(mainView, "Operazione non riuscita!", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    });
                }
            });
            taskCallback.done(false, "", null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainDefaultResponseMessage response) {
        if(response != null && response.getCode().equals(AppConstants.OK))
            taskCallback.done(true, calId, response);
        else
            taskCallback.done(false, "", response);

    }
}
