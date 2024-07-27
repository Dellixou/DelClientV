package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums;

// TODO: finish the "SIDES" since they are broken.
public enum NodePickStyle {
    CROSS(new int[][]{
            new int[]{
                    1, 0, 0
            },
            new int[]{
                    -1, 0, 0
            },
            new int[]{
                    0, 1, 0
            },
            new int[]{
                    0, -1, 0
            },
            new int[]{
                    0, 0, 1
            },
            new int[]{
                    0, 0, -1
            },
    }),
    SIDES(
            getAll()
    );

    public final int[][] styleArray;

    NodePickStyle(int[][] ints) {
        styleArray = ints;
    }


    public static int[][] getAll() {
        int[][] returnInts = new int[27][3];
        int i = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    returnInts[i] = new int[] {x, y, z};
                    i++;
                }
            }
        }

        return returnInts;
    }
}
