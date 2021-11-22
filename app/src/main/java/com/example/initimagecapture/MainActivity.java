package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button settings_btn, analyses_btn, camera_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings_btn = (Button) findViewById(R.id.button_settings);
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsActivity();
            }
        });

        analyses_btn = (Button) findViewById(R.id.button_analyses);
        analyses_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnalysesActivity();
            }
        });

        camera_btn = (Button) findViewById(R.id.button_camera);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraActivity();
            }
        });
    }

    public void openSettingsActivity() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void openAnalysesActivity() {
        Intent intent = new Intent(this, Analyses.class);
        startActivity(intent);
    }

    public void openCameraActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }
}