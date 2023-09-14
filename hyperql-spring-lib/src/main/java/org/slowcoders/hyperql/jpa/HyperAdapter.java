package org.slowcoders.hyperql.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.HyperQuery;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.EntitySet;
import org.slowcoders.hyperql.jdbc.JdbcRepositoryBase;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HyperAdapter<ENTITY, ID> implements EntitySet<ENTITY, ID> {
    protected final JdbcRepositoryBase<ID> repository;
    protected final Class<ENTITY> entityType;
    protected final ObjectMapper objectMapper;

    public HyperAdapter(JdbcRepositoryBase<ID> repository, Class<ENTITY> entityType, ObjectMapper objectMapper) {
        this.repository = repository;
        this.entityType = entityType;
        this.objectMapper = objectMapper;
    }
    public HyperAdapter(JdbcRepositoryBase<ID> repository, Class<ENTITY> entityType) {
        this(repository, entityType, repository.getStorage().getObjectMapper());
    }

    public JdbcRepositoryBase<ID> getRepository() {
        return this.repository;
    }
    protected HyperAdapter(JdbcStorage storage, Class<ENTITY> entityType) {
        this.entityType = entityType;
        this.repository = storage.registerTable(this, entityType);
        this.objectMapper = storage.getObjectMapper();
    }

    public final Class<ENTITY> getEntityType() {
        return this.entityType;
    }

    @Override
    public HyperQuery createQuery(Map<String, Object> hqlFilter) {
        return (HyperQuery)repository.createQuery(hqlFilter);
    }

    @Override
    public ENTITY find(ID id, HyperSelect select) {
        Map raw_entity = repository.find(id, select);
        ENTITY entity = convertToEntity(raw_entity);
        return entity;
    }

    public ENTITY convertToEntity(Map rawEntity) {
        if (rawEntity == null) return null;
        ENTITY entity = objectMapper.convertValue(rawEntity, entityType);
        return entity;
    }

    private List<ENTITY> replaceContentToEntities(List res) {
        if (res.size() == 0 || res.get(0).getClass().isAssignableFrom(entityType)) {
            return res;
        }
        for (int i = res.size(); --i >= 0; ) {
            ENTITY v = convertToEntity((Map)res.get(i));
            res.set(i, v);
        }
        return res;
    }

    @Override
    public List<ENTITY> find(Iterable<ID> idList, HyperSelect select) {
        List res = repository.find(idList, select);
        return replaceContentToEntities(res);
    }

    @Override
    public List<ENTITY> findAll(HyperSelect select, Sort sort) {
        List res = repository.findAll(select, sort);
        return replaceContentToEntities(res);
    }


    @Override
    public List<ID> insert(Collection<? extends Map<String, Object>> entities, InsertPolicy insertPolicy) {
        return repository.insert(entities, insertPolicy);
    }

    @Override
    public ENTITY insert(Map<String, Object> properties, InsertPolicy insertPolicy) {
        Map res =  repository.insert(properties, insertPolicy);
        return convertToEntity(res);
    }

    @Override
    public void update(Iterable<ID> idList, Map<String, Object> properties) {
        repository.update(idList, properties);
    }

    @Override
    public void update(Map<String, Object> filter, Map<String, Object> properties) {
        repository.update(filter, properties);
    }

    @Override
    public void update(ID id, Map<String, Object> updateSet) {
        EntitySet.super.update(id, updateSet);
    }

    @Override
    public void delete(Iterable<ID> idList) {
        repository.delete(idList);
    }

    @Override
    public void delete(ID id) {
        EntitySet.super.delete(id);
    }
}