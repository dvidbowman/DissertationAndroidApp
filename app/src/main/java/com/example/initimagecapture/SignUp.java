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

import org.json.JSONException;
import org.json.JSONObject;

// From github.com/vishnusivadasvs/advanced-httpurlconnection

public class SignUp extends AppCompatActivity {
    // Controls
    private TextInputEditText username_textField, password_textField;
    private TextView alreadyUser_textView;
    private Button signUp_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username_textField = findViewById(R.id.textField_username);
        password_textField = findViewById(R.id.textField_password);
        signUp_btn = findViewById(R.id.button_signUp);
        alreadyUser_textView = findViewById(R.id.textView_alreadyUser);

        // OnClickListener for AlreadyAUser TextView
        alreadyUser_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // OnClickListener for SignUp button
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username, password;

                username = String.valueOf(username_textField.getText()).trim();
                password = String.valueOf(password_textField.getText()).trim();

                if(!username.equals("") && !password.equals("")) {
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String[] fields = new String[2];

                            fields[0] = "username";
                            fields[1] = "password";

                            String[] data = new String[2];
                            data[0] = username;
                            data[1] = password;

                            makeRequest signUpRequest = new makeRequest("http://192.168.0.29/projectPHP/signup.php", "POST", "signUp", fields, data);
                            if (signUpRequest.startRequest()) {
                                if(signUpRequest.onComplete()) {

                                    try {
                                        JSONObject obj = new JSONObject(signUpRequest.getResult());

                                        if (obj.getString("message").equals("none")) {
                                            Toast.makeText(getApplicationContext(), "Sign Up Success", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), Login.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "JSON Parse Failure", Toast.LENGTH_SHORT).show();
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