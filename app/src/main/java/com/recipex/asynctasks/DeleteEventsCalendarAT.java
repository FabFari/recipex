package com.recipex.asynctasks;

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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.recipex.AppConstants;
import com.recipex.activities.AddMeasurement;
import com.recipex.activities.AddPrescription;
import com.recipex.taskcallbacks.CalendarDeleteTC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sara on 07/06/2016.
 */

/**
 * deletes event from the user's calendar
 */
public class DeleteEventsCalendarAT extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private CalendarDeleteTC mCallback;

    private final static String TAG = "ELIMINA_EVENTO";

    //per inserire gli id degli eventi che creo sul calendario (vuota se non aggiungo eventi)
    private ArrayList<String> idEventiCalendar;

    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;

    public DeleteEventsCalendarAT(GoogleAccountCredential credential, Context context, CalendarDeleteTC c,
                                  ArrayList<String> idEventi) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("RecipeX")
                .build();
        this.context=context;
        this.mCallback=c;
        idEventiCalendar=idEventi;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        try {
            String idCalendar=pref.getString("calendar", "");
            Log.d(TAG, "calendario"+idCalendar);
            Events events = mService.events().list(idCalendar)
                    .setSingleEvents(false)
                    .execute();
            List<Event> items = events.getItems();
            Iterator<Event> i=items.iterator();
            Log.d(TAG, "idevento"+idEventiCalendar.get(0));
            while(i.hasNext()){
                Event e=(Event)i.next();
                Log.d(TAG, "ideventoit"+e.getId());
                if(idEventiCalendar.contains(e.getId())) {
                    Log.d(TAG, "elimina");
                    mService.events().delete(idCalendar, e.getId()).execute();
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ECCEZIONE CALENDAR", e.getCause().toString());
            mLastError = e;
            cancel(true);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean response) {
        if(response){
            Log.d(TAG, ""+idEventiCalendar.size());
            mCallback.done(true, "");
        }
        else{
            mCallback.done(false, "");
        }
    }

    @Override
    protected void onCancelled() {
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                if(mCallback instanceof AddMeasurement)
                    ((AddMeasurement)context).showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
                else if (mCallback instanceof AddPrescription)
                    ((AddPrescription)context).showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                //ADD ALL ACTIVITIES THAT CALL ELIMINAEVENTICALENDAR

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
