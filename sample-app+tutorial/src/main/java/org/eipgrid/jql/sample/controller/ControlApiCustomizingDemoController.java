package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.JqlController;
import org.eipgrid.jql.JqlEntity;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/jql/starwars/character")
public class ControlApiCustomizingDemoController extends JqlEntity.CRUDController<Object> {

    public ControlApiCustomizingDemoController(JdbcJqlService service) {
        super(service.getRepository("starwars.character"));
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

    @PutMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가 API 변경. default 값 설정.")
    @Transactional
    public JqlEntity add(@RequestBody Map<String, Object> entity) throws Exception {
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
