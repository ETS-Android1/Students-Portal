package com.code94.studentsportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout usernameField, passwordField;
    Button nextBtn, prevBtn;
    Boolean prevBtnActive = false;
    String username, password;
    DatabaseReference dbref;
    RelativeLayout.LayoutParams nextBtnParams;
    ProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        usernameField = (TextInputLayout) findViewById(R.id.username);
        passwordField = (TextInputLayout) findViewById(R.id.password);

        nextBtn = (Button) findViewById(R.id.nextButton);
        prevBtn = (Button) findViewById(R.id.prevButton);
        nextBtnParams = (RelativeLayout.LayoutParams) nextBtn.getLayoutParams();
        progressBar = (ProgressIndicator) findViewById(R.id.progressBar);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!prevBtnActive) {
                    if(usernameField.getEditText().getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter your username!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    usernameField.setVisibility(View.GONE);
                    passwordField.setVisibility(View.VISIBLE);
                    prevBtn.setVisibility(View.VISIBLE);
                    prevBtnActive = true;
                    nextBtnParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.password);
                    return;
                }
                if(passwordField.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                enableComponents(false);
                Map<String, String> params = new HashMap();
                params.put("username", usernameField.getEditText().getText().toString());
                params.put("password", passwordField.getEditText().getText().toString());

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://kcetpc-scrapper.herokuapp.com/login", new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("Resp", response.toString());
                                try {
                                    if(response.getInt("status")  == 200) {
                                        handlePostData(response);
                                    } else {
                                        enableComponents(true);
                                        Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    enableComponents(true);
                                    Toast.makeText(getApplicationContext(), "Unable to process your at the moment", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Err", error.toString());
                                enableComponents(true);
                                Toast.makeText(getApplicationContext(), "Unable to process your at the moment", Toast.LENGTH_SHORT).show();
                            }
                        });
                queue.add(jsObjRequest);
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordField.setVisibility(View.GONE);
                usernameField.setVisibility(View.VISIBLE);
                prevBtn.setVisibility(View.GONE);
                prevBtnActive = false;
                nextBtnParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.username);
            }
        });


    }

    public void handlePostData(JSONObject response) throws JSONException {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", true);
        editor.putString("name", response.getString("name"));
        editor.putString("username", usernameField.getEditText().getText().toString());
        editor.putString("rollNo", usernameField.getEditText().getText().toString());
        editor.putString("dob", passwordField.getEditText().getText().toString());
        editor.putString("password", passwordField.getEditText().getText().toString());
        editor.putString("firstName", response.getString("firstName"));
        editor.putString("initial", response.getString("initial"));
        editor.putString("firstName", response.getString("firstName"));
        editor.putString("sessionId", response.getString("sessionId"));
        editor.commit();
        enableComponents(true);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finishActivity(0);
    }

    public void enableComponents(boolean status) {
        usernameField.setEnabled(status);
        passwordField.setEnabled(status);
        nextBtn.setEnabled(status);
        prevBtn.setEnabled(status);
        if(status) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}