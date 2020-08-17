package com.example.mybodyeyebody;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private PhotoFragment photoFragment;
    private ShowResultFragment showResultFragment;

    private long keyPressInterval = 1500;
    private long backKeyPressedTime;
    private SharedPreferences sharedPreferences;
    private static final String SHAREDPREFERENCE_NAME = "sharedPreferenceForProfile";

    private Button tryAgainButton;
    private Button goToTakePictureButton;
    private Button displaySettingResult;

    private LinearLayout layoutIfProfileExists;
    private LinearLayout layoutIfNoProfile;

    private Spinner fatPercentageSpinner;

    private int numOfSpinnerPosition;
    private int genderCode; // 1 : male , 2 : female

    private RadioGroup radioGroup_gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutIfProfileExists = findViewById(R.id.layoutIfProfileExists);
        layoutIfNoProfile = findViewById(R.id.layoutIfNoProfile);

        if (getStringFromSharedPreference() == "") {
            showNoProfileLayout();
            replaceToFragment("PhotoFragment"); // 사진이 preference에 없으면 fragment 호출
        } else {
            showProfileLayout();
        }

        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceToFragment("PhotoFragment");
            }
        });

        goToTakePictureButton = findViewById(R.id.goToTakePictureButton);
        goToTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceToFragment("PhotoFragment");
            }
        });

        radioGroup_gender = findViewById(R.id.radioGroup_gender);
        radioGroup_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton_male) {
                    genderCode = 1;
                } else {
                    genderCode = 2;
                }
            }
        });

        fatPercentageSpinner = findViewById(R.id.fatPercentageSpinner);
        fatPercentageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                numOfSpinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        displaySettingResult = findViewById(R.id.displaySettingResult);
        displaySettingResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (genderCode == 0) {
                    Toast.makeText(MainActivity.this, "성별을 선택해주소", Toast.LENGTH_SHORT).show();
                } else if (numOfSpinnerPosition == 0) {
                    Toast.makeText(MainActivity.this, "체지방률 선택해주소", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("log", "????");
                    replaceToFragment("ShowResultFragment");
                }
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

    private void replaceToFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (tag == "PhotoFragment") {
            photoFragment = new PhotoFragment();
            fragmentTransaction.replace(R.id.container, photoFragment, tag);
        } else if (tag == "ShowResultFragment") {
            showResultFragment = new ShowResultFragment();
            fragmentTransaction.replace(R.id.container, showResultFragment, tag);
        }
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
