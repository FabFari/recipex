package com.recipex.asynctasks;


import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserRequestsMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.GetUserRequestsTC;

import java.io.IOException;

/**
 * gets list of user's requests
 */
public class GetUserRequestsAT extends AsyncTask<Void, Void, MainUserRequestsMessage>{
    public static String TAG = "GET_USER_REQUESTS_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    GetUserRequestsTC taskCallback;
    Long user_id;

    public GetUserRequestsAT(GetUserRequestsTC taskCallback, Activity activity, View mainView, Long user_id,
                            RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
    }

    protected MainUserRequestsMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.GetRequests get = apiHandler.user().getRequests(user_id);
            MainUserRequestsMessage response = get.execute();
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
    protected void onPostExecute(MainUserRequestsMessage response) {
        if(response != null && response.getResponse().getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, null);

    }
}
