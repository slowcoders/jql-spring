package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.JdbcRepositoryBase;
import org.eipgrid.jql.jdbc.JdbcStorage;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.Sort;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.*;

public abstract class JpaRepositoryBase<ENTITY, ID> extends JdbcRepositoryBase<ENTITY, ID> {

    private final HashMap<ID, Object> associatedCache = new HashMap<>();
    private final JqlTable<KVEntity, ID> rawRepository;

    protected JpaRepositoryBase(JdbcStorage storage, Class<ENTITY> entityType) {
        super(storage, storage.loadSchema(entityType));
        this.rawRepository = new RawRepository(this);
    }

    public ID insert(Map<String, Object> dataSet) {
        ENTITY entity = insertEntity(dataSet);
        return getEntityId(entity);
    }

    public ENTITY insertEntity(Map<String, Object> dataSet) {
        ObjectMapper converter = storage.getObjectMapper();
        ENTITY entity = converter.convertValue(dataSet, getEntityType());
        ENTITY newEntity = this.insertOrUpdate(entity);
        return newEntity;
    }

    public List<ID> insert(Collection<? extends Map<String, Object>> entities) {
        List<ID> res = super.insert(entities);
        return res;
    }

    public List<ENTITY> insertEntities(Collection<ENTITY> entities) {
        List<ENTITY> result = new ArrayList<>();

        for (ENTITY entity : entities) {
            result.add(insertEntity(entity));
        }

        return result;
    }


    public ENTITY insertEntity(ENTITY entity) {
        if (hasGeneratedId()) {
            ID id = getEntityId(entity);
            if (id != null) {
                throw new IllegalArgumentException("Entity can not be created with id");
            }
        }
        ENTITY newEntity = insertOrUpdate(entity);
        return newEntity;
    }

    public abstract ID getEntityId(ENTITY entity);

    // Insert Or Update Entity
    // @Override
    public ENTITY insertOrUpdate(ENTITY entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    private EntityManager getEntityManager() {
        return getStorage().getEntityManager();
    }

    public ENTITY update(ENTITY entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void update(ID id, Map<String, Object> updateSet) {
        ENTITY entity = find(id);
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
        return storage.getObjectMapper();
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

    public Object getAssociatedCached(ENTITY entity) {
        Object cached = associatedCache.get(getEntityId(entity));
        return cached;
    }

    public void putAssociatedCache(ENTITY entity, Object value) {
        associatedCache.put(getEntityId(entity), value);
    }

    public JqlTable<KVEntity, ID> getRawTable() {
        return this.rawRepository;
    }

    private static class RawRepository<ID> implements JqlTable<KVEntity, ID> {
        private final JpaRepositoryBase<KVEntity, ID> repository;

        public RawRepository(JpaRepositoryBase<KVEntity, ID> repository) {
            this.repository = repository;
        }

        @Override
        public JqlQuery<KVEntity> createQuery(Map<String, Object> filter) {
            return repository.createQuery(filter);
        }

        @Override
        public KVEntity find(ID id, JqlSelect select) {
            return repository.find(id, select, KVEntity.class);
        }

        @Override
        public List<KVEntity> find(Iterable<ID> idList, JqlSelect select) {
            return repository.find(idList, select, KVEntity.class);
        }

        @Override
        public List<KVEntity> findAll(JqlSelect select, Sort sort) {
            return repository.findAll(select, sort, KVEntity.class);
        }

        @Override
        public ID insert(Map<String, Object> properties) {
            return repository.insert(properties);
        }

        @Override
        public List<ID> insert(Collection<? extends Map<String, Object>> entities) {
            return repository.insert(entities);
        }

        @Override
        public KVEntity insertEntity(Map<String, Object> properties) {
            return repository.insertEntity(properties);
        }

        @Override
        public void update(Iterable<ID> idList, Map<String, Object> properties) {
            repository.update(idList, properties);
        }

        @Override
        public void update(ID id, Map<String, Object> updateSet) {
            JqlTable.super.update(id, updateSet);
        }

        @Override
        public void delete(Iterable<ID> idList) {
            repository.delete(idList);
        }

        @Override
        public void delete(ID id) {
            JqlTable.super.delete(id);
        }
    }

    private static class RawRepository2<ID> extends JqlRepository<KVEntity, ID> {

        private final JpaRepositoryBase<KVEntity, ID> repository;

        public RawRepository2(JpaRepositoryBase<KVEntity, ID> repository) {
            super(repository.schema, repository.getObjectMapper());
            this.repository = repository;
        }

        @Override
        public JqlStorage getStorage() {
            return this.repository.getStorage();
        }

        @Override
        public JqlTable<KVEntity, ID> getRawTable() {
            return this;
        }

        @Override
        public ID convertId(Object id) {
            return null;
        }

        @Override
        public <T> List<T> find(Iterable<ID> idList, JqlSelect select, Class<T> entityType) {
            return repository.find(idList, select, entityType);
        }

        @Override
        public <T> T find(ID id, JqlSelect select, Class<T> entityType) {
            return repository.find(id, select, entityType);
        }

        @Override
        public <T> List<T> find(JqlQuery query, Class<T> entityType) {
            return repository.find(query, entityType);
        }

        @Override
        public JqlQuery<KVEntity> createQuery(Map<String, Object> filter) {
            return repository.createQuery(filter);
        }

        @Override
        public ID insert(Map<String, Object> properties) {
            return repository.insert(properties);
        }

        @Override
        public List<ID> insert(Collection<? extends Map<String, Object>> entities) {
            return repository.insert(entities);
        }

        @Override
        public KVEntity insertEntity(Map<String, Object> properties) {
            return repository.insertEntity(properties);
        }

        @Override
        public void update(Iterable<ID> idList, Map<String, Object> updateSet) {

        }

        @Override
        public void delete(Iterable<ID> idList) {

        }
    }
}