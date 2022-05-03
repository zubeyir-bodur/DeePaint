package com.example.deepaint;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Deprecated
 */
public class SegmentationVisualizer {
    /*
    // PROPERTIES

    // The image
    private int[][][] I;
    private int width;
    private int height;

    // The set of masks
    private ArrayList<int[][]> masks;

    // Opacity of the segments - ranges from 0 to 1
    private float opacity;

    public SegmentationVisualizer(Bitmap bitmap, int width, int height, ArrayList<int[][]> masks, float opacity) {
        I = getMatrixRGB(bitmap);
        this.width = width;
        this.height = height;

        // No need to copy the set of masks, may be too large
        this.masks = masks;
        this.opacity = opacity;
    }

    public int[][][] getMapImageCombined() {
        int[][][] segmentationMap = getSegmentationMap();
        int[][][] combined = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int d = 0; d < 3; d++) {
                    combined[y][x][d] = (int)(I[y][x][d]*(1-opacity)) + (int)(segmentationMap[y][x][d]*opacity);
                }
            }
        }
        return combined;
    }

    public int[][][] getSegmentationMap() {
        // Elliptic kernel for magnifying the edges
        int[][] SE = new int[][]{
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0},
        };
        int num_masks = this.masks.size();
        int[][] colors = new int[num_masks][3];
        int[][][] visualizer = new int[height][width][3];

        for (int i = 0; i < num_masks; i++) {
            colors[i] = new int[]{  (int)Math.floor(Math.random()* 256),
                                    (int)Math.floor(Math.random()* 256),
                                    (int)Math.floor(Math.random()* 256) };

            int[][] mask = new int[height][width];
            for (int y = 0; y < height; y++) {
                if (width >= 0)
                    System.arraycopy(masks.get(i)[y], 0, mask[y], 0, width);
            }
            int[][] edge = getSobelEdges(mask);
            int[][] edge_bold = dilate(edge, SE);
            int[][][] colored_edge = new int[height][width][3];
            int[][][] colored_region = new int[height][width][3];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (edge_bold[y][x] != 0){
                        System.arraycopy(colors[i], 0, colored_edge[y][x], 0, 3);
                    }
                    else if (mask[y][x] > 0) {
                        for (int d = 0; d < 3; d++)
                            colored_region[y][x][d] = (int)(colors[i][d] * opacity);
                    }
                }
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int d = 0; d < 3; d++)
                        visualizer[y][x][d] += colored_region[y][x][d] + colored_edge[y][x][d];
                    }
                }
            }
        return visualizer;
    }

    public static int[][][] getMatrixRGB(Bitmap bmb) {
        return null;
    }
    public static int[][] getSobelEdges(int[][] grayscale) {
        return null;
    }
    public static int[][] dilate(int[][] grayscale, int[][] SE) {
        return null;
    }*/
}
