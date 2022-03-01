package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GetDyeArea extends AppCompatActivity {
    // Controls
    private TextView screenCoord_textView, bitmapCoord_textView, percentCO2_textView;
    private Button analyse_btn;
    private ImageView crop_imgv, dyedArea_imgv;

    Bitmap srcBitmap = BitmapFactory.decodeByteArray(User.getCroppedImageByteArray(), 0, User.getCroppedImageByteArray().length);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_dye_area);

        percentCO2_textView = (TextView) findViewById(R.id.textView_PCO2);
        analyse_btn = (Button) findViewById(R.id.button_analyseImage);
        crop_imgv = (ImageView) findViewById(R.id.imageView_crop);

        percentCO2_textView.setText("Calculated %CO2: " + String.valueOf(getCO2Percentage()));

        crop_imgv.setImageBitmap(srcBitmap);

        /*  Code for Condensing down Dyed Area
        dyedArea_imgv = (ImageView) findViewById(R.id.imageView_dyedArea);
        screenCoord_textView = (TextView) findViewById(R.id.textView_screenCoord);
        bitmapCoord_textView = (TextView) findViewById(R.id.textView_bitmapCoord);      <-- Add these controls back on the .xml

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
                    dyedAreaTopRightX = 160;
                }
                else if (bitmapX + 80 > srcBitmap.getWidth()) {
                    dyedAreaTopRightX = srcBitmap.getWidth();
                    dyedAreaTopLeftX = srcBitmap.getWidth() - 160;
                }
                else {
                    dyedAreaTopLeftX = bitmapX - 80;
                    dyedAreaTopRightX = bitmapX + 80;
                }

                if (bitmapY - 50 < 0) {
                    dyedAreaTopLeftY = 0;
                    dyedAreaBottomRightY = 100;
                }
                else if (bitmapY + 50 > srcBitmap.getHeight()) {
                    dyedAreaBottomRightY = srcBitmap.getHeight();
                    dyedAreaTopLeftY = srcBitmap.getHeight() - 100;
                }
                else{
                    dyedAreaTopLeftY = bitmapY - 50;
                    dyedAreaBottomRightY = bitmapY + 50;
                }

                screenCoord_textView.setText("Screen touch coordinates : " +
                        String.valueOf(eventXY[0]) + ", " + String.valueOf(eventXY[1]));

                bitmapCoord_textView.setText("Bitmap touch coordinates : " +
                        String.valueOf(bitmapX) + ", " + String.valueOf(bitmapY));

                Bitmap areaToAnalyse = Bitmap.createBitmap(srcBitmap, dyedAreaTopLeftX, dyedAreaTopLeftY, (dyedAreaTopRightX - dyedAreaTopLeftX), (dyedAreaBottomRightY - dyedAreaTopLeftY));
                dyedArea_imgv.setImageBitmap(areaToAnalyse);

                //int[] pixelsToAnalyse = {};
                //areaToAnalyse.getPixels(pixelsToAnalyse, 0, areaToAnalyse.getWidth(), 0, 0, areaToAnalyse.getWidth(), areaToAnalyse.getHeight());

                return true;
            }
        });

         */
    }

    public double getCO2Percentage() {
        //int colours = srcBitmap.getPixel(srcBitmap.getWidth() / 2, srcBitmap.getHeight() / 2);
        //double redValue = Color.red(colours);
        //int greenValue = Color.green(colours);
        //int blueValue = Color.blue(colours);
        double redValue = 220.758;

        double redBy255 = redValue / 255;
        double uncorrectedGamma;

        if (redBy255 > 0.04045) {
            uncorrectedGamma = Math.pow(((redBy255 + 0.055) / 1.055), 2.4);;
        }
        else {
            uncorrectedGamma = redBy255 / 12.92;
        }

        double correctedRed = uncorrectedGamma * 255;

        double noToLog = (255 / correctedRed);

        double appAbsorbance = Math.log10(noToLog);     // Excel uses Log10 by default

        return (1.21 - appAbsorbance) / ((appAbsorbance - 0.14) * 16);
    }
}