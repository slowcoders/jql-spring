package org.slowcoders.hyperql.jpa;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.HyperQuery;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.jdbc.JdbcRepositoryBase;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.slowcoders.hyperql.schema.QSchema;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.*;

public abstract class JpaTable<ENTITY, ID> extends HyperAdapter<ENTITY, ID> {

    private final HashMap<ID, Object> associatedCache = new HashMap<>();
    private final EntityManager entityManager;

    protected JpaTable(JdbcRepositoryBase<ID> repositoryBase, Class<ENTITY> entityType) {
        super(repositoryBase, entityType);
        this.entityManager = repositoryBase.getStorage().getEntityManager();
    }

    protected JpaTable(JdbcStorage storage, Class<ENTITY> entityType) {
        super(storage, entityType);
        this.entityManager = storage.getEntityManager();
    }

    public final QSchema getSchema() {
        return repository.getSchema();
    }

    @Override
    public HyperQuery createQuery(HyperSelect select, Map<String, Object> hqlFilter) {
        return (HyperQuery)repository.createQuery(select, hqlFilter);
    }

    public ID insert(Map<String, Object> dataSet, InsertPolicy insertPolicy) {
        ENTITY entity = super.convertToEntity(dataSet);
        ID id = getEntityId(entity);;
        return id;
    }

    public List<ID> insert(Collection<? extends Map<String, Object>> entities, InsertPolicy insertPolicy) {
        List<ID> res = super.insert(entities, insertPolicy);
        return res;
    }

    public final List<ENTITY> insertEntities(Collection<ENTITY> entities) {
        return this.insertEntities(entities, InsertPolicy.ErrorOnConflict);
    }

    public List<ENTITY> insertEntities(Collection<ENTITY> entities, InsertPolicy insertPolicy) {
        List<ENTITY> result = new ArrayList<>();

        for (ENTITY entity : entities) {
            result.add(insertEntity(entity, insertPolicy));
        }

        return result;
    }

    public final ENTITY insertEntity(ENTITY entity) {
        return this.insertEntity(entity, InsertPolicy.ErrorOnConflict);
    }

    public ENTITY insertEntity(ENTITY entity, InsertPolicy insertPolicy) {
        ENTITY newEntity = entity;

        if (repository.hasGeneratedId()) {
            ID id = getEntityId(entity);
            if (id != null) {
                if (insertPolicy != InsertPolicy.IgnoreOnConflict) {
                    return entity;
                }
                throw new IllegalArgumentException("Entity can not be created with generated id");
            }
        }

        newEntity = insertOrUpdate(entity);
        return newEntity;
    }

    public abstract ID getEntityId(ENTITY entity);

    // Insert Or Update Entity
    // @Override
    public ENTITY insertOrUpdate(ENTITY entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    public final EntityManager getEntityManager() {
        return entityManager;
    }

    public ENTITY update(ENTITY entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void update(ID id, Map<String, Object> updateSet) {
        ENTITY entity = find(id, null);
        if (entity == null) {
            throw new IllegalArgumentException("Entity is not found with ID: " + id);
        }
        try {
            getObjectMapper().updateValue(entity, updateSet);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        update(entity);
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public ID convertId(Object id) {
        return repository.convertId(id);
    }

    @Override
    public void update(Iterable<ID> idList, Map<String, Object> updateSet) {
        ArrayList<ENTITY> list = new ArrayList<>();
        for (ID id: idList) {
            update(id, updateSet);
        }
    }

    public void update(Collection<ENTITY> entities) throws IOException {
        for (ENTITY e: entities) {
            update(e);
        }
    }

    public void deleteEntity(ENTITY entity) {
        EntityManager em = getEntityManager();
        em.remove(entity);
    }


    public void delete(ID id) {
        EntityManager em = getEntityManager();
        ENTITY entity = em.find(getEntityType(), id);
        if (entity != null) {
            deleteEntity(entity);
        }
        else {
            super.delete(id);
        }
    }

    public void deleteEntities(Collection<ENTITY> entities) {
        for (ENTITY entity : entities) {
            deleteEntity(entity);
        }
    }


    @Override
    public void delete(Iterable<ID> idList) {
        super.delete(idList);
        for (ID id : idList) {
            removeEntityCache(id);
        }
    }

    public void clearEntityCaches() {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(getEntityType());
    }

    public void removeEntityCache(ID id) {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(getEntityType(), id);
        this.associatedCache.remove(id);
    }

    public boolean isCached(ID id) {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        return cache.contains(getEntityType(), id);
    }

    public Object getAssociatedCache(ENTITY entity) {
        Object cached = associatedCache.get(getEntityId(entity));
        return cached;
    }

    public void putAssociatedCache(ENTITY entity, Object value) {
        associatedCache.put(getEntityId(entity), value);
    }



}