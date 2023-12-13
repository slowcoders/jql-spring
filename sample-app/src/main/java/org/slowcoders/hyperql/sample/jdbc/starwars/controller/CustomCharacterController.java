package org.slowcoders.hyperql.sample.jdbc.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.hyperql.EntitySetController;
import org.slowcoders.hyperql.OutputOptions;
import org.slowcoders.hyperql.sample.jdbc.starwars.service.SecuredCharacterService;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/hql/starwars/character")
public class CustomCharacterController extends EntitySetController.CRUD<Long> implements EntitySetController.ListAll<Long> {

    private final SecuredCharacterService service;

    public CustomCharacterController(SecuredCharacterService service) {
        super(service.getCharacterEntitySet());
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
        service.deleteCharacter(idList, accessToken);
        return idList;
    }

    @Override
    @Operation(summary = "엔터티 추가 (default 값 설정 기능 추가)")
    @Transactional
    public <ENTITY> ENTITY add(
            @Schema(implementation = Object.class)
            @RequestBody Map<String, Object> properties) throws Exception {
        return (ENTITY) service.addNewCharacter(properties);
    }


}
