package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;


import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.NodePickStyle;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.intefaces.IWorldProvider;

import java.util.HashSet;
import java.util.PriorityQueue;

public class AStarPathfinder {
    private boolean calcing = false;

    public Node calculate0(IWorldProvider world, NodePickStyle pickStyle, int maxIter, Node startNode, Node endNode) {
        calcing = true;
        int curIter = 0;

        HashSet<Node> closed = new HashSet<>();
        HashSet<Node> openHash = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(startNode);
        openHash.add(startNode);

        while (!open.isEmpty() && calcing && curIter < maxIter) {
            Node best = open.poll();
            closed.add(best);

            if (endNode.equals(best)) {
                return best;
            }

            for (int[] transformation : pickStyle.styleArray) {
                Node transformed = best.getNodeWithTransformation(transformation);

                if (closed.contains(transformed) || openHash.contains(transformed))
                    continue;

                if (!world.isTranslationValid(transformed, best)) {
                    continue;
                }

                transformed.initiateCosts(world, endNode);
                open.add(transformed);
                openHash.add(transformed);
            }

            curIter++;
        }

        calcing = false;
        return null;
    }

    public Node calculate1(IWorldProvider world, NodePickStyle pickStyle, int[] pos1, int[] pos2) {
        Node start = new Node(pos1[0], pos1[1], pos1[2], null);
        Node end = new Node(pos2[0], pos2[1], pos2[2], null);

        return this.calculate0(world,
                pickStyle,
                Integer.MAX_VALUE,
                new Node(start.x, start.y, start.z, start.distanceTo(end), 0, null),
                new Node(end.x, end.y, end.z, 0, end.distanceTo(start), null)
        );
    }

    public Node calculate1(IWorldProvider world, NodePickStyle pickStyle, int maxIter, int[] pos1, int[] pos2) {
        Node start = new Node(pos1[0], pos1[1], pos1[2], null);
        Node end = new Node(pos2[0], pos2[1], pos2[2], null);

        return this.calculate0(world,
                pickStyle,
                maxIter,
                new Node(start.x, start.y, start.z, start.distanceTo(end), 0, null),
                new Node(end.x, end.y, end.z, 0, end.distanceTo(start), null)
        );
    }

    public void turnOffPathing() {
        calcing = false;
    }
}
