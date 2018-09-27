package io.github.richardjcase.friride;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class ProfileActivity extends AppCompatActivity {
    boolean editingBio, changingPassword;


    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
            Uri selectedImage = imageReturnedIntent.getData();
            ImageView imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageURI(selectedImage);
            new UpdateImageTask(imageView).execute((Void) null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });

        Button editBioButton = (Button)findViewById(R.id.bio_button);
        editBioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingBio = !editingBio;
                EditText editText = (EditText) findViewById(R.id.bio_edit);
                TextView bioText = (TextView) findViewById(R.id.bio_text);
                Button bioButton = (Button)findViewById(R.id.bio_button);

                if(editingBio) {
                    bioText.setVisibility(View.GONE);
                    editText.setText(bioText.getText());
                    editText.setVisibility(View.VISIBLE);
                    bioButton.setText("Save");
                }else{
                    new UpdateBioTask().execute((Void) null);
                    bioText.setText(editText.getText());
                    bioText.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.GONE);
                    bioButton.setText("Edit Bio");
                }
            }
        });

        Button changePasswordButton = (Button)findViewById(R.id.password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changingPassword = !changingPassword;
                EditText oldPassText = (EditText)findViewById(R.id.oldpass_edit);
                EditText newPassText = (EditText)findViewById(R.id.newpass_edit);
                EditText confirmText = (EditText)findViewById(R.id.confirm_edit);
                Button passButton = (Button)findViewById(R.id.password_button);

                if(changingPassword){
                    oldPassText.setVisibility(View.VISIBLE);
                    newPassText.setVisibility(View.VISIBLE);
                    confirmText.setVisibility(View.VISIBLE);
                    passButton.setText("Save");
                }else{
                    if(!newPassText.getText().toString().equals(confirmText.getText().toString())){
                        newPassText.setError("Passwords don't match");
                        newPassText.requestFocus();
                        return;
                    }

                    new ChangePasswordTask(oldPassText.getText().toString(),
                            newPassText.getText().toString()).execute((Void) null);
                    oldPassText.setVisibility(View.GONE);
                    newPassText.setVisibility(View.GONE);
                    confirmText.setVisibility(View.GONE);
                    passButton.setText("Change Password");
                }
            }
        });

        new UserInfoTask((TextView)findViewById(R.id.status_text)).execute((Void) null);
    }

    public class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {
        private String oldpass, newpass;

        ChangePasswordTask(String oldpass, String newpass){
            this.oldpass = oldpass;
            this.newpass = newpass;
        }

        @Override
        protected Boolean doInBackground(Void... voids){
            try{
                FriRide.editPassword(oldpass, newpass);
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success){

        }
    }

    public class UpdateBioTask extends AsyncTask<Void, Void, Boolean> {
        UpdateBioTask(){}

        @Override
        protected Boolean doInBackground(Void... voids){
            EditText editText = (EditText) findViewById(R.id.bio_edit);

            try{
                FriRide.editBio(editText.getText().toString());
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success){

        }
    }

    public class UpdateImageTask extends AsyncTask<Void, Void, Boolean> {
        ImageView image;

        UpdateImageTask(ImageView image){
            this.image = image;
        }

        @Override
        protected Boolean doInBackground(Void... voids){
            try{
                FriRide.editPicture(image);
            }catch(Exception e){
                Log.d("EXCEPTION", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success){

        }
    }

    public class UserInfoTask extends AsyncTask<Void, Void, Boolean> {
        Bitmap bitmap;
        int retries, reputation;
        TextView user;

        UserInfoTask(TextView user){
            this.user = user;
            retries = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONObject jsonObject = FriRide.getProfile();
                TextView repView = (TextView)findViewById(R.id.rep_text);
                TextView bioView = (TextView)findViewById(R.id.bio_text);
                String rep = jsonObject.getString("rep");
                reputation = Integer.parseInt(rep);
                repView.setText("Reputation: " + rep);

                if(jsonObject.has("bio")) {
                    String bio = jsonObject.getString("bio");
                    bioView.setText(bio);
                }

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

                user.setText(Session.getUsername());

                if(reputation > 10){
                    user.setTextColor(Color.GREEN);
                }else if(reputation < 0){
                    user.setTextColor(Color.RED);
                }
            }else{
                user.setTextColor(Color.RED);
                user.setText("Could not find info for " + Session.getUsername());
            }
        }
    }
}
