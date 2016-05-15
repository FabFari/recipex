package com.recipex.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.clans.fab.FloatingActionMenu;
import com.recipex.AppConstants;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;
import com.recipex.CircleTransform;
import com.recipex.R;
import com.recipex.fragments.CaregiversFragment;
import com.recipex.fragments.FamiliariFragment;
import com.recipex.fragments.MisurazioniFragment;
import com.recipex.fragments.TabFragment;
import com.recipex.fragments.TerapieFragment;
import com.recipex.utilities.Terapia;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.InputStream;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "HOME";

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;

    Toolbar toolbar;
    FloatingActionMenu fab_menu;
    FloatingActionButton fab;

    SharedPreferences pref;

    //to use showcaseview
    private static final String SHOWCASE_ID_MAIN = "Showcase_single_use_main";
    boolean utenteSemplice;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            setContentView(R.layout.activity_home);
        else
            setContentView(R.layout.activity_home2);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Intent intent = getIntent();
        boolean justRegistered = intent.getBooleanExtra("justRegistered", false);

        pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        boolean alreadyLogged = pref.getBoolean("alreadyLogged", true);

        if(justRegistered) {
            Snackbar snackbar = Snackbar
                    .make(mDrawerLayout, "Registrazione avvenuta con successo!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        else {
            if(!alreadyLogged) {
                Snackbar snackbar = Snackbar
                        .make(mDrawerLayout, "Login eseguito con successo!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("alreadyLogged", true);
        editor.commit();



        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // GET FABs
        fab_menu = (FloatingActionMenu) findViewById(R.id.home_fab_menu_measurement);


        fab = (FloatingActionButton) findViewById(R.id.fab_tutorial);
        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                Intent myIntent = new Intent(Home.this, AddMeasurement.class);
                Activity activity = (Activity) view.getContext();
                activity.startActivity(myIntent);
                activity.finish();
            }
        });
        */
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        utenteSemplice=pref.getBoolean("utenteSemplice", true);

        Log.d("UTENTESEMPLICE ", " "+utenteSemplice);
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if(utenteSemplice)
            mFragmentTransaction.replace(R.id.containerView, new MisurazioniFragment()).commit();
        else mFragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

        /*String nome = new StringBuilder(getIntent().getStringExtra("nome")).append(" ").append(getIntent().getStringExtra("cognome")).toString();

        String email = new StringBuilder(getIntent().getStringExtra("email")).toString();

        String photo = new StringBuilder(getIntent().getStringExtra("foto")).toString();
        System.out.println(photo);*/

        // Change Fabrizio
        final Long userId=pref.getLong("userId", 0L);
        Log.d("HOME", "userId: "+ userId);
        String nome=pref.getString("nome", "");
        String cognome=pref.getString("cognome", "");
        String email=pref.getString("email","");
        String photo=pref.getString("foto","");

        nome=new StringBuilder(nome).append(" ").append(cognome).toString();

        System.out.println("entrata "+nome+" "+email);
        View header=mNavigationView.getHeaderView(0);
        TextView nameuser = (TextView)header.findViewById(R.id.nome);
        TextView emailuser = (TextView)header.findViewById(R.id.email);
        ImageView photouser = (ImageView)header.findViewById(R.id.imageView);
        nameuser.setText(nome);
        emailuser.setText(email);
        Picasso.with(Home.this).load(photo).transform(new CircleTransform()).into(photouser);

        photouser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Home.this, Profile.class);
                i.putExtra("profileId", userId);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "MENUCREATE");

        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "MENU");
        if (id == R.id.action_logout) {
            //Login.signOutFromGplus();
            pref.edit().remove("email").commit();
            pref.edit().putBoolean("token", true).commit();
            // Fabrizio Change
            pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("email", null);
            editor.putString("nome", null);
            editor.putString("cognome", null);
            editor.putString("foto", null);
            editor.putBoolean("alreadyLogged", false);
            editor.commit();
            settings = getApplicationContext().getSharedPreferences(AppConstants.PREFS_NAME, 0);
            editor.putString(AppConstants.DEFAULT_ACCOUNT, null);
            editor.commit();
            Intent i = new Intent(Home.this, Login.class);
            i.putExtra("hasLogOut", true);
            this.startActivity(i);
            //Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
            this.finish();
            return true;
        }
        else if(id == R.id.action_tutorial){

            MaterialShowcaseView.resetAll(this);
            Toast.makeText(this, "All Showcases reset", Toast.LENGTH_SHORT).show();
            presentShowcaseView(350);

        }
        else if(id == R.id.home_search) {
            Intent myIntent = new Intent(getApplicationContext(), UserSearch.class);
            this.startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //containerView è il FrameLayout del layout della home

        if (id == R.id.home) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            if(utenteSemplice)
                fragmentTransaction.replace(R.id.containerView, new MisurazioniFragment()).commit();
            else fragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();
        }
        //ora fanno tutti la stessa cosa poi cambio
        else if (id == R.id.nav_infermieri) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView, new CaregiversFragment()).commit();
        } else if (id == R.id.nav_familiari) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView,new FamiliariFragment()).commit();
        } else if (id == R.id.nav_requests) {
            Intent myIntent = new Intent(getApplicationContext(), UserRequests.class);
            startActivity(myIntent);

        } else if (id == R.id.nav_aiuto) {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + "112"));
            startActivity(phoneIntent);
        
		} else if (id == R.id.nav_calendario) {
            PackageManager pm = getPackageManager();
            try {
                pm.getPackageInfo("com.google.android.calendar", PackageManager.GET_ACTIVITIES);
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.calendar");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(intent);
            }
            catch (PackageManager.NameNotFoundException e) {
                Uri webpage = Uri.parse("https://calendar.google.com/calendar");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }

        } else if (id == R.id.nav_terapie) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView, new TerapieFragment()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void presentShowcaseView(int withDelay){
//        new MaterialShowcaseView.Builder(this)
//                .setTarget(mSlidingTabLayoutTabs)
//                .setTitleText("Hello")
//                .setDismissText("Ho Capito")
//                .setContentText("Queste solo ne zezioni thell'applicazione! \n Geeftory è la sezione in cui puoi trovare le sotrie degli oggetti \n Geeft è dove puoi vedere gli oggeti presenti su geeft e prenotare quello a cui sei interessato!")
//                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
//                .singleUse(SHOWCASE_ID_MAIN) // provide a unique ID used to ensure it is only shown once
//                .show();



        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(withDelay); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID_MAIN);

//        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
//            @Override
//            public void onShow(MaterialShowcaseView itemView, int position) {
//                Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
//            }
//        });

        sequence.setConfig(config);

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(toolbar)
                        .setDismissText("OK")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Scorrendo da sinistra a destra, troverai informazioni su di te")
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(fab)
                        .setDismissText("HO CAPITO")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Cliccando qui puoi aggiungere un tuo assistito, una tua misurazione o un tuo bisogno")
                        .withRectangleShape()
                        .build()
        );
        /*
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget((TabLayout)findViewById(R.id.tabs))
                        .setDismissText("HO CAPITO")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Qui puoi vedere i tuoi assistiti, le tue misurazioni e i tuoi bisogni")
                        .withRectangleShape()
                        .build()
        );
        */
        sequence.start();

    }
    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
    private int fetchPrimaryDarkColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

}
