package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;

public class ImageManipulation extends AppCompatActivity {
    // Controls
    private TextView reactiveLabel_txtv, warpedLabel_txtv, failLabel1_txtv, failLabel2_txtv;
    private Button discard_btn, next_btn;
    private ImageView preview_imgv, detection_imgv, constant_imgv, main_imgv;
    public static Bitmap initResult, mainResult, constantResult;
    private static Bitmap mainCropped, constantCropped;
    public static int detectionCounter = 0;
    private static boolean detectionFailed = false;
    private boolean cameFromCamera;

    private byte[] initialByteArray;
    private byte[] croppedReactiveByteArray;
    private byte[] croppedNonReactiveByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manipulation);
        detectionCounter = 0;
        detectionFailed = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cameFromCamera = extras.getBoolean("cameFromCamera");
            initialByteArray = extras.getByteArray("initialByteArray");
        }

        // Control Definition
        warpedLabel_txtv = findViewById(R.id.textView_WarpedImage);
        reactiveLabel_txtv = findViewById(R.id.textView_ReactiveAreas);
        failLabel1_txtv = findViewById(R.id.textView_failLabel1);
        failLabel2_txtv = findViewById(R.id.textView_failLabel2);
        failLabel1_txtv.setVisibility(View.INVISIBLE);
        failLabel2_txtv.setVisibility(View.INVISIBLE);
        discard_btn = findViewById(R.id.button_discardImage);
        next_btn = findViewById(R.id.button_nextImageAnalysis);
        preview_imgv = findViewById(R.id.imageView_preview);
        detection_imgv = findViewById(R.id.imageView_detection);
        constant_imgv = findViewById(R.id.imageView_constantReact);
        main_imgv = findViewById(R.id.imageView_mainReact);

        // Sets Bitmap of preview ImageView using bytearray of image just taken
        Bitmap bitmap = BitmapFactory.decodeByteArray(initialByteArray, 0, initialByteArray.length);
        preview_imgv.setImageBitmap(bitmap);

        // Automatic Rectangle Detection and Display of Cropped Images
        try {
            Bitmap initPass = detectionUtility.findRectangle(bitmap);       // Initial Bandage Outline detection
            preview_imgv.setImageBitmap(initPass);
            detection_imgv.setImageBitmap(initResult);

            if (initResult == null) {
                detectionFailed = true;
            }
            else {
                // New Bitmap created that only contains reactive patch
                Bitmap mainReactBmp = Bitmap.createBitmap(initResult, 10, 250, initResult.getWidth() - 10, initResult.getHeight() - 250);
                Bitmap mainPass =  detectionUtility.findRectangle(mainReactBmp);
                main_imgv.setImageBitmap(mainResult);

                if (mainResult == null) {
                    detectionFailed = true;
                    Toast.makeText(getApplicationContext(), "Missing Bitmap", Toast.LENGTH_LONG).show();
                }
                else {
                    // Reactive Patch Bitmap cropped further to only use center
                    int reactiveCropStartX = mainResult.getWidth() / 4;
                    int reactiveCropStartY = mainResult.getHeight() / 4;
                    int reactiveCropWidth = mainResult.getWidth() / 2;
                    int reactiveCropHeight = mainResult.getHeight() / 2;
                    mainCropped = Bitmap.createBitmap(mainResult, reactiveCropStartX, reactiveCropStartY, reactiveCropWidth, reactiveCropHeight);

                    // New Bitmap created that only contains non-reactive patch
                    Bitmap constantReactBmp = Bitmap.createBitmap(initResult, 10, 10, initResult.getWidth() - 20, initResult.getHeight() - 535);
                    Bitmap constantPass = detectionUtility.findRectangle(constantReactBmp);
                    constant_imgv.setImageBitmap(constantResult);

                    if (constantResult == null) {
                        detectionFailed = true;
                    }
                    else {
                        // Non-reactive Patch Bitmap cropped further to only use center
                        int nonreactiveCropStartX = constantResult.getWidth() / 4;
                        int nonreactiveCropStartY = constantResult.getHeight() / 4;
                        int nonreactiveCropWidth = constantResult.getWidth() / 2;
                        int nonreactiveCropHeight = constantResult.getHeight() / 2;
                        constantCropped = Bitmap.createBitmap(constantResult, nonreactiveCropStartX, nonreactiveCropStartY, nonreactiveCropWidth, nonreactiveCropHeight);
                    }
                }

            }

            if (detectionFailed) {  // If any of the sections are not detected properly
                Toast.makeText(getApplicationContext(), "Missing Bitmap", Toast.LENGTH_LONG).show();
                next_btn.setVisibility(View.INVISIBLE);
                detection_imgv.setVisibility(View.INVISIBLE);
                main_imgv.setVisibility(View.INVISIBLE);
                constant_imgv.setVisibility(View.INVISIBLE);
                reactiveLabel_txtv.setVisibility(View.INVISIBLE);
                warpedLabel_txtv.setVisibility(View.INVISIBLE);
                failLabel1_txtv.setVisibility(View.VISIBLE);
                failLabel2_txtv.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // OnClickListener for Next button
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream reactivebaos = new ByteArrayOutputStream();
                mainCropped.compress(Bitmap.CompressFormat.JPEG, 100, reactivebaos);
                croppedReactiveByteArray = reactivebaos.toByteArray();

                ByteArrayOutputStream nonreactivebaos = new ByteArrayOutputStream();
                constantCropped.compress(Bitmap.CompressFormat.JPEG, 100, nonreactivebaos);
                croppedNonReactiveByteArray = nonreactivebaos.toByteArray();

                openCurrentAnalysisActivity();
            }
        });

        // OnClickListener for Discard button
        discard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detection_imgv.setImageDrawable(null);
                main_imgv.setImageDrawable(null);
                constant_imgv.setImageDrawable(null);
                initResult = null;

                if (cameFromCamera) {
                    openImageCaptureActivity();
                }
                else {
                    openImageImportActivity();
                }
            }
        });

    }

    // Activity Methods
    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

    private void openImageImportActivity() {
        Intent intent = new Intent(this, ImageImport.class);
        startActivity(intent);
    }

    private void openCurrentAnalysisActivity() {
        Intent intent = new Intent(this, CurrentAnalysis.class);
        intent.putExtra("cameFromCamera", cameFromCamera);
        intent.putExtra("initialByteArray", initialByteArray);
        intent.putExtra("croppedReactiveByteArray", croppedReactiveByteArray);
        intent.putExtra("croppedNonReactiveByteArray", croppedNonReactiveByteArray);
        startActivity(intent);
    }

}