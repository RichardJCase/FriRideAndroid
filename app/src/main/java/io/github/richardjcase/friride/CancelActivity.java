package io.github.richardjcase.friride;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CancelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel);

        final RadioGroup rideGroup = (RadioGroup) findViewById(R.id.ride_group);
        final Spinner ratingSpinner = (Spinner) findViewById(R.id.rating_spinner);
        final Button cancelButton = (Button) findViewById(R.id.cancel_button);
        TextView textView = (TextView) findViewById(R.id.status_text);
        textView.setTextColor(Color.BLACK);
        textView.setText("Looking Up Rides...");

        new PopulateRideTask(rideGroup).execute((Void) null);

        rideGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                cancelButton.setEnabled(true);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton selected = (RadioButton) rideGroup.getChildAt(rideGroup.getCheckedRadioButtonId());
                String uname = selected.getText().toString().split(" ")[1];
                String rating = ratingSpinner.getSelectedItem().toString();

                new CancelTask(uname, rating).execute((Void) null);
                cancelButton.setEnabled(false);
                rideGroup.clearCheck();
                rideGroup.removeAllViews();
                new PopulateRideTask(rideGroup).execute((Void) null);
            }
        });
    }

    public class PopulateRideTask extends AsyncTask<Void, Void, Boolean> {
        private RadioGroup radioGroup;
        private String rides, drives;

        PopulateRideTask(RadioGroup radioGroup) {
            this.radioGroup = radioGroup;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                drives = FriRide.getUserDrives();
                rides = FriRide.getUserRides();
            }catch(IOException e) {
                return false;
            }

            return true;
        }

        private void addRides(String lbl, String data, RadioGroup radioGroup) throws JSONException {
            if(data.equals("{}")) return;
            JSONArray jsonArray = new JSONObject(data).getJSONArray(lbl);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_enabled}
                    },
                    new int[] {
                            Color.WHITE,
                            Color.WHITE
                    }
            );

            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String user = jsonObject.getString("driver");
                String to = jsonObject.getString("dest");
                String from = jsonObject.getString("from");

                RadioButton radioButton = new RadioButton(CancelActivity.this);
                radioButton.setId(i);
                radioButton.setText(id + " " + user + " : " + from + " to " + to);
                radioButton.setVisibility(View.VISIBLE);
                radioButton.setTextSize(16);
                radioButton.setBackground(getDrawable(R.drawable.rounded_option));
                radioButton.setTextColor(Color.WHITE);
                radioButton.setButtonTintList(colorStateList);

                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 16, 0, 0);
                radioButton.setLayoutParams(params);
                radioGroup.addView(radioButton);
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                TextView textView = (TextView) findViewById(R.id.status_text);

                try {
                    addRides("drives", drives, radioGroup);
                    addRides("rides", rides, radioGroup);
                    textView.setText("Select a ride to cancel.");
                }catch(JSONException e){
                    textView.setTextColor(Color.RED);
                    textView.setText("Error in obtaining rides.");
                    Log.d("EXCEPTION", e.getMessage());
                }
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    public class CancelTask extends AsyncTask<Void, Void, Boolean> {
        private String uname, rating;

        CancelTask(String uname, String rating) {
            this.uname = uname;
            this.rating = rating;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                FriRide.cancelRide(uname, rating);
            }catch (IOException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {

        }
    }
}
