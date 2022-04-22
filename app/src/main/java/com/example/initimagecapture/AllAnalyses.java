package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.json.*;

public class AllAnalyses extends AppCompatActivity {
    // Controls
    private Button back_btn, filter_btn;
    private RecyclerView images_recyclerView;
    private Spinner months_spinner, years_spinner;

    // ArrayLists to hold Image objects
    ArrayList<UserImage> images = new ArrayList<>();
    ArrayList<UserImage> filteredImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_analyses);
        years_spinner = findViewById(R.id.spinner_years);
        months_spinner = findViewById(R.id.spinner_months);

        // Adding Values for Year Spinner using current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int[] yearsArray = new int[5];
        for (int i = 1; i < yearsArray.length; i++) {   // Gets previous 4 years
            yearsArray[i] = currentYear - i + 1;        // +1 needed to account for the "--" placeholder value
        }

        String[] yearsArrayString = Arrays.stream(yearsArray)
                .mapToObj(String::valueOf)
                .toArray(String[]::new);
        yearsArrayString[0] = "--";
        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearsArrayString);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        years_spinner.setAdapter(yearsAdapter);

        // Adding values for Month Spinner
        ArrayAdapter<CharSequence> monthsAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
        monthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        months_spinner.setAdapter(monthsAdapter);


        // OnClickListener for Back Button
        back_btn = findViewById(R.id.button_backAnalyses);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        // OnClickListener for Filter Button
        filter_btn = findViewById(R.id.button_filter);
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filteredImages.clear();
                String[] monthCodes = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"}; // Array Positions match Month format in an images Date string
                int monthInt = months_spinner.getSelectedItemPosition();
                String yearString = years_spinner.getSelectedItem().toString();

                if (monthInt == 0 && yearString.equals("--")) {             // If no filters are selected
                    filteredImages.addAll(images);
                }
                else if (monthInt == 0 && !yearString.equals("--")) {       // If only year filter is selected
                    for (UserImage item : images) {
                        if (item.getImgDate().startsWith(yearString)) {
                            filteredImages.add(item);
                        }
                    }
                }
                else if (monthInt != 0 && yearString.equals("--")) {        // If only month filter is selected
                    for (UserImage item : images) {
                        if (item.getImgDate().substring(5, 7).equals(monthCodes[monthInt - 1])) {   // Dates are saved starting "YYYY-MM-DD", so the month is always substring position 5 to 7
                            filteredImages.add(item);
                        }
                    }
                }
                else if (monthInt != 0 && !yearString.equals("--")) {       // If both month and year filters are selected
                    for (UserImage item : images) {
                        if (item.getImgDate().startsWith(yearString) && item.getImgDate().substring(5, 6).equals(monthCodes[monthInt - 1])) {
                            filteredImages.add(item);
                        }
                    }
                }

                // RecyclerView reset with relevant images shown
                RVAdapter adapter = new RVAdapter(getApplicationContext(), filteredImages);
                images_recyclerView.setAdapter(adapter);
                images_recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            }
        });

        // Initial RecyclerView setup makes requests to get User Images
        images_recyclerView = (RecyclerView) findViewById(R.id.recyclerView_Images);
        for (int i = 0; i < User.getUserImageNo(); i++) {
            makeRequest getUserImagesRequest = new makeRequest("http://192.168.0.29/projectPHP/getuserimages.php", "POST", "getUserImages", String.valueOf(i));
            if(getUserImagesRequest.startRequest()) {
                if(getUserImagesRequest.onComplete()) {

                    try {
                        JSONObject obj = new JSONObject(getUserImagesRequest.getResult());

                        if (!obj.getString("id").equals("0")) {
                            String id = obj.getString("id");
                            String date = obj.getString("date");
                            String value = obj.getString("value");
                            String avgRed = obj.getString("avgRed");
                            String pco2 = obj.getString("pco2");

                            UserImage tempImage = new UserImage(id, date, value, avgRed, pco2);
                            images.add(tempImage);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Toast.makeText(getApplicationContext(), "Images found: " + images.size(), Toast.LENGTH_SHORT).show();

        RVAdapter adapter = new RVAdapter(this, images);
        images_recyclerView.setAdapter(adapter);
        images_recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}