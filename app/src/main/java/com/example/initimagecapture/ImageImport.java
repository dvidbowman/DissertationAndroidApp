package com.example.initimagecapture;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class ImageImport extends AppCompatActivity {
    // Controls
    private Button back_btn, chooseImage_btn, next_btn;
    private ImageView importedImage_imgv;

    private byte[] exportedByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_import);

        // Control Definition
        importedImage_imgv = (ImageView) findViewById(R.id.imageView_importedImage);

        // OnClickListener for Back button
        back_btn = (Button) findViewById(R.id.button_backImport);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        // ActivityLauncher for opening Storage
        ActivityResultLauncher<Intent> openStorageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            try {
                                Uri selectedImageUri = data.getData();

                                if (selectedImageUri != null) {
                                    importedImage_imgv.setImageURI(selectedImageUri);
                                    next_btn.setVisibility(View.VISIBLE);       // Next button only shown when an image is selected
                                }
                            }
                            catch (NullPointerException e) {
                                Toast.makeText(getApplicationContext(), "Image data could not be found", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        // OnClickListener for ChooseImage button
        chooseImage_btn = (Button) findViewById(R.id.button_chooseImage);
        chooseImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                openStorageLauncher.launch(intent);
            }
        });

        // OnClickListener for Next button
        next_btn = (Button) findViewById(R.id.button_nextImport);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) importedImage_imgv.getDrawable();
                Bitmap importedBitmap = drawable.getBitmap();
                importedImage_imgv.invalidate();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                importedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                exportedByteArray = stream.toByteArray();
                //User.setUserByteArray(stream.toByteArray());
                openImageManipulationActivity();
            }
        });
        next_btn.setVisibility(View.INVISIBLE);

    }

    // Activity Methods
    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void openImageManipulationActivity() {
        Intent intent = new Intent(this, ImageManipulation.class);
        intent.putExtra("cameFromCamera", false);
        intent.putExtra("initialByteArray", exportedByteArray);
        startActivity(intent);
        finish();
    }
}