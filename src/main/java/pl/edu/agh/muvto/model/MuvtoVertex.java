package pl.edu.agh.muvto.model;

public class MuvtoVertex extends EntityWithId {

    public MuvtoVertex(int id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("Vertex(%d)", getId());
    }
}
