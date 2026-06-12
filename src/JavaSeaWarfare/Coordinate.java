package VetorNaval;

public class Coordinate {           //cordenada da matriz (mapa)

    private int l;
    private int c;
    private boolean hit;

    public Coordinate(int row, int col) {
        this.l = row;
        this.c = col;
        this.hit = false;
    }

    public int getLine() {
        return l;
    }

    public int getColumn() {
        return c;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean atingida) {
        this.hit = atingida;
    }
}

