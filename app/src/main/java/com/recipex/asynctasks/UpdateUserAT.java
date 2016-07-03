package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUpdateUserMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.UpdateUserTC;

import java.io.IOException;

/**
 * updates user's info.
 */
public class UpdateUserAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {

    public static String TAG = "GET_USER_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    UpdateUserTC taskCallback;
    Long user_id;
    MainUpdateUserMessage content;

    public UpdateUserAT(UpdateUserTC taskCallback, Activity activity, View mainView, Long user_id,
                            MainUpdateUserMessage content, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.user_id = user_id;
        this.content = content;
        this.apiHandler = apiHandler;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.UpdateUser put = apiHandler.user().updateUser(user_id, content);
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
        if(response != null && response.getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, null);

    }

}

