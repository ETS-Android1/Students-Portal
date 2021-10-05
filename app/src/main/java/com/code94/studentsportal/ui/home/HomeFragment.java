package com.code94.studentsportal.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.code94.studentsportal.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private View view;
    private MaterialCardView checkMarkCard, checkAttendanceCard;
    private BottomNavigationView bottomNavigationView;
    private TextView checkAttendanceTextView;
    private ImageView checkAttendanceImageView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        TextView firstName = (TextView) view.findViewById(R.id.firstName);
        firstName.setText("Hello " + sharedPreferences.getString("firstName", "") + "!");
        checkMarkCard = (MaterialCardView) view.findViewById(R.id.checkMarkCard);
        checkAttendanceCard = (MaterialCardView) view.findViewById(R.id.checkAttendanceCard);
        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        checkAttendanceTextView = (TextView) view.findViewById(R.id.checkAttendanceText);
        checkAttendanceTextView.requestLayout();
        checkAttendanceImageView = (ImageView) view.findViewById(R.id.checkAttendanceImage);
        checkAttendanceImageView.requestLayout();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(250, 167);
        checkMarkCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View navigationView = bottomNavigationView.findViewById(R.id.navigation_internal_marks);
                navigationView.performClick();
            }
        });
        checkAttendanceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View navigationView = bottomNavigationView.findViewById(R.id.navigation_attendance);
                navigationView.performClick();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}