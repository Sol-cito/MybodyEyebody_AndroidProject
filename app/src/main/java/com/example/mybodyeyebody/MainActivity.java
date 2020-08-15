package com.example.mybodyeyebody;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private PhotoFragment photoFragment;
    private long keyPressInterval = 1500;
    private long backKeyPressedTime;
    private SharedPreferences sharedPreferences;
    private static final String SHAREDPREFERENCE_NAME = "sharedPreferenceForProfile";
    private String path_of_takenPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getStringFromSharedPreference() == "") {
            replaceToFragment(); // 사진이 preference에 없으면 fragment 호출
        } else {
            displayImageOnScreen();
        }
    }

    private void replaceToFragment() {
        photoFragment = new PhotoFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, photoFragment, "PhotoFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backKeyPressedTime > keyPressInterval) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "뒤로가기를 한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
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
        path_of_takenPhoto = sharedPreferences.getString("path_of_takenPhoto", "");
        return path_of_takenPhoto;
    }

    public void displayImageOnScreen() {
        File file = new File(path_of_takenPhoto);
        if (file != null) {
            Log.e("log", "(MainActivity) path_of_takenPhoto : " + path_of_takenPhoto);
            Log.e("log", "(MainActivity) 파일 널 아님");
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
                if (bitmap != null) {
                    Log.e("log", "비트맵 널 아님");
                    ImageView testImageView = findViewById(R.id.testImageView);
                    testImageView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("log", "(MainActivity) 파일 널임");
        }
    }
}
