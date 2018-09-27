package io.github.richardjcase.friride;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class FriRide {
    public static String url;

    public static String getResponse(URLConnection urlConnection) throws IOException {
        String response = "";
        InputStream responseStream = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
        String line;
        while((line = br.readLine()) != null)
            response += line;

        return response;
    }

    public static void writeParams(URLConnection urlConnection, String urlParameters) throws IOException {
        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
    }

    public static boolean login(String username, String password) throws IOException {
        HttpsURLConnection urlConnection = new FriRideRequest(url)
                .makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.LOGIN);

        String urlParameters = "username=" + username + "&password=" + password;
        writeParams(urlConnection, urlParameters);

        String response = getResponse(urlConnection);

        Map<String, List<String>> fields = urlConnection.getHeaderFields();
        if(!fields.containsKey("Set-Cookie")){
            Log.d("SESSION", "no session made");
            return false;
        }

        String sessionID = (String)fields.get("Set-Cookie").toArray()[0];
        sessionID = sessionID.split(";")[0].substring(3);
        Session.startSession(username, sessionID);

        return !response.contains("Invalid");
    }

    public static JSONObject getProfile() throws IOException, JSONException {
        HttpsURLConnection urlConnection = new FriRideRequest(url)
                .makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.PROFILE,
                        "?user=" + Session.getUsername(), new String[]{});

        String result = getResponse(urlConnection);

        JSONArray jsonArray = new JSONObject(result).getJSONArray("user");
        return jsonArray.getJSONObject(0);
    }

    public static JSONArray getRides(Coords location) throws IOException, JSONException {
        HttpURLConnection urlConnection = new FriRideRequest(url)
                .makeRequest(FriRideRequest.RequestMethod.GET,
                        FriRideRequest.RequestType.RIDE, "?loc=" + location.toString().replace(" ", "%20"),
                        new String[]{});

        String result = getResponse(urlConnection);

        return new JSONObject(result).getJSONArray("available_rides");
    }

    public static String getUserRides() throws IOException {
        FriRideRequest friRideRequest = new FriRideRequest(url);
        friRideRequest.makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.MYRIDES);
        return friRideRequest.getResponse();
    }

    public static String getUserDrives() throws IOException {
        FriRideRequest hytchRequest = new FriRideRequest(url);
        hytchRequest.makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.MYDRIVES);
        return hytchRequest.getResponse();
    }

    public static void rate(String uname, String rating) throws IOException {
        FriRideRequest hytchRequest = new FriRideRequest(url);
        HttpsURLConnection urlConnection = hytchRequest.makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.RATE);

        String urlParameters = "rating=" + rating + "&user=" + uname;
        writeParams(urlConnection, urlParameters);

        hytchRequest.getResponse();
    }

    public static Ride toRate() throws IOException, JSONException {
        Ride ride = new Ride();
        FriRideRequest hytchRequest = new FriRideRequest(url);
        hytchRequest.makeRequest(FriRideRequest.RequestMethod.GET, FriRideRequest.RequestType.TORATE);
        String response = hytchRequest.getResponse();

        ArrayList<Ride> rides = new ArrayList<Ride>();
        JSONArray jsonArray = new JSONObject(response).getJSONArray("rides");
        if (jsonArray.length() == 0) throw new JSONException("JSON array length is zero, hence no results.");
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        ride.ID = Integer.parseInt(jsonObject.getString("ID"));
        ride.status = Integer.parseInt(jsonObject.getString("isdriver")); //cheating here to select if it is rider or driver to rate
        ride.loc = jsonObject.getString("loc");
        ride.dest = jsonObject.getString("dest");
        if (jsonObject.has("comment"))
            ride.comment = jsonObject.getString("comment");
        ride.created = jsonObject.getString("created");
        ride.rider = jsonObject.getString("rider");
        ride.driver = jsonObject.getString("driver");

        return ride;
    }

    public static void editPicture(ImageView image) throws IOException {
        String ctrl = "\r\n";
        String boundary = "*****" + Long.toHexString(System.currentTimeMillis()) + "*****";
        String data = "--" + boundary + ctrl;
        data += "Content-Disposition: form-data; name=pic; filename=img.png" + ctrl;
        data += "Content-Type: image/png" + ctrl;
        data += "Content-Transfer-Encoding: binary" + ctrl + ctrl;

        FriRideRequest hytchRequest = new FriRideRequest(url);
        HttpsURLConnection urlConnection = hytchRequest
                .makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.PICEDIT,
                        "", new String[]{"Content-Type:multipart/form-data; boundary="+ boundary});

        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

        wr.writeBytes("--" + boundary + ctrl);
        wr.writeBytes("Content-Disposition: form-data; name=user" + ctrl);
        wr.writeBytes("Content-Type: text/plain" + ctrl);
        wr.writeBytes(ctrl + Session.getUsername() + ctrl);
        wr.flush();

        wr.writeBytes(data);
        wr.flush();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        for(byte b : bytes)
            wr.write(b);

        wr.flush();

        wr.writeBytes(ctrl);
        wr.flush();

        wr.writeBytes("--" + boundary + "--" + ctrl);
        wr.flush();

        wr.close();

        hytchRequest.getResponse();
    }

    public static void editBio(String newBio) throws IOException {
        FriRideRequest hytchRequest = new FriRideRequest(url);
        HttpsURLConnection urlConnection = hytchRequest.makeRequest(FriRideRequest.RequestMethod.POST,
                FriRideRequest.RequestType.BIOEDIT);

        String urlParameters = "user=" + Session.getUsername() + "&bio=" + newBio;
        writeParams(urlConnection, urlParameters);

        hytchRequest.getResponse();
    }

    public static void editPassword(String oldPassword, String newPassword) throws IOException {
        FriRideRequest hytchRequest = new FriRideRequest(url);
        HttpsURLConnection urlConnection = hytchRequest.makeRequest(FriRideRequest.RequestMethod.POST,
                FriRideRequest.RequestType.CHANGEPASSWORD);
        String urlParameters = "user=" + Session.getUsername() + "&oldpass=" + oldPassword + "&newpass=" + newPassword;
        writeParams(urlConnection, urlParameters);

        hytchRequest.getResponse();
    }

    public static boolean requestRide(String from, String to, String comment, String payment, Coords loc) throws IOException {
        FriRideRequest hytchRequest = new FriRideRequest(url);
        HttpURLConnection urlConnection = hytchRequest.makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.RIDE);

        String urlParameters = "from=" + from + "&to=" + to + "&comment=" + comment +
                "&payment=" + payment + "&loc=" + loc.toString().replace(" ", "%20");
        writeParams(urlConnection, urlParameters);

        return !hytchRequest.getResponse().contains("error");
    }

    public static void cancelRide(String uname, String rating) throws IOException {
        FriRideRequest friRideRequest = new FriRideRequest(url);
        HttpsURLConnection urlConnection = friRideRequest.makeRequest(FriRideRequest.RequestMethod.POST, FriRideRequest.RequestType.CANCEL);

        String urlParameters = "rating=" + rating + "&user=" + uname;
        writeParams(urlConnection, urlParameters);

        friRideRequest.getResponse();
    }
}
