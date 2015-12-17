package pl.edu.agh.muvto.model;

public class MuvtoEdge extends EntityWithId {

    private int capacity;
    private int fill;
    private double baseAttractiveness;

    public MuvtoEdge(int id,
                     int capacity,
                     int fill,
                     double baseAttractiveness) {
        super(id);
        this.capacity = capacity;
        this.fill = fill;
        this.baseAttractiveness = baseAttractiveness;
    }

    public MuvtoEdge withFill(int fill) {
        return new MuvtoEdge(getId(),
                             getCapacity(),
                             fill,
                             getBaseAttractiveness());
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

    public double getBaseAttractiveness() {
        return baseAttractiveness;
    }

    public double getEffectiveAttractiveness() {
        return (1 - 0.5 * getWeight()) * getBaseAttractiveness();
    }

    public double distanceTo(MuvtoEdge other) {
        return Math.abs(this.getFill() - other.getFill());
    }
}
