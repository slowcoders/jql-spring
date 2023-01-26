package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.JqlEntity;
import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.JqlQuery;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.*;

@NoRepositoryBean
public abstract class JPARepositoryBase<ENTITY, ID> extends JDBCRepositoryBase<ENTITY, ID> { // extends JPAQueryBuilder<ENTITY, ID> implements JqlRepository<ID> {

    private final static HashMap<Class<?>, JPARepositoryBase<?,?>>jqlServices = new HashMap<>();
//    private final JqlService service;
    private final HashMap<ID, Object> associatedCache = new HashMap<>();
//
//    private final MappingJackson2HttpMessageConverter jsonConverter;
//    private final ConversionService conversionService;
    private final Class<ENTITY> entityType;
//    private final Class<ID> idType;
//    private JqlRepository<ENTITY, ID> storage;
//    private QSchema jqlSchema;
//    private boolean hasGeneratedId;


    public JPARepositoryBase(JqlService service, Class<ENTITY> entityType, Class<ID> idType) {
        super(service, entityType);
//        this.service = service;
        this.entityType = entityType;
//        this.idType = idType;
//        this.jqlSchema = service.loadSchema(entityType);
//        this.storage = service.getRepository(service.loadSchema(entityType).getTableName());
//        this.conversionService = service.getConversionService();
//    jsonConverter = service.getJsonConverter();
//        this.service.registerRepository(this);
        jqlServices.put(this.getEntityType(), this);
    }

    @Override
    public List<ENTITY> find(JqlQuery query) {
        List<ENTITY> res = super.find(query, this.entityType);
        // TODO entityManager
        return res;
    }

    public ID insert(Map<String, Object> dataSet) {
        ID id = super.insert(dataSet);
        // TODO entityManager
        return id;
    }

    public List<ID> insert(Collection<Map<String, Object>> entities) {
        List<ID> res = super.insert(entities);
        // TODO entityManager
        return res;
    }


    public ID insert(ENTITY entity) {
        if (hasGeneratedId()) {
            ID id = getEntityId(entity);
            if (id != null) {
                throw new IllegalArgumentException("Entity can not be created with id");
            }
        }
        ENTITY newEntity = insertOrUpdate(entity);
        return getEntityId(entity);
    }

    public abstract ID getEntityId(ENTITY entity);

    // Insert Or Update Entity
    // @Override
    public ENTITY insertOrUpdate(ENTITY entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    private EntityManager getEntityManager() {
        return getService().getEntityManager();
    }

    public ENTITY update(ENTITY entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void update(ID id, Map<String, Object> updateSet) throws IOException {
        ENTITY entity = find(id);
        if (entity == null) {
            throw new IllegalArgumentException("Entity is not found with ID: " + id);
        }
        getObjectMapper().updateValue(entity, updateSet);
        update(entity);
    }


    @Override
    public void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException {
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
        ENTITY entity = em.find(entityType, id);
        if (entity != null) deleteEntity(entity);
    }

    public void deleteEntities(Collection<ENTITY> entities) {
        for (ENTITY entity : entities) {
            deleteEntity(entity);
        }
    }


    @Override
    public int delete(Collection<ID> idList) {
        return super.delete(idList);
    }

    public void clearEntityCache(ID id) {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        if (id == null) {
            cache.evict(getEntityType());
        }
        else {
            cache.evict(getEntityType(), id);
        }
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

//    @Override
//    public ID convertId(Object _id) {
//        return conversionService.convert(_id, this.idType);
//    }


    public static class Util {
        public static <T> JPARepositoryBase<T, Object> findRepository(Class<T> entityType) {
            return (JPARepositoryBase<T, Object>) JPARepositoryBase.jqlServices.get(entityType);
        }
    }
}