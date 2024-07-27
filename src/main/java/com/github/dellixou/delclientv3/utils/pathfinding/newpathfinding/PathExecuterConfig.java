package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;

public class PathExecuterConfig {
    public PathExecuterConfig(double minDistanceToNextNode, double jumpDistance, int rotationTimeMS,
                              boolean rePathfindOnStuck) {
        this.minDistanceToNextNode = minDistanceToNextNode;
        this.jumpDistance = jumpDistance;
        this.rotationTimeMS = rotationTimeMS;
        this.rePathfindOnStuck = rePathfindOnStuck;
    }

    public double minDistanceToNextNode;
    public double jumpDistance;
    public int rotationTimeMS;
    public boolean rePathfindOnStuck;
}

