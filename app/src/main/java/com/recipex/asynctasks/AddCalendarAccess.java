package com.recipex.asynctasks;

/**
 * Created by Sara on 04/07/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.AclRule;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.CalendarTC;

/**
 * Async task to give caregiver permission to see my calendar
 */
public class AddCalendarAccess extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private CalendarTC mCallback;
    private String idCalendar;
    private String emailCaregiver;

    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;

    public AddCalendarAccess(GoogleAccountCredential credential, Context context, CalendarTC c,
                                     String id, String e) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("RecipeX")
                .build();
        this.context=context;
        this.mCallback=c;
        this.idCalendar=id;
        this.emailCaregiver=e;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        try {
            Log.d("AddCalendarAccess", idCalendar);
            // Create access rule with associated scope
            AclRule rule = new AclRule();
            AclRule.Scope scope = new AclRule.Scope();
            scope.setType("user").setValue(emailCaregiver);
            rule.setScope(scope).setRole("writer");

            // Insert new access rule
            AclRule createdRule = mService.acl().insert(idCalendar, rule).execute();
            System.out.println(createdRule.getId());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            mLastError = e;
            cancel(true);
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean response) {
        if(response){
            mCallback.done(response);
        }
        else{
            Toast.makeText(context, "Operazione non riuscita", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                AppConstants.showGooglePlayServicesAvailabilityErrorDialog((Activity)context,
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                ((Activity)context).startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        AppConstants.REQUEST_AUTHORIZATION);
            } else {
                Toast.makeText(context, "The following error occurred:\n"
                        + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("ERRORE CALENDAR", mLastError.getMessage());
            }
        } else {
            Toast.makeText(context, "Request cancelled.", Toast.LENGTH_SHORT).show();
        }
    }
}
