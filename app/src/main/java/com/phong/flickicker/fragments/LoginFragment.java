package com.phong.flickicker.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.phong.flickicker.R;
import com.phong.flickicker.tasks.OAuthTask;

/**
 * Created by Phong on 9/13/2015.
 */
public class LoginFragment extends Fragment {
    public LoginFragment(){

    }
    public static Fragment newInstance() {
        Fragment fragment = new LoginFragment();

        return fragment;
    }
    Button mLoginBtn;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_layout, container, false);
        mLoginBtn = (Button) rootView.findViewById(R.id.login_buton);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OAuthTask task = new OAuthTask(getActivity());
		        task.execute();
            }
        });
        return rootView;
    }
}
