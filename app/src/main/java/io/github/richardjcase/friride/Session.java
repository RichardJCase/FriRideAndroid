package io.github.richardjcase.friride;

import java.util.Map;

public class Session {
    private static String username, sessionID;
    private static Map<Integer, Ride> rides;
    private static Ride selectedRide;

    public static void startSession(String username, String sessionID){
        Session.username = username;
        Session.sessionID = sessionID;
    }

    public static String getUsername(){
        return username;
    }

    public static String getSessionID(){
        return sessionID;
    }

    public static void setSelectedRide(Ride ride){
        selectedRide = ride;
    }

    public static Ride getSelectedRide(){
        return selectedRide;
    }

    public static void setRide(int id, Ride rideInfo){
        if(id == -1){
            rides.remove(rideInfo.ID);
            return;
        }

        rides.put(id, rideInfo);
    }

    public static void removeRide(int id){
        Ride ride = new Ride();
        ride.ID = id;
        setRide(-1, ride);
    }
}
