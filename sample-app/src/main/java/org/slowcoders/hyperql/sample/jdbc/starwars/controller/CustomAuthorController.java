package org.slowcoders.hyperql.sample.jdbc.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.hyperql.EntitySetController;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.OutputOptions;
import org.slowcoders.hyperql.sample.jdbc.starwars.service.SecuredAuthorService;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/hql/starwars/author")
public class CustomAuthorController extends EntitySetController.CRUD<Long> implements EntitySetController.ListAll<Long> {

    private final SecuredAuthorService service;

    public CustomAuthorController(SecuredAuthorService service) {
        super(service.getAuthorEntitySet());
        this.service = service;
    }


    @Override
    public Response find(
            OutputOptions req,
            @Schema(implementation = Object.class)
            @RequestBody Map<String, Object> filter) throws Exception {
        Response resp = super.find(req, filter);
        resp.setProperty("lastExecutedSql", resp.getQuery().getExecutedQuery());
        return resp;
    }

    /**
     * Custom
     * @param idList
     * @return
     */
    @Override
    @Operation(summary = "엔터티 삭제 (사용 금지됨. secure-delete API 로 대체)")
    @Transactional
    public Collection<Long> delete(@PathVariable("idList") Collection<Long> idList) {
        throw new RuntimeException("Delete is forbidden by custom controller. Use ControlApiCustomizingDemoController.secure-delete api");
    }

    @PostMapping("/secure-delete/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제 (권한 검사 기능 추가. 테스트용 AccessToken 값='1234')")
    @Transactional
    public Collection<Long> delete(@PathVariable("idList") Collection<Long> idList,
                                     @RequestParam String accessToken) {
        service.deleteAuthor(idList, accessToken);
        return idList;
    }

    @Override
    @Operation(summary = "엔터티 추가 (default 값 설정 기능 추가)")
    @Transactional
    public Response add(
            @RequestParam(value = "select", required = false) String select$,
            @Schema(implementation = Object.class)
            @RequestBody Map<String, Object> properties) throws Exception {
        Long id = service.addNewAuthor(properties);
        if (select$ != null) {
            HyperSelect select = HyperSelect.of(select$);
            Object createdEntity = getEntitySet().find(id, select);
            return Response.of(createdEntity, select);
        } else {
            return Response.of("inserted", id);
        }
    }
}
