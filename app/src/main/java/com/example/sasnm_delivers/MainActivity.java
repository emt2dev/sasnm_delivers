package com.example.sasnm_delivers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView textViewAddress, textViewUser;
    String companyId = "1";
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String controller = "mobile/delivery/";
    String listAllOrders = "orders/" + companyId;
    String completedOrder = "completed";
    String acceptOrder = "accept";
    String urlBuilder = apiDomain + controller; // this is sent to the api
    SharedPreferences sharedPreferences;
    String order_id, longitude, latitude, deliveryAddress;
    CardView cardViewDeliveryDetails;
    TextView textViewTitle;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewAddress = findViewById(R.id.new_delivery);
        textViewUser = findViewById(R.id.userDetails);

        sharedPreferences = getSharedPreferences("SASNM_delivers", MODE_PRIVATE);

        cardViewDeliveryDetails = findViewById(R.id.deliveryDetails);
        textViewTitle = findViewById(R.id.textDeliveryTitle);
        textViewTitle.setText("No pending delivery");
        cardViewDeliveryDetails.setVisibility(View.GONE);
        if (sharedPreferences.getString("login", "false").equals("false")) {
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        }
        checkPermissions();
        GPSUtils gpsUtils = new GPSUtils(this);
        gpsUtils.statusCheck();
        textViewUser.setText(sharedPreferences.getString("name", "") + "\n" +
                sharedPreferences.getString("email", ""));
        fetchData();
        Button buttonLocation = findViewById(R.id.startDelivery);
        Button buttonSuccess = findViewById(R.id.btn_success); // update order and delivery as complete
        Button buttonAccept = findViewById(R.id.btn_accept); // update order and delivery as complete

        buttonSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlCompleted = urlBuilder + completedOrder + "/" + order_id;
                markOrderStatus(urlCompleted);
            }
        });

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlAccept = urlBuilder + acceptOrder + order_id + "/" + sharedPreferences.getString("userId", "");
                markOrderStatus(urlAccept);
            }
        });

        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewLocation.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("long", longitude);
                startActivity(intent);
            }
        });

    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void markOrderStatus(String url) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("")) {

                            Toast.makeText(MainActivity.this, "Order Updated ", Toast.LENGTH_SHORT).show();
                            cardViewDeliveryDetails.setVisibility(View.GONE);
                            textViewTitle.setText("No pending delivery");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }

    public void parseJSON(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.length() > 0) {
                cardViewDeliveryDetails.setVisibility(View.VISIBLE);
                textViewTitle.setText("New Delivery Details Found");
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                order_id = obj.getString("id");
                deliveryAddress = obj.getString("deliveryAddress");
                textViewAddress.setText("Address: " + deliveryAddress);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlOrders = urlBuilder + listAllOrders + "/" + sharedPreferences.getString("userId", "");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlOrders,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }
}