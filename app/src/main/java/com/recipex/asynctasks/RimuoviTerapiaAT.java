package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.RimuoviTerapiaTC;
import com.recipex.taskcallbacks.SendRequestTC;

import java.io.IOException;

/**
 * Created by Fabrizio on 30/05/2016.
 */
public class RimuoviTerapiaAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    public static String TAG = "SEND_REQUEST_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    RimuoviTerapiaTC taskCallback;
    Long id;
    Long user_id;

    public RimuoviTerapiaAT(RimuoviTerapiaTC taskCallback, Activity activity, View mainView, Long id,
                            Long user_id, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.id = id;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
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
            taskCallback.done(false, null);
        }

        return null;
    }

    @Override
    protected void onPostExecute(MainDefaultResponseMessage response) {
        if(response != null && response.getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, response);

    }
}
