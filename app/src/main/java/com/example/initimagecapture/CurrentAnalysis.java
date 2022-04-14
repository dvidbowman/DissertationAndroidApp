package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrentAnalysis extends AppCompatActivity {
    // Controls
    private TextView reactivePCO2_txtv, nonreactivePCO2_txtv;
    private Button back_btn, save_btn;
    private ImageView croppedReactive_imgv;

    Bitmap reactiveBitmap = BitmapFactory.decodeByteArray(User.getCroppedReactiveByteArray(), 0, User.getCroppedReactiveByteArray().length);
    Bitmap nonreactiveBitmap = BitmapFactory.decodeByteArray(User.getCroppedNonReactiveByteArray(), 0, User.getCroppedNonReactiveByteArray().length);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_analysis);

        reactivePCO2_txtv = (TextView) findViewById(R.id.textView_reactivePCO2);
        nonreactivePCO2_txtv = (TextView) findViewById(R.id.textView_nonreactivePCO2);
        save_btn = (Button) findViewById(R.id.button_saveAnalysis);
        back_btn = (Button) findViewById(R.id.button_backDyeArea);
        croppedReactive_imgv = (ImageView) findViewById(R.id.imageView_crop);

        if (!User.getLoggedIn()) {
            save_btn.setVisibility(View.INVISIBLE);
        }

        croppedReactive_imgv.setImageBitmap(reactiveBitmap);

        reactivePCO2_txtv.setText("Reactive Calculated %CO2: " + String.valueOf(operationsUtility.getCalculatedCO2Percentage(reactiveBitmap)));
        nonreactivePCO2_txtv.setText("Non-reactive Calculated %CO2: " + String.valueOf(operationsUtility.getCalculatedCO2Percentage(nonreactiveBitmap)));

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getCameFromCamera()) {
                    openImageCaptureActivity();
                }
                else {
                    openImageImportActivity();
                }
            }
        });

    }

    private void openImageImportActivity() {
        Intent intent = new Intent(this, ImageImport.class);
        startActivity(intent);
    }

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

}