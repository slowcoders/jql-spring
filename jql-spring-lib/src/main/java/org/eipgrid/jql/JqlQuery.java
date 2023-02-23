package org.eipgrid.jql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public abstract class JqlQuery {
    @Getter
    @JsonIgnore
    @Schema(implementation = String.class)
    private JqlSelect selection;

    @Getter
    @JsonIgnore
    @Schema(implementation = String.class)
    private Sort sort;

    @Getter @Setter
    private int offset;

    @Getter @Setter
    private int limit;

    protected void setSelection(JqlSelect select) {
        this.selection = select;
    }

    @JsonProperty()
    public final void setSelection(String jqlSelectStatement) {
        setSelection(JqlSelect.of(jqlSelectStatement));
    }

    public final void setSelection(String[] selectedPropertyNames) {
        setSelection(JqlSelect.of(selectedPropertyNames));
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public final void setSort(String orders[]) {
        setSort(JqlRestApi.buildSort(orders));
    }

    public abstract <T> List<T> getResultList(OutputFormat outputType);

    public final <T> List<T> getResultList() { return getResultList(OutputFormat.Object); }

    public abstract long count();

    public final <T> T getSingleResult() {
        List<T> res = getResultList();
        return res.size() > 0 ? res.get(0) : null;
    }

    public String getExecutedQuery() { return null; }
}
