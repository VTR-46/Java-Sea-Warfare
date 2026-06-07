package VetorNaval;

public class Coordinate {

    private int l;
    private int c;
    private boolean hit;

    public Coordinate(int li, int co) {
        this.l = li;
        this.c = co;
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

