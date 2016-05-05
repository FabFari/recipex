package com.recipex.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddMeasurementMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainAddPrescriptionMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainPrescriptionInfoMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.taskcallbacks.TaskCallbackAggiungiTerapia;

import java.io.IOException;

/**
 * Created by Sara on 03/05/2016.
 */
public class AggiungiTerapiaAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    Context mContext;
    TaskCallbackAggiungiTerapia mCallback;
    String nome;
    int ingrediente;
    String tipo;
    int dose;
    String unità;
    int quantità;
    boolean ricetta;
    String foglioIllustrativo;
    int assistente;
    int cadenza;

    GoogleAccountCredential credential;
    SharedPreferences settings;

    SharedPreferences pref;

    public AggiungiTerapiaAT(Context context) {
        mContext = context;
    }

    public AggiungiTerapiaAT(Context context, String nome, int ingrediente, String tipo, int dose, String unità,
                             int quantità, boolean ricetta, String foglioIllustrativo, int assistente, int cadenza,
                             TaskCallbackAggiungiTerapia mCallback) {
        mContext = context;
        this.mCallback = mCallback;
        this.nome = nome;
        this.ingrediente = ingrediente;
        this.tipo = tipo;
        this.dose = dose;
        this.unità = unità;
        this.quantità = quantità;
        this.ricetta = ricetta;
        this.foglioIllustrativo = foglioIllustrativo;
        this.assistente = assistente;
        this.cadenza = cadenza;

    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        settings = mContext.getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("account", accountName);
        editor.commit();
        credential.setSelectedAccountName(accountName);
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused) {

        // Retrieve service handle.
        credential = GoogleAccountCredential.usingAudience(mContext,
                AppConstants.AUDIENCE);
        pref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String email = pref.getString("email", "");
        setSelectedAccountName(email);

        RecipexServerApi apiServiceHandle = AppConstants.getApiServiceHandle(credential);

        try {
            MainAddPrescriptionMessage reg = new MainAddPrescriptionMessage();

            //CAMPI OBBLIGATORI
            reg.setName(nome);
            reg.setActiveIngredient((long)ingrediente);
            reg.setKind(tipo);
            reg.setDose((long) dose);
            reg.setUnits(unità);
            reg.setQuantity((long) quantità);

            //NON OBBLIGATORI
            reg.setPil(foglioIllustrativo);
            reg.setCaregiver((long)assistente);

            RecipexServerApi.Prescription.AddPrescription post = apiServiceHandle.prescription().addPrescription((long)5719238044024832L, reg);

            MainDefaultResponseMessage response = post.execute();

            System.out.println("RESPONSE " + response.getMessage());
            Log.d("RESPONSE ", response.getMessage());

            return response;
        } catch (IOException e) {
            Toast.makeText(mContext, "Exception during API call! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainDefaultResponseMessage greeting) {

    }
}