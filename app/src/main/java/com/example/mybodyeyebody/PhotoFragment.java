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
    private static final String PREFERENCE = "getSharedPreference";

    private static final int SDK_int = Build.VERSION.SDK_INT;

    private ImageView cameraImage;
    private String path_of_takenPhoto;
    private Uri imageUri;

    private LinearLayout topLayoutOfFragment;

    private int cameraPermissionCheck;

    /* test image view to put photo taken */
    private ImageView testImageView;

    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photo, container, false);

        testImageView = rootView.findViewById(R.id.testImageView); // thumbnail insert
        sharedPreferences = getContext().getSharedPreferences("imagePreference", Activity.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("thumbnailExists", false)) {
            Log.e("log", "사진 저장되어있음");
            getThumbnail();
            /* 찍은 썸네일을 file path로 불러와서 imageView에 뿌려주고, 필요없는 다른 UI들은 숨김처리한다. */
            /* 사진 찍은 후 얼굴인식 API 적용 */
            /* 썸네일 보여주면서 "이 사진을 대상으로 하시겠습니까?" 하고 확인 버튼 만듬 -> 누르면 MainActivity로 넘어가고, Main에서 설문조사 시작*/
            /* '사진 다시찍기' 버튼을 만들어 사진을 다시찍는 기능 추가 */
        }

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
                Toast.makeText(getContext(), "카메라 없음...", Toast.LENGTH_SHORT).show();
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
            File image = null;
            try {
                image = createImageFile();
            } catch (IOException e) {
                Toast.makeText(getContext(), "[ERROR] 시스템 에러가 발생하였습니다\n 위치 : PhotoFragment.dispatchTakePictureIntent()", Toast.LENGTH_SHORT).show();
            }
            if (image != null) {
                imageUri = FileProvider.getUriForFile(getContext(), "com.example.mybodyeyebody.fileprovider", image);
                Log.e("log", "이미지 Uri : " + imageUri);
                takePictureIntent.putExtra("data", imageUri);
/*                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
        File image = File.createTempFile(
                prefix, // @NotNull String prefix
                suffix, // String  Suffix
                directory// directory
        );
        path_of_takenPhoto = image.getAbsolutePath();
        Log.e("log", "사진 찍었을 때 경로 : " + path_of_takenPhoto);
        /* test */
        SharedPreferences.Editor editor = sharedPreferences.edit(); // test
        editor.putString("thumbnail_path", image.getAbsolutePath()); // 사진 찍었으면 true로 저장
        editor.commit();
        return image;
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
                Toast.makeText(getContext(), "썸네일 널임", Toast.LENGTH_SHORT).show();
            } else { // 썸네일 세팅
                Toast.makeText(getContext(), "썸네일 널 아님", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    testImageView.setImageBitmap(bitmap);
                    SharedPreferences.Editor editor = sharedPreferences.edit(); // test
                    editor.putBoolean("thumbnailExists", true); // 사진 찍었으면 true로 저장
                    editor.commit();
                } else {
                    Log.e("log", "비트맵 널임. data : ");
                }
            }
        }
    }

    public void getThumbnail() {
        String path = sharedPreferences.getString("thumbnail_path", "");
        File file = new File(path);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Log.e("log", "앱솔 패스 : " + path);
            if (bitmap == null) {
                Log.e("log", "비트맵 널임");
            }
            testImageView.setImageBitmap(bitmap);
        }
    }
}
