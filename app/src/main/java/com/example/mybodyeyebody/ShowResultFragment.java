package com.example.mybodyeyebody;

import android.app.Activity;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowResultFragment extends Fragment {

    private LinearLayout topLayoutOfFragment_show_result;
    private CircleImageView circleView_showResult;
    private ImageView bodyPicture;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_show_result, container, false);

        /* consume the touch event so it can't touch the main activity */
        topLayoutOfFragment_show_result = rootView.findViewById(R.id.topLayoutOfFragment_show_result);
        topLayoutOfFragment_show_result.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        circleView_showResult = rootView.findViewById(R.id.circleView_showResult);
        bodyPicture = rootView.findViewById(R.id.bodyPicture);
        displayBodyPicture();
        return rootView;
    }

    public void showProfileOnDisplay() {
        String imageUri = ((MainActivity) getActivity()).getStringFromSharedPreference("imageUri");
        Uri uri = Uri.parse(imageUri);
        if (uri != null) {
            circleView_showResult.setImageURI(uri);
            Log.e("log", "ShowResultFragment Uri 널 아님");
        } else {
            Log.e("log", "ShowResultFragment Uri 널임");
        }
    }

    public void displayBodyPicture() {
        String genderCode = ((MainActivity) getActivity()).getStringFromSharedPreference("genderCode");
        if (genderCode.equals("male")) {
            bodyPicture.setImageResource(R.drawable.test_20percent_male);
        } else {
            bodyPicture.setImageResource(R.drawable.test_20percent_female);
        }
        showProfileOnDisplay();
    }
}
