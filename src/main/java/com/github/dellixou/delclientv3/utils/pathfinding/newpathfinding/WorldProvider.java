package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;

import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.BlockState;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.intefaces.IWorldProvider;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.HashSet;

public class WorldProvider implements IWorldProvider {

    private Minecraft mc = Minecraft.getMinecraft();

    final HashSet<Block> NON_SOLID_BLOCKS = new HashSet<>(
            Arrays.asList(
                    Blocks.water,
                    Blocks.lava,
                    Blocks.flowing_lava,
                    Blocks.flowing_water,
                    Blocks.red_flower,
                    Blocks.yellow_flower,
                    Blocks.tallgrass,
                    Blocks.double_plant,
                    Blocks.carpet,
                    Blocks.snow_layer,
                    Blocks.air
            )
    );
    final HashSet<Block> BLOCKS_NOT_PERMITTED_TO_WALK_ON = new HashSet<>(
            Arrays.asList()
    );

    @Override
    public BlockState getBlockState(int[] ints) {
        Block block = mc.theWorld.getBlockState(new BlockPos(ints[0], ints[1],
                ints[2])).getBlock();
        //ChatUtils.debugMessage(Arrays.toString(ints));
        return !NON_SOLID_BLOCKS.contains(block) ? BlockState.OBSTRUCTED :
                BlockState.UNOBSTRUCTED;
    }

    @Override
    public boolean isTranslationValid(Node node, Node parent) {
        //RenderUtil.addBlockToRenderSync(new BlockPos(node.x, node.y, node.z));
        return isWalk(node, parent) || isJump(node, parent) || isFall(node, parent);
    }

    @Override
    public double addToTotalCost(Node node) {
        int[] nodePosition = new int[]{node.x, node.y + 2, node.z};
        double totalCost = 0;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    int[] newNode = new int[]{nodePosition[0] + x, nodePosition[1] + y, nodePosition[2] + z};
                    if (getBlockState(newNode) == BlockState.OBSTRUCTED) {
                        totalCost += 1.0;
                    }
                }
            }
        }

        return totalCost;
    }

    boolean isWalk(Node node, Node parent) {
        Node blockUnder = node.getNodeWithTransformation(new int[]{0, -1, 0});
        Node blockAbove = node.getNodeWithTransformation(new int[]{0, 1, 0});

        return (
                parent.y - node.y == 0
                        && getBlockState(node) == BlockState.UNOBSTRUCTED
                        && getBlockState(blockUnder) == BlockState.OBSTRUCTED
                        && getBlockState(blockAbove) == BlockState.UNOBSTRUCTED
                //&& bresenham(parent, node) == null
        );
    }

    boolean isJump(Node node, Node parent) {
        Node blockUnderNode = node.getNodeWithTransformation(new int[]{0, -1, 0});
        Node blockAboveNode = node.getNodeWithTransformation(new int[]{0, 1, 0});

        Node block1AboveParent = parent.getNodeWithTransformation(new int[]{0, 1, 0});
        Node block2AboveParent = parent.getNodeWithTransformation(new int[]{0, 2, 0});

        return (
                node.y - parent.y > 0
                        && getBlockState(node) == BlockState.UNOBSTRUCTED
                        && getBlockState(blockUnderNode) == BlockState.OBSTRUCTED
                        && getBlockState(blockAboveNode) == BlockState.UNOBSTRUCTED
                        && getBlockState(block1AboveParent) == BlockState.UNOBSTRUCTED
                        && getBlockState(block2AboveParent) == BlockState.UNOBSTRUCTED
        );
    }

    boolean isFall(Node node, Node parent) {
        Node blockUnderNode = node.getNodeWithTransformation(new int[]{0, -1, 0});
        Node block1AboveNode = node.getNodeWithTransformation(new int[]{0, 1, 0});
        Node block2AboveNode = node.getNodeWithTransformation(new int[]{0, 2, 0});

        return (
                node.y - parent.y < 0
                        && getBlockState(blockUnderNode) == BlockState.OBSTRUCTED
                        && getBlockState(block1AboveNode) == BlockState.UNOBSTRUCTED
                        && getBlockState(block2AboveNode) == BlockState.UNOBSTRUCTED
        );
    }
}
