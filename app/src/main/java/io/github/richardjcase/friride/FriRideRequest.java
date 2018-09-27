package io.github.richardjcase.friride;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FriRideRequest {
    private String baseURL;
    private HttpsURLConnection urlConnection;

    public static enum RequestMethod {
        GET, POST
    }

    public static enum RequestType {
        LOGIN, RIDE, MODRIDE, MYRIDES, MYDRIVES, TORATE, RATE, PICEDIT, BIOEDIT, PROFILE,
        CHANGEPASSWORD, FORGOTPASSWORD, CANCEL, EMPTY
    }

    private boolean validRequest(RequestMethod requestMethod, RequestType requestType){
        if(requestMethod == RequestMethod.GET){
            switch(requestType) {
                case EMPTY:
                case RIDE:
                case MYRIDES:
                case MYDRIVES:
                case PROFILE:
                case TORATE:
                    return true;
                default:
                    return false;
            }
        }

        return true;
    }

    private static String requestLocation(RequestType requestType){
        switch(requestType) {
            case LOGIN:
                return "/login";
            case RIDE:
                return "/ride";
            case MODRIDE:
                return "/modride";
            case MYRIDES:
                return "/myrides";
            case MYDRIVES:
                return "/mydrives";
            case RATE:
                return "/rate";
            case TORATE:
                return "/torate";
            case PICEDIT:
                return "/picedit";
            case BIOEDIT:
                return "/bioedit";
            case PROFILE:
                return "/profile";
            case CHANGEPASSWORD:
                return "/changepassword";
            case FORGOTPASSWORD:
                return "/forgotpassword";
            case CANCEL:
                return "/cancel";
            default:
                return "/";
        }
    }

    public FriRideRequest(String baseURL){
        this.baseURL = baseURL;
    }

    public HttpsURLConnection makeRequest(RequestMethod requestMethod, RequestType requestType, String getArgs, String[] props) throws IllegalArgumentException, IOException {
        if(!validRequest(requestMethod, requestType)) throw new IllegalArgumentException("Request method and type do not match any API calls.");
        if(urlConnection != null) throw new IOException("Duplicate request.");
        URL url = new URL(baseURL + requestLocation(requestType) + getArgs);
        urlConnection = (HttpsURLConnection) url.openConnection();

        urlConnection.setRequestProperty("Acceptcharset", "en-us");
        urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        urlConnection.setRequestProperty("charset", "EN-US");
        urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        for(String prop : props){
            String[] slices = prop.split(":");
            urlConnection.setRequestProperty(slices[0], slices[1]);
        }

        String sessionID = Session.getSessionID();
        if(sessionID != null && !sessionID.equals(""))
            urlConnection.setRequestProperty("Cookie", "ID=" + Session.getSessionID());

        if(requestMethod == RequestMethod.POST) {
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
        }

        urlConnection.connect();
        return urlConnection;
    }

    public HttpsURLConnection makeRequest(RequestMethod requestMethod, RequestType requestType) throws IllegalArgumentException, IOException {
        return makeRequest(requestMethod, requestType, "", new String[]{});
    }

    public String getResponse() throws IOException {
        if(urlConnection == null) throw new IOException("No request to obtain response.");

        String response = "";
        InputStream responseStream = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
        String line;
        while((line = br.readLine()) != null)
            response += line;

        br.close();
        return response;
    }
}
