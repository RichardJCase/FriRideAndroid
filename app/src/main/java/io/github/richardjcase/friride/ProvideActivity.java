package io.github.richardjcase.friride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProvideActivity extends AppCompatActivity {
    private ArrayList<Ride> availableRides;

    private void assignRide(int id){
        int max = availableRides.size() - 1;
        int min = 0;
        int i = (max - min) / 2;
        Ride ride = availableRides.get(i);
        while(ride.ID != id){
            if(ride.ID > id){
                max = i - 1;
            }else{
                min = i + 1;
            }

            i = (max - min) / 2 + min;
            ride = availableRides.get(i);
        }

        Session.setSelectedRide(ride);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);

        final RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radio_buttons);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            showRides();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = radioGroup.getCheckedRadioButtonId();
                if(id == -1) return;
                assignRide(id);
                startActivity(new Intent(ProvideActivity.this, RideDetailActivity.class));
                finish();
            }
        });

        submitButton.setEnabled(false);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case GPS.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showRides();
                }else{
                    TextView textView = (TextView)findViewById(R.id.status_text);
                    textView.setText("Location not available");
                    textView.setTextColor(Color.RED);
                }
            }
        }
    }

    private void showRides(){
        GPS.checkPermission(this, new AvailableRidesTask());
    }

    public class AvailableRidesTask extends AsyncTask<Coords, Void, Boolean> {
        AvailableRidesTask(){
            availableRides = new ArrayList<Ride>();
        }

        @Override
        protected Boolean doInBackground(Coords... coords) {
            try {
                JSONArray jsonArray = FriRide.getRides(coords[0]);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.has("driver") && !jsonObject.getString("driver").equals("")) continue;

                    Ride ride = new Ride();
                    ride.ID = Integer.parseInt(jsonObject.getString("ID"));
                    ride.rider = jsonObject.getString("rider");
                    ride.driver = "";
                    ride.dest = jsonObject.getString("dest");
                    ride.from = jsonObject.getString("from");
                    ride.loc = jsonObject.getString("loc");
                    ride.status = Integer.parseInt(jsonObject.getString("status"));
                    ride.payment = jsonObject.getString("payment");
                    if(jsonObject.has("comment"))
                        ride.comment = jsonObject.getString("comment");

                    availableRides.add(ride);
                }
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            TextView tv = (TextView)findViewById(R.id.status_text);

            if(success) {
                if(availableRides.size() == 0){
                    tv.setText("No rides found.");
                    return;
                }

                tv.setVisibility(View.GONE);

                RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radio_buttons);
                for(Ride ride : availableRides){
                    RadioButton radioButton = new RadioButton(ProvideActivity.this);
                    radioButton.setId(ride.ID);
                    radioButton.setText(ride.ID + " - " + ride.payment + ": " + ride.from + " to " + ride.dest);
                    radioButton.setVisibility(View.VISIBLE);
                    radioButton.setTextSize(16);
                    radioButton.setBackground(getDrawable(R.drawable.rounded_option));
                    radioButton.setTextColor(Color.WHITE);

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

                    radioButton.setButtonTintList(colorStateList);

                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 16, 0, 0);
                    radioButton.setLayoutParams(params);

                    radioGroup.addView(radioButton);
                }

                final Button submitButton = (Button) findViewById(R.id.submit_button);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId){
                        submitButton.setEnabled(true);
                    }
                });
            }else{
                tv.setText("Error obtaining available rides.");
                tv.setTextColor(Color.RED);
            }
        }
    }
}
