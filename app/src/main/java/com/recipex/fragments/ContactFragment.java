package com.recipex.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recipex.R;
import com.recipex.adapters.ContactAdapter;
import com.recipex.utilities.ContactItem;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends DialogFragment {
    private RecyclerView mRecyclerView;
    private ContactAdapter adapter;

    private String user_mail;
    private String user_phone;
    private String crgv_phone;

    // this method create view for your Dialog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user_mail = getArguments().getString("user_mail", null);
        user_phone = getArguments().getString("user_phone", null);
        crgv_phone = getArguments().getString("crgv_phone", null);

        View v = inflater.inflate(R.layout.contact_dialog, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.contact_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<ContactItem> myList = new ArrayList<ContactItem>();
        myList.add(new ContactItem(R.drawable.ic_email, "E-mail"));
        if(user_phone != null || crgv_phone != null)
            myList.add(new ContactItem(R.drawable.ic_phone, "Telefono"));
        else
            myList.add(new ContactItem(R.drawable.ic_phone_off, "Telefono"));
        if(user_phone != null || crgv_phone != null)
            myList.add(new ContactItem(R.drawable.ic_sms, "SMS"));
        else
            myList.add(new ContactItem(R.drawable.ic_sms_off, "SMS"));

        adapter = new ContactAdapter(myList, getActivity(), user_mail, user_phone, crgv_phone);
        mRecyclerView.setAdapter(adapter);

        return v;
    }
}
