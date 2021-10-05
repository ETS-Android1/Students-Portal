package com.code94.studentsportal.ui.attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.code94.studentsportal.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class AttendanceFragment extends Fragment {

    private AttendanceViewModel dashboardViewModel;
    private View view;
    private ImageButton btn;
    private TextInputLayout dateField, masterDateField;
    private Bundle savedState = null;
    boolean reAuthStatus = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        savedState = null;
        view = inflater.inflate(R.layout.fragment_attendance, container, false);
        btn = (ImageButton) view.findViewById(R.id.getAttendanceBtn);
        dateField = (TextInputLayout) view.findViewById(R.id.dateField);
        masterDateField = (TextInputLayout) view.findViewById(R.id.masterDateField);
        ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
        progressIndicator.setVisibility(View.GONE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(masterDateField.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please choose a date!", Toast.LENGTH_SHORT).show();
                    return;
                }
                makePostRequest();
            }
        });
        dateField.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarConstraints calendarConstraints = new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now()).build();
                MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                        .setCalendarConstraints(calendarConstraints)
                        .setTitleText("Select date")
                        .build();
                datePicker.show(getParentFragmentManager(), "DatePicker");
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        dateField.getEditText().setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selection));
                        masterDateField.getEditText().setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(selection));
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void makePostRequest() {
        RelativeLayout queryContainer = (RelativeLayout) view.findViewById(R.id.queryContainer);
        for (int i = 0; i < queryContainer.getChildCount(); i++) {
            View child = queryContainer.getChildAt(i);
            child.setEnabled(false);
        }
        ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
        progressIndicator.setVisibility(View.VISIBLE);
        Log.e("WAY", "Sending Request");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        Log.e("sessionId", sharedPreferences.getString("sessionId", ""));
        RequestQueue queue = Volley.newRequestQueue(getContext());
        Map<String, String> params = new HashMap();
        params.put("date", masterDateField.getEditText().getText().toString());
        params.put("sessionId", sharedPreferences.getString("sessionId", ""));
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://kcetpc-scrapper.herokuapp.com/attendance", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Resp", response.toString());
                        boolean skipChildEnabling = false;
                        try {
                            if(response.getInt("status")  == 200) {
                                handlePostData(response);
                            } else if(response.getInt("status")  == 403) {
                                if(!reAuthStatus) {
                                    reAuth();
                                    skipChildEnabling = true;
                                } else {
                                    Toast.makeText(getContext(), "Kindly logout from the app and try again!", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getContext(), "No data available!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Unable to process your at the moment!", Toast.LENGTH_SHORT).show();
                        } finally {
                            if(!skipChildEnabling) {
                                ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
                                progressIndicator.setVisibility(View.GONE);
                                for (int i = 0; i < queryContainer.getChildCount(); i++) {
                                    View child = queryContainer.getChildAt(i);
                                    child.setEnabled(true);
                                }
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Err", error.toString());
                        Toast.makeText(getContext(), "Unable to process your at the moment!", Toast.LENGTH_SHORT).show();
                        ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
                        progressIndicator.setVisibility(View.GONE);
                        for (int i = 0; i < queryContainer.getChildCount(); i++) {
                            View child = queryContainer.getChildAt(i);
                            child.setEnabled(true);
                        }
                    }
                });
        queue.add(jsObjRequest);
    }

    private void handlePostData(JSONObject response) throws JSONException {
        JSONArray data = response.getJSONArray("data");
        for(int i = 0; i < data.length(); i++) {
            String chipId = "hour" + (i + 1) + "Chip";
            int resID = getResources().getIdentifier(chipId, "id", getContext().getPackageName());
            Chip chip = (Chip) view.findViewById(resID);
            String status = data.getString(i);
            chip.setText(chip.getText().subSequence(0, 8));
            if(status.equals("PR")) {
                chip.setChipIconResource(R.drawable.ic_baseline_check_circle_outline_24);
                chip.setChipBackgroundColorResource(R.color.chipPR);
            } else if(status.equals("AB")) {
                chip.setChipIconResource(R.drawable.ic_baseline_error_outline_24);
                chip.setChipBackgroundColorResource(R.color.chipAB);
            } else if(status.equals("OD")) {
                chip.setChipIconResource(R.drawable.ic_baseline_check_circle_outline_24);
                chip.setChipBackgroundColorResource(R.color.chipOD);
            } else {
                chip.setChipIconResource(R.drawable.ic_baseline_remove_circle_outline_24);
                chip.setChipBackgroundColorResource(R.color.chipOther);
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(chip.getText().length() > 8) {
                        chip.setText(chip.getText().subSequence(0, 8));
                    } else {
                        chip.setText(chip.getText() + " " + status);
                    }
                }
            });
        }
        LinearLayout attendanceContainer = (LinearLayout) view.findViewById(R.id.attendanceContainer);
        attendanceContainer.setVisibility(View.VISIBLE);
    }

    private void reAuth() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        Map<String, String> params = new HashMap();
        params.put("username", sharedPreferences.getString("username", ""));
        params.put("password", sharedPreferences.getString("password", ""));
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://kcetpc-scrapper.herokuapp.com/login", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Resp", response.toString());
                        try {
                            if(response.getInt("status")  == 200) {
                                handleAuthData(response);
                            } else {
                                Log.e("REAUTH", "STATUS-Failed");
//                                handleErr(from);
//                                Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("REAUTH", "STATUS-Failed-500");
//                            handleErr(from);
//                            Toast.makeText(getApplicationContext(), "Unable to process your at the moment", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("REAUTH", "STATUS-Failed-500");
//                        handleErr(from);
//                        Toast.makeText(getApplicationContext(), "Unable to process your at the moment", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsObjRequest);
    }

    private void handleAuthData(JSONObject response) throws JSONException {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", true);
        editor.putString("name", response.getString("name"));
        editor.putString("firstName", response.getString("firstName"));
        editor.putString("initial", response.getString("initial"));
        editor.putString("firstName", response.getString("firstName"));
        editor.putString("sessionId", response.getString("sessionId"));
        editor.commit();
        reAuthStatus = true;
        makePostRequest();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}