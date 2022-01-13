package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ImageAnalysis extends AppCompatActivity {
    private Button discard_btn, save_btn;
    private ImageView preview_imgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        discard_btn = findViewById(R.id.button_discardImage);
        save_btn = findViewById(R.id.button_saveImage);
        preview_imgv = findViewById(R.id.imageView_preview);

        Bitmap bitmap = BitmapFactory.decodeByteArray(User.getUserByteArray(), 0, User.getUserByteArray().length);
        preview_imgv.setImageBitmap(bitmap);

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    save(User.getUserByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        discard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageCaptureActivity();
            }
        });

    }

    private void save(byte[] bytes) throws IOException {
        makeRequest imageUploadRequest = new makeRequest("http://192.168.0.29/projectPHP/imageupload.php", "POST", "imageUpload", bytes);
        if (imageUploadRequest.startRequest()) {
            if(imageUploadRequest.onComplete()) {

                try {
                    JSONObject obj = new JSONObject(imageUploadRequest.getResult());
                    if (obj.getString("message").equals("none")) {
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                        User.setUserImageNo(User.getUserImageNo() + 1);
                        openImageCaptureActivity();
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

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }
}