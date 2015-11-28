package pl.edu.agh.muvto.model;

public class MuvtoEdge extends EntityWithId {

    private int capacity;
    private int fill;
    private double attractiveness;

    public MuvtoEdge(int id, int capacity, int fill, double attractiveness) {
        super(id);
        this.capacity = capacity;
        this.fill = fill;
        this.attractiveness = attractiveness;
    }

    @Override
    public String toString() {
        return String.format("Edge(%d)", getId());
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFill() {
        return fill;
    }

    public double getWeight() {
        return getFill()/(double)getCapacity();
    }
    
    public double getAttractiveness() {
        return attractiveness;
    }
}
