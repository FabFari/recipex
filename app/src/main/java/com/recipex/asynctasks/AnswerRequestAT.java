package com.recipex.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.AnswerRequestTC;

import java.io.IOException;
import java.util.Arrays;

/**
 * answers user's request
 */
public class AnswerRequestAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    public static String TAG = "SEND_REQUEST_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    AnswerRequestTC taskCallback;
    Long user_id;
    Long request_id;
    Boolean answer;

    public AnswerRequestAT (AnswerRequestTC taskCallback, Activity activity, View mainView, Long user_id,
                         Long request_id, Boolean answer, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.user_id = user_id;
        this.request_id = request_id;
        this.answer = answer;
        this.apiHandler = apiHandler;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.Request.AnswerRequest put = apiHandler.request().answerRequest(user_id, request_id, answer);
            MainDefaultResponseMessage response = put.execute();

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
        if(response != null && response.getCode().equals(AppConstants.OK)) {
            taskCallback.done(true, response);
        }
        else
            taskCallback.done(false, response);

    }
}
