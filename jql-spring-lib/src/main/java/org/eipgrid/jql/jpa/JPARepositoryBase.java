package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.spring.JQLService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.eipgrid.jql.JqlSelect;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.*;

@NoRepositoryBean
public abstract class JPARepositoryBase<ENTITY, ID> extends JPAQueryBuilder<ENTITY, ID> implements JQLRepository<ENTITY, ID> {

    private final static HashMap<Class<?>, JPARepositoryBase<?,?>>jqlServices = new HashMap<>();
    private final JQLService service;
    private final HashMap<ID, Object> associatedCache = new HashMap<>();

    private final MappingJackson2HttpMessageConverter jsonConverter;

    public JPARepositoryBase(JQLService service) {
        super(service);
        this.service = service;
        jsonConverter = service.getJsonConverter();
    }

    @PostConstruct
    private void initService() {
//        this.jqlSchema =
                this.service.registerRepository(this);
        jqlServices.put(this.getEntityType(), this);
    }

//    public JqlSchema getSchema() {
//        return this.jqlSchema;
//    }

    public ObjectMapper getObjectMapper() {
        return jsonConverter.getObjectMapper();
    }

    @Override
    public ENTITY find(ID id) {
        return super.find(id);
    }

//    @Override
//    public Page<ENTITY> find(Map<String, Object> jqlFilter, @NotNull Pageable pageReq) {
//        return find(jqlFilter, pageReq, super.getEntityType());
//    }

    //@Override
    public final <T> Page<T> find(Map<String, Object> conditions, Pageable pageReq, Class<T> resultType) {
        Query query = super.buildSearchQuery(conditions, pageReq.getSort(), resultType);
        int size = pageReq.getPageSize();
        int offset = (int) (pageReq.getPageNumber()) * size;
        List<T> res = query.setMaxResults(size)
                .setFirstResult(offset).getResultList();
        long count = count(conditions);
        return new PageImpl(res, pageReq, count);
    }

    @Override
    public long count(Map<String, Object> jqlFilter) {
        long count = (Long) super.buildCountQuery(jqlFilter).getSingleResult();
        return count;
    }

    @Override
    public List<ENTITY> find(Map<String, Object> jqlFilter, JqlSelect columns) {
        return find(jqlFilter, columns, columns.getLimit(), super.getEntityType());
    }

    //@Override
    public <T> List<T> find(Map<String, Object> filterConditions, JqlSelect columns, int limit, Class<T> resultType) {
        Query query = super.buildSearchQuery(filterConditions, columns.getSort(), resultType);

        /** Hibernate 버그: limit 값이 생략되면, join 된 row 수만큼 entity 가 생성된다.
         *  이에 limit 값 항상 명시 필요함. */
        if (limit < 0) limit = Integer.MAX_VALUE;

        query = query.setMaxResults(limit);
        List<T> res = query.getResultList();
        if (resultType != super.getEntityType()) {
            for (int i = res.size(); --i >= 0; ) {
                T v = (T)super.convert(res.get(i), resultType);
                res.set(i, v);
            }
        }
        return res;
    }

    @Override
    public List<ENTITY> list(Collection<ID> idList) {
        Query query = super.buildSearchQuery(idList, null, null);
        List<ENTITY> res = query.getResultList();
        return res;
    }

    @Override
    public ENTITY findTop(Map<String, Object> jqlFilter, Sort sort) {
        Query query = super.buildSearchQuery(jqlFilter, sort, this.getEntityType());
        List<ENTITY> res = query.setMaxResults(1).getResultList();
        return res.size() > 0 ? res.get(0) : null;
    }

    @Override
    public ID insert(Map<String, Object> dataSet) throws IOException {
        ENTITY entity = super.convert(dataSet, getEntityType());
        return insert(entity);
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

    public List<ID> insertAll(Collection<Map<String, Object>> entities) {
        throw new RuntimeException("not implemented");
    }

    public int insertAll(List<ENTITY> entities) {
        throw new RuntimeException("not implemented");
    }

    // Insert Or Update Entity
    // @Override
    public ENTITY insertOrUpdate(ENTITY entity) {
        getEntityManager().persist(entity);
        return entity;
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
        EntityManager em = super.getEntityManager();
        em.remove(entity);
    }


    @Override
    public void delete(ID id) {
        EntityManager em = super.getEntityManager();
        ENTITY entity = em.find(getEntityType(), id);
        if (entity != null) deleteEntity(entity);
    }

    public void deleteEntities(Collection<ENTITY> entities) {
        for (ENTITY entity : entities) {
            deleteEntity(entity);
        }
    }


    @Override
    public int delete(Collection<ID> idList) {
        Query sql = super.buildDeleteQuery(idList);
        return sql.executeUpdate();
    }

    @Override
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

    public static class Util {
        public static <T> JPARepositoryBase<T, Object> findRepository(Class<T> entityType) {
            return (JPARepositoryBase<T, Object>) JPARepositoryBase.jqlServices.get(entityType);
        }
    }
}