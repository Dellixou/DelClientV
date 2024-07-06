package com.github.dellixou.delclientv3.utils.misc;

import java.util.ArrayList;
import java.util.List;

public class Route {

    List<Waypoint> waypoints = new ArrayList<>();
    public float red = 1;
    public float green = 1;
    public float blue = 1;
    private String name = "route";

    public Route(float red, float green, float blue, String name){
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.name = name;
    }

    public List<Waypoint> getWaypoints(){
        return waypoints;
    }

    public List<Waypoint> getNonIndeWaypoints(){
        List<Waypoint> result = new ArrayList<>();
        for(Waypoint waypoint : waypoints){
            if(!waypoint.getIndependent()){
                result.add(waypoint);
            }
        }
        return result;
    }

    public List<Waypoint> getIndeWaypoints(){
        List<Waypoint> result = new ArrayList<>();
        for(Waypoint waypoint : waypoints){
            if(waypoint.getIndependent()){
                result.add(waypoint);
            }
        }
        return result;
    }

    public void addWaypoints(double x, double y, double z, boolean stopVelocity, boolean useJump, boolean lookOnly, boolean useRightClick, double yaw, double pitch, boolean independent, RouteItem routeItem, boolean edgeJump, boolean bonzo, boolean wait, float time){
        waypoints.add(new Waypoint(x, y, z, stopVelocity, useJump, lookOnly, useRightClick, yaw, pitch, independent, routeItem, edgeJump, bonzo, wait, time));
    }

    public void removeLastWaypoint(){
        waypoints.remove(waypoints.size()-1);
    }

    public void resetWaypoints(){
        waypoints.clear();
    }

    public String getName(){
        return this.name;
    }

}
