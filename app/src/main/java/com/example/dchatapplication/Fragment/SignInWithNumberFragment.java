package com.example.dchatapplication.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dchatapplication.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInWithNumberFragment extends Fragment {


    public SignInWithNumberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in_with_number, container, false);
    }

}
