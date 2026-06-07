package VetorNaval;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayerBase {

    
    public static final int AGUA = Map.AGUA;
    public static final int NAVIO = Map.NAVIO;
    public static final int ACERTO = Map.ACERTO;
    public static final int ERRO = Map.ERRO;
    public static final int INVALIDO = Map.INVALIDO;
    public static final int REPETIDO = Map.REPETIDO;

    private String name;

    
    private Map ownBoard = new Map();
    private Map opponentBoard = new Map();

    private List<Ship> ships = new ArrayList<>();
    private int shipPartsRemaining = 0;

    public PlayerBase(String name) {
        this.name = name;
    }

    public abstract int[] getMove();

    public String getName() {
        return name;
    }

    public void addShip(Ship ship) {
        ships.add(ship);
        shipPartsRemaining += ship.getSize();
        placeShipOnOwnBoard(ship);
    }

    private void placeShipOnOwnBoard(Ship ship) {
        for (Coordinate coord : ship.getPositions()) {
            ownBoard.setCellState(coord.getLine(), coord.getColumn(), Map.NAVIO);
        }
    }

    public int receiveAttack(int row, int col) {
        if (!ownBoard.isValid(row, col)) {
            return Map.INVALIDO;
        }

        int currentStatus = ownBoard.getCellState(row, col);
        if (currentStatus == Map.ACERTO || currentStatus == Map.ERRO) {
            return Map.REPETIDO;
        }

        if (currentStatus == Map.NAVIO) {
            ownBoard.setCellState(row, col, Map.ACERTO);
            shipPartsRemaining--;
            for (Ship s : ships) {
                s.registrarAcerto(row, col);
            }
            return Map.ACERTO;
        } else {
            ownBoard.setCellState(row, col, Map.ERRO);
            return Map.ERRO;
        }
    }

    public void updateOpponentMap(int row, int col, int result) {
        opponentBoard.setCellState(row, col, result);
    }

    public boolean isDefeated() {
        return shipPartsRemaining <= 0;
    }

    public void resetBoardsAndShips() {
        ownBoard.reset();
        opponentBoard.reset();
        shipPartsRemaining = 0;
        for (Ship s : ships) {
            s.reset();
            shipPartsRemaining += s.getSize();
            placeShipOnOwnBoard(s);
        }
    }

    public int getShipPartsRemaining() {
        return shipPartsRemaining;
    }

    public int getOwnCellState(int row, int col) {
        return ownBoard.getCellState(row, col);
    }

    public int getOpponentCellState(int row, int col) {
        return opponentBoard.getCellState(row, col);
    }

    public List<Ship> getShips() {
        return ships;
    }

    // mtodos  para aos objetos Map para renderização 
    public void displayOwnBoard() {
        ownBoard.displayBoard(true);
    }

    public void displayOpponentBoard() {
        opponentBoard.displayBoard(false);
    }
}
