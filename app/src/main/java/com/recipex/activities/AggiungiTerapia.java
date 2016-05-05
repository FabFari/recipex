package com.recipex.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.recipex.R;
import com.recipex.asynctasks.AggiungiTerapiaAT;
import com.recipex.asynctasks.Register;
import com.recipex.taskcallbacks.TaskCallbackAggiungiTerapia;

public class AggiungiTerapia extends AppCompatActivity implements TaskCallbackAggiungiTerapia{

    String nome, ingrediente, tipo, dose, unità, quantità, ricetta, foglio, caregiver, tempo;
    EditText inserisciNome, inserisciIngrediente, inserisciTipo, inserisciDose, inserisciUnità, inserisciQuantità,
            inserisciRicetta, inserisciFoglio, inserisciCaregiver, inserisciTempo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_terapia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Aggiungi Terapia");

        inserisciNome= (EditText)findViewById(R.id.insertNomeTerapia);
        inserisciIngrediente= (EditText)findViewById(R.id.insertIngrediente);
        inserisciTipo=(EditText)findViewById(R.id.insertTipo);
        inserisciDose= (EditText)findViewById(R.id.insertDose);
        inserisciUnità=(EditText)findViewById(R.id.insertUnità);
        inserisciQuantità=(EditText)findViewById(R.id.insertQuantità);
        inserisciRicetta=(EditText)findViewById(R.id.insertRicetta);
        inserisciFoglio=(EditText)findViewById(R.id.insertFoglio);
        inserisciCaregiver=(EditText)findViewById(R.id.insertAssistente);
        inserisciTempo=(EditText)findViewById(R.id.insertTempo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.registrati) {
            if (inserisciNome.getText().length() > 1 && inserisciIngrediente.getText().length() > 1 &&
                    inserisciTipo.getText().length()>1 && inserisciDose.getText().length()>1 &&
                    inserisciUnità.getText().length()>1 && inserisciQuantità.getText().length()>1 &&
                    inserisciRicetta.getText().length()>1) {

                nome=inserisciNome.getText().toString();
                ingrediente=inserisciIngrediente.getText().toString();

                int ingr2=Integer.parseInt(ingrediente);

                tipo=inserisciTipo.getText().toString();
                dose = inserisciDose.getText().toString();

                int dose2= Integer.parseInt(dose);

                unità = inserisciUnità.getText().toString();
                quantità = inserisciQuantità.getText().toString();

                int quanto=Integer.parseInt(quantità);

                ricetta = inserisciRicetta.getText().toString();
                boolean recipe=(ricetta.equals("SI"))? true: false;
                Log.d("RICETTA ", " "+recipe);

                foglio = inserisciFoglio.getText().toString();
                caregiver = inserisciCaregiver.getText().toString();

                int assistente=Integer.parseInt(caregiver);

                tempo =inserisciTempo.getText().toString();

                int cadenza=Integer.parseInt(tempo);


                Log.d("REGISTRAZIONE ", "Sono qui");
                if (checkNetwork()) new AggiungiTerapiaAT(getApplicationContext(), nome, ingr2, tipo, dose2, unità,
                        quanto, recipe, foglio, assistente, cadenza, this).execute();

            }
            else {
                Toast.makeText(getApplicationContext(),"Compilare i campi obbligatori", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
        if(isOnline) {
            return true;
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Ops..qualcosa è andato storto!")
                    .setMessage("Sembra che tu non sia collegato ad internet! ")
                    .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
            return false;
        }
    }


}
