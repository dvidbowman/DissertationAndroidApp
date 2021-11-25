package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Analyses extends AppCompatActivity {
    private Button back_btn, getImage_btn;
    private ImageView imgv_shownImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyses);
        imgv_shownImage = findViewById(R.id.imageView_shownImg);

        back_btn = (Button) findViewById(R.id.button_backAnalyses);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        getImage_btn = (Button) findViewById(R.id.button_getImage);
        getImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getUserImages getUserImages = new getUserImages("http://192.168.0.29/projectPHP/getuserimages.php", "POST");
                        if(getUserImages.startPut()) {
                            if (getUserImages.onComplete()) {

                                if (getUserImages.getResult().equals("empty")) {
                                    Toast.makeText(getApplicationContext(), "Not working", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), getUserImages.getResult(), Toast.LENGTH_LONG).show();
                                    byte[] imgByteArray = Base64.decode(getUserImages.getResult(), Base64.DEFAULT);
                                    Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length);
                                    imgv_shownImage.setImageBitmap(imgBitmap);
                                }

                            }
                        }
                    }
                });
            }
        });
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}