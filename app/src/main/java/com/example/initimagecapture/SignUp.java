package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class SignUp extends AppCompatActivity {

    private TextInputEditText fullName_textField, username_textField, password_textField, email_textField;
    private TextView alreadyUser_textView;
    private Button signUp_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fullName_textField = findViewById(R.id.textField_fullName);
        username_textField = findViewById(R.id.textField_username);
        password_textField = findViewById(R.id.textField_password);
        email_textField = findViewById(R.id.textField_email);
        signUp_btn = findViewById(R.id.button_signUp);
        alreadyUser_textView = findViewById(R.id.textView_alreadyUser);

        alreadyUser_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName, username, password, email;

                fullName = String.valueOf(fullName_textField.getText()).trim();
                username = String.valueOf(username_textField.getText()).trim();
                password = String.valueOf(password_textField.getText()).trim();
                email = String.valueOf(email_textField.getText()).trim();

                if(!fullName.equals("") && !username.equals("") && !password.equals("") && !email.equals("")) {
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String[] fields = new String[4];
                            fields[0] = "fullname";
                            fields[1] = "username";
                            fields[2] = "password";
                            fields[3] = "email";

                            String[] data = new String[4];
                            data[0] = fullName;
                            data[1] = username;
                            data[2] = password;
                            data[3] = email;

                            PutData putData = new PutData("http://192.168.0.29/LoginRegister/signup.php", "POST", fields, data);
                            if(putData.startPut()) {
                                if(putData.onComplete()) {
                                    String result = putData.getResult();

                                    if(result.equals("Sign Up Success")) {
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please complete all fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}