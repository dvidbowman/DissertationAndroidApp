package com.example.initimagecapture;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    // OpenCV Installer Message
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV installed successfully");
        }
        else {
            Log.d(TAG, "OpenCV could not be installed");
        }
    }

    // Controls
    private Button analyses_btn, camera_btn, logout_btn, import_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DialogInterface presented when user clicks LogOut, or SeeAnalyses while not logged in
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        openLoginActivity();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        // OnClickListener for Analyses Button
        analyses_btn = (Button) findViewById(R.id.button_analyses);
        analyses_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getInstance().getLoggedIn()) {
                    openAllAnalysesActivity();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("You must be logged in to view saved Analyses. Would you like to Log In now?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }

            }
        });

        // OnClickListener for TakeAnImage button
        camera_btn = (Button) findViewById(R.id.button_takeImage);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageCaptureActivity();
            }
        });

        // OnClickListener for LogOut button
        logout_btn = (Button) findViewById(R.id.button_logout);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you sure you want to Log Out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        // OnClickListener for ImportImage button
        import_btn = (Button) findViewById(R.id.button_importImage);
        import_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageImportActivity();
            }
        });
    }

    // Activity Methods
    public void openAllAnalysesActivity() {
        Intent intent = new Intent(this, AllAnalyses.class);
        startActivity(intent);
    }

    public void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

    public void openLoginActivity() {
        User.getInstance().resetUser();

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void openImageImportActivity() {
        Intent intent = new Intent(this, ImageImport.class);
        startActivity(intent);
    }
}