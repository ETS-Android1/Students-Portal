package com.code94.studentsportal.ui.internalMarks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class InternalMarksFragment extends Fragment {

    private InternalMarksViewModel notificationsViewModel;

    View view;
    private ImageButton btn;
    private TextInputLayout semesterField, cycleTestField;
    private boolean reAuthStatus = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_internal_marks, container, false);

        btn = (ImageButton) view.findViewById(R.id.getMarkBtn);
        semesterField = (TextInputLayout) view.findViewById(R.id.semesterField);
        cycleTestField = (TextInputLayout) view.findViewById(R.id.cycleTextField);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(semesterField.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"Please enter a semester!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cycleTestField.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"Please enter a semester!", Toast.LENGTH_SHORT).show();
                    return;
                }
                makePostRequest();
            }
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public int getDpValue(double size) {
        float dpRatio = getContext().getResources().getDisplayMetrics().density;
        int pixelForDp = (int)(size * dpRatio);
        return pixelForDp;
    }

    private void makePostRequest() {
        semesterField.setEnabled(false);
        cycleTestField.setEnabled(false);
        btn.setEnabled(false);
        ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
        progressIndicator.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        Map<String, String> params = new HashMap();
        params.put("semester", semesterField.getEditText().getText().toString());
        params.put("cycleTest", cycleTestField.getEditText().getText().toString());
        params.put("sessionId", sharedPreferences.getString("sessionId", ""));
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://kcetpc-scrapper.herokuapp.com/marks", new JSONObject(params),
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
                                    Log.e("REAUTH", "STATUS-Stopped-Multiple-Auth");
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
                                semesterField.setEnabled(true);
                                cycleTestField.setEnabled(true);
                                btn.setEnabled(true);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Unable to process your at the moment!", Toast.LENGTH_SHORT).show();
                        ProgressIndicator progressIndicator = getActivity().findViewById(R.id.progressBar);
                        progressIndicator.setVisibility(View.GONE);
                        semesterField.setEnabled(true);
                        cycleTestField.setEnabled(true);
                        btn.setEnabled(true);
                    }
                });
        queue.add(jsObjRequest);
    }

    private void handlePostData(JSONObject response) throws JSONException {
        JSONArray data = response.getJSONArray("data");
        LinearLayout dataContainer = (LinearLayout) view.findViewById(R.id.dataContainer);
        dataContainer.removeAllViews();
        for(int i = 0; i < data.length(); i++) {
            JSONObject markData = data.getJSONObject(i);

            // Subject Container
            LinearLayout subjectContainer = new LinearLayout(getContext());
            subjectContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams subjectContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subjectContainerParams.setMargins(0, getDpValue(50), 0, 0);
            if(i == 0) subjectContainer.setLayoutParams(subjectContainerParams);
            TextView subjectName = new TextView(getContext());
            subjectName.setText(markData.getString("subjectCode") + "-" + markData.getString("subjectName"));
            subjectName.setTextAppearance(R.style.SubjectNameAppearence);

            // Marks Container
            LinearLayout marksContainer = new LinearLayout(getContext());
            marksContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams marksContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            marksContainerParams.setMargins(getDpValue(3), getDpValue(5), 0, 0);
            marksContainer.setLayoutParams(marksContainerParams);
            LinearLayout.LayoutParams marksTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            marksTextParams.setMargins(getDpValue(32), getDpValue(10), 0, 0);
            TextView assignmentMarks = new TextView(getContext());
            assignmentMarks.setText("Assignment - " + markData.getString("assignmentMark"));
            assignmentMarks.setTextAppearance(R.style.MarksAppearence);
            assignmentMarks.setLayoutParams(marksTextParams);
            TextView examMarks = new TextView(getContext());
            examMarks.setText("Exam - " + markData.getString("examMark"));
            examMarks.setTextAppearance(R.style.MarksAppearence);

            // Status Container
            LinearLayout statusContainer = new LinearLayout(getContext());
            statusContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams statusContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statusContainerParams.setMargins(0, getDpValue(10), 0, 0);
            statusContainer.setLayoutParams(statusContainerParams);
            RelativeLayout statusBadgeContainer = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams statusBadgeContainerParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            statusBadgeContainerParams.setMargins(getDpValue(12), 0, getDpValue(12), 0);
            statusBadgeContainer.setLayoutParams(statusBadgeContainerParams);
            ImageView badgeView = new ImageView(getContext());

            // Badge
            if(markData.getString("status").equals("PASS"))
                badgeView.setBackgroundResource(R.drawable.badge_pass);
            else if(markData.getString("status").equals("FAIL"))
                badgeView.setBackgroundResource(R.drawable.badge_fail);
            else
                badgeView.setBackgroundResource(R.drawable.badge_na);

            RelativeLayout.LayoutParams badgeViewParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            badgeViewParams.addRule(RelativeLayout.CENTER_VERTICAL);
            badgeView.setLayoutParams(badgeViewParams);
            statusBadgeContainer.addView(badgeView);
            statusContainer.addView(statusBadgeContainer);

            // Total Marks
            TextView totalMarks = new TextView(getContext());
            totalMarks.setText("Status - " + markData.getString("status"));
            totalMarks.setTextAppearance(R.style.MarksAppearence);
            statusContainer.addView(totalMarks);

            examMarks.setLayoutParams(marksTextParams);
            View divider = new View(new ContextThemeWrapper(getContext(), R.style.Divider), null, 0);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dividerParams.setMargins(0, 50, 0, 50);
            divider.setLayoutParams(dividerParams);
            marksContainer.addView(assignmentMarks);
            marksContainer.addView(examMarks);
            marksContainer.addView(statusContainer);
            subjectContainer.addView(subjectName);
            subjectContainer.addView(marksContainer);
            subjectContainer.addView(divider);
            dataContainer.addView(subjectContainer);
        }
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
//                                handlePostData(response, from);
                            } else {
                                Log.e("REAUTH", "STATUS-Failed");
//                                handleErr(from);
//                                Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
}