package com.recipex.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;
import com.appspot.recipex_1281.recipexServerApi.model.MainRequestInfoMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.AppConstants;
import com.recipex.R;
import com.recipex.activities.Profile;
import com.recipex.activities.UserRequests;
import com.recipex.fragments.UserRequestFragment;
import com.recipex.utilities.ConnectionDetector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView senderPic;
        TextView senderName;
        TextView requestKind;
        TextView requestId;
        TextView isRequestNew;
        ImageView requestAccept;
        ImageView requestDecline;

        RequestViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.requests_cardview);
            senderPic = (ImageView)itemView.findViewById(R.id.requests_sender_pic);
            senderName = (TextView)itemView.findViewById(R.id.requests_sender_name);
            requestKind = (TextView)itemView.findViewById(R.id.requests_kind);
            requestId = (TextView)itemView.findViewById(R.id.requests_id);
            requestAccept = (ImageView)itemView.findViewById(R.id.requests_accept);
            requestDecline = (ImageView)itemView.findViewById(R.id.requests_decline);
            isRequestNew = (TextView)itemView.findViewById(R.id.requests_new);
        }
    }

    List<MainRequestInfoMessage> requests;
    //Activity activity;
    UserRequestFragment fragment;
    CircularProgressView progressView;
    private static final int EMPTY_VIEW = 10;

    public RequestsAdapter(List<MainRequestInfoMessage> requests, UserRequestFragment fragment,
                           CircularProgressView progressView){
        if(requests != null)
            this.requests = requests;
        else
            this.requests = new ArrayList<MainRequestInfoMessage>();
        this.fragment = fragment;
        this.progressView = progressView;
    }

    @Override
    public int getItemCount() {
        if(requests != null && requests.size() > 0)
            return requests.size();
        else
            return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (requests == null || requests.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_user_requests_empty, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_item, viewGroup, false);
        RequestViewHolder rvh = new RequestViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder instanceof RequestViewHolder) {
            RequestViewHolder requestViewHolder = (RequestViewHolder) viewHolder;
            final int pos = i;
            requestViewHolder.senderName.setText(String.format(Locale.getDefault(), "%s %s",
                    requests.get(i).getSenderName(), requests.get(i).getSenderSurname()));

            String kind = requests.get(i).getKind();
            if (kind.equals(AppConstants.FAMILIARE))
                requestViewHolder.requestKind.setText("Familiare");
            else if (kind.equals(AppConstants.MEDICO_BASE))
                requestViewHolder.requestKind.setText("Medico di base");
            else if (kind.equals(AppConstants.INF_DOMICILIARE))
                requestViewHolder.requestKind.setText("Infermiere domiciliare");
            else
                requestViewHolder.requestKind.setText("Caregiver");

            Picasso.with(fragment.getActivity().getApplicationContext()).load(requests.get(i).
                    getSenderPic()).into(requestViewHolder.senderPic);
            boolean isNew = requests.get(i).getPending();

            if (isNew)
                requestViewHolder.isRequestNew.setVisibility(View.VISIBLE);

            requestViewHolder.requestAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressView.startAnimation();
                    progressView.setVisibility(View.VISIBLE);
                    fragment.executeAsyncTask(requests.get(pos).getId(), true);
                }
            });
            requestViewHolder.requestDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressView.startAnimation();
                    progressView.setVisibility(View.VISIBLE);
                    fragment.executeAsyncTask(requests.get(pos).getId(), false);
                }
            });

            requestViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(fragment.getActivity().getApplicationContext(), Profile.class);
                    myIntent.putExtra("profileId", requests.get(pos).getSender());
                    fragment.startActivity(myIntent);
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
