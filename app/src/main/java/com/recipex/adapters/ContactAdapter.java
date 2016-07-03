package com.recipex.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.R;
import com.recipex.utilities.ContactItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for list of ways you can contact a caregiver
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final static String TAG = "CONTACT_ADAPTER";

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView icon;
        TextView label;

        ContactViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.contact_cardview);
            icon = (ImageView)itemView.findViewById(R.id.contact_icon);
            label = (TextView)itemView.findViewById(R.id.contact_text);
        }
    }

    List<ContactItem> contact_items;
    Activity activity;
    String user_mail;
    String user_phone;
    String crgv_phone;

    public ContactAdapter(List<ContactItem> contact_items, Activity activity, String user_mail,
                          String user_phone, String crgv_phone){
        if(contact_items != null)
            this.contact_items = contact_items;
        else
            this.contact_items = new ArrayList<ContactItem>();
        this.activity = activity;
        this.user_mail = user_mail;
        this.user_phone = user_phone;
        this.crgv_phone = crgv_phone;
    }

    @Override
    public int getItemCount() {
        if(contact_items != null && contact_items.size() > 0)
            return contact_items.size();
        else
            return 0;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item, viewGroup, false);
        ContactViewHolder uvh = new ContactViewHolder(v);
        return uvh;
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder contactViewHolder, int i) {
        contactViewHolder.icon.setImageResource(contact_items.get(i).getIcon_id());
        contactViewHolder.label.setText(contact_items.get(i).getLabel());

        switch(contact_items.get(i).getIcon_id()) {
            case R.drawable.ic_email:
                contactViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + user_mail));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipex: Richiesta di contatto");
                        //emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                        activity.startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                    }
                });
            break;
            case R.drawable.ic_phone:
                contactViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        if(crgv_phone != null)
                            phoneIntent.setData(Uri.parse("tel:" + crgv_phone));
                        else
                            phoneIntent.setData(Uri.parse("tel:" + user_phone));
                        activity.startActivity(phoneIntent);
                    }
                });
            break;
            case R.drawable.ic_sms:
                contactViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        if(crgv_phone != null)
                            smsIntent.putExtra("address", crgv_phone);
                        else
                            smsIntent.putExtra("address", user_phone);
                        //smsIntent.putExtra("sms_body","Body of Message");
                        activity.startActivity(smsIntent);
                    }
                });
            break;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
