package VetorNaval;

import java.util.ArrayList;
import java.util.List;

public class Ship {

    private String name;
    private int size;
    private int hits;
    private boolean shotDown;
    private List<Coordinate> positions;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.hits = 0;
        this.shotDown = false;
        this.positions = new ArrayList<>();
    }

    public void adicionarPosicao(int linha, int coluna) {
        if (positions.size() < size) {
            positions.add(new Coordinate(linha, coluna));
        }
    }

    public boolean registrarAcerto(int linha, int coluna) {
        for (Coordinate c : positions) {
            if (c.getLine() == linha && c.getColumn() == coluna && !c.isHit()) {
                c.setHit(true);
                hits++;
                if (hits >= size) {
                    shotDown = true;
                }
                return true;
            }
        }
        return false;
    }

    public boolean isShotDown() {
        return shotDown;
    }

    public void reset() {
        this.hits = 0;
        this.shotDown = false;
        for (Coordinate c : positions) {
            c.setHit(false);
        }
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public List<Coordinate> getPositions() {
        return positions;
    }


}
