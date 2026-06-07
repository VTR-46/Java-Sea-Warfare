package VetorNaval;

import java.util.Scanner;

public class GameManager {

    private Player player1;
    private Player player2;
    private Scanner scanner;

    public GameManager(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean playAgain = true;

        while (playAgain) {
            setupGame();
            playMatch();

            System.out.print("\nDesejam jogar novamente? (S/N): ");
            String response = scanner.nextLine();
            playAgain = response.trim().equalsIgnoreCase("S");

            if (playAgain) {
                System.out.println("\nReiniciando tabuleiros...\n");
                player1.resetBoardsAndShips();
                player2.resetBoardsAndShips();
            }
        }
        System.out.println("Obrigado por jogar Batalha Naval!");
    }

    // onde os navios sao posicionados
    private void setupGame() {
        System.out.println("--- FASE DE PREPARAÇÃO ---");

        System.out.println("\n>>> Almirante " + player1.getName() + ", posicione sua frota!");
        placeShipsForPlayer(player1);

        System.out.println("\n>>> Almirante " + player2.getName() + ", posicione sua frota!");
        placeShipsForPlayer(player2);
    }

    
    private void placeShipsForPlayer(Player player) {
         
        String[] shipNames = {"Porta-Aviões", "Contratorpedeiro", "Submarino"};
        int[] shipSizes = {5, 3, 2};

        for (int i = 0; i < shipNames.length; i++) {
            boolean placed = false;
            while (!placed) {
                System.out.println("\nMapa atual de " + player.getName() + ":");
                player.displayOwnBoard();

                System.out.println("\nPosicionando: " + shipNames[i] + " (Tamanho: " + shipSizes[i] + " espaços)");
                System.out.println("Onde ficará a ponta do navio?");

                // pegar a coordenada inicial
                int[] coord = player.getMove();
                int row = coord[0];
                int col = coord[1];

                System.out.print("Orientação (1 - Horizontal, 2 - Vertical): ");
                String orientStr = scanner.nextLine();
                Guidance guidance = orientStr.trim().equals("1") ? Guidance.HORIZONTAL : Guidance.VERTICAL;

                // validar e posicionar o navio
                if (isValidPlacement(player, row, col, shipSizes[i], guidance)) {
                    Ship newShip = new Ship(shipNames[i], shipSizes[i]);

                    // Preenche as coordenadas
                    for (int j = 0; j < shipSizes[i]; j++) {
                        if (guidance == Guidance.HORIZONTAL) {      //horinzonmtal
                            newShip.adicionarPosicao(row, col + j);
                        } else {                                    //vertical
                            newShip.adicionarPosicao(row + j, col);
                        }
                    }

                    player.addShip(newShip);
                    placed = true;
                    System.out.println("✅ " + shipNames[i] + " posicionado com sucesso!");
                } else {
                    System.out.println("❌ Posição inválida! O navio saiu do mapa ou colidiu com outro. Tente novamente.");
                }
            }
        }


        System.out.println("\nSua frota está pronta, " + player.getName() + ":");
        player.displayOwnBoard();
        System.out.println("\nPressione ENTER para passar a vez...");
        scanner.nextLine();

        // limpa" a tela para o adversario não ver os navios
        for (int k = 0; k < 50; k++) {
            System.out.println();
        }
    }

    // verifica se o navio cabe no mapa e nao sobrepoe outro
    private boolean isValidPlacement(Player player, int startRow, int startCol, int size, Guidance guidance) {
        for (int i = 0; i < size; i++) {
            int row = startRow + (guidance == Guidance.VERTICAL ? i : 0);
            int col = startCol + (guidance == Guidance.HORIZONTAL ? i : 0);

            // verifica se a parte do navio sai dos limites do mapa de 10x10
            if (row >= Map.TAMANHO || col >= Map.TAMANHO) {
                return false;
            }
            // verifica se ja existe um navio naquela coordenada
            if (player.getOwnCellState(row, col) == Map.NAVIO) {
                return false;
            }
        }
        return true;
    }

    // Loop principal
    private void playMatch() {
        boolean gameOver = false;
        Player currentPlayer = player1;
        Player opponent = player2;

        System.out.println("\n--- PARTIDA INICIADA ---");

        while (!gameOver) {
            System.out.println("\n====================================");
            System.out.println("Turno de: " + currentPlayer.getName());
            System.out.println("Seu radar (Mapa do Oponente):");
            currentPlayer.displayOpponentBoard();

            // 1. 
            int[] move = currentPlayer.getMove();

            // 2. 
            int result = opponent.receiveAttack(move[0], move[1]);

            // 3. 
            if (result == Map.INVALIDO || result == Map.REPETIDO) {
                System.out.println("Jogada inválida ou coordenada já atacada! Tente novamente.");
                continue; 
            }

            // 4. aatualiza o radar
            currentPlayer.updateOpponentMap(move[0], move[1], result);

            if (result == Map.ACERTO) {
                System.out.println("💥 FOGO! Você acertou um navio inimigo!");
            } else {
                System.out.println("💦 ÁGUA! Tiro no mar.");
            }

            // 6. Condiçao de vitoria
            if (opponent.isDefeated()) {
                System.out.println("\n🏆 *** " + currentPlayer.getName().toUpperCase() + " VENCEU A PARTIDA! *** 🏆");
                gameOver = true;
            } else {
                // troca os jogadores para o próximo turno
                Player temp = currentPlayer;
                currentPlayer = opponent;
                opponent = temp;
            }
        }
    }
}
