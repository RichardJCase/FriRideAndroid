package io.github.richardjcase.friride;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RequestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRequest();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case GPS.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    submitRequest();
                }else{
                    TextView textView = (TextView)findViewById(R.id.status_text);
                    textView.setText("Location not available");
                    textView.setTextColor(Color.RED);
                }
            }
        }
    }

    private void submitRequest(){
        String to, from, comment, payment;

        EditText et = (EditText)findViewById(R.id.from_text);
        from = et.getText().toString();

        et = (EditText)findViewById(R.id.to_text);
        to = et.getText().toString();
        if(to.equals("") || from.equals("")) return;

        et = (EditText)findViewById(R.id.comment_text);
        comment = et.getText().toString();

        et = (EditText)findViewById(R.id.payment_text);
        payment = et.getText().toString();

        GPS.checkPermission(this, new RequestTask(from, to, comment, payment));
    }


    public class RequestTask extends AsyncTask<Coords, Void, Boolean> {
        private final String from, to, comment, payment;

        RequestTask(String from, String to, String comment, String payment) {
            this.from = from;
            this.to = to;
            this.comment = comment;
            this.payment = payment;
        }

        @Override
        protected Boolean doInBackground(Coords... coords) {
            TextView tv = (TextView)findViewById(R.id.error_message);
            tv.setVisibility(View.INVISIBLE);

            try {
                return FriRide.requestRide(from, to, comment, payment, coords[0]);
            } catch (Exception e) {
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                finish();
            } else {
                TextView tv = (TextView)findViewById(R.id.error_message);
                tv.setVisibility(View.VISIBLE);
            }
        }
    }
}
