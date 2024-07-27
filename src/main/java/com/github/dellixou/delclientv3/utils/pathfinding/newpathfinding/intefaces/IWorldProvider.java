package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.intefaces;

import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.Node;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.BlockState;

public interface IWorldProvider {
    BlockState getBlockState(int[] position);

    default BlockState getBlockState(Node position) {
        return getBlockState(new int[]{position.x, position.y, position.z});
    }

    boolean isTranslationValid(Node node, Node parent);

    default double addToTotalCost(Node node) {
        return 0.0;
    }
}