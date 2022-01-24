package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GetDyeArea extends AppCompatActivity {
    // Controls
    private TextView disp_textView;
    private Button analyse_btn;
    private ImageView crop_imgv;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_dye_area);

        analyse_btn = (Button) findViewById(R.id.button_analyseImage);
        crop_imgv = (ImageView) findViewById(R.id.imageView_crop);
        disp_textView = (TextView) findViewById(R.id.textView_disp);

        Bitmap bitmap = BitmapFactory.decodeByteArray(User.getCroppedImageByteArray(), 0, User.getCroppedImageByteArray().length);
        crop_imgv.setImageBitmap(bitmap);

        crop_imgv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                disp_textView.setText("Touch coordinates : " +
                        String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()));

                return true;
            }
        });
    }
}