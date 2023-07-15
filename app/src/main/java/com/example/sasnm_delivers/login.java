package com.example.sasnm_delivers;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    // init user entered variables
    TextInputEditText editTextEmail, editTextPassword;
    String email, password, userId;
    TextView textViewError, textViewRegister;
    Button buttonSubmit;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // preferences
        sharedPreferences = getSharedPreferences("SASNM_delivers", MODE_PRIVATE);

        // here, if login is successful, we prevent the app from opening the login activity and go straight into the main activity
        if (sharedPreferences.getString("login", "false").equals("true")) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        // init user entered variables
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        // init view items
        textViewError = findViewById(R.id.error);
        buttonSubmit = findViewById(R.id.submit);
        progressBar = findViewById(R.id.loading);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE); // shows the progress bar
                textViewError.setVisibility(View.GONE); // removes errors
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                // here we provide strings that will be concat'd together, makes it easier for future version updates
                String apiDomain = "http://10.0.2.2:5035/api/v2/";
                String controllerMethod = "mobile/delivery/login";
                String url = apiDomain + controllerMethod; // this is sent to the api

                // here we begin creating the post request.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                // this is the actual post request with handlers
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE); // removes progress bar once the request is sent
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String userId = jsonObject.getString("userId");
                                    if (!userId.equals("")) {
                                        userId = jsonObject.getString("userId");
                                        SharedPreferences.Editor myEdit = sharedPreferences.edit(); // stores the email
                                        myEdit.putString("login", "true");
                                        myEdit.putString("userId", userId);
                                        myEdit.apply();

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class); // opens the main activity
                                        startActivity(intent);
                                        finish(); // closes this activity
                                    } else {
                                        textViewError.setText("invalid email, password, or account does not exist"); // sets error to 401 unauth'd
                                        textViewError.setVisibility(View.VISIBLE); // displays error
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace(); // handles error
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE); // removes display
                        textViewError.setText(error.getLocalizedMessage()); // error message
                        textViewError.setVisibility(View.VISIBLE); // displays error
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("Email", email.toString()); // passed in the POST request
                        paramV.put("Password", password.toString()); // passed in the POST request
                        return paramV;
                    }
                };
                queue.add(stringRequest); // processsed by volley
            }
        });
    }
}