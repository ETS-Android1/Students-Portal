package com.code94.studentsportal.ui.progressBar;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.code94.studentsportal.R;
import com.google.android.material.progressindicator.ProgressIndicator;

public class ProgressBar extends AppCompatActivity {

    ProgressIndicator materialProgressBar;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        materialProgressBar = (ProgressIndicator) findViewById(R.id.progressBar);
    }

    public ProgressBar() {}

    public void showProgressBar() {
        materialProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        materialProgressBar.setVisibility(View.GONE);
    }

}
