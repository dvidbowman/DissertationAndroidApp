package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class GetDyeArea extends AppCompatActivity {
    // Controls
    private TextView screenCoord_txtv, bitmapCoord_txtv, reactivePCO2_txtv, nonreactivePCO2_txtv;
    private Button analyse_btn, back_btn;
    private ImageView croppedReactive_imgv, dyedArea_imgv;

    Bitmap reactiveBitmap = BitmapFactory.decodeByteArray(User.getCroppedReactiveByteArray(), 0, User.getCroppedReactiveByteArray().length);
    Bitmap nonreactiveBitmap = BitmapFactory.decodeByteArray(User.getCroppedNonReactiveByteArray(), 0, User.getCroppedNonReactiveByteArray().length);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_dye_area);

        reactivePCO2_txtv = (TextView) findViewById(R.id.textView_reactivePCO2);
        nonreactivePCO2_txtv = (TextView) findViewById(R.id.textView_nonreactivePCO2);
        analyse_btn = (Button) findViewById(R.id.button_analyseImage);
        back_btn = (Button) findViewById(R.id.button_backDyeArea);
        croppedReactive_imgv = (ImageView) findViewById(R.id.imageView_crop);

        croppedReactive_imgv.setImageBitmap(reactiveBitmap);

        reactivePCO2_txtv.setText("Reactive Calculated %CO2: " + String.valueOf(getCO2Percentage(reactiveBitmap)));
        nonreactivePCO2_txtv.setText("Non-reactive Calculated %CO2: " + String.valueOf(getCO2Percentage(nonreactiveBitmap)));

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getCameFromCamera()) {
                    openImageCaptureActivity();
                }
                else {
                    openImportImageActivity();
                }
            }
        });

    }

    public double getCO2Percentage(Bitmap bmp) {
        double redTotal = 0;

        if (User.getSaveRGBValues()) {
            File sdCard = android.os.Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/documents");
            dir.mkdirs();
            //String currentDateTime = new java.text.SimpleDateFormat("dd-MM-yyyy-HH.mm.ss").format(new Date());

            File file = new File(dir, "rgbValues.txt");

            try {
                FileOutputStream fos = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(fos);

                for (int y = 0; y < bmp.getHeight(); y++) {           // Currently uses every pixel in the Bitmap
                    for (int x = 0; x < bmp.getWidth(); x++) {
                        int pixelColours = bmp.getPixel(x, y);
                        pw.println(Color.red(pixelColours) + ",");
                        redTotal += Color.red(pixelColours);
                    }
                }

                pw.flush();
                pw.close();
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            for (int y = 0; y < bmp.getHeight(); y++) {           // Currently uses every pixel in the Bitmap
                for (int x = 0; x < bmp.getWidth(); x++) {
                    int pixelColours = bmp.getPixel(x, y);
                    redTotal += Color.red(pixelColours);
                }
            }
        }

        double redAverage = redTotal / (bmp.getWidth() * bmp.getHeight());

        double redBy255 = redAverage / 255;
        double uncorrectedGamma;

        if (redBy255 > 0.04045) {
            uncorrectedGamma = Math.pow(((redBy255 + 0.055) / 1.055), 2.4);
        }
        else {
            uncorrectedGamma = redBy255 / 12.92;
        }

        double correctedRed = uncorrectedGamma * 255;

        double noToLog = (255 / correctedRed);

        double appAbsorbance = Math.log10(noToLog);     // Excel uses Log10 by default

        double percentageCO2 = (1.21 - appAbsorbance) / ((appAbsorbance - 0.14) * 16);

        return (double) Math.round(percentageCO2 * 100000d) / 100000d;
        // 1.21 was the apparent absorbance under 0% CO2
        // 0.14 was the apparent absorbance under 100% CO2
    }

    private void openImportImageActivity() {
        Intent intent = new Intent(this, ImportImage.class);
        startActivity(intent);
    }

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

}