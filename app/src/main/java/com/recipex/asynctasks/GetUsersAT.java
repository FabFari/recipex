package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserListOfUsersMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.GetUserRequestsTC;
import com.recipex.taskcallbacks.GetUsersTC;

import java.io.IOException;

public class GetUsersAT extends AsyncTask<Void, Void, MainUserListOfUsersMessage>{
    public static String TAG = "GET_USERS_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    GetUsersTC taskCallback;

    public GetUsersAT(GetUsersTC taskCallback, Activity activity, View mainView, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.apiHandler = apiHandler;
    }

    protected MainUserListOfUsersMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.GetUsers get = apiHandler.user().getUsers();
            MainUserListOfUsersMessage response = get.execute();
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
    protected void onPostExecute(MainUserListOfUsersMessage response) {
        if(response != null && response.getResponse().getCode().equals(AppConstants.OK))
            taskCallback.done(true, response);
        else
            taskCallback.done(false, null);

    }
}
