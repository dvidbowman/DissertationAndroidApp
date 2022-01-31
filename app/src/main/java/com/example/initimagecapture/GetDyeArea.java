package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GetDyeArea extends AppCompatActivity {
    // Controls
    private TextView screenCoord_textView, bitmapCoord_textView;
    private Button analyse_btn;
    private ImageView crop_imgv, dyedArea_imgv;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_dye_area);

        analyse_btn = (Button) findViewById(R.id.button_analyseImage);
        crop_imgv = (ImageView) findViewById(R.id.imageView_crop);
        dyedArea_imgv = (ImageView) findViewById(R.id.imageView_dyedArea);
        screenCoord_textView = (TextView) findViewById(R.id.textView_screenCoord);
        bitmapCoord_textView = (TextView) findViewById(R.id.textView_bitmapCoord);

        Bitmap srcBitmap = BitmapFactory.decodeByteArray(User.getCroppedImageByteArray(), 0, User.getCroppedImageByteArray().length);
        crop_imgv.setImageBitmap(srcBitmap);

        crop_imgv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float eventX = event.getX();
                float eventY = event.getY();
                float[] eventXY = new float[] {eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView) v).getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(eventXY);
                int bitmapX = Integer.valueOf((int) eventXY[0]);
                int bitmapY = Integer.valueOf((int) eventXY[1]);

                if (bitmapX < 0) {
                    bitmapX = 0;
                } else if (bitmapX > srcBitmap.getWidth() - 1) {
                    bitmapX = srcBitmap.getWidth() - 1;
                }

                if (bitmapY < 0) {
                    bitmapY = 0;
                } else if (bitmapY > srcBitmap.getHeight() - 1) {
                    bitmapY = srcBitmap.getHeight() - 1;
                }

                int dyedAreaTopLeftX;
                int dyedAreaTopLeftY;
                int dyedAreaTopRightX;
                int dyedAreaBottomRightY;

                if (bitmapX - 80 < 0) {
                    dyedAreaTopLeftX = 0;
                } else {
                    dyedAreaTopLeftX = bitmapX - 80;
                }

                if (bitmapY - 50 < 0) {
                    dyedAreaTopLeftY = 0;
                } else {
                    dyedAreaTopLeftY = bitmapY - 50;
                }

                if (bitmapX + 80 > srcBitmap.getWidth()) {
                    dyedAreaTopRightX = srcBitmap.getWidth();
                } else {
                    dyedAreaTopRightX = bitmapX + 80;
                }

                if (bitmapY + 80 > srcBitmap.getHeight()) {
                    dyedAreaBottomRightY = srcBitmap.getHeight();
                } else {
                    dyedAreaBottomRightY = bitmapY + 80;
                }

                screenCoord_textView.setText("Screen touch coordinates : " +
                        String.valueOf(eventXY[0]) + ", " + String.valueOf(eventXY[1]));

                bitmapCoord_textView.setText("Bitmap touch coordinates : " +
                        String.valueOf(bitmapX) + ", " + String.valueOf(bitmapY));

                Bitmap areaToAnalyse = Bitmap.createBitmap(srcBitmap, dyedAreaTopLeftX, dyedAreaTopLeftY, (dyedAreaTopRightX - dyedAreaTopLeftX), (dyedAreaBottomRightY - dyedAreaTopLeftY));
                dyedArea_imgv.setImageBitmap(areaToAnalyse);

                return true;
            }
        });
    }
}