package io.github.richardjcase.friride;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


public class ReviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        new UserRatesTask().execute((Void) null);

        final Button submit = (Button) findViewById(R.id.submit_button);
        final TextView status = (TextView) findViewById(R.id.status_text);

        final Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setSelection(2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                submit.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                submit.setEnabled(false);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RateUserTask(status.getText().toString(), spinner.getSelectedItem().toString()).execute((Void) null);
                new UserRatesTask().execute((Void) null);
                submit.setEnabled(false);
            }
        });
    }

    public class RateUserTask extends AsyncTask<Void, Void, Boolean> {
        String uname, rating;

        RateUserTask(String uname, String rating){
            this.uname = uname;
            this.rating = rating;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                FriRide.rate(uname, rating);
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }
    }

    /**
     * Creates the UI for first person to review to be popped off of the stack
     */
    public class UserRatesTask extends AsyncTask<Void, Void, Boolean> {
        boolean driver;
        Ride ride;

        UserRatesTask(){

        }

        private Ride getTopRide(){
            try {
                return FriRide.toRate();
            }catch (Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return null;
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ride = getTopRide();
            return ride != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                TextView status = (TextView) findViewById(R.id.status_text);
                TextView to = (TextView) findViewById(R.id.to_text);
                TextView from = (TextView) findViewById(R.id.from_text);

                driver = ride.status == 1;
                status.setText((driver) ? ride.rider : ride.driver);
                to.setText(ride.dest);
                to.setVisibility(View.VISIBLE);

                from.setText(ride.from);
                from.setVisibility(View.VISIBLE);
            }else{
                TextView status = (TextView) findViewById(R.id.status_text);
                TextView to = (TextView) findViewById(R.id.to_text);
                TextView from = (TextView) findViewById(R.id.from_text);
                Spinner spinner = (Spinner) findViewById(R.id.spinner);

                status.setText("No users to rate found.");
                to.setVisibility(View.GONE);
                from.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
            }
        }
    }
}
