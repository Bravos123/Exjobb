package com.example.navigationtesting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.navigationtesting.fragments.FragmentSatellite;

import java.util.ArrayList;
import java.util.Comparator;

public class SatelliteFragPagerAdapter extends FragmentPagerAdapter {
    private int numberOfSatellites;
    ArrayList<GalileoSatelliteData> galileoSatellites;

    public SatelliteFragPagerAdapter(FragmentManager fm) {
        super(fm);


    }

    @Override
    public Fragment getItem(int position) {
        Log.i("Project", "getItem: "+position);
        return FragmentSatellite.newInstance(galileoSatellites.get(position).getSvid());
    }

    @Override
    public int getCount() {
        return numberOfSatellites;
    }

    public void updateSatellitesDate(ArrayList<GalileoSatelliteData> galileoSatellites){
        this.galileoSatellites = galileoSatellites;
        this.galileoSatellites.sort(new Comparator<GalileoSatelliteData>() {
            @Override
            public int compare(GalileoSatelliteData o1, GalileoSatelliteData o2) {
                return o1.getSvid() - o2.getSvid();
            }
        });
        numberOfSatellites = galileoSatellites.size();
    }


}
