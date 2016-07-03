package com.recipex.adapters;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;
import com.appspot.recipex_1281.recipexServerApi.model.MainUserMainInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.Profile;
import com.recipex.activities.UserSearch;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * adapter for users' info
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    /**
     * class containing user's relevant data
     */
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

    List<MainUserMainInfoMessage> users;
    UserSearch activity;
    CircularProgressView progressView;

    public UsersAdapter(List<MainUserMainInfoMessage> users, UserSearch activity,
                           CircularProgressView progressView){
        if(users != null)
            this.users = users;
        else
            this.users = new ArrayList<MainUserMainInfoMessage>();
        this.activity = activity;
        this.progressView = progressView;
    }

    @Override
    public int getItemCount() {
        if(users != null && users.size() > 0)
            return users.size();
        else
            return 0;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        UserViewHolder uvh = new UserViewHolder(v);
        return uvh;
    }

    @Override
    public void onBindViewHolder(final UserViewHolder userViewHolder, int i) {
            final int pos = i;
            userViewHolder.userName.setText(String.format(Locale.getDefault(), "%s %s",
                    users.get(i).getName(), users.get(i).getSurname()));
            String field = users.get(i).getField();
            if(field != null && field.length() > 0) {
                userViewHolder.crgvField.setText(field);
                userViewHolder.crgvField.setVisibility(View.VISIBLE);
                userViewHolder.crgvIcon.setVisibility(View.VISIBLE);
            }
            Picasso.with(activity.getApplicationContext()).load(users.get(i).
                    getPic()).into(userViewHolder.userPic);

            userViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(activity.getApplicationContext(), Profile.class);
                    myIntent.putExtra("profileId", users.get(pos).getId());
                    activity.startActivity(myIntent);
                }
            });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setDataset(List<MainUserMainInfoMessage> updatedUsers) {
        this.users = updatedUsers;
    }

}
