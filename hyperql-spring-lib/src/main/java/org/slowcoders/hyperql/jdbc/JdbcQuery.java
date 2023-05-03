package org.slowcoders.hyperql.jdbc;

import org.slowcoders.hyperql.HyperQuery;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.OutputFormat;
import org.slowcoders.hyperql.parser.HyperFilter;
import org.slowcoders.hyperql.schema.QResultMapping;
import org.springframework.data.domain.Sort;

import java.util.List;

public class JdbcQuery<ENTITY> extends HyperQuery<ENTITY> {

    //protected static int SingleEntityOffset = JqlQuery.SingleEntityOffset;
    private final JdbcRepositoryBase<?> table;
    private final HyperFilter filter;
    /*package*/ String executedQuery;
    /*package*/ Object extraInfo;

    public JdbcQuery(JdbcRepositoryBase<?> table, HyperSelect select, HyperFilter filter) {
        assert (filter != null);
        this.table = table;
        this.filter = filter;
        super.select(select);
    }

    public final Class<ENTITY> getJpaEntityType() {
        return filter.getJpqlEntityType();
    }

//    @Override
//    public JqlSelect getSelection() {
//        JqlSelect select = super.getSelection();
//        if (select == null || select == JqlSelect.Auto) {
//            HashMap<String, Object> resultMap = filter.getResultMappingMap();
//            super.select(JqlSelect.of(resultMap));
//        }
//        return select;
//    }

    @Override
    protected void select(HyperSelect select) {
        super.select(select);
        invalidateCache(false);
    }

    @Override
    public JdbcQuery<ENTITY> sort(Sort sort) {
        super.sort(sort);
        invalidateCache(false);
        return this;
    }

    private void invalidateCache(boolean all) {
        executedQuery = null;
        if (all) extraInfo = null;
    }

    @Override
    public List<ENTITY> getResultList(OutputFormat outputType) {
        return table.find(this, outputType);
    }

    @Override
    public long count() {
        return table.count(this);
    }

    public HyperFilter getFilter() {
        return this.filter;
    }

    public String getExecutedQuery() {
        return executedQuery;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    public String appendPaginationQuery(String sql) {
        String s = "";
        long limit = getLimit();
        if (limit > 0 || getOffset() > 0) {
            if (limit <= 0) limit = Long.MAX_VALUE;
            s += "\nLIMIT " + limit;
        }
        if (getOffset() > 0) {
            s += "\nOFFSET " + getOffset();
        }
        if (s.length() > 0) {
            sql += s;
        }
        return sql;
    }

    public List<QResultMapping> getResultMappings() {
        HyperSelect select = super.getSelection();
        if (select == null) {
            select = HyperSelect.Auto;
        }
        filter.setSelectedProperties(select.getPropertyMap());
        return filter.getResultMappings();
    }
}
