package com.recipex.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainRequestInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.Home;
import com.recipex.activities.Profile;
import com.recipex.activities.UserSearch;
import com.recipex.asynctasks.UpdateRelationInfoAT;
import com.recipex.taskcallbacks.UpdateRelationInfoTC;
import com.recipex.utilities.ConnectionDetector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sara on 12/05/2016.
 */

/**
 * Adapter for Caregivers fragment
 */
public class CaregiverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * class containing parameters to be shown in the cardview
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView userPic;
        TextView userName;
        TextView crgvField;
        ImageView crgvIcon;
        ImageView remove;

        UserViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.user_search_cardview);
            userPic = (ImageView)itemView.findViewById(R.id.user_search_user_pic);
            userName = (TextView)itemView.findViewById(R.id.user_search_user_name);
            crgvField = (TextView)itemView.findViewById(R.id.user_search_crgv_field);
            crgvIcon = (ImageView)itemView.findViewById(R.id.user_search_crgv_icon);
            remove = (ImageView)itemView.findViewById(R.id.user_search_remove);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static final int EMPTY_VIEW = 10;
    private final static String TAG = "CAREGIVER_ADAPTER";

    List<MainUserMainInfoMessage> caregivers;
    MainUserMainInfoMessage physician;
    MainUserMainInfoMessage nurse;
    Home activity;
    CircularProgressView progressView;
    RecipexServerApi apiHandler;
    ConnectionDetector cd;
    Long user_id;
    UpdateRelationInfoTC taskCallback;

    public CaregiverAdapter(List<MainUserMainInfoMessage> users, MainUserMainInfoMessage physician,
                            MainUserMainInfoMessage nurse, Home activity, CircularProgressView progressView,
                            UpdateRelationInfoTC taskCallback, Long user_id, RecipexServerApi apiHandler){
        if(users != null)
            this.caregivers = users;
        else
            this.caregivers = new ArrayList<MainUserMainInfoMessage>();
        this.activity = activity;
        this.progressView = progressView;

        //check if there is a physician or a nurse (not present in the list of caregivers)
        if(physician!=null && nurse!=null){
            caregivers.add(0, physician);
            caregivers.add(1, nurse);
        }
        else if(physician!=null && nurse==null) {
            Log.d("CaregiverAdapter", physician.getName());
            caregivers.add(0, physician);
        }
        else if(physician==null && nurse!=null)
            caregivers.add(0, nurse);

        this.physician=physician;
        this.nurse=nurse;
        this.taskCallback = taskCallback;
        this.user_id = user_id;
        this.apiHandler = apiHandler;
    }

    @Override
    public int getItemCount() {
        if(caregivers != null && caregivers.size() > 0)
            return caregivers.size();
        else
            return 1;
    }
    @Override
    public int getItemViewType(int position) {
        if (caregivers == null || caregivers.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        //no elements present
        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_caregivers_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.caregiver_item, viewGroup, false);
        UserViewHolder rvh = new UserViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        final int pos = i;
        if(viewHolder instanceof UserViewHolder) {
            final UserViewHolder userViewHolder=(UserViewHolder)viewHolder;
            if(physician!=null){
                //there is a physician at first position. Set listeners for clicking and removal
                if(i==0) {
                    userViewHolder.crgvIcon.setImageResource(R.drawable.ic_pc_physician_accent);
                    userViewHolder.crgvIcon.setVisibility(View.VISIBLE);
                    userViewHolder.userName.setText(String.format(Locale.getDefault(), "%s %s",
                            physician.getName(), physician.getSurname()));
                    if (physician.getField() != null && physician.getField().length() > 0) {
                        userViewHolder.crgvField.setText(physician.getField());
                        userViewHolder.crgvField.setVisibility(View.VISIBLE);
                    }
                    Picasso.with(activity.getApplicationContext()).load(physician.
                            getPic()).into(userViewHolder.userPic);

                    userViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                            myIntent.putExtra("profileId", physician.getId());
                            activity.startActivity(myIntent);
                        }
                    });

                    userViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                            // Add the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(AppConstants.checkNetwork(activity)) {
                                        new UpdateRelationInfoAT(user_id, physician.getId(), AppConstants.MEDICO_BASE,
                                                activity.getWindow().getDecorView().getRootView(), activity, taskCallback,
                                                apiHandler, AppConstants.ASSISTITO).execute();
                                    }
                                }
                            });
                            builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.cancel();
                                }
                            });

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("Vuoi rimuovere "+physician.getName()+" "+physician.getSurname()
                                    +" dai tuoi caregivers?")
                                    .setTitle("Attenzione");

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });

                }
                //if I have also the nurse, there will be at second position
                if(i==1 && nurse!=null){
                    userViewHolder.crgvIcon.setImageResource(R.drawable.ic_visiting_nurse_accent);
                    userViewHolder.crgvIcon.setVisibility(View.VISIBLE);
                    userViewHolder.userName.setText(String.format(Locale.getDefault(), "%s %s",
                            nurse.getName(), nurse.getSurname()));
                    if (nurse.getField() != null && nurse.getField().length() > 0) {
                        userViewHolder.crgvField.setText(nurse.getField());
                        userViewHolder.crgvField.setVisibility(View.VISIBLE);
                    }
                    Picasso.with(activity.getApplicationContext()).load(nurse.
                            getPic()).into(userViewHolder.userPic);

                    userViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                            myIntent.putExtra("profileId", nurse.getId());
                            activity.startActivity(myIntent);
                        }
                    });

                    userViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                            // Add the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(AppConstants.checkNetwork(activity)) {
                                        new UpdateRelationInfoAT(user_id, nurse.getId(), AppConstants.INF_DOMICILIARE,
                                                activity.getWindow().getDecorView().getRootView(), activity, taskCallback,
                                                apiHandler, AppConstants.ASSISTITO).execute();
                                    }
                                }
                            });
                            builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.cancel();
                                }
                            });

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("Vuoi rimuovere "+nurse.getName()+" "+nurse.getSurname()
                                    +" dai tuoi caregivers?")
                                    .setTitle("Attenzione");

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });

                }
            }
            //if I have only the nurse, there will be at first position
            else if(physician==null && nurse!=null && i==0){
                userViewHolder.crgvIcon.setImageResource(R.drawable.ic_visiting_nurse_accent);
                userViewHolder.crgvIcon.setVisibility(View.VISIBLE);
                userViewHolder.userName.setText(String.format(Locale.getDefault(), "%s %s",
                        nurse.getName(), nurse.getSurname()));
                if (nurse.getField() != null && nurse.getField().length() > 0) {
                    userViewHolder.crgvField.setText(nurse.getField());
                    userViewHolder.crgvField.setVisibility(View.VISIBLE);
                }
                Picasso.with(activity.getApplicationContext()).load(nurse.
                        getPic()).into(userViewHolder.userPic);

                userViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                        myIntent.putExtra("profileId", nurse.getId());
                        activity.startActivity(myIntent);
                    }
                });

                userViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                        // Add the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(AppConstants.checkNetwork(activity)) {
                                    new UpdateRelationInfoAT(user_id, nurse.getId(), AppConstants.INF_DOMICILIARE,
                                            activity.getWindow().getDecorView().getRootView(), activity, taskCallback,
                                            apiHandler, AppConstants.ASSISTITO).execute();
                                }
                            }
                        });
                        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage("Vuoi rimuovere "+nurse.getName()+" "+nurse.getSurname()
                                +" dai tuoi caregivers?")
                                .setTitle("Attenzione");

                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

            }

            else {
                userViewHolder.crgvIcon.setImageResource(R.drawable.ic_caregivers_accent);
                userViewHolder.userName.setText(String.format(Locale.getDefault(), "%s %s",
                        caregivers.get(i).getName(), caregivers.get(i).getSurname()));
                String field = caregivers.get(i).getField();
                if (field != null && field.length() > 0) {
                    userViewHolder.crgvField.setText(field);
                    userViewHolder.crgvField.setVisibility(View.VISIBLE);
                    userViewHolder.crgvIcon.setVisibility(View.VISIBLE);
                }

                Picasso.with(activity.getApplicationContext()).load(caregivers.get(i).
                        getPic()).into(userViewHolder.userPic);

                userViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                        myIntent.putExtra("profileId", caregivers.get(pos).getId());
                        activity.startActivity(myIntent);
                    }
                });

                userViewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "REMOVE CLICKED!!");
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                        // Add the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(AppConstants.checkNetwork(activity)) {
                                    //Log.d(TAG, "ID: "+user_id);
                                    //Log.d(TAG, "CAREGIVER ID: "+caregivers.get(pos).getId());
                                    new UpdateRelationInfoAT(user_id, caregivers.get(pos).getId(), AppConstants.CAREGIVER,
                                            activity.getWindow().getDecorView().getRootView(), activity, taskCallback,
                                            apiHandler, AppConstants.ASSISTITO).execute();
                                }
                            }
                        });
                        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage("Vuoi rimuovere "+caregivers.get(pos).getName()+" "+
                                caregivers.get(pos).getSurname()+" dai tuoi caregivers?")
                                .setTitle("Attenzione");

                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}