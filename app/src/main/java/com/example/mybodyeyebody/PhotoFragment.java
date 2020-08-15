package com.example.mybodyeyebody;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class PhotoFragment extends Fragment {
    /* FINALS */
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1000; // my own request code
    private static final String PREFERENCE = "getSharedPreference";

    private static final int SDK_int = Build.VERSION.SDK_INT;

    private ImageView cameraImage;
    private String path_of_takenPhoto;
    private Uri imageUri;

    private LinearLayout topLayoutOfFragment;

    private int cameraPermissionCheck;

    /* test */
    private ImageView testImageView;

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
        /* To-do */
        /* https://developer.android.com/training/camera/photobasics.html#java */
        /*  3. API 연동
         *  5. 사진 찍은거 있으면(preference 활용하면 될듯) fragment 안뜨고 Main으로 가게 만들기 */

        /* test */
        testImageView = rootView.findViewById(R.id.testImageView);


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
                imageUri = FileProvider.getUriForFile(getContext(), "com.example.mybodyeyebody.fileprovider", imageFile);
                Log.e("log", "이미지 Uri : " + imageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                /*
                --> putExtra를 쓰면 camera는 imageUri 에 captured image를 write한다.
                따라서 아래 onActivityResult 에서의 getExtras는 null이 되며, 썸네일을 잡지 못한다.
                썸네일을 잡으려면 default로 보내야 함.
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
        path_of_takenPhoto = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Toast.makeText(getContext(), "당신의 면상을 찍어주세요!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getContext(), "[ERROR] 사진을 불러오는 데 실패하였습니다. \n 위치 : PhotoFragment.onActivityResult()", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("log", "path_of_takenPhoto : " + path_of_takenPhoto);
                File file = new File(path_of_takenPhoto);
                if (file != null) {
                    Log.e("log", "파일 널 아님!!!!!!!!!!!!!!1");
                    try {
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                        if (bitmap != null) {
                            Log.e("log", "비트맵 널 아님");
                            testImageView.setImageBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                finishFragment();
            }
        }
    }

    public void finishFragment() {
        Log.e("log", "finish Fragment");
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().detach(PhotoFragment.this).commit();
    }
}
