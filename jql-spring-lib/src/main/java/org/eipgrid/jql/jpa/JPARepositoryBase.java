package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.util.SourceWriter;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.*;

@NoRepositoryBean
public abstract class JPARepositoryBase<ENTITY, ID> extends JDBCRepositoryBase<ENTITY, ID> { // extends JPAQueryBuilder<ENTITY, ID> implements JqlRepository<ID> {

    private final static HashMap<Class<?>, JPARepositoryBase<?,?>>jqlServices = new HashMap<>();
    private final HashMap<ID, Object> associatedCache = new HashMap<>();

    public JPARepositoryBase(JqlService service, Class<ENTITY> entityType, Class<ID> idType) {
        super(service, service.loadSchema(entityType));
        jqlServices.put(this.getEntityType(), this);
    }

//    @Override
//    public List<ENTITY> find(JqlQuery query) {
//        List<Object[]> idList = super.listPrimaryKeys(query);
//        SourceWriter sw = new SourceWriter<>('\'');
//        List<QColumn> pkColumns = schema.getPKColumns();
//        boolean isMultiPk = pkColumns.size() > 1;
//
//        sw.write("select t FROM ").write(schema.getEntityClassName()).write(" t WHERE ");
//        if (isMultiPk) sw.write("(");
//        for (QColumn col : pkColumns) {
//            sw.write("t." + col.getJsonKey()).write(", ");
//        }
//        sw.replaceTrailingComma(isMultiPk ? ")" : "");
//        sw.write(" IN (");
//        for (Object[] id : idList) {
//            if (isMultiPk) sw.write("(");
//            for (Object k : id) {
//                sw.writeValue(k).write(", ");
//            }
//            sw.replaceTrailingComma(isMultiPk ? "), " : ", ");
//        }
//        sw.replaceTrailingComma(")");
//        EntityManager em = getEntityManager();
//        Query q = em.createQuery(sw.toString());
//        List<ENTITY> res = q.getResultList();
//
//        return res;
//    }

    public ID insert(Map<String, Object> dataSet) throws IOException {
        ObjectMapper converter = service.getObjectMapper();
        ENTITY entity = converter.convertValue(dataSet, getEntityType());
        ENTITY entity2 = this.insertOrUpdate(entity);
        return getEntityId(entity2);
    }

    public List<ID> insert(Collection<Map<String, Object>> entities) {
        List<ID> res = super.insert(entities);
//        List<ENTITY> res2 = this.find(res);
//        EntityManager em = getEntityManager();
//        em.setProperty();
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

    private ObjectMapper getObjectMapper() {
        return service.getObjectMapper();
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
    public int delete(Collection<ID> idList) {
        return super.delete(idList);
    }

    public void clearEntityCaches() {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(getEntityType());
    }

    public void clearEntityCache(ID id) {
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