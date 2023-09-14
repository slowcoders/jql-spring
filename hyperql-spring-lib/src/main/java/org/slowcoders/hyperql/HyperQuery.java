package org.slowcoders.hyperql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.List;

public abstract class HyperQuery<ENTITY> {
    @Getter
    @JsonIgnore
    @Schema(implementation = String.class)
    private HyperSelect selection;

    @Getter
    @JsonIgnore
    @Schema(implementation = String.class)
    private Sort sort;

    @Getter
    private int offset;

    @Getter
    private int limit;

    @Getter
    private boolean distinct;

    protected void select(HyperSelect selection) {
        this.selection = selection;
    }

    @JsonProperty()
    public final HyperQuery<ENTITY> select(String hqlSelectStatement) {
        select(HyperSelect.of(hqlSelectStatement));
        return this;
    }

    public final HyperQuery<ENTITY> select(String[] selectedPropertyNames) {
        select(HyperSelect.of(selectedPropertyNames));
        return this;
    }

    public HyperQuery<ENTITY> sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public final HyperQuery<ENTITY> sort(String orders[]) {
        sort(RestTemplate.buildSort(orders));
        return this;
    }

    public HyperQuery<ENTITY> offset(int offset) {
        this.offset = offset;
        return this;
    }

    public HyperQuery<ENTITY> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public HyperQuery<ENTITY> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }


    public abstract List<ENTITY> getResultList(OutputFormat outputType);

    public final List<ENTITY> getResultList() { return getResultList(OutputFormat.Object); }

    public abstract long count();

    public final ENTITY getSingleResult() {
        List<ENTITY> res = getResultList();
        return res.size() > 0 ? res.get(0) : null;
    }

    public String getExecutedQuery() { return null; }

}
