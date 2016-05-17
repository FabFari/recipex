package com.recipex.fragments;

/**
 * Created by Sara on 24/04/2016.
 */
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recipex.R;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 2 ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View x;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // only for lollipop and newer versions
            x =  inflater.inflate(R.layout.tab_layout_fab,null);
        else
            x =  inflater.inflate(R.layout.tab_layout,null);

        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);

        /**
         *Set an Apater for the View Pager
         */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        //setupViewPager(viewPager);

        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        //tabLayout.setupWithViewPager(viewPager);

        return x;

    }
    /*
    private void setupViewPager(ViewPager viewPager) {
        MyAdapter adapter = new MyAdapter(getFragmentManager());
        adapter.addFragment(new PazientiFragment(), "Assistiti");
        adapter.addFragment(new MisurazioniFragment(), "Misurazioni");
        viewPager.setAdapter(adapter);
    }
    */

    class MyAdapter extends FragmentPagerAdapter{
        // private final List<Fragment> mFragmentList = new ArrayList<>();
        // private final List<String> mFragmentTitleList = new ArrayList<>();

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : return new PazientiFragment();
                case 1 : return new MisurazioniFragment();
            }
            return null;
        }

        /*
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        */

        @Override
        public int getCount() {

            return int_items;

        }

        /*
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        */

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "Assistiti";
                case 1 :
                    return "Misurazioni";
            }
            return null;
        }

        /*
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        */

    }

}