package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.DeletePrescriptionTC;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Fabrizio on 30/05/2016.
 */

/**
 * deletes a prescription
 */
public class DeletePrescriptionAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    public static String TAG = "SEND_REQUEST_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    DeletePrescriptionTC taskCallback;
    Long id;
    Long user_id;
    ArrayList<String> idCal;

    public DeletePrescriptionAT(DeletePrescriptionTC taskCallback, Activity activity, View mainView, Long id,
                                Long user_id, ArrayList<String> idsCal, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.id = id;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
        this.idCal=idsCal;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.Prescription.DeletePrescription post = apiHandler.prescription().deletePrescription(id, user_id);
            MainDefaultResponseMessage response = post.execute();
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
            taskCallback.done(false, null, null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainDefaultResponseMessage response) {
        if(response != null && response.getCode().equals(AppConstants.OK))
            taskCallback.done(true, idCal, response);
        else
            taskCallback.done(false, null, response);

    }
}
