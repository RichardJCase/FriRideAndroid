package io.github.richardjcase.friride;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class GPS {
    public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static LocationManager locationManager;
    private Context context;
    private double lat, lon;
    private boolean initialized;
    private AsyncTask<Coords, Void, Boolean> newLocationCallback;

    public GPS(final AppCompatActivity context, AsyncTask<Coords, Void, Boolean> newLocationCallback){
        initialized = false;
        this.context = context;
        this.newLocationCallback = newLocationCallback;
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                onNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        try {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }

            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }catch(Exception e){
            Log.d("EXCEPTION", e.getMessage());
        }
    }

    private void onNewLocation(Location location){
        initialized = true;
        lat = location.getLatitude();
        lon = location.getLongitude();

        if(newLocationCallback != null)
            newLocationCallback.execute(new Coords(lat, lon));
    }

    public void setNewLocationCallback(AsyncTask<Coords, Void, Boolean> callback){
        newLocationCallback = callback;
    }

    public Coords getLocation(){
        return (initialized) ? new Coords(lat, lon) : null;
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public static void checkPermission(AppCompatActivity context, AsyncTask<Coords, Void, Boolean> task){
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            new GPS(context, task);
        } else {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }
}
