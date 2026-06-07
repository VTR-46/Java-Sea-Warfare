package VetorNaval;

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

                int line = Integer.parseInt(lineStr);
                int column = Integer.parseInt(columnStr);

                if (line >= 0 && line <= 9 && column >= 0 && column <= 9) {
                    move[0] = line;
                    move[1] = column;
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