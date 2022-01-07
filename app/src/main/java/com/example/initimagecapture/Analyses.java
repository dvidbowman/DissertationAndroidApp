package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

public class Analyses extends AppCompatActivity {
    private Button back_btn;
    private RecyclerView images_recyclerView;

    ArrayList<String> imgIDs = new ArrayList<String>();
    ArrayList<String> imgDates = new ArrayList<String>();
    ArrayList<String> imgValues = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyses);

        back_btn = (Button) findViewById(R.id.button_backAnalyses);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        images_recyclerView = (RecyclerView) findViewById(R.id.recyclerView_Images);


        getUserImages getUserImages = new getUserImages("http://192.168.0.29/projectPHP/getuserimages.php", "POST");
        if(getUserImages.startPut()) {
            if (getUserImages.onComplete()) {

                try {
                    JSONObject obj = new JSONObject(getUserImages.getResult());

                    if (obj.getString("id").equals("0")) {
                        Toast.makeText(getApplicationContext(), "You currently have no images saved.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        imgIDs.add(obj.getString("id"));
                        imgDates.add(obj.getString("date"));
                        imgValues.add(obj.getString("value"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Images Could Not Be Loaded", Toast.LENGTH_LONG).show();
                }

            }
        }

        RVAdapter adapter = new RVAdapter(this, imgIDs, imgDates, imgValues);
        images_recyclerView.setAdapter(adapter);
        images_recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}