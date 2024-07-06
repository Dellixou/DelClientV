package com.github.dellixou.delclientv3.utils.gui.animations;

public class MovingAnimation {
    private int counter = 0;
    private double startValue;
    private double endValue;
    private final int duration;
    private boolean canAnimate = true;

    public MovingAnimation(double startValue, double endValue, int duration) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.duration = duration;
    }

    public double getProgress(){
        if(!canAnimate) return endValue;
        if(counter >= duration){
            return endValue;
        }
        if(counter == 0){
            counter++;
            return startValue;
        }
        //int progress = (int) Math.ceil((double) counter/duration * endValue);
        double progress = startValue + (endValue - startValue) * ((double) counter / duration);
        counter++;
        return progress;
    }

    public void reset(){
        counter = 0;
    }

    public void setValues(double startValue, double endValue){
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public double getEndValue() {
        return this.endValue;
    }

    public double getStartValue() {
        return this.startValue;
    }

    public void setAnimating(boolean animating){
        this.canAnimate = animating;
    }

}
