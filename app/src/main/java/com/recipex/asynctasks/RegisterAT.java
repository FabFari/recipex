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
import com.recipex.taskcallbacks.LoginTC;
import java.io.IOException;

/**
 * Created by Sara on 26/04/2016.
 */

/**
 * chacks if user is already registered. If it is not, and he wants to, he is registered.
 */
public class RegisterAT extends AsyncTask<Void, Void, MainDefaultResponseMessage> {
    Context mContext;
    LoginTC mCallback;
    String email;
    String nome;
    String cognome;
    String photo;
    String birth;
    String bio;
    String sesso;
    String città;
    String indirizzo;
    String numeri;
    String campoSpecializzazione;
    Long anniEsperienza;
    String postoLavoro;
    String numeriBusiness;
    String disponibilità;
    Long userId;
    Boolean wantToRegister;

    RecipexServerApi apiHandler;

    public RegisterAT(Context context) {
        mContext = context;
    }

    public RegisterAT(Context context, String email, String nome, String cognome, String photo, String bio, String birth,
                      String sesso, String città, String indirizzo, String numeri, String campoSpecializzazione,
                      Long anniEsperienza, String postoLavoro, String numeriBusiness, String disponibilità,
                      LoginTC mCallback, RecipexServerApi apiHandler, boolean wantToRegister) {
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
        this.apiHandler = apiHandler;
        this.wantToRegister = wantToRegister;
    }

    protected MainDefaultResponseMessage doInBackground(Void... unused) {
        try {
            MainRegisterUserMessage reg = new MainRegisterUserMessage();

            //CAMPI OBBLIGATORI
            reg.setEmail(email);
            Log.d("EMAIL ", email);
            reg.setBirth(birth);
            Log.d("DATA ", birth);
            reg.setName(nome);
            Log.d("NAME ", reg.getName());
            Log.d("NAME ", nome);
            reg.setSurname(cognome);
            Log.d("COGNOME ", cognome);
            reg.setSex(sesso);
            Log.d("SESSO ", sesso);
            reg.setPic(photo);
            Log.d("PIC ", photo);

            //CAMPI FACOLTATIVI USER: potrebbero essere vuoti
            reg.setAddress(indirizzo);
            reg.setPersonalNum(numeri);
            reg.setCity(città);

            /*
            SharedPreferences pref = mContext.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed=pref.edit();
            */

            if(!campoSpecializzazione.equals("")) {
                reg.setBusinessNum(numeriBusiness);
                reg.setAvailable(disponibilità);
                reg.setPlace(postoLavoro);
                reg.setField(campoSpecializzazione);
                reg.setYearsExp(anniEsperienza);
                reg.setBio(bio);
            }


            if(!wantToRegister)
                reg.setPlace("Voglio loggarmi");

            // System.out.println("CAMPO SPEC 2 "+campoSpecializzazione);

            RecipexServerApi.User.RegisterUser post = apiHandler.user().registerUser(reg);


            MainDefaultResponseMessage response = post.execute();

            // System.out.println("RESPONSE " + response.getMessage());
            Log.d("RESPONSE ", response.getMessage());

            return response;

        } catch (IOException e) {
            Looper.prepare();
            Log.d("REGISTRAZIONE TASK ", "EXCEPTIONC "+e.getCause());
            Log.d("REGISTRAZIONE TASK ", "EXCEPTION "+e.getMessage());
            Toast.makeText(mContext, "Exception during API call! "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainDefaultResponseMessage response) {
        SharedPreferences pref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if(response != null) {

            if(!wantToRegister) {
                // Vuole loggarsi
                Log.d("DEBUG", "User è registrato");
                if (response.getUser() != null) {
                    // Utente registrato -> OK
                    userId = Long.parseLong(response.getPayload());
                    editor.putLong("userId", userId);
                    editor.apply();
                    MainRegisterUserMessage utente = response.getUser();
                    if (utente.getField() == null) {
                        Log.d("FIELD ", "field null");
                        editor.putBoolean("utenteSemplice", true);
                        editor.apply();
                        Log.d("UTENTESEMPLICE REGISTER", " " + pref.getBoolean("utenteSemplice", false));
                        mCallback.done(true, utente.get("email").toString(), utente.getCalendarId());
                    } else {
                        editor.putBoolean("utenteSemplice", false);
                        editor.apply();
                        mCallback.done(true, utente.get("email").toString(), utente.getCalendarId());
                    }
                } else {
                    Log.d("DEBUG", "User NON era REGISTRATO!");
                    // System.out.println(email);
                    mCallback.done(false, email, null);
                }
            }
            else {
                // Vuole Registrarsi
                if(!response.getMessage().equals("User already existent.")) {
                    userId = Long.parseLong(response.getPayload());
                    editor.putLong("userId", userId);
                    editor.apply();
                    if (campoSpecializzazione.equals("")) {
                        editor.putBoolean("utenteSemplice", true);
                        editor.apply();
                        Log.d("UTENTESEMPLICE REGISTER", " " + pref.getBoolean("utenteSemplice", false));
                        mCallback.done(true, email, null);
                    } else {
                        editor.putBoolean("utenteSemplice", false);
                        editor.apply();
                        mCallback.done(true, email, null);
                    }
                }
                else
                    mCallback.done(false, email, response.getUser().getCalendarId());
            }
        }
    }

}
