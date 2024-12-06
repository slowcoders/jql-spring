package org.slowcoders.hyperql.jdbc.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.EntitySet;
import org.slowcoders.hyperql.jdbc.JdbcQuery;
import org.slowcoders.hyperql.HyperQuery;
import org.slowcoders.hyperql.jdbc.SqlWriter;
import org.slowcoders.hyperql.parser.*;
import org.slowcoders.hyperql.schema.QJoin;
import org.slowcoders.hyperql.schema.QResultMapping;
import org.slowcoders.hyperql.js.JsType;
import org.slowcoders.hyperql.schema.*;
import org.slowcoders.hyperql.util.KVEntity;
import org.springframework.data.domain.Sort;

import java.util.*;

public abstract class SqlGenerator extends SqlConverter implements QueryGenerator {

    public static final boolean JSON_RS = true;
    private final boolean isNativeQuery;
    private EntityFilter currentNode;

    private List<QResultMapping> resultMappings;
    private boolean noAliases;

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final boolean USE_FLAT_KEY = false;
    private String sortCollation;
    private String[] viewParams;

    public SqlGenerator(boolean isNativeQuery) {
        super(new SqlWriter());
        this.isNativeQuery = isNativeQuery;
    }

    protected String getCommand(SqlConverter.Command command) {
        return command.toString();
    }

    protected void writeFilter(EntityFilter hql) {
        currentNode = hql;
        String accessFilter = hql.getSqlToCheckReadable();
        if (accessFilter != null) {
            sw.write("(").write(accessFilter).writeln(")");
            sw.write(" AND ");
        }
        if (hql.visitPredicates(this)) {
            sw.write(" AND ");
        }
        for (EntityFilter child : hql.getChildNodes()) {
            if (!child.isEmpty()) {
                var f = child.asTableFilter();
                if (f != null && f.isArrayNode()) continue;
                writeFilter(child);
            }
        }
    }

    private void writeQualifiedColumnName_internal(EntityFilter currentNode, QColumn column, Object value) {
        if (!currentNode.isJsonNode()) {
            String name = isNativeQuery ? column.getPhysicalName() : column.getJsonKey();
            if (!noAliases) {
                sw.write(currentNode.getMappingAlias()).write('.');
            }
            sw.write(name);
            if (column.isJsonNode() && value != null) {
                JsType valueType = JsType.of(value.getClass());
                writeTypeCast(valueType);
            }
        }
        else {
            JsType valueType = value == null ? null : JsType.of(value.getClass());
            if (!isNativeQuery) {
                writeQualifiedJsonPath_pjql(currentNode, column, valueType);
            } else {
                writeQualifiedJsonPath(currentNode, column, valueType);
            }
        }
    }

    private void writeQualifiedJsonPath_pjql(EntityFilter currentNode, QColumn column, JsType valueType) {
        sw.write("CAST(jsonb_extract_path_text(");
        writeJsonPath_pjql(currentNode);
        sw.writeQuoted(column.getJsonKey());
        sw.write(") as ");
        writeTypeText_pjql(valueType);
        sw.write(")");
    }

    private void writeTypeText_pjql(JsType vf) {
        switch (vf) {
            case Boolean:
                sw.write("Boolean");
                break;
            case Integer:
                sw.write("int");
                break;
            case Float:
                sw.write("Float");
                break;
            case Date:
                sw.write("Date");
                break;
            case Time:
                sw.write("time");
                break;
            case Timestamp:
                sw.write("timestamp");
                break;
            case Text:
                sw.write("text");
                break;
            case Object:
            case Array:
                sw.write("object");
                break;
        }
    }

    private void writeJsonPath_pjql(EntityFilter node) {
        if (node.isJsonNode() && node.getParentNode().asTableFilter() == null) {
            EntityFilter parent = node.getParentNode();
            writeJsonPath_pjql(parent);
        }
        if (node.getParentNode().asTableFilter() == null) {
            sw.writeQuoted(node.getMappingAlias());
        } else {
            sw.write(node.getMappingAlias());
        }
        sw.write(", ");
    }

    protected void writeQualifiedColumnName(QColumn column, Object value) {
        writeQualifiedColumnName_internal(currentNode, column, value);
    }

    protected void writeTypeCast(JsType vf) {
        // do nothing.. postgresql 전용으로 사용되고 있음....
    }


    protected abstract void writeQualifiedJsonPath(EntityFilter node, QColumn column, JsType valueType);

    protected void writeWhere(TableFilter where) {
        if (!where.isEmpty()) {
            sw.write("\nWHERE ");
            int len = sw.length();
            writeFilter(where);
            if (sw.length() == len) {
                // no conditions.
                sw.shrinkLength(7);
            }
            else if (sw.endsWith(" AND ")) {
                sw.shrinkLength(5);
            }
            sw.writeln();
        }
    }

    private void writeJoin2(TableFilter filter) {
        String parentAlias = filter.getMappingAlias();
        for (var subFilter : filter.getJoinedFilters()) {
            String alias = subFilter.getMappingAlias();
            QJoin join = subFilter.getEntityJoin();
            QJoin associated = join.getAssociativeJoin();
            if (subFilter.isArrayNode()) {
                writeArrayJoinStatement(subFilter, parentAlias, alias);
            } else {
                writeJoinStatement(join, parentAlias, associated == null ? alias : "p" + alias);
                if (associated != null) {
                    writeJoinStatement(associated, "p" + alias, alias);
                }
                writeJoin2(subFilter);
            }
        }
    }


    private void writeFrom(TableFilter filter, boolean ignoreEmptyFilter) {
        String tableName = isNativeQuery ? filter.getTableExpression(this.viewParams) : filter.getSchema().getEntityType().getName();
        sw.write("FROM ");
        sw.write(tableName);
        if (!noAliases) {
            sw.write(isNativeQuery ? " as " : " ").write(filter.getMappingAlias());
        }
        if (JSON_RS && isNativeQuery) {
            writeJoin2(filter);
            return;
        }
        for (QResultMapping mapping : this.resultMappings) {
            QJoin join = mapping.getEntityJoin();
            if (join == null) continue;

            if (ignoreEmptyFilter && mapping.isEmpty()) continue;

            String parentAlias = mapping.getParentNode().getMappingAlias();
            String alias = mapping.getMappingAlias();
            if (isNativeQuery) {
                QJoin associated = join.getAssociativeJoin();
                writeJoinStatement(join, parentAlias, associated == null ? alias : "p" + alias);
                if (associated != null) {
                    writeJoinStatement(associated, "p" + alias, alias);
                }
            }
            else {
                sw.write((mapping.getSelectedColumns().size() > 0 || mapping.hasChildMappings()) ? " join fetch " : " join ");
                sw.write(parentAlias).write('.').write(join.getJsonKey()).write(" ").write(alias).write("\n");
            }
        }

//        if (!isNativeQuery) {
//            sw.replaceTrailingComma("");
//        }
    }

    private void writeJoinCondition(QJoin join, String baseAlias, String alias) {
        boolean isInverseMapped = join.isInverseMapped();
        for (QColumn fk : join.getJoinConstraint()) {
            QColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sw.write(alias).write(".").write(linked.getPhysicalName());
            sw.write(" = ");
            sw.write(baseAlias).write(".").write(anchor.getPhysicalName());
            sw.write("\nand ");
        }
        sw.shrinkLength(4);
    }

    private void writeJoinStatement(QJoin join, String baseAlias, String alias) {
        String mediateTable = join.getLinkedSchema().getTableName();
        sw.write("\ninner join ").write(mediateTable).write(" as ").write(alias).write(" on\n\t");
        writeJoinCondition(join, baseAlias, alias);
    }

    private void writeArrayJoinStatement(TableFilter filter, String baseAlias, String alias) {
        QJoin join = filter.getEntityJoin();
        sw.write("\ninner join (");
        writeJsonArraySelect(filter, baseAlias, new ArrayList<>());
        sw.write(") as ").write(alias).write(" on\n\t");
        writeJoinCondition(join, baseAlias, alias);
    }

    public String createCountQuery(HyperFilter where, String[] viewParams) {
        this.viewParams = viewParams;
        this.resultMappings = where.getResultMappings();
        sw.write("\nSELECT count(*) ");
        this.writeFrom(where, false);
        writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    private boolean needDistinctPagination(HyperFilter where) {
        if (!where.hasArrayDescendantNode()) return false;

        for (QResultMapping mapping : this.resultMappings) {
            QJoin join = mapping.getEntityJoin();
            if (join == null) continue;

            if (mapping.getSelectedColumns().size() == 0) continue;

            if (mapping.isArrayNode()) {
                return true;
            }
        }
        return false;
    }

    public String createSelectQuery(JdbcQuery query) {
        if (JSON_RS && isNativeQuery) {
            return createJsonSelectQuery(query);
        }

        sw.reset();
        this.resultMappings = query.getResultMappings();
        this.viewParams = query.getViewParams();
        HyperFilter where = query.getFilter();

        String select_cmd = (isNativeQuery && !query.isDistinct()) ? "SELECT" : "SELECT DISTINCT";

        boolean need_complex_pagination = !JSON_RS && isNativeQuery && query.getLimit() > 0 && needDistinctPagination(where);
        if (need_complex_pagination) {
            sw.write("\nWITH _cte AS (\n"); // WITH _cte AS NOT MATERIALIZED
            sw.incTab();
            sw.write(select_cmd).write(" t_0.* ");
            writeFrom(where, true);
            writeWhere(where);
            writeOrderBy(query, false);
            writePagination(query);
            sw.decTab();
            sw.write("\n)");
        }

        sw.writeln("").writeln(select_cmd);
        sw.incTab();
        if (!isNativeQuery) {
            sw.write(where.getMappingAlias()).write(',');
        }
        else {
            for (QResultMapping mapping : this.resultMappings) {
                String alias = mapping.getMappingAlias();
                for (QColumn col : mapping.getSelectedColumns()) {
                    sw.write(alias).write('.').write(col.getPhysicalName()).write(", ");
                }
                sw.write('\n');
            }
        }
        sw.decTab();
        sw.replaceTrailingComma("\n");
        writeFrom(where, false);
        writeWhere(where);
        writeOrderBy(query, false);//where.hasArrayDescendantNode());
//        if (!need_complex_pagination && isNativeQuery) {
//            writePagination(query);
//        }
        String sql = sw.reset();
        return sql;
    }

    private void resolveOrderBy(TableFilter filter, Sort sort) {
        if (sort == null) return;

        List<Sort.Order> orders = sort.toList();
        for (Sort.Order order : orders) {
            String key = order.getProperty();
            EntityFilter node = filter.getFilterNode(key, HqlParser.NodeType.Leaf);
            this.currentNode = node;
            TableFilter table = node.asTableFilter();
            key = key.substring(key.lastIndexOf('.') + 1);
            QColumn column;
            if (table != null) {
                column = table.getSchema().getColumn(key);
            } else {
                column = new JsonColumn(node, key, String.class);
            }
            while (table == null || (!table.isArrayNode() && table.getEntityJoin() != null && node.getParentNode() != null)) {
                node = node.getParentNode();
                table = node.asTableFilter();
            }
            writeQualifiedColumnName(column, null);
            String qname = sw.reset();
            table.addOrderBy(order.isAscending() ? Sort.Order.asc(qname) : Sort.Order.desc(qname));
        }
    }

    private String createJsonSelectQuery(JdbcQuery<?> query) {
        sw.reset();
        HyperFilter where = query.getFilter();
        resolveOrderBy(where, query.getSort());
        this.resultMappings = query.getResultMappings();
        this.sortCollation = where.getSchema().getStorage().getSortCollation();
        this.viewParams = query.getViewParams();
        String select_cmd = (isNativeQuery && !query.isDistinct()) ? "SELECT" : "SELECT DISTINCT";

        sw.writeln().writeln(select_cmd);
        ArrayList<Object> columnNames = new ArrayList<>();
        var nameStack = new ArrayList<String>();
        getColumnMappings(where, columnNames, nameStack);
        sw.incTab();
        writeJsonSelectColumns(where);
        sw.decTab();
        sw.replaceTrailingComma("\n");
        where.setColumnNameMappings(columnNames);

        writeFrom(where, false);
        writeWhere(where);
        writeOrderBy_json(where);
//        if (!need_complex_pagination && isNativeQuery) {
//            writePagination(query);
//        }
        String sql = sw.reset();
        return sql;
    }

    private Object toNamePath(ArrayList<String> nameStack, String key) {
        if (nameStack.isEmpty()) return key;

//        nameStack.add(key);
        var res = nameStack.toArray(new String[nameStack.size()+1] );
        res[res.length-1] = key;
//        nameStack.remove(nameStack.size() - 1);
        return res;
    }

    private void retrieveNamePath(EntityFilter entityNode, ArrayList<String> names) {
        if (entityNode.isJsonNode()) {
            retrieveNamePath(entityNode.getParentNode(), names);
            names.add(entityNode.getMappingAlias());
        }
    }


    private void getColumnMappings(TableFilter filter, ArrayList<Object> columnNames, ArrayList<String> nameStack) {
        for (QColumn col : filter.getSelectedColumns()) {
            Object namePath;
            if (col instanceof JsonColumn jc) {
                ArrayList<String> names = new ArrayList<>();
                retrieveNamePath(jc.getEntityNode(), names);
                names.add(jc.getJsonKey());
                namePath = names.toArray(new String[names.size()]);
            } else {
                namePath = toNamePath(nameStack, col.getPhysicalName());
            }
            columnNames.add(namePath);
        }
        for (var subFilter : filter.getJoinedFilters()) {
            if (subFilter.isArrayNode()) {
                if (subFilter.hasAnySelectSubColumns()) {
                    var subNameStack = new ArrayList<String>();
                    var subColumnNames = new ArrayList<Object>();
                    getColumnMappings(subFilter, subColumnNames, subNameStack);
                    var columnNameMap = KVEntity.of("name", toNamePath(subNameStack, subFilter.getEntityJoin().getJsonKey()));
                    columnNameMap.put("columns", subColumnNames);
                    columnNames.add(columnNameMap);
                }
            } else {
                nameStack.add(subFilter.getEntityJoin().getJsonKey());
                getColumnMappings(subFilter, columnNames, nameStack);
                nameStack.remove(nameStack.size() - 1);
            }
        }

    }

    private void writeJsonSelectColumns(TableFilter filter) {
        for (QColumn col : filter.getSelectedColumns()) {
//            sw.write(tableAlias).write('.');
            if (col instanceof JsonColumn jc) {
                writeQualifiedColumnName_internal(jc.getEntityNode(), col, null);
            } else {
                writeQualifiedColumnName_internal(filter, col, null);
            }
            sw.writeln(",");
        }
        for (var subFilter : filter.getJoinedFilters()) {
            if (subFilter.isArrayNode()) {
                if (subFilter.hasAnySelectSubColumns()) {
                    String jsKey = subFilter.getEntityJoin().getJsonKey();
                    sw.write(subFilter.getMappingAlias()).write(".row$").writeln(",");
                }
            } else {
                writeJsonSelectColumns(subFilter);
            }
        }
    }

    private void writeJoinedColumnNames(QJoin join, String alias) {
        boolean isInverseMapped = join.isInverseMapped();
        for (QColumn fk : join.getJoinConstraint()) {
            QColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sw.write(alias).write(".").write(linked.getPhysicalName());
            sw.writeln(",");
        }
    }

    abstract protected String getJsonArrayAggregateFunction();
    abstract protected String getJsonBuildArrayFunction();

    private void writeJsonArraySelect(TableFilter mapping, String baseAlias, ArrayList<String> nameStack) {
        String alias = mapping.getMappingAlias();
        QJoin join = mapping.getEntityJoin();

        sw.write("select \n");
        sw.incTab();
        QJoin associated = join.getAssociativeJoin();
        if (associated != null) {
            writeJoinedColumnNames(join, "p" + alias);
        } else {
            writeJoinedColumnNames(join, alias);
        }
        if (mapping.hasAnySelectSubColumns()) {
            var json_agg = getJsonArrayAggregateFunction();
            var json_build_array = getJsonBuildArrayFunction();
            sw.writeln(json_agg + "(" + json_build_array + "(");
//            sw.writeln("json_agg(json_build_array(");
            sw.incTab();
            writeJsonSelectColumns(mapping);

            sw.decTab();
            sw.replaceTrailingComma(")) as row$\n");
        } else {
            sw.replaceTrailingComma("\n");
        }
        writeFrom(mapping, false);
        if (associated != null) {
            String mediateTable = associated.getBaseSchema().getTableName();
            sw.write("\ninner join ").write(mediateTable).write(" as p").write(alias).write(" on\n\t");
            writeJoinCondition(associated, "p" + alias, alias);
//            writeJoinStatement(associated, alias, "p" + alias);
        }
        writeWhere(mapping);
        sw.write("\ngroup by ");
        writeJoinedColumnNames(join, associated != null ? "p" + alias : alias);
        sw.replaceTrailingComma("\n");
        sw.decTab();
    }

    private void writeOrderBy_json(TableFilter filter) {
        List<Sort.Order> orders = filter.getOrders();
        if (orders.isEmpty()) return;

        sw.write("\nORDER BY ");
        orders.forEach(order -> {
            String qname = order.getProperty();
            sw.write(qname);
//            sw.write(this.sortCollation);
            sw.write(order.isAscending() ? " asc" : " desc").write(", ");
        });
        sw.replaceTrailingComma("\n");
    }

    private void writeOrderBy(JdbcQuery query, boolean need_joined_result_set_ordering) {
        HyperFilter where = query.getFilter();
        Sort sort = query.getSort();
        if (!need_joined_result_set_ordering) {
            if (sort == null || sort.isUnsorted()) return;
        }

        sw.write("\nORDER BY ");
        final HashSet<String> explicitSortColumns = new HashSet<>();
        if (sort != null) {
            QSchema schema = where.getSchema();
            String collation = where.getSchema().getStorage().getSortCollation();
            sort.forEach(order -> {
                String p = order.getProperty();
                String qname = where.getMappingAlias() + '.' + resolveColumnName(schema.getColumn(p));
                explicitSortColumns.add(qname);
                sw.write(qname);
                sw.write(collation);
                sw.write(order.isAscending() ? " asc" : " desc").write(", ");
            });
        }
        if (isNativeQuery && need_joined_result_set_ordering) {
            for (QResultMapping mapping : this.resultMappings) {
                if (!mapping.hasArrayDescendantNode()) continue;
                if (mapping != where && !mapping.isArrayNode()) continue;
                String table = mapping.getMappingAlias();
                for (QColumn column : mapping.getSchema().getPKColumns()) {
                    String qname = table + '.' + column.getPhysicalName();
                    if (!explicitSortColumns.contains(qname)) {
                        sw.write(table).write('.').write(column.getPhysicalName()).write(", ");
                    }
                }
            }
        }
        sw.replaceTrailingComma("");
    }

    private String resolveColumnName(QColumn column) {
        return isNativeQuery ? column.getPhysicalName() : column.getJsonKey();
    }
    private void writePagination(HyperQuery pagination) {
        int offset = pagination.getOffset();
        int limit  = pagination.getLimit();
        // 참고) MariaDB/Mysql 의 경우, Offset 은 Limit 의 하위 statement 이다.
        if (limit > 0) sw.write("\nLIMIT " + limit);
        if (offset > 0) sw.write("\nOFFSET " + offset);
    }

    protected void writeUpdateValueSet(QSchema schema, Map<String, Object> updateSet) {
        writeUpdateValueMap(schema, updateSet, "");
        sw.replaceTrailingComma("\n");
    }

    private void writeUpdateValueMap(QSchema schema, Map<String, Object> updateMap, String base_key) {
        for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
            String key = entry.getKey();
            Object rawValue = entry.getValue();
            if (!USE_FLAT_KEY && rawValue instanceof Map) {
                QColumn col = schema.findColumn(key);
                if (col == null || !col.isJsonNode()) {
                    writeUpdateValueMap(schema, (Map<String, Object>) rawValue, key + ".");
                    continue;
                }
                try {
                    rawValue = objectMapper.writeValueAsString(rawValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            QColumn col = schema.getColumn(base_key + key);
            Object value = BatchUpsert.convertJsonValueToColumnValue(col, rawValue);
            sw.write("  ");
            sw.write(col.getPhysicalName()).write(" = ").writeValue(value);
            sw.write(",\n");
        }
        sw.replaceTrailingComma("\n");
    }


    public String createUpdateQuery(HyperFilter where, Map<String, Object> updateSet) {
        sw.write("\nUPDATE ").write(where.getSchema().getTableName()).write(" ").write(where.getMappingAlias()).writeln(" SET");
        writeUpdateValueSet(where.getSchema(), updateSet);
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;

    }

    public String createDeleteQuery(HyperFilter where) {
        sw.write("\nDELETE ");
        this.resultMappings = Collections.emptyList();
        this.noAliases = true;
        this.writeFrom(where, false);
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    public String prepareFindByIdStatement(QSchema schema) {
        sw.write("\nSELECT * FROM ").write(schema.getTableName()).write("\nWHERE ");
        List<QColumn> keys = schema.getPKColumns();
        for (int i = 0; i < keys.size(); ) {
            String key = keys.get(i).getPhysicalName();
            sw.write(key).write(" = ? ");
            if (++ i < keys.size()) {
                sw.write(" AND ");
            }
        }
        String sql = sw.reset();
        return sql;
    }

    protected void writeInsertStatementInternal(QSchema schema, Map entity) {
        Set<String> keys = ((Map<String, ?>)entity).keySet();
        sw.writeln("(");
        sw.incTab();
        for (String name : keys) {
            sw.write(schema.getColumn(name).getPhysicalName());
            sw.write(", ");
        }
        sw.shrinkLength(2);
        sw.decTab();
        sw.writeln("\n) VALUES (");
        for (String k : keys) {
            QColumn col = schema.getColumn(k);
            Object v = BatchUpsert.convertJsonValueToColumnValue(col, entity.get(k));
            sw.writeValue(v).write(", ");
        }
        sw.replaceTrailingComma(")");
    }

    public abstract String createInsertStatement(JdbcSchema schema, Map<String, Object> entity, EntitySet.InsertPolicy insertPolicy);


    protected void writePreparedInsertStatementValueSet(List<JdbcColumn> columns) {
        sw.writeln("(");
        for (QColumn col : columns) {
            sw.write(col.getPhysicalName()).write(", ");
        }
        sw.replaceTrailingComma("\n) VALUES (");
        for (QColumn column : columns) {
            sw.write("?,");
        }
        sw.replaceTrailingComma(")");
    }

    public abstract String prepareBatchInsertStatement(JdbcSchema schema, List<JdbcColumn> columns, EntitySet.InsertPolicy insertPolicy);
}
