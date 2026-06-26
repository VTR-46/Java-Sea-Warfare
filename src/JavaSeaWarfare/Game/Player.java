package JavaSeaWarfare.Game;

import JavaSeaWarfare.Game.PlayerBase;
import java.util.Scanner;

public class Player extends PlayerBase {

    private static final Scanner scanner = new Scanner(System.in);

    public Player(String name) {
        super(name);
    }

    @Override
    public int[] getMove() {
        int[] move = new int[2];
        boolean valid = false;

        while (!valid) {
            try {
                System.out.print(getName() + ", digite a linha (0-9): ");
                String lineStr = scanner.nextLine();

                System.out.print(getName() + ", digite a coluna (0-9): ");
                String columnStr = scanner.nextLine();

                int row = Integer.parseInt(lineStr);
                int col = Integer.parseInt(columnStr);

                if (row >= 0 && row <= 9 && col >= 0 && col <= 9) {
                    move[0] = row;
                    move[1] = col;
                    valid = true;
                } else {
                    System.out.println("Valores fora do intervalo. Tente novamente.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números inteiros.");
            }
        }

        return move;
    }

    public static void closeScannerAtProgramEnd() {
        scanner.close();        //gislaine
    }
}