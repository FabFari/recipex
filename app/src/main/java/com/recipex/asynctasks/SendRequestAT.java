package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainRequestSendMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.SendRequestTC;

import java.io.IOException;

/**
 * sends a request to a caregiver
 */
public class SendRequestAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {

    public static String TAG = "SEND_REQUEST_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    SendRequestTC taskCallback;
    Long user_id;
    MainRequestSendMessage content;

    public SendRequestAT(SendRequestTC taskCallback, Activity activity, View mainView, Long user_id,
                                MainRequestSendMessage content, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.user_id = user_id;
        this.content = content;
        this.apiHandler = apiHandler;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.Request.SendRequest post = apiHandler.request().sendRequest(user_id, content);
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
        if(response != null && response.getCode().equals(AppConstants.CREATED))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, response);

    }

}
