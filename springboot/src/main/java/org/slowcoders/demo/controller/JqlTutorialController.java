package org.slowcoders.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.jql.JQLReadOnlyController;
import org.slowcoders.jql.JQLRepository;
import org.slowcoders.jql.jdbc.JQLJdbcService;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

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
    public Object pagination(@Parameter(example = "public") @PathVariable("schema") String schema,
                       @Parameter(example = "owners") @PathVariable("table") String table,
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
    public Object sort(@Parameter(example = "public") @PathVariable("schema") String schema,
                       @Parameter(example = "owners") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @Parameter(description = "'firstName, -lastName'을 입력하여 정렬 결과를 확인한다.", schema = @Schema(implementation = String.class))
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }

    @PostMapping(path = "/{schema}/{table}/join-search")
    @ResponseBody
    @Operation(summary = "or query",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "or query<br>" +
                    "JQL 은 {} 내부의 항목은 And 연산으로, [] 내부의 항목은 or 연산으로 묶는다.<br>" +
                    "<br>" +
                    "아래는 owner 의 ID 3 보다 작거나, 5보다 큰 사용자에 속한 애완동물 리스트를 확인하는 Jql 구문이다.<OL>" +
                    "<LI> { \"owners@eq\": [ { \"id@lt\": 3 }, { \"id@gt\": 5 } ]" +
                    "</OL>" +
                    "위 예시는 except 연산자를 이용하여 and 연산으로 변경할 수 있다.<OL>" +
                    "<LI> { \"owners@except\": { \"id@between\": [3, 6] } " +
                    "</OL>" +
                    "아래는 cat 과 dog 의 항목을 리스트를 출력한다<OL>" +
                    "<LI> { \"types\": { \"name\": [\"cat\", \"dog\"] }" +
                    "<LI> { \"types\": [ { \"name\": \"cat\" },  { \"name\": \"dog\"} ] }" +
                    "<LI> { \"types@except\": [ { \"name@ne\": \"cat\" },  { \"name@ne\": \"dog\"} ] }" +
                    "<LI> { \"types\": { \"@except\" : [ { \"name@ne\": \"cat\" },  { \"name@ne\": \"dog\"} ] } }" +
                    "</OL>")
    public Object joinSearch(@Parameter(example = "public") @PathVariable("schema") String schema,
                       @Parameter(example = "pets") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                             @Parameter(description = "{ \"owners\": { \"id\": 3 } } 을 입력하여 특정 사용자에 속한 애완동물 리스트를 확인한다.")
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
                    "아래는 ID 3,4 를 가진 두 사용자에 속한 애완동물 리스트를 확인하는 Jql 구문이다.<OL>" +
                    "<LI> { \"owners@eq\": { \"id@eq\": [3,4] } }" +
                    "<LI> { \"owners@eq\": [ { \"id@eq\": 3 }, { \"id@eq\": 4 } ]" +
                    "</OL>" +
                    "@eq 를 생략하고 아래와 같이 좀 더 간략하게 사용할 수 있다.<OL>" +
                    "<LI> { \"owners\": { \"id\": [3,4] } }<br>" +
                    "<LI> { \"owners\": [ { \"id\": 3 }, { \"id\": 4 } ]" +
                    "</OL>" +
                    "컬럼에 적용한 @eq 와 @in 연산의 검색 결과가 동일하며, 아래 3개는 동일한 결과를 출력한다.<OL>" +
                    "<LI> { \"owners\": { \"id\": [3,4] } }" +
                    "<LI> { \"owners\": { \"id@eq\": [3,4] } }" +
                    "<LI> { \"owners\": { \"id@in\": [3,4] } }" +
                    "</OL>" +
                    "단, join 된 column 에 @in 연산자를 사용하면, @eq 과 동일한 조건으로 검색하되, join 된 entity 를 검색 결과에 포함시키지 않는다.<OL>" +
                    "<LI> { \"owners@in\": { \"id\": [3,4] } }" +
                    "<LI> { \"owners@in\": [ { \"id\": 3 }, { \"id\": 4 } ]" +
                    "</OL>")
    public Object selectIn(@Parameter(example = "public") @PathVariable("schema") String schema,
                             @Parameter(example = "pets") @PathVariable("table") String table,
                             @RequestParam(value = "page", required = false) Integer page,
                             @Parameter(name = "limit", example = "5")
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "sort", required = false) String[] _sort,
                             @RequestBody() HashMap<String, Object> filter) {
        return find(schema, table, page, limit, _sort, filter);
    }


    private Object find(String schema, String table, Integer page, Integer limit, String[] _sort, HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(schema, table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        page = page - 1;
        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }
}
