package VetorNaval;

public class Map {

    public static final int TAMANHO = 10;

    // estados de cada bloco do mapa
    public static final int AGUA = 0;
    public static final int NAVIO = 1;
    public static final int ACERTO = 2;
    public static final int ERRO = 3;
    public static final int INVALIDO = -1;
    public static final int REPETIDO = -2;

    private int[][] grade;

    public Map() {
        this.grade = new int[TAMANHO][TAMANHO];
        reset();
    }

    // Limpa o tabuleiro preenchendo com agua
    public void reset() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                grade[i][j] = AGUA;
            }
        }
    }

    //  se a coordenada está dentro dos limites do tabuleiro
    public boolean isValid(int row, int col) {
        return row >= 0 && row < TAMANHO && col >= 0 && col < TAMANHO;
    }

    // obtem o estado atual de uma coordenada
    public int getCellState(int row, int col) {
        if (!isValid(row, col)) {
            return INVALIDO;
        }
        return grade[row][col];
    }

    // modifica o estado de uma coordenada especifica
    public void setCellState(int row, int col, int state) {
        if (isValid(row, col)) {
            grade[row][col] = state;
        }
    }

    // imrpime o mapa no terminal
    public void displayBoard(boolean revealShips) {
        System.out.print("  ");
        for (int j = 0; j < TAMANHO; j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < TAMANHO; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < TAMANHO; j++) {
                int state = grade[i][j];
                switch (state) {
                    case ACERTO:
                        System.out.print("X "); // tiro certeiro num navio
                        break;
                    case ERRO:
                        System.out.print("* ");   // tiro na agua
                        break;
                    case NAVIO:
                        System.out.print(revealShips ? "N " : "~ "); // oculta ou revela
                        break;
                    default:
                        System.out.print("~ ");   // gua não revelada
                        break;
                }
            }
            System.out.println();
        }
    }
}
