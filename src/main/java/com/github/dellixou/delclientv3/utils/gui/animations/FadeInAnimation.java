package com.github.dellixou.delclientv3.utils.gui.animations;

public class FadeInAnimation {
    private int counter = 0;
    private final int maxValue;
    private final int duration;

    public FadeInAnimation(int maxValue, int duration) {
        this.maxValue = maxValue;
        this.duration = duration;
    }

    public int getProgress(){
        if(counter >= duration){
            return (int) maxValue;
        }
        if(counter == 0){
            counter++;
            return 1;
        }
        int progress = (int) Math.ceil((double) counter/duration * maxValue);
        counter++;
        return progress;
    }

    public void reset(){
        counter = 0;
    }

}
