package io.github.richardjcase.friride;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class StatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        new UserRidesAsyncTask().execute((Void) null);
    }

    private void addLabelsFromResponse(String base, String response, String arrayName){
        ArrayList<Ride> rides = new ArrayList<Ride>();

        try {
            JSONArray jsonArray = new JSONObject(response).getJSONArray(arrayName);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Ride ride = new Ride();

                ride.ID = Integer.parseInt(jsonObject.getString("ID"));
                ride.status = Integer.parseInt(jsonObject.getString("status"));
                ride.loc = jsonObject.getString("loc");
                ride.dest = jsonObject.getString("dest");
                if(jsonObject.has("comment"))
                    ride.comment = jsonObject.getString("comment");
                ride.rider = jsonObject.getString("rider");
                ride.driver = jsonObject.getString("driver");

                rides.add(ride);
            }

            if(rides.size() == 0) return;

            LinearLayout baseView = (LinearLayout) findViewById(R.id.layout);

            TextView textView = new TextView(StatusActivity.this);
            textView.setText(base);
            textView.setTextSize(24);
            textView.setTextColor(Color.BLACK);
            baseView.addView(textView);

            for(Ride ride : rides){
                textView = new TextView(StatusActivity.this);
                String text = ride.ID + ": " + ride.loc + " to " + ride.dest;
                text += (ride.comment != null) ? "\n" + ride.comment : "";
                textView.setText(text);
                textView.setTextSize(16);
                textView.setTextColor(Color.BLACK);
                baseView.addView(textView);
            }
        }catch (Exception e){
            Log.d("EXCEPTION", e.getMessage());
        }
    }

    public class UserRidesAsyncTask extends AsyncTask<Void, Void, Boolean> {
        UserRidesAsyncTask(){
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String response = FriRide.getUserRides();
                addLabelsFromResponse("Pending rides:", response, "rides");

                response = FriRide.getUserDrives();
                addLabelsFromResponse("Driving:", response, "drives");
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                TextView statusText = (TextView)findViewById(R.id.status_text);
                statusText.setVisibility(View.GONE);
            }
        }
    }
}
