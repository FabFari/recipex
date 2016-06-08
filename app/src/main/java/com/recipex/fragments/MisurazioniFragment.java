package com.recipex.fragments;


import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;

import android.support.design.widget.FloatingActionButton;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.AddMeasurement;
import com.recipex.adapters.RVAdapter;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainMeasurementInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;
import com.recipex.asynctasks.GetMeasurementsUser;
import com.recipex.taskcallbacks.TaskCallbackElimina;
import com.recipex.taskcallbacks.TaskCallbackGetMeasurements;
import com.recipex.utilities.ConnectionDetector;
import com.recipex.utilities.Misurazione;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Sara on 02/05/2016.
 */
public class MisurazioniFragment extends Fragment implements TaskCallbackGetMeasurements, TaskCallbackElimina/*, Toolbar.OnMenuItemClickListener*/ {

    private final static String TAG = "MISURAZIONI_FRAGMENT";
    private final static int ADD_MEASUREMENT = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int EMPTY_VIEW = 10;

    private SharedPreferences settings;
    private GoogleAccountCredential credential;
    private String accountName;
    private ConnectionDetector cd;

    private final int scrollnum=6;

    List<Misurazione> misurazioni=new LinkedList<>();

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 3;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    View mainView;

    private static final String SHOWCASE_ID_MAIN = "Showcase_single_use_main";

    List<String> date;
    FloatingActionMenu fab_menu;
    com.github.clans.fab.FloatingActionButton fab_pressione;
    com.github.clans.fab.FloatingActionButton fab_freq_cardiaca;
    com.github.clans.fab.FloatingActionButton fab_freq_respiratoria;
    com.github.clans.fab.FloatingActionButton fab_temperatura;
    com.github.clans.fab.FloatingActionButton fab_spo2;
    com.github.clans.fab.FloatingActionButton fab_diabete;
    com.github.clans.fab.FloatingActionButton fab_dolore;
    com.github.clans.fab.FloatingActionButton fab_colesterolo;

    private TextView emptyText;
    private CircularProgressView progressView;
    private CoordinatorLayout coordinatorLayout;
    ImageView delete;

    private Long userId;

    static RecyclerView curRecView;
    SharedPreferences pref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            rootView = inflater.inflate(R.layout.recyclerview, container, false);
        else
            rootView = inflater.inflate(R.layout.recyclerview2, container, false);

        FloatingActionButton fabfragment=(FloatingActionButton)rootView.findViewById(R.id.fabfragment);
        fabfragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), AddMeasurement.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        initUI(rootView);
        mainView = rootView;

        return rootView;
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Home activity = (Home ) getActivity();
        Toolbar toolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.home);
        toolbar.setOnMenuItemClickListener(this);

    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_MEASUREMENT:
                if(resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "ADD_MEASUREMENT: RESULT_OK");
                    Snackbar snackbar = Snackbar
                            .make(getActivity().getWindow().getDecorView().getRootView(),
                                    "Misurazone aggiunta con successo!", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    credential = GoogleAccountCredential.usingAudience(getActivity(), AppConstants.AUDIENCE);
                    setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

                    if (credential.getSelectedAccountName() == null) {
                        Log.d(TAG, "AccountName == null: startActivityForResult.");
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    }
                    else {
                        if(checkNetwork()) {
                            RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                            new GetMeasurementsUser(userId, getContext(), this, apiHandler, scrollnum, 0).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            break;
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "Nell'if.");
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
                        editor.apply();
                        // User is authorized
                        RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);
                        if (checkNetwork()) {
                            new GetMeasurementsUser(userId, getContext(), this, apiHandler,scrollnum, 0).execute();
                            progressView.startAnimation();
                            progressView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
        }
    }

    private void initUI(View rootView) {
        progressView = (CircularProgressView) rootView.findViewById(R.id.home_progress_view);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.home_coordinator);
        emptyText = (TextView) rootView.findViewById(R.id.home_empty_message);
        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.my_recyclerview);
        curRecView=rv;
        final LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        // GET FABs
        fab_menu = (FloatingActionMenu) rootView.findViewById(R.id.home_fab_menu_measurement);
        fab_pressione = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item1);
        fab_freq_cardiaca = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item2);
        fab_freq_respiratoria = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item3);
        fab_temperatura = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item4);
        fab_spo2 = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item5);
        fab_diabete = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item6);
        fab_dolore = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item7);
        fab_colesterolo = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_fab_menu_item8);

        // SET CLICK LISTENERS
        fab_pressione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.PRESSIONE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_cardiaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_CARDIACA);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_freq_respiratoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.FREQ_RESPIRAZIONE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_temperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.TEMP_CORPOREA);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_spo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.SPO2);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_diabete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.GLUCOSIO);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_dolore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.DOLORE);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });
        fab_colesterolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change Fabrizio
                fab_menu.close(true);
                Intent myIntent = new Intent(getActivity(), AddMeasurement.class);
                myIntent.putExtra("kind", AppConstants.COLESTEROLO);
                Activity activity = (Activity) view.getContext();
                startActivityForResult(myIntent, ADD_MEASUREMENT);
            }
        });

        pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        userId = pref.getLong("userId", 0L);

        settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        credential = GoogleAccountCredential.usingAudience(getActivity(), AppConstants.AUDIENCE);
        Log.d(TAG, "Credential: " + credential);
        setSelectedAccountName(settings.getString(AppConstants.DEFAULT_ACCOUNT, null));

        if (credential.getSelectedAccountName() == null) {
            Log.d(TAG, "AccountName == null: startActivityForResult.");
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        else {
            if (userId != 0 && checkNetwork()) {
                final RecipexServerApi apiHandler = AppConstants.getApiServiceHandle(credential);

                final TaskCallbackGetMeasurements f=this;
                //SCROLL
                /*rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if(checkNetwork() && !misurazioni.isEmpty()){
                            new GetMeasurementsUser(userId, getContext(), f, apiHandler, scrollnum,
                                    misurazioni.get(misurazioni.size()-1).data);
                        }
                    }
                });*/
                rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        visibleItemCount = curRecView.getChildCount();
                        totalItemCount = llm.getItemCount();
                        firstVisibleItem = llm.findFirstVisibleItemPosition();

                        Log.d(TAG, ""+visibleItemCount);
                        Log.d(TAG, ""+totalItemCount);
                        Log.d(TAG, ""+firstVisibleItem);

                        if (loading) {
                            if (totalItemCount > previousTotal) {
                                loading = false;
                                previousTotal = totalItemCount;
                            }
                        }
                        if (!loading && (totalItemCount - visibleItemCount)
                                <= (firstVisibleItem + visibleThreshold)) {
                            // End has been reached

                            //https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews
                            Log.i(TAG, "end called");

                            if(checkNetwork() && !misurazioni.isEmpty()){
                                Log.d(TAG, "scrollll");
                                Log.d(TAG, misurazioni.get(misurazioni.size()-1).data);
                                new GetMeasurementsUser(userId, getContext(), f, apiHandler, scrollnum,
                                        misurazioni.get(misurazioni.size()-1).id).execute();
                            }

                            loading = true;
                        }

                    }
                });


                new GetMeasurementsUser(userId, getContext(), this, apiHandler, scrollnum, 0).execute();
                progressView.startAnimation();
                progressView.setVisibility(View.VISIBLE);
            } else {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(),
                                "Operazione fallita! Si è verificato un errore imprevisto!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }

    public boolean checkNetwork() {
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        // Check if Internet present
        if (cd.isConnectingToInternet()) {
            return true;
        }else{
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Nessuna connesione a internet!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ESCI", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return false;
    }

    //callback elimina
    public void done(boolean b, String id){
        if(!b){
            Snackbar snackbar = Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(),
                            "Operazione fallita! Si è verificato un errore imprevisto!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        else{

        }
    }
    //callback from GetMisurazioniUser
    public void done(MainUserMeasurementsMessage response){
        if((response!=null && response.getMeasurements()!=null) || !misurazioni.isEmpty() ) {
            Log.e(TAG, "Nel done di getMisurazioni");
            List<MainMeasurementInfoMessage> lista=new LinkedList<>();
            if(response!=null && response.getMeasurements() != null && !response.getMeasurements().isEmpty()) {
                lista = response.getMeasurements();
                Iterator<MainMeasurementInfoMessage> i = lista.iterator();
                while (i.hasNext()) {
                    MainMeasurementInfoMessage cur = i.next();
                    Misurazione mcur = new Misurazione();
                    switch (cur.getKind()) {
                        case AppConstants.COLESTEROLO:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getChlLevel()));
                            break;
                        case AppConstants.FREQ_CARDIACA:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getBpm()), "", "");
                            break;
                        case AppConstants.PRESSIONE:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getSystolic()),
                                    Long.toString(cur.getDiastolic()), "");
                            break;
                        case AppConstants.FREQ_RESPIRAZIONE:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getRespirations()), "", "");
                            break;
                        case AppConstants.SPO2:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getSpo2()));
                            break;
                        case AppConstants.GLUCOSIO:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getHgt()));
                            break;
                        case AppConstants.TEMP_CORPOREA:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), "", "", Double.toString(cur.getDegrees()));
                            break;
                        case AppConstants.DOLORE:
                            mcur = new Misurazione(cur.getKind(), cur.getDateTime(), Long.toString(cur.getNrs()), "", "");
                            break;
                    }
                    if (cur.getNote() != null)
                        mcur.setNota(cur.getNote());

                    mcur.setIdCalendar(cur.getCalendarId());
                    mcur.setId(cur.getId());
                    misurazioni.add(mcur);
                    Log.d("MisIterator", mcur.data);

                }
            }
            /*if(response.getMeasurements().isEmpty())
                misurazioni.clear();
            else {*/
            if(!misurazioni.isEmpty()) {
                RVAdapter adapter = new RVAdapter(misurazioni);
                curRecView.setAdapter(adapter);
                Log.d(TAG, "itemCount: " + adapter.getItemCount());
                if (adapter.getItemViewType(0) == EMPTY_VIEW) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().getWindow().getDecorView().getRootView(),
                                    "Ancora nessuna misurazione?\nAggiungine subito una cliccando il bottone!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
            //}
        }
		else {
            RVAdapter adapter = new RVAdapter(new ArrayList<Misurazione>());
            curRecView.setAdapter(adapter);
        }

        progressView.stopAnimation();
        progressView.setVisibility(View.GONE);
        //Toast.makeText(getActivity(), "You don't have prescriptions. Add one clicking on the button", Toast.LENGTH_LONG).show();
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences settings = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstants.DEFAULT_ACCOUNT, accountName);
        editor.apply();
        Log.d(TAG, "ACCOUNT NAME: " + accountName);
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }


   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        SharedPreferences pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        Log.d("Misurazioni", "MENU");
        if (id == R.id.action_logout) {
            //Login.signOutFromGplus();
            pref.edit().remove("email").commit();
            pref.edit().putBoolean("token", true).commit();
            // Fabrizio Change
            pref = getActivity().getSharedPreferences("MyPref",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("alreadyLogged", false);
            editor.commit();
            Intent i = new Intent(getActivity(), Login.class);
            i.putExtra("hasLogOut", true);
            this.startActivity(i);
            //Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return true;
        }
        else if(id == R.id.action_tutorial){

            MaterialShowcaseView.resetAll(getActivity());
            Toast.makeText(getActivity(), "All Showcases reset", Toast.LENGTH_SHORT).show();
            presentShowcaseView(350);

        }
        else if(id == R.id.home_search) {
            Intent myIntent = new Intent(getActivity(), UserSearch.class);
            this.startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }*/

    /*@Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();

        SharedPreferences pref=getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Log.d("Misurazioni", "MENU");
        if (id == R.id.action_logout) {
            //Login.signOutFromGplus();
            pref.edit().remove("email").commit();
            pref.edit().putBoolean("token", true).commit();
            // Fabrizio Change
            pref = getActivity().getSharedPreferences("MyPref",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("alreadyLogged", false);
            editor.commit();
            Intent i = new Intent(getActivity(), Login.class);
            i.putExtra("hasLogOut", true);
            this.startActivity(i);
            //Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return true;
        }
        else if(id == R.id.action_tutorial){

            MaterialShowcaseView.resetAll(getActivity());
            Toast.makeText(getActivity(), "All Showcases reset", Toast.LENGTH_SHORT).show();
            presentShowcaseView(350);
            return true;
        }
        else if(id == R.id.home_search) {
            Intent myIntent = new Intent(getActivity(), UserSearch.class);
            this.startActivity(myIntent);
            return true;
        }
        return false;
    }*/

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

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), SHOWCASE_ID_MAIN);

//        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
//            @Override
//            public void onShow(MaterialShowcaseView itemView, int position) {
//                Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
//            }
//        });

        sequence.setConfig(config);

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget(getActivity().findViewById(R.id.toolbar))
                        .setDismissText("OK")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Scorrendo da sinistra a destra, troverai informazioni su di te")
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget(getActivity().findViewById(R.id.home_fab_menu_measurement))
                        .setDismissText("HO CAPITO")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Cliccando qui puoi aggiungere un tuo assistito, una tua misurazione o un tuo bisogno")
                        .withRectangleShape()
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget((TabLayout)getActivity().findViewById(R.id.tabs))
                        .setDismissText("HO CAPITO")
                        .setMaskColour(fetchPrimaryDarkColor())
                        .setDismissTextColor(fetchAccentColor())
                        .setContentText("Qui puoi vedere i tuoi assistiti, le tue misurazioni e i tuoi bisogni")
                        .withRectangleShape()
                        .build()
        );

        sequence.start();

    }
    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
    private int fetchPrimaryDarkColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }


}
