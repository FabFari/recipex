package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserRelationsMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.CheckUserRelationsTC;

import java.io.IOException;

/**
 * finds what is the relation with a caregiver
 */
public class CheckUserRelationsAT extends AsyncTask<Void, Void, MainUserRelationsMessage> {

    public static String TAG = "CHECK_USER_RELATION_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    CheckUserRelationsTC taskCallback;
    Long user_id;
    Long profile_id;

    public CheckUserRelationsAT(CheckUserRelationsTC taskCallback, Activity activity, View mainView, Long user_id,
                            Long profile_id, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.user_id = user_id;
        this.profile_id = profile_id;
        this.apiHandler = apiHandler;
    }

    protected MainUserRelationsMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.CheckUserRelations get = apiHandler.user().checkUserRelations(user_id, profile_id);
            MainUserRelationsMessage response = get.execute();
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
    protected void onPostExecute(MainUserRelationsMessage response) {
        if(response != null && response.getResponse().getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, response);

    }

}