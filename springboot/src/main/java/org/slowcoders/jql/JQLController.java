package org.slowcoders.jql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class JQLController<ENTITY, ID> extends JQLReadOnlyController<ENTITY, ID> {

    protected JQLController(JQLRepository<ENTITY, ID> repository) {
        super(repository);
    }

    @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    @Transactional
    public ENTITY add_form(@ModelAttribute ENTITY entity) throws Exception {
        ID id = getRepository().insert(entity);
        return getRepository().find(id);
    }

    @PostMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    @Transactional
    public ENTITY add(@RequestBody ENTITY entity) throws Exception {
        ID id = getRepository().insert(entity);
        return getRepository().find(id);
    }

    @PatchMapping(path = "/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 내용 일부 변경")
    @Transactional
    public List<ENTITY> update(
            @Schema(type="string", required = true) @PathVariable("idList") Collection<ID> idList,
            @RequestBody HashMap<String, Object> updateSet) throws Exception {
        getRepository().update(idList, updateSet);
        List<ENTITY> entities = getRepository().list(idList);
        return entities;
    }

    @DeleteMapping("/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    @Transactional
    public Collection<ID> delete(@PathVariable("idList") Collection<ID> idList) {
        getRepository().delete(idList);
        return idList;
    }

}
