package com.example.initimagecapture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

                    ImageUpload imageUpload = new ImageUpload("http://192.168.0.29/ImageUpload/imageupload.php", "POST", bitmapToByte(currentImageBitmap));
                    if(imageUpload.startPut()) {
                        if(imageUpload.onComplete()) {
                            String result = imageUpload.getResult();

                            if(result.equals("Image uploaded successfully")) {
                                shownImage_imgv.setImageResource(android.R.color.transparent);
                                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
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
}