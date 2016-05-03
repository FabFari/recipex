package com.recipex.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainRegisterUserMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.TaskCallbackLogin;

import java.io.IOException;
import java.util.List;

/**
 * Created by Sara on 26/04/2016.
 */
public class Register extends AsyncTask<Void, Void, MainRegisterUserMessage> {
    Context mContext;
    TaskCallbackLogin mCallback;
    String email;
    String nome;
    String cognome;
    String photo;
    String birth;
    String bio;
    String sesso;
    String città;
    String indirizzo;
    List<String> numeri;
    String campoSpecializzazione;
    Long anniEsperienza;
    String postoLavoro;
    List<String> numeriBusiness;
    String disponibilità;

    GoogleAccountCredential credential;

    SharedPreferences settings;

    SharedPreferences pref;

    public Register(Context context) {
        mContext = context;
    }

    public Register(Context context, String email, String nome, String cognome, String photo, String bio, String birth,
                     String sesso, String città, String indirizzo, List<String> numeri, String campoSpecializzazione,
                    Long anniEsperienza, String postoLavoro, List<String> numeriBusiness, String disponibilità,
                    TaskCallbackLogin mCallback) {
        mContext = context;
        this.mCallback = mCallback;
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.photo = photo;
        this.birth=birth;
        this.bio = bio;
        this.sesso=sesso;
        this.città = città;
        this.indirizzo = indirizzo;
        this.numeri = numeri;
        this.campoSpecializzazione = campoSpecializzazione;
        this.anniEsperienza =anniEsperienza;
        this.postoLavoro = postoLavoro;
        this.numeriBusiness = numeriBusiness;
        this.disponibilità = disponibilità;
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        settings=mContext.getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("account", accountName);
        editor.commit();
        credential.setSelectedAccountName(accountName);
    }
    protected MainRegisterUserMessage doInBackground(Void... unused) {

        // Retrieve service handle.
        credential = GoogleAccountCredential.usingAudience(mContext,
                AppConstants.AUDIENCE);
        setSelectedAccountName(email);

        Log.d("REGISTRAZIONE TASK ", "Do in back");

        RecipexServerApi apiServiceHandle = AppConstants.getApiServiceHandle(credential);


        if(apiServiceHandle==null){
            Log.d("REGISTRAZIONE TASK ", "NULL");

            Toast.makeText(mContext, "NULL", Toast.LENGTH_LONG).show();
        }
        Log.d("DB","doInBack");

        try {
            MainRegisterUserMessage reg = new MainRegisterUserMessage();

            //CAMPI OBBLIGATORI
            reg.setEmail(email);
            reg.setBirth(birth);
            reg.setName(nome);
            reg.setSurname(cognome);

            //CAMPI FACOLTATIVI USER: potrebbero essere vuoti
            reg.setSex(sesso);
            reg.setAddress(indirizzo);
            //reg.setPersonalNum(numeri.get(0));
            reg.setCity(città);

            pref=mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            if(anniEsperienza!=0) {
                reg.setBusinessNum(numeriBusiness.get(0));
                reg.setAvailable(disponibilità);
                reg.setPlace(postoLavoro);
                reg.setField(campoSpecializzazione);
                reg.setYearsExp(anniEsperienza);
                reg.setBio(bio);
                editor.putBoolean("utenteSemplice", false);
            }
            else{
                editor.putBoolean("utenteSemplice", false);
            }

            System.out.println("CAMPO SPEC 2 "+campoSpecializzazione);

            RecipexServerApi.User.RegisterUser post = apiServiceHandle.user().registerUser(reg);

            MainDefaultResponseMessage response = post.execute();
            if(response.getMessage().equals("User already existent.")){
                return reg;
            }else{
                Log.d("RESPONSE ", response.getMessage());

                return null;
            }
        } catch (IOException e) {
            Looper.prepare();
            Log.d("REGISTRAZIONE TASK ", "EXCEPTIONC "+e.getCause());
            Log.d("REGISTRAZIONE TASK ", "EXCEPTION "+e.getMessage());
            Toast.makeText(mContext, "Exception during API call! "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainRegisterUserMessage greeting) {
        SharedPreferences pref=mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if (greeting!=null) {
            Log.d("DEBUG","User è registrato");
            if(greeting.getField()==null){

                editor.putBoolean("utenteSemplice", true);
                mCallback.done(true, greeting.get("email").toString());
            }
            else{
                editor.putBoolean("utenteSemplice", false);
                mCallback.done(true, greeting.get("email").toString());
            }

        }else{
            Log.d("DEBUG","User NON era REGISTRATO!");
            System.out.println(email);
            mCallback.done(false, email.toString());
        }
    }

}
