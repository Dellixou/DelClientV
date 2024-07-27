package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;

import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.intefaces.IWorldProvider;

import java.util.Stack;

public class Node implements Comparable {
    public final int x, y, z;
    private double costH;
    private double costP;
    private double costTotal;
    private final Node parent;

    public Node(int x, int y, int z, Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.costH = 0;
        this.costP = 0;

        this.costTotal = costH + costP;

        this.parent = parent;
    }

    public Node(int x, int y, int z, double costH, double costP, Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.costH = costH;
        this.costP = costP;
        this.costTotal = costH + costP;

        this.parent = parent;
    }

    public void initiateCosts(IWorldProvider world, Node endGoal) {
        this.costH = endGoal.distanceTo(this);
        this.costP = parent != null ? parent.costP + this.distanceTo(parent) : 0;
        this.costTotal = costH + costP + world.addToTotalCost(this);
    }

    public double distanceTo(Node o) {
        return Math.sqrt((o.x - this.x) * (o.x - this.x) + (o.y - this.y) * (o.y - this.y) + (o.z - this.z) * (o.z - this.z));
    }

    public Node getNodeWithTransformation(int[] transformationMatrix) {
        return new Node(this.x + transformationMatrix[0], this.y + transformationMatrix[1],
                this.z + transformationMatrix[2], this);
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(this.costTotal, ((Node) o).costTotal);
    }

    @Override
    public boolean equals(Object o) {
        Node other = (Node) o;
        return other.x == this.x && other.y == this.y && other.z == this.z;
    }

    public Stack<Node> toStack() {
        Node currentNode = this;
        Stack<Node> returnStack = new Stack<>();
        while (currentNode != null) {
            returnStack.push(currentNode);
            currentNode = currentNode.parent;
        }

        return returnStack;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    @Override
    public int hashCode() {
        return (this.y + this.z * 31) * 31 + this.x;
    }
}