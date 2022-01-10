package com.example.initimagecapture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.hardware.camera2.*;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ImageCapture extends AppCompatActivity {
    private Button back_btn, camera_btn, save_btn;
    private ImageView shownImage_imgv;
    private Bitmap currentImageBitmap;


    private static final int REQUEST_IMAGE_CAPTURE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);



        back_btn = (Button) findViewById(R.id.button_backCapture);
        shownImage_imgv = findViewById(R.id.imageView_capturedImage);
        camera_btn = findViewById(R.id.button_useCamera);
        save_btn = findViewById(R.id.button_saveImage);



        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                if (checkCameraHardware(getApplicationContext())) {

                } else {
                    Toast.makeText(getApplicationContext(), "This device has no available camera", Toast.LENGTH_SHORT);
                }

                 */

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shownImage_imgv.getDrawable() == null) {
                    Toast.makeText(getApplicationContext(), "No image to upload", Toast.LENGTH_SHORT).show();
                }
                else {
                    makeRequest imageUploadRequest = new makeRequest("http://192.168.0.29/projectPHP/imageupload.php", "POST", "imageUpload", bitmapToByte(currentImageBitmap));
                    if (imageUploadRequest.startRequest()) {
                        if(imageUploadRequest.onComplete()) {

                            try {
                                JSONObject obj = new JSONObject(imageUploadRequest.getResult());

                                if (obj.getString("message").equals("none")) {
                                    //shownImage_imgv.setImageResource(android.R.color.transparent);
                                    shownImage_imgv.setImageDrawable(null);
                                    Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                                    User.setUserImageNo(User.getUserImageNo() + 1);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error Uploading Image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            currentImageBitmap = imageBitmap;
            shownImage_imgv.setImageBitmap(imageBitmap);
        }

    }

    private byte[] bitmapToByte(Bitmap bmp) {

        ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bOutputStream);

        return bOutputStream.toByteArray();
    }

    // Check for camera on device
    private boolean checkCameraHardware(Context c) {
        if (c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        else {
            return false;
        }
    }

    private static CameraDevice getCameraInstance() {
        CameraDevice c = null;

        try {

        } catch (Exception e) {

        }

        return c;
    }
}