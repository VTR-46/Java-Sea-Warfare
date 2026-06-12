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

    public void addPosition(int row, int cow) {
        if (positions.size() < size) {
            positions.add(new Coordinate(row, cow));
        }
    }

    public boolean registerHit(int row, int cow) {
        for (Coordinate c : positions) {
            if (c.getLine() == row && c.getColumn() == cow && !c.isHit()) {
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
