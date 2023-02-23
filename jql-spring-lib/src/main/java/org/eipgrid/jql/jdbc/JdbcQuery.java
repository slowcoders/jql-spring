package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.OutputFormat;
import org.eipgrid.jql.parser.JqlFilter;
import org.springframework.data.domain.Sort;

import java.util.List;

public class JdbcQuery extends JqlQuery {

    //protected static int SingleEntityOffset = JqlQuery.SingleEntityOffset;
    private final JdbcTable table;
    private final JqlFilter filter;

    /*package*/ String executedQuery;
    /*package*/ Object extraInfo;

    public JdbcQuery(JdbcTable table, JqlSelect select, JqlFilter jqlFilter) {
        this.table = table;
        this.filter = jqlFilter;
        super.setSelection(select);
    }

    @Override
    public JqlSelect getSelection() {
        JqlSelect select = super.getSelection();
        if (select == null) {
            select = JqlSelect.of("");
            super.setSelection(select);
        }
        return select;
    }

    @Override
    protected void setSelection(JqlSelect select) {
        super.setSelection(select);
        invalidateCache(false);
    }

    @Override
    public void setSort(Sort sort) {
        super.setSort(sort);
        invalidateCache(false);
    }

    private void invalidateCache(boolean all) {
        executedQuery = null;
        if (all) extraInfo = null;
    }

    @Override
    public List<?> getResultList(OutputFormat outputType) {
        return table.find(this, outputType);
    }

    @Override
    public long count() {
        return table.count(this);
    }

    public JqlFilter getFilter() {
        return this.filter;
    }

    public String getExecutedQuery() {
        return executedQuery;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }
}
