package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static byog.Core.RandomUtils.uniform;
import static byog.Core.RandomUtils.uniformInclusive;

/**
 * Invariants
 * 1. The minimum rectangular side length is 3(i.e. topY - bottomY >=2, rightX - leftX >= 2).
 * 2. Adjacent rectangles have at least 3 tiles adjacent to each other, with a channel of width 1.
 * 3. Each rectangle is adjacent to at least 1 and at most 4 rectangles.
 */
public class MapGenerator {

    private class Rectangle {
        int topY;
        int bottomY;
        int rightX;
        int leftX;

        Rectangle(int leftX, int rightX, int bottomY, int topY) {
            this.topY = topY;
            this.bottomY = bottomY;
            this.rightX = rightX;
            this.leftX = leftX;
        }

        public boolean isConflict(Rectangle r) {
            return !(leftX > r.rightX || bottomY > r.topY || r.leftX > rightX || r.bottomY > topY);
        }
    }

    int WIDTH;
    int HEIGHT;
    long SEED;
    Random RANDOM;
    int maxRectWidth = 8;
    int maxRectHeight = 8;
    int minSideLength = 3;
    int minAdjacentLength = 3;
    List<Rectangle> existedRectangle = new ArrayList<>(15);

    public MapGenerator(int width, int height, long seed) {
        WIDTH = width;
        HEIGHT = height;
        SEED = seed;
        RANDOM = new Random(seed);
    }

    public MapGenerator(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        RANDOM = new Random();
    }

    private void addRectangle(TETile[][] world, Rectangle rectangle) {
        int x = rectangle.leftX;
        int y = rectangle.bottomY;
        int height = rectangle.topY - rectangle.bottomY;
        int width = rectangle.rightX - rectangle.leftX;
        for (int row = 0; row <= height; row++) {
            if (row == 0 || row == height) {
                for (int col = 0; col <= width; col++) {
                    world[x + col][y + row] =
                            TETile.colorVariant(Tileset.WALL, 64, 64, 64, RANDOM);
                }
            } else {
                for (int col = 0; col <= width; col++) {
                    if (col == 0 || col == width) {
                        world[x + col][y + row] =
                                TETile.colorVariant(Tileset.WALL, 64, 64, 64, RANDOM);
                    } else {
                        world[x + col][y + row] =
                                TETile.colorVariant(Tileset.FLOOR, 64, 64, 64, RANDOM);
                    }
                }
            }
        }
        existedRectangle.add(rectangle);
    }

    private Rectangle generateLeftRectangle(Rectangle origin) {
        int rightX = origin.leftX - 1;
        int leftX = uniformInclusive(RANDOM, rightX - maxRectWidth + 1, rightX - minSideLength + 1);

        int sideLengthY = uniformInclusive(RANDOM, minSideLength, maxRectHeight);
        int bottomY = uniformInclusive(RANDOM, origin.bottomY + minAdjacentLength - sideLengthY,
                origin.topY - minAdjacentLength + 1);
        int topY = bottomY + sideLengthY - 1;
        Rectangle newRectangle = new Rectangle(leftX, rightX, bottomY, topY);
        return newRectangle;
    }

    private Rectangle generateBottomRectangle(Rectangle origin) {
        int topY = origin.bottomY - 1;
        int bottomY =
                uniformInclusive(RANDOM, topY - maxRectHeight + 1, topY - minSideLength + 1);

        int sideLengthX = uniformInclusive(RANDOM, minSideLength, maxRectWidth);
        int leftX = uniformInclusive(RANDOM, origin.leftX + minAdjacentLength - sideLengthX,
                origin.rightX - minAdjacentLength + 1);
        int rightX = leftX + sideLengthX - 1;
        Rectangle newRectangle = new Rectangle(leftX, rightX, bottomY, topY);
        return newRectangle;
    }

    private Rectangle generateRightRectangle(Rectangle origin) {
        int leftX = origin.rightX + 1;
        int rightX = uniformInclusive(RANDOM, leftX + minSideLength - 1, leftX + maxRectWidth - 1);

        int sideLengthY = uniformInclusive(RANDOM, minSideLength, maxRectHeight);
        int bottomY = uniformInclusive(RANDOM, origin.bottomY + minAdjacentLength - sideLengthY,
                origin.topY - minAdjacentLength + 1);
        int topY = bottomY + sideLengthY - 1;
        Rectangle newRectangle = new Rectangle(leftX, rightX, bottomY, topY);
        return newRectangle;
    }

    private Rectangle generateTopRectangle(Rectangle origin) {
        int bottomY = origin.topY + 1;
        int topY = uniformInclusive(RANDOM, bottomY + minSideLength - 1,
                bottomY + maxRectWidth - 1);

        int sideLengthX = uniformInclusive(RANDOM, minSideLength, maxRectWidth);
        int leftX = uniformInclusive(RANDOM, origin.leftX + minAdjacentLength - sideLengthX,
                origin.rightX - minAdjacentLength + 1);
        int rightX = leftX + sideLengthX - 1;
        Rectangle newRectangle = new Rectangle(leftX, rightX, bottomY, topY);
        return newRectangle;
    }


    private int getHorizontalChannelPosition(Rectangle a, Rectangle b) {
        int bottom = Math.max(a.bottomY, b.bottomY) + 1;
        int top = Math.min(a.topY, b.topY);
        return uniform(RANDOM, bottom, top);
    }

    private void addHorizontalChannel(TETile[][] world, int x, int y) {
        world[x][y] = TETile.colorVariant(Tileset.FLOOR, 64, 64, 64, RANDOM);
        world[x + 1][y] = TETile.colorVariant(Tileset.FLOOR, 64, 64, 64, RANDOM);
    }

    private int getVerticalChannelPosition(Rectangle a, Rectangle b) {
        int bottom = Math.max(a.leftX, b.leftX) + 1;
        int top = Math.min(a.rightX, b.rightX);
        return uniform(RANDOM, bottom, top);
    }

    private void addVerticalChannel(TETile[][] world, int x, int y) {
        world[x][y] = TETile.colorVariant(Tileset.FLOOR, 64, 64, 64, RANDOM);
        world[x][y + 1] = TETile.colorVariant(Tileset.FLOOR, 64, 64, 64, RANDOM);
    }

    private void generateAdjacentRectangle(TETile[][] world, Rectangle rectangle) {
        Rectangle a = generateLeftRectangle(rectangle);
        if (isValid(a)) {
            addRectangle(world, a);
            int y = getHorizontalChannelPosition(rectangle, a);
            addHorizontalChannel(world, a.rightX, y);
            generateAdjacentRectangle(world, a);
        }
        Rectangle c = generateBottomRectangle(rectangle);
        if (isValid(c)) {
            addRectangle(world, c);
            int x = getVerticalChannelPosition(rectangle, c);
            addVerticalChannel(world, x, c.topY);
            generateAdjacentRectangle(world, c);
        }
        Rectangle b = generateRightRectangle(rectangle);
        if (isValid(b)) {
            addRectangle(world, b);
            int y = getHorizontalChannelPosition(rectangle, b);
            addHorizontalChannel(world, rectangle.rightX, y);
            generateAdjacentRectangle(world, b);
        }
        Rectangle d = generateTopRectangle(rectangle);
        if (isValid(d)) {
            addRectangle(world, d);
            int x = getVerticalChannelPosition(rectangle, d);
            addVerticalChannel(world, x, rectangle.topY);
            generateAdjacentRectangle(world, d);
        }
    }

    private boolean isValid(Rectangle rectangle) {
        if (rectangle.bottomY < 0 || rectangle.topY >= HEIGHT
                || rectangle.rightX >= WIDTH || rectangle.leftX < 0) {
            return false;
        }
        if (rectangle.topY - rectangle.bottomY < 2 || rectangle.rightX - rectangle.leftX < 2) {
            return false;
        }
        for (Rectangle r : existedRectangle) {
            if (rectangle.isConflict(r)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate the map use parameter.
     *
     * @return The 2D TETile[][] representing the state of the world.
     */
    public TETile[][] generate() {
        TETile[][] world = new TETile[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        int sideLengthX = uniform(RANDOM, minSideLength, maxRectWidth + 1);
        int sideLengthY = uniform(RANDOM, minSideLength, maxRectHeight + 1);
        int leftX = uniform(RANDOM, 0, WIDTH - sideLengthX);
        int bottomY = uniform(RANDOM, 0, HEIGHT - sideLengthY);
        Rectangle rectangle = new Rectangle(leftX, leftX + sideLengthX - 1,
                bottomY, bottomY + sideLengthY - 1);
        addRectangle(world, rectangle);
        generateAdjacentRectangle(world, rectangle);
        return world;
    }
}
