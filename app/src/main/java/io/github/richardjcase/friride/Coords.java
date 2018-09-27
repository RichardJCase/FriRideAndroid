package io.github.richardjcase.friride;

public class Coords {
    public double latitude, longitude;

    public Coords(){
        latitude = 0;
        longitude = 0;
    }

    public Coords(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toString(){
        return latitude + " " + longitude;
    }
}
