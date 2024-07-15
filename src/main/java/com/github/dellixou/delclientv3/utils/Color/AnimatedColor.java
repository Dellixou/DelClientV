package com.github.dellixou.delclientv3.utils.Color;

import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.utils.gui.animations.LinearAnimation;

import java.awt.Color;

public class AnimatedColor {
    private boolean isActive = false;
    private LinearAnimation animation;
    private final Color baseColor;
    private final Color activeColor;
    private final int animationDuration;
    private boolean guiColor;

    public AnimatedColor(Color baseColor, Color activeColor, int animationDuration, boolean guiColor) {
        this.baseColor = baseColor;
        if(guiColor){
            this.activeColor = ColorUtil.getClickGUIColor();
        }else{
            this.activeColor = activeColor;
        }
        this.animationDuration = animationDuration;
        this.guiColor = guiColor;
    }

    public Color update(boolean shouldBeActive) {
        if (shouldBeActive != isActive) {
            isActive = shouldBeActive;
            animation = new LinearAnimation(isActive ? 0 : 1, isActive ? 1 : 0, animationDuration, true);
        }

        if (animation != null) {
            if (animation.isAnimationDone()) {
                if(guiColor){
                    return isActive ? ColorUtil.getClickGUIColor() : baseColor;
                }else{
                    return isActive ? activeColor : baseColor;
                }
            } else {
                float progress = animation.getAnimationValue();
                if(guiColor){
                    return ColorUtils.lerpColor(baseColor, ColorUtil.getClickGUIColor(), progress);
                }else{
                    return ColorUtils.lerpColor(baseColor, activeColor, progress);
                }
            }
        }

        return baseColor;
    }
}
