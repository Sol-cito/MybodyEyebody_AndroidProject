package com.example.mybodyeyebody;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class PhotoFragment extends Fragment {
    /* FINALS */
    private static final int REQUEST_IMAGE_CAPTURE = -1;
    private static final int SDK_int = Build.VERSION.SDK_INT;

    private ImageView cameraImage;
    private String path_of_takenPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photo, container, false);

        cameraImage = rootView.findViewById(R.id.cameraImage);
        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean hasCamera;
                try {
                    if (SDK_int > 16) {
                        hasCamera = getPackageManagerMethod().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
                    } else {
                        hasCamera = getPackageManagerMethod().hasSystemFeature(PackageManager.FEATURE_CAMERA);
                    }
                    if (hasCamera) {
                        Toast.makeText(getContext(), "카메라 있음", Toast.LENGTH_SHORT).show();
                        dispatchTakePictureIntent();
                    } else {
                        Toast.makeText(getContext(), "카메라 없음...", Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getContext(), "[ERROR] 시스템 에러가 발생하였습니다\n 위치 : PhotoFragment.getPackageManagerMethod()", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        /* To-do */
        /*  1. MainActivity 클릭 안되게 할 것
         *  2. 카메라 어플 사용 가능 설정 - > done
         *  3. API 연동
         *  4. 사진 찍은거 gallery 에 저장 -> done
         *  5. 사진 찍은거 있으면(preference 활용하면 될듯) fragment 안뜨고 Main으로 가게 만들기
         *  6. 뒤로가기 버튼 누를 시 종료 기능 추가 */
        return rootView;
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
                Uri imageUri = FileProvider.getUriForFile(getContext(), "com.example.mybodyeyebody.fileprovider", image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private PackageManager getPackageManagerMethod() throws NullPointerException {
        return getContext().getPackageManager();
    }

    private File createImageFile() throws IOException {
        Date currentDateAndTime = Calendar.getInstance().getTime();
//        String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDateAndTime);
//        String prefix = "[MybodyEybody]" + date;
        String prefix = "test";
        String suffix = ".jpg";
        File directory = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // 안드로이드 환경변수 중 dir path
        File image = File.createTempFile(
                prefix, // @NotNull String prefix
                suffix, // String  Suffix
                directory// directory
        );
        path_of_takenPhoto = image.getAbsolutePath();
        return image;
    }
}
