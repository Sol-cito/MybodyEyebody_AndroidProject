package com.example.mybodyeyebody;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private PhotoFragment photoFragment;
    private long keyPressInterval = 1500;
    private long backKeyPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceToFragment(); // 사진이 DB에 없으면 fragment 호출
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
}
