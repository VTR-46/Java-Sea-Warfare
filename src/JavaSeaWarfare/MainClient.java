package JavaSeaWarfare;

public class MainClient {
    public static void main(String[] args) {

        Player p1 = new Player("Jogador 1");
        Player p2 = new Player("Jogador 2");


        GameManager game = new GameManager(p1, p2);
        game.start();
        

        Player.closeScannerAtProgramEnd();
    }
}