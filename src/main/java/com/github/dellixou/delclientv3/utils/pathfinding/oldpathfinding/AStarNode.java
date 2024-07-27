package com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding;

import java.util.Objects;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class AStarNode {

    private final int x;
    private final int y;
    private final int z;
    private double hCost;
    private int gCost;
    private AStarNode parent;
    private BlockPos blockPos;

    private boolean isJumpNode;

    private boolean isFallNode;

    public AStarNode(BlockPos pos, AStarNode parentNode, AStarNode endNode) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockPos = pos;

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public AStarNode(int xRel, int yRel, int zRel, AStarNode parentNode, AStarNode endNode) {
        this.x = xRel + parentNode.getX();
        this.y = yRel + parentNode.getY();
        this.z = zRel + parentNode.getZ();
        this.blockPos = new BlockPos(x, y, z);

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public AStarNode(BlockPos pos, AStarNode endNode) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockPos = pos;

        calculateHeuristic(endNode);
    }

    public AStarNode(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public boolean canBeTraversed() {
        if (isBlockSolid(blockPos) || isBlockSolid(new BlockPos(x, y + 1, z))) {
            return false;
        }

        if (parent.isFallNode() && parent.getY() == y) {
            return false;
        }

        if (isBlockSolid(new BlockPos(x, y - 1, z))) {
            return true;
        }

        if (parent == null) {
            return false;
        }

        if (parent.blockPos.getY() - 1 == y - 2 && isBlockSolid(new BlockPos(x, y - 2, z))) {
            setJumpNode(true);
            return true;
        }

        if (parent.isFallNode() && y == parent.getY() - 1) {
            setFallNode(true);
            return true;
        }

        if (parent.blockPos.getY() == y && isBlockSolid(new BlockPos(parent.blockPos.getX(), parent.blockPos.getY() - 1, parent.blockPos.getZ()))) {
            setFallNode(true);
            return true;
        }


        return false;
    }

    private boolean isBlockSolid(BlockPos block) {
        return Minecraft.getMinecraft().theWorld.getBlockState(block)
                .getBlock().isBlockSolid(Minecraft.getMinecraft().theWorld, block, null) ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockSlab ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockStainedGlass ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockPane ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockFence ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockPistonExtension ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockEnderChest ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockTrapDoor ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockPistonBase ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockChest ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockStairs ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockCactus ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockWall ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockGlass ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockSkull ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock() instanceof BlockSand ||
                Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock().getMaterial() instanceof MaterialLiquid;
    }

    private void calculateHeuristic(AStarNode endNode) {
        this.hCost = (double) (Math.abs(endNode.getX() - x) + Math.abs(endNode.getY() - y) + Math.abs(endNode.getZ() - z)) * 10;
    }

    public double getTotalCost() {
        return hCost + gCost;
    }

    public int getX() {
        return x;
    }

    public int getGCost() {
        return gCost;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public AStarNode getParent() {
        return parent;
    }

    public void setParent(AStarNode parent) {
        this.parent = parent;

        int xDiff = Math.abs(x - parent.getX());
        int yDiff = Math.abs(y - parent.getY());
        int zDiff = Math.abs(z - parent.getZ());


        this.gCost = parent.getGCost() + (xDiff + yDiff + zDiff) * 10;
    }

    public Vec3 asVec3(double xAdd, double yAdd, double zAdd) {
        return new Vec3(x + xAdd, y + yAdd, z + zAdd);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AStarNode)) {
            return false;
        }

        AStarNode other = (AStarNode) o;

        return x == other.getX() && y == other.getY() && z == other.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, hCost, gCost, parent, blockPos, isJumpNode, isFallNode);
    }

    public boolean isFallNode() {
        return isFallNode;
    }

    public void setFallNode(boolean fallNode) {
        isFallNode = fallNode;
    }

    public boolean isJumpNode() {
        return isJumpNode;
    }

    public void setJumpNode(boolean jumpNode) {
        isJumpNode = jumpNode;
    }

    public BlockPos getAsBlockPos() {
        return new BlockPos(x, y, z);
    }
}