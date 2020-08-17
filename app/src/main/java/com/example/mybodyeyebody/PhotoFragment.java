package com.example.mybodyeyebody;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class PhotoFragment extends Fragment {
    /* FINALS */
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1000; // my own request code

    private static final int SDK_int = Build.VERSION.SDK_INT;

    private ImageView cameraImage;
    private Uri imageUri;

    private LinearLayout topLayoutOfFragment;

    private int cameraPermissionCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photo, container, false);

        /* consume the touch event so it can't touch the main activity */
        topLayoutOfFragment = rootView.findViewById(R.id.topLayoutOfFragment);
        topLayoutOfFragment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        cameraImage = rootView.findViewById(R.id.cameraImage);
        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* check if camera permission has been granted. If not, ask user's permission */
                cameraPermissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
                if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    checkIfCameraExists();
                }
            }
        });
        return rootView;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkIfCameraExists();
    }

    private void checkIfCameraExists() {
        final boolean hasCamera;
        try {
            if (SDK_int > 16) {
                hasCamera = getPackageManagerMethod().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
            } else {
                hasCamera = getPackageManagerMethod().hasSystemFeature(PackageManager.FEATURE_CAMERA);
            }
            if (hasCamera) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "[ERROR] 카메라를 불러오는 데 실패하였습니다\n 위치 : PhotoFragment.checkIfCameraExists()", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[ERROR] 시스템 에러가 발생하였습니다\n 위치 : PhotoFragment.getPackageManagerMethod()", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /* get a camera feature by using an intent */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManagerMethod()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(getContext(), "[ERROR] 시스템 에러가 발생하였습니다\n 위치 : PhotoFragment.dispatchTakePictureIntent()", Toast.LENGTH_SHORT).show();
            }
            if (imageFile != null) {
                imageUri = FileProvider.getUriForFile(getContext(), "com.example.mybodyeyebody", imageFile);
                Log.e("log", "이미지 Uri : " + imageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                /*
                --> putExtra를 쓰면 camera는 imageUri 에 captured image를 write한다.
                따라서 아래 onActivityResult 에서의 getExtras는 null이 되며, 썸네일을 잡지 못한다.
                썸네일을 잡으려면 default로 보내야 함.
                그러나 사진 원본을 띄우려면 imageUri 가 필요하므로, putExtra를 넣어준다.
                참고 : https://stackoverflow.com/questions/9890757/android-camera-data-intent-returns-null
                */
                if (takePictureIntent.resolveActivity(getPackageManagerMethod()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private PackageManager getPackageManagerMethod() throws NullPointerException {
        return getContext().getPackageManager();
    }

    private File createImageFile() throws IOException {
        Date currentDateAndTime = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDateAndTime);
        String prefix = "[MybodyEyebody]" + date;
        String suffix = ".jpg";
        File directory = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // 안드로이드 환경변수 중 dir path
        File imageFile = File.createTempFile(
                prefix, // @NotNull String prefix
                suffix, // String  Suffix
                directory// directory
        );
        Log.e("log", "createImageFile에서 파일 dir 경로 : " + directory.toString());
        return imageFile;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Toast.makeText(getContext(), "당신의 얼굴을 찍어주세요!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.e("log", "imageURI : " + imageUri.toString());
            ((MainActivity) getActivity()).setSharedPreferences("imageUri", imageUri.toString());
            ((MainActivity) getActivity()).displayImageOnScreen();
            finishFragment();
        } else {
            Toast.makeText(getContext(), "[ERROR] 사진을 가져오는 데 실패하였습니다. \n 위치 : PhotoFragment.onActivityResult()", Toast.LENGTH_SHORT).show();
            Log.e("log", "Request has not succeeded");
        }
    }

    public void finishFragment() {
        Log.e("log", "finish Fragment");
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(PhotoFragment.this);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    /* To do */
    /* 1. 사진 크기 조절해서 일정 위치에 놓기
       2. 사진 동그랗게 만들기
    *  3. 성별, 체지방률 설정 버튼 만들기
    *  4. 성별, 체지방률 설정 버튼 누르면 다른 프래그먼트 띄우기
    *  5. 설정 프래그먼트에서 데이터 설정하면 사진 합성하기 ->> 끝!
    *  6. 보너스 : 얼굴인식 API + 사진 crop + 병맛 motivation fragment 만들기 */
}
