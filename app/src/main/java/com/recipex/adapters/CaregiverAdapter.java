package com.recipex.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.model.MainRequestInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.R;
import com.recipex.activities.Home;
import com.recipex.activities.Profile;
import com.recipex.activities.UserSearch;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sara on 12/05/2016.
 */
public class CaregiverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView userPic;
        TextView userName;
        TextView crgvField;
        ImageView crgvIcon;

        UserViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.user_search_cardview);
            userPic = (ImageView)itemView.findViewById(R.id.user_search_user_pic);
            userName = (TextView)itemView.findViewById(R.id.user_search_user_name);
            crgvField = (TextView)itemView.findViewById(R.id.user_search_crgv_field);
            crgvIcon = (ImageView)itemView.findViewById(R.id.user_search_crgv_icon);
        }
    }
    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static final int EMPTY_VIEW = 10;

    List<MainUserMainInfoMessage> caregivers;
    MainUserMainInfoMessage physician;
    MainUserMainInfoMessage nurse;
    Home activity;
    CircularProgressView progressView;

    public CaregiverAdapter(List<MainUserMainInfoMessage> users, MainUserMainInfoMessage physician,
                            MainUserMainInfoMessage nurse, Home activity,
                        CircularProgressView progressView){
        if(users != null)
            this.caregivers = users;
        else
            this.caregivers = new ArrayList<MainUserMainInfoMessage>();
        this.activity = activity;
        this.progressView = progressView;
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

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_caregivers_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        UserViewHolder rvh = new UserViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        final int pos = i;
        if(viewHolder instanceof UserViewHolder) {
            UserViewHolder userViewHolder=(UserViewHolder)viewHolder;
            //significa che ho aggiunto il medico di base, quindi sarà sicuramente alla prima posizione
            if(physician!=null){
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
                }
                //se ho anche l'infermiera, sarà alla seconda posizione
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
                }
            }
            //se ho solo l'infermiera sarà alla prima posizione
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
            }

            else {
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
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setDataset(List<MainUserMainInfoMessage> updatedUsers) {
        this.caregivers = updatedUsers;
    }

}
