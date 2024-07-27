package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.utils.enums.RouteItem;

public class Waypoint {

    private double x;
    private double y;
    private double z;
    private boolean stopVelocity;
    private boolean useJump;
    private boolean lookOnly;
    private boolean useRightClick;
    private double yaw;
    private double pitch;
    private boolean independent;
    private RouteItem routeItem;
    private boolean edgeJump;
    private boolean bonzo;
    private boolean wait;
    private float time;

    private boolean done;
    public boolean isWaitingJump = false;

    public Waypoint(double x, double  y, double  z, boolean stopVelocity, boolean useJump, boolean lookOnly, boolean useRightClick, double yaw, double pitch, boolean independent, RouteItem routeItem, boolean edgeJump, boolean bonzo, boolean wait, float time){
        this.x = x;
        this.y = y;
        this.z = z;
        this.stopVelocity = stopVelocity;
        this.useJump = useJump;
        this.lookOnly = lookOnly;
        this.useRightClick = useRightClick;
        this.yaw = yaw;
        this.pitch = pitch;
        this.independent = independent;
        this.routeItem = routeItem;
        this.edgeJump = edgeJump;
        this.bonzo = bonzo;
        this.wait = wait;
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean getUseJump(){
        return this.useJump;
    }

    public boolean getEdgeJump(){
        return this.edgeJump;
    }

    public boolean getStopVelocity(){
        return this.stopVelocity;
    }

    public boolean getLookOnly(){
        return this.lookOnly;
    }

    public boolean getClick(){
        return this.useRightClick;
    }

    public double getYaw(){
        return this.yaw;
    }

    public double getPitch(){
        return this.pitch;
    }

    public boolean getIndependent(){
        return this.independent;
    }

    public boolean getBonzo(){
        return this.bonzo;
    }

    public RouteItem getRouteItem(){
        return this.routeItem;
    }

    public boolean getWait(){
        return this.wait;
    }

    public float getTime(){
        return this.time;
    }

}
