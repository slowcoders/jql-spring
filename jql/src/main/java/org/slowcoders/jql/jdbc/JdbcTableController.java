package org.slowcoders.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import org.slowcoders.jql.JQLReadOnlyController;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JdbcEntityController<ID> extends JQLReadOnlyController<KVEntity, ID> {

    @Autowired
    ConversionService conversionService;

    protected JdbcEntityController(JDBCRepositoryBase<ID> repo) {
        super(repo);
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public KVEntity add(@RequestBody Map<String, Object> entity) throws Exception {
        ID id = getRepository().insert(entity);
        KVEntity newEntity = getRepository().find(id);
        return newEntity;
    }

    @PatchMapping(path = "/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 변경")
    public List update(@PathVariable("idList") Collection<ID> idList, @RequestBody HashMap<String, Object> updateSet) throws Exception {
        getRepository().update(idList, updateSet);
        List<KVEntity> entities = getRepository().list(idList);
        return entities;
    }

    @DeleteMapping("/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    public Collection<ID> delete(@PathVariable("idList") Collection<ID> idList) {
        getRepository().delete(idList);
        return idList;
    }
}
