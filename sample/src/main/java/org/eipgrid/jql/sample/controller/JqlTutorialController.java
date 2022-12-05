package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.spring.JQLReadOnlyController;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.eipgrid.jql.JqlSelect;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/jql-tutorial")
public class JqlTutorialController {

    private final JQLJdbcService service;

    public JqlTutorialController(JQLJdbcService service) {
        this.service = service;
    }

    JQLRepository<KVEntity, Object> getRepository(String dbSchema, String tableName) {
        String tablePath = service.makeTablePath(dbSchema, tableName);
        return service.makeRepository(tablePath);
    }

    @PostMapping(path = "/{schema}/{table}/pagination")
    @ResponseBody
    @Operation(summary = "Pagination 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "limit 과 page 값을 변경하여 Pagination 검색을 수행한다.")
    public Object pagination(@Parameter(example = "starwars") @PathVariable("schema") String schema,
                       @Parameter(example = "character") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }

    @PostMapping(path = "/{schema}/{table}/sort")
    @ResponseBody
    @Operation(summary = "Sort 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "Sort 값을 추가하여 정렬된 결과를 얻는다.")
    public Object sort(@Parameter(example = "starwars") @PathVariable("schema") String schema,
                       @Parameter(example = "character") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @Parameter(description = "'name' 과 '-name'을 각각 입력하여 정렬 결과를 확인한다.", schema = @Schema(implementation = String.class))
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }

    @PostMapping(path = "/{schema}/{table}/or-search")
    @ResponseBody
    @Operation(summary = "or query",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "or query<br>" +
                    "JQL 은 {} 내부의 항목은 And 연산으로, [] 내부의 항목은 or 연산으로 묶는다.<br>" +
                    "<br>" +
                    "아래는 pilot 의 ID 가 1001 보다 작거나, 1003보다 큰 비행선의 리스트를 확인하는 Jql 구문이다.<OL>" +
                    "<LI> { \"pilot@eq\": [ { \"id@lt\": 1001 }, { \"id@gt\": 1003 } ] }" +
                    "</OL>" +
                    "위 예시는 not in 연산자를 이용하여 and 연산으로 변경할 수 있다.<OL>" +
                    "<LI> { \"pilot@not in\": { \"id@between\": [1001, 1003] } }" +
                    "</OL>" +
                    "</OL>")
    public Object joinSearch(@Parameter(example = "starwars") @PathVariable("schema") String schema,
                       @Parameter(example = "starship") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "1005")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                             @Parameter(description = "{ \"pilot\": { \"id\": 1001 } } 을 입력하여 특정 조종사가 조종하는 비행선 리스트를 확인한다.")
                       @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }

    @PostMapping(path = "/{schema}/{table}/select-in")
    @ResponseBody
    @Operation(summary = "한 칼럼에 대한 in 검색",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "@eq 연산자와 @in 연산자<br>" +
                    "@eq 과 @in 의 검색 기능은 동일하다. 단, @in 검색 시엔 join 된 entity 가 검색 결과에 포함되지 않는다.<br>" +
                    "@eq 연산자는 생략할 수 있다. 즉 @eq 연산자는 default operator 이다.<br>" +
                    "JQL 샘플<br>" +
                    "아래는 ID 1001,1002 를 가진 두 character 에 속한 starship 리스트를 확인하는 Jql 구문이다.<OL>" +
                    "<LI> { \"pilot\": { \"id\": [1001,1002] } }<br>" +
                    "<LI> { \"pilot\": [ { \"id\": 1001 }, { \"id\": 1002 } ] }" +
                    "</OL>" +
                    "컬럼에 적용한 @eq 와 @in 연산의 검색 결과가 동일하며, 아래 3개는 동일한 결과를 출력한다.<OL>" +
                    "<LI> { \"pilot\": { \"id\": [1001,1002] } }" +
                    "<LI> { \"pilot\": { \"id@eq\": [1001,1002] } }" +
                    "<LI> { \"pilot\": { \"id@in\": [1001,1002] } }" +
                    "</OL>" +
                    "단, join 된 column 에 @in 연산자를 사용하면, @eq 과 동일한 조건으로 검색하되, join 된 entity 를 검색 결과에 포함시키지 않는다.<OL>" +
                    "<LI> { \"pilot@in\": { \"id\": [1001,1002] } }" +
                    "<LI> { \"pilot@in\": [ { \"id\": 1001 }, { \"id\": 1002 } ] }" +
                    "</OL>")
    public Object selectIn(@Parameter(example = "starwars") @PathVariable("schema") String schema,
                             @Parameter(example = "starship") @PathVariable("table") String table,
                             @RequestParam(value = "page", required = false) Integer page,
                             @Parameter(name = "limit", example = "1005")
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "sort", required = false) String[] _sort,
                             @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }


    private Object find(String schema, String table, Integer page, Integer _limit, String[] _columns, HashMap<String, Object> filter) {
        int limit = _limit == null ? 0 : _limit;
        boolean need_pagination = page != null && limit > 1;
        int offset = need_pagination ? page * limit : 0;
        JqlSelect select = JqlSelect.by(_columns, offset, limit);

        JQLRepository<KVEntity, Object> repository = getRepository(schema, table);
        List<KVEntity> res = repository.find(filter, select);

        if (need_pagination) {
            long count = repository.count(filter);
            PageRequest pageReq = PageRequest.of(page, limit, select.getOrders());
            return new PageImpl(res, pageReq, count);
        } else {
            return res;
        }
    }
}
