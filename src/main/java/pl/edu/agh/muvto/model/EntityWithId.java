package pl.edu.agh.muvto.model;

public class EntityWithId {

    protected int id;
    
    protected EntityWithId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof EntityWithId)) {
            return false;
        }

        EntityWithId other = (EntityWithId) obj;
        return id == other.id;
    }

}
