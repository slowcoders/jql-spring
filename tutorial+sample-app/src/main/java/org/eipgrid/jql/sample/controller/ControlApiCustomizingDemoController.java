package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.eipgrid.jql.jdbc.JdbcTableController;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;

@RestController
@RequestMapping("/api/jql/starwars/character")
@Profile("!jpa")
public class ControlApiCustomizingDemoController extends JdbcTableController<Object> {

    public ControlApiCustomizingDemoController(JQLJdbcService service) {
        super(service,"starwars.character");
    }

    /**
     * Custom
     * @param idList
     * @return
     */
    @DeleteMapping("/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제 (사용 금지됨. secure-delete API 로 대체)")
    @Transactional
    public Collection<Object> delete(@PathVariable("idList") Collection<Object> idList) {
        throw new RuntimeException("Delete is forbidden by custom controller. Use ControlApiCustomizingDemoController.secure-delete api");
    }

    @PostMapping("/secure-delete/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제 방지용 API 변경 (AccessToken 검사 기능 추가. AccessToken 값='1234')")
    @Transactional
    public Collection<Object> delete(@PathVariable("idList") Collection<Object> idList,
                                     @RequestParam String accessToken) {
        if ("1234".equals(accessToken)) {
            return super.delete(idList);
        } else {
            throw new RuntimeException("Not authorized");
        }
    }

    @PostMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가 API 변경. default 값 설정.")
    @Transactional
    public KVEntity add(@RequestBody KVEntity entity) throws Exception {
        if (entity.get("note") == null) {
            entity.put("note", createNote());
        }
        return super.add(entity);
    }

    private KVEntity createNote() {
        KVEntity entity = new KVEntity();
        entity.put("autoCreated", new Date());
        return entity;
    }

}
