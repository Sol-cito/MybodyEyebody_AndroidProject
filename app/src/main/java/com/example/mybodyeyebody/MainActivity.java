package com.example.mybodyeyebody;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private PhotoFragment photoFragment;
    private long keyPressInterval = 1500;
    private long backKeyPressedTime;
    private SharedPreferences sharedPreferences;
    private static final String SHAREDPREFERENCE_NAME = "sharedPreferenceForProfile";

    private Button tryAgainButton;
    private Button goToTakePictureButton;

    private LinearLayout layoutIfProfileExists;
    private LinearLayout layoutIfNoProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutIfProfileExists = findViewById(R.id.layoutIfProfileExists);
        layoutIfNoProfile = findViewById(R.id.layoutIfNoProfile);

        if (getStringFromSharedPreference() == "") {
            showNoProfileLayout();
            replaceToFragment(); // 사진이 preference에 없으면 fragment 호출
        } else {
            showProfileLayout();
        }

        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceToFragment();
            }
        });

        goToTakePictureButton = findViewById(R.id.goToTakePictureButton);
        goToTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceToFragment();
            }
        });
    }

    public void showProfileLayout() {
        layoutIfProfileExists.setVisibility(View.VISIBLE);
        layoutIfNoProfile.setVisibility(View.GONE);
        displayImageOnScreen();
    }

    public void showNoProfileLayout() {
        layoutIfProfileExists.setVisibility(View.GONE);
        layoutIfNoProfile.setVisibility(View.VISIBLE);
    }

    private void replaceToFragment() {
        photoFragment = new PhotoFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, photoFragment, "PhotoFragment");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (photoFragment == null || !photoFragment.isAdded()) {
            if (System.currentTimeMillis() - backKeyPressedTime > keyPressInterval) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "뒤로가기를 한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
            }
        } else {
            setDialogBuilder(1);
        }
    }

    public void setDialogBuilder(int flag) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (flag == 1) {
            alertDialogBuilder.setTitle("사진 찍기 종료");
            alertDialogBuilder.setMessage("사진 찍기를 취소하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(photoFragment);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    fragmentTransaction.commit();
                }
            });
        }
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    public void createSharedPreference() {
        sharedPreferences = getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void setSharedPreferences(String key, String value) {
        createSharedPreference();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringFromSharedPreference() {
        createSharedPreference();
        return sharedPreferences.getString("imageUri", "");
    }

    public void displayImageOnScreen() {
        String imageUri = getStringFromSharedPreference();
        Uri uri = Uri.parse(imageUri);
        if (uri != null) {
            CircleImageView testCircleView = findViewById(R.id.testCircleView);
            testCircleView.setImageURI(uri);
            Log.e("log", "(MainActivity) Uri 널 아님");
        } else {
            Log.e("log", "(MainActivity) Uri 널임");
        }
    }
}
