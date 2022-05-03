package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class CurrentAnalysis extends AppCompatActivity {
    // Controls
    private TextView reactivePCO2_txtv, nonreactivePCO2_txtv, reactiveRed_txtv, nonReactiveRed_txtv;
    private Button back_btn, save_btn;
    private ImageView croppedReactive_imgv, fullImage_imgv;

    public static double averageRed;
    private double savedAverageRed;
    private double savedPCO2;

    private boolean cameFromCamera;
    private byte[] initialByteArray;

    Bitmap reactiveBitmap;
    Bitmap nonreactiveBitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_analysis);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cameFromCamera = extras.getBoolean("cameFromCamera");
            byte[] croppedReactiveByteArray = extras.getByteArray("croppedReactiveByteArray");
            byte[] croppedNonReactiveByteArray = extras.getByteArray("croppedNonReactiveByteArray");
            initialByteArray = extras.getByteArray("initialByteArray");
            reactiveBitmap = BitmapFactory.decodeByteArray(croppedReactiveByteArray, 0, croppedReactiveByteArray.length);
            nonreactiveBitmap = BitmapFactory.decodeByteArray(croppedNonReactiveByteArray, 0, croppedNonReactiveByteArray.length);
        }

        // Control Definition
        reactivePCO2_txtv = (TextView) findViewById(R.id.textView_reactivePCO2);
        nonreactivePCO2_txtv = (TextView) findViewById(R.id.textView_nonReactivePCO2);
        reactiveRed_txtv = (TextView) findViewById(R.id.textView_reactiveRed);
        nonReactiveRed_txtv = (TextView) findViewById(R.id.textView_nonReactiveRed);
        save_btn = (Button) findViewById(R.id.button_saveAnalysis);
        back_btn = (Button) findViewById(R.id.button_backDyeArea);
        croppedReactive_imgv = (ImageView) findViewById(R.id.imageView_crop);
        fullImage_imgv = (ImageView) findViewById(R.id.imageView_fullImage);

        // Save button is hidden if user is not logged in
        if (!User.getInstance().getLoggedIn()) {
            save_btn.setVisibility(View.INVISIBLE);
        }

        croppedReactive_imgv.setImageBitmap(reactiveBitmap);

        Bitmap bitmap = BitmapFactory.decodeByteArray(initialByteArray, 0, initialByteArray.length);    // Bitmap for full-sized image created
        fullImage_imgv.setImageBitmap(bitmap);

        savedPCO2 = operationsUtility.getCalculatedCO2Percentage(reactiveBitmap);
        reactivePCO2_txtv.setText(String.valueOf(savedPCO2));
        reactiveRed_txtv.setText(String.valueOf(averageRed));
        savedAverageRed = averageRed;
        nonreactivePCO2_txtv.setText(String.valueOf(operationsUtility.getCalculatedCO2Percentage(nonreactiveBitmap)));
        nonReactiveRed_txtv.setText(String.valueOf(averageRed));

        // OnClickListener for Back button
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back button takes user to activity they came from
                if (cameFromCamera) {
                    openImageCaptureActivity();
                }
                else {
                    openImageImportActivity();
                }
            }
        });

        // OnClickListener for Save button
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(initialByteArray);
                save_btn.setVisibility(View.INVISIBLE);
            }
        });

    }

    // Saving cropped image in database
    private void save(byte[] bytes) {
        String deviceManufacturer = Build.MANUFACTURER;
        String deviceModel = Build.MODEL;
        String deviceOs = Build.VERSION.RELEASE;

        makeRequest imageUploadRequest = new makeRequest("http://192.168.0.29/projectPHP/imageupload.php", "POST", "imageUpload", bytes, deviceManufacturer, deviceModel, deviceOs, String.valueOf(averageRed), String.valueOf(savedPCO2));
        if (imageUploadRequest.startRequest()) {
            if(imageUploadRequest.onComplete()) {

                try {
                    JSONObject obj = new JSONObject(imageUploadRequest.getResult());
                    if (obj.getString("message").equals("none")) {
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                        User.getInstance().setUserImageNo(User.getInstance().getUserImageNo() + 1);
                        openMainActivity();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), imageUploadRequest.getResult(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Activity Methods
    private void openImageImportActivity() {
        Intent intent = new Intent(this, ImageImport.class);
        startActivity(intent);
    }

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}