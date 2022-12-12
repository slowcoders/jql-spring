package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.spring.JQLReadOnlyController;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface JdbcTableController<ID> extends JQLReadOnlyController<KVEntity, ID> {

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    default KVEntity add(@RequestBody Map<String, Object> entity) throws Exception {
        ID id = getRepository().insert(entity);
        KVEntity newEntity = getRepository().find(id);
        return newEntity;
    }

    @PatchMapping(path = "/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 변경")
    default List update(@PathVariable("idList") Collection<ID> idList,
                       @RequestBody HashMap<String, Object> updateSet) throws Exception {
        getRepository().update(idList, updateSet);
        List<KVEntity> entities = getRepository().list(idList);
        return entities;
    }

    @DeleteMapping("/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    default Collection<ID> delete(@PathVariable("idList") Collection<ID> idList) {
        getRepository().delete(idList);
        return idList;
    }
}
