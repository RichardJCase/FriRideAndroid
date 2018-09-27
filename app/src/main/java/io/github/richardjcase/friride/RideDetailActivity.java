package io.github.richardjcase.friride;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class RideDetailActivity extends AppCompatActivity {
    private int rep;

    private void addRideInfo(){
        TextView to, from, comment;
        to = (TextView)findViewById(R.id.to_text);
        from = (TextView)findViewById(R.id.from_text);
        comment = (TextView)findViewById(R.id.comment_text);

        Ride selectedRide = Session.getSelectedRide();
        to.setText("To: " + selectedRide.dest);
        from.setText("From: " + selectedRide.from);

        if(selectedRide.comment != null)
            comment.setText("Comment: " + selectedRide.comment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        addRideInfo();
        new UserInfoTask().execute((Void) null);

        Button confirmButton = (Button)findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateRideTask().execute((Void) null);
            }
        });
    }

    public class UserInfoTask extends AsyncTask<Void, Void, Boolean> {
        Bitmap bitmap;
        int retries;

        UserInfoTask(){
            retries = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpURLConnection urlConnection = new FriRideRequest(getString(R.string.website))
                        .makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.PROFILE,
                                "?user=" + Session.getSelectedRide().rider, new String[]{});

                String result = "";
                InputStream responseStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while((line = br.readLine()) != null)
                    result += line;

                JSONArray jsonArray = new JSONObject(result).getJSONArray("user");
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                TextView repView = (TextView)findViewById(R.id.rep_text);
                String rep = jsonObject.getString("rep");
                RideDetailActivity.this.rep = Integer.parseInt(rep);
                repView.setText("Reputation: " + rep);

                final HttpURLConnection imageConnection = new FriRideRequest(getString(R.string.website))
                        .makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.EMPTY,
                            jsonObject.getString("image"), new String[]{});

                bitmap = BitmapFactory.decodeStream(imageConnection.getInputStream());
                if(bitmap == null) return false;
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            TextView user = (TextView)findViewById(R.id.rider_text);
            String rider = Session.getSelectedRide().rider;

            if(success){
                try {
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);
                }catch(Exception e){
                    if(retries != 5){
                        //undocumented random behavior from android threading
                        ++retries;
                        onPostExecute(true);
                    }

                    Log.d("EXCEPTION", e.getMessage());
                }

                user.setText(rider);

                if(rep > 10){
                    user.setTextColor(Color.GREEN);
                }else if(rep < 0){
                    user.setTextColor(Color.RED);
                }
            }else{
                user.setTextColor(Color.RED);
                user.setText("Could not find info for " + rider);
            }
        }
    }

    public class UpdateRideTask extends AsyncTask<Void, Void, Boolean> {
        UpdateRideTask(){}

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpURLConnection urlConnection = new FriRideRequest(getString(R.string.website))
                        .makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.MODRIDE);

                String urlParameters = "ID=" + Session.getSelectedRide().ID + "&status=1";
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                String response = "";
                InputStream responseStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while((line = br.readLine()) != null)
                    response += line;

                //todo: send notification to user
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                finish();
            }else{
                Toast.makeText(RideDetailActivity.this, "Error in accepting ride task.", Toast.LENGTH_LONG);
            }
        }
    }
}
