package org.slowcoders.hyperql;

import org.slowcoders.hyperql.schema.QSchema;

import java.util.*;

public abstract class HyperRepository<ID> implements EntitySet<Map, ID> {

    protected final QSchema schema;
    private Observer<ID> observer;

    public static final Class<Map> rawEntityType = Map.class;

    protected HyperRepository(QSchema schema) {
        this.schema = schema;
    }


    public abstract HyperStorage getStorage();

    public final String getTableName() {
        return schema.getTableName();
    }

    public final QSchema getSchema() { return schema; }

    public abstract ID convertId(Object id);

    public final boolean hasGeneratedId() {
        return schema.hasGeneratedId();
    }

    public abstract List<ID> insert(Collection<? extends Map<String, Object>> entities, InsertPolicy insertPolicy);

    public abstract void update(Iterable<ID> idList, Map<String, Object> updateSet);

    public abstract int update(Map<String, Object> filter, Map<String, Object> updateSet);

    public abstract void delete(Iterable<ID> idList);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperRepository that = (HyperRepository) o;
        return Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

    public final Observer<ID> getObserver() {
        return observer;
    }

    public void setObserver(Observer<ID> observer) {
        if (this.getSchema().getPKColumns().size() != 1) {
            throw new RuntimeException("Observable repository must have single primary key.");
        }
        this.observer = observer;
    }

    public interface Observer<ID> {
        void onInserted(Iterable<ID> idList);
        void onUpdated(Iterable<ID> idList);
        void onDeleted(Iterable<ID> idList);
    }
}
