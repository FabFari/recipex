package com.recipex.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.UpdateRelationInfoTC;

import java.io.IOException;

/**
 * Created by Fabrizio on 26/05/2016.
 */

/**
 * updates or removes a relation between two users.
 */
public class UpdateRelationInfoAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    public static String TAG = "UPDATE_RELATION_INFO_AT";
    RecipexServerApi apiHandler;
    Activity activity;
    View mainView;
    UpdateRelationInfoTC taskCallback;
    Long id;
    Long relation_id;
    String kind;
    String role;

    public UpdateRelationInfoAT(Long id, Long relation_id, String kind, View mainView, Activity activity,
                                UpdateRelationInfoTC taskCallback, RecipexServerApi apiHandler) {
        this.taskCallback = taskCallback;
        this.activity = activity;
        this.mainView = mainView;
        this.id = id;
        this.relation_id = relation_id;
        this.kind = kind;
        this.apiHandler = apiHandler;
    }

    public UpdateRelationInfoAT(Long id, Long relation_id, String kind, View mainView, Activity activity,
                                UpdateRelationInfoTC taskCallback, RecipexServerApi apiHandler, String role) {
        this(id, relation_id, kind, mainView, activity, taskCallback, apiHandler);
        this.role = role;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused){

        try {
            RecipexServerApi.User.UpdateRelationInfo patch = apiHandler.user().updateRelationInfo(id, relation_id, kind);
            if(!kind.equals(AppConstants.FAMILIARE))
                patch.setRole(role);

            MainDefaultResponseMessage response = patch.execute();
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
