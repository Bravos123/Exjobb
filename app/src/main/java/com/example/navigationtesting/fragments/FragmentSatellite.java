package com.example.navigationtesting.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.navigationtesting.R;


public class FragmentSatellite extends Fragment {


    public FragmentSatellite() {
        // Required empty public constructor
    }

    public static FragmentSatellite newInstance(int satelliteNumber) {
        FragmentSatellite fragment = new FragmentSatellite();
        Bundle args = new Bundle();
        args.putInt("satellite number", satelliteNumber);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle args = getArguments();
        int satellitsNumber = args.getInt("satellite number");

        View v = inflater.inflate(R.layout.fragment_fragment_satellite, container, false);

        TextView tst = v.findViewById(R.id.testTextVIew);
        tst.setText("Satellite number: "+Integer.toString(satellitsNumber));

        return v;
    }




}
