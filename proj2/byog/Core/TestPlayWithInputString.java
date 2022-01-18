package byog.Core;

import byog.TileEngine.TETile;

public class TestPlayWithInputString {
    public static void main(String[] args) {
        Game game = new Game();
        TETile[][] world = game.playWithInputString("N123SDDDWWWDDD");
        System.out.println(TETile.toString(world));
    }
}
