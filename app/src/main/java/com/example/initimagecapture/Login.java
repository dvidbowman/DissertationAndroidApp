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

public class Login extends AppCompatActivity {
    // Controls
    private TextInputEditText username_textField, password_textField;
    private TextView goToSignUp_textView;
    private Button logIn_btn, devSkip_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Control Definition
        username_textField = findViewById(R.id.textField_username);
        password_textField = findViewById(R.id.textField_password);
        logIn_btn = findViewById(R.id.button_Login);
        devSkip_btn = findViewById(R.id.button_devskip);
        goToSignUp_textView = findViewById(R.id.textView_goToSignUp);

        // OnClickListener for DevSkip button
        devSkip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.getInstance().setUserId(0);
                User.getInstance().setUserImageNo(0);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // OnClickListener for GoToSignUp TextView
        goToSignUp_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        // OnClickListener for LogIn button
        logIn_btn.setOnClickListener(new View.OnClickListener() {
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

                            makeRequest logInAuthRequest = new makeRequest("http://192.168.0.29/projectPHP/login.php", "POST", "logIn", fields, data);
                            if(logInAuthRequest.startRequest()) {
                                if(logInAuthRequest.onComplete()) {

                                    try {
                                        JSONObject obj = new JSONObject(logInAuthRequest.getResult());

                                        if (obj.getString("message").equals("none")) {      // Message will be different than 'none' if an error occurs
                                            User.getInstance().setUserId(Integer.parseInt(obj.getString("id")));
                                            User.getInstance().setUserImageNo(Integer.parseInt(obj.getString("noImages")));
                                            User.getInstance().setLoggedIn(true);

                                            Toast.makeText(getApplicationContext(), "Login Successful: UserID = " + User.getInstance().getUserId(), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();    // Error message displayed in Toast
                                        }

                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "JSON Parse Failure:" + logInAuthRequest.getResult(), Toast.LENGTH_SHORT).show();
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