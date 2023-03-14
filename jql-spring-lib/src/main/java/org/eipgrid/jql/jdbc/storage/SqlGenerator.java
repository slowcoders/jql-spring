package org.eipgrid.jql.jdbc.storage;

import org.eipgrid.jql.jdbc.JdbcQuery;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.js.JsType;
import org.eipgrid.jql.schema.*;
import org.eipgrid.jql.parser.Expression;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.parser.EntityFilter;
import org.eipgrid.jql.util.SourceWriter;
import org.springframework.data.domain.Sort;

import java.util.*;

public class SqlGenerator extends SqlConverter implements QueryGenerator {

    private final boolean isNativeQuery;
    private EntityFilter currentNode;
    private List<? extends QResultMapping> resultMappings;

    int cntMappingAlias = 0;

    public SqlGenerator(boolean isNativeQuery) {
        super(new SourceWriter('\''));
        this.isNativeQuery = isNativeQuery;
    }

    protected String getCommand(SqlConverter.Command command) {
        return command.toString();
    }

    protected void writeFilter(EntityFilter jql) {
        currentNode = jql;
        Expression ps = jql.getPredicates();
        if (!ps.isEmpty()) {
            ps.accept(this);
            sw.write(" AND ");
        }
        for (EntityFilter child : jql.getChildNodes()) {
            if (!child.isEmpty()) {
                writeFilter(child);
            }
        }
    }

    HashMap<String, String> mappingAliases = new HashMap<>();
    private String getMappingAlias(EntityFilter filter) {
        return mappingAliases.get(filter.getEntityMappingPath().toString());
    }

    private String getMappingAlias(QResultMapping mapping) {
        return mappingAliases.get(mapping.getEntityMappingPath().toString());
    }

    protected void writeQualifiedColumnName(QColumn column, Object value) {
        if (!currentNode.isJsonNode()) {
            String name = isNativeQuery ? column.getPhysicalName() : column.getJsonKey();
            sw.write(getMappingAlias(currentNode)).write('.').write(name);
        }
        else {
            sw.write('(');
            writeJsonPath(currentNode);
            JsType valueType = value == null ? null : JsType.of(value.getClass());
            if (valueType == JsType.Text) {
                sw.write('>');
                valueType = null;
            }
            sw.writeQuoted(column.getJsonKey());
            sw.write(')');
            if (valueType != null) {
                writeTypeCast(valueType);
            }
        }
    }

    private void writeJsonPath(EntityFilter node) {
        if (node.isJsonNode()) {
            EntityFilter parent = node.getParentNode();
            writeJsonPath(parent);
            if (parent.isJsonNode()) {
                sw.writeQuoted(getMappingAlias(node));
            } else {
                sw.write(getMappingAlias(node));
            }
            sw.write("->");
        } else {
            sw.write(getMappingAlias(node)).write('.');
        }
    }

    private void writeTypeCast(JsType vf) {
        switch (vf) {
            case Boolean:
                sw.write("::BOOLEAN");
            case Integer:
            case Float:
                sw.write("::NUMERIC");
                break;
            case Date:
                sw.write("::DATE");
                break;
            case Time:
                sw.write("::TIME");
                break;
            case Timestamp:
                sw.write("::TIMESTAMP");
                break;
            case Text:
                sw.write("::TEXT");
                break;
            case Object:
            case Array:
                sw.write("::JSONB");
                break;
        }
    }

    protected void writeWhere(JqlFilter where) {
        if (!where.isEmpty()) {
            sw.write("\nWHERE ");
            writeFilter(where);
            if (sw.endsWith(" AND ")) {
                sw.shrinkLength(5);
            }
        }
    }

    private void writeFrom(JqlFilter where) {
        writeFrom(where, where.getTableName(), false);
    }

    private void writeFrom(JqlFilter where, String tableName, boolean ignoreEmptyFilter) {
        sw.write("FROM ").write(tableName).write(isNativeQuery ? " as " : " ").write(getMappingAlias(where));
        for (QResultMapping fetch : this.resultMappings) {
            QJoin join = fetch.getEntityJoin();
            if (join == null) continue;

            if (ignoreEmptyFilter && fetch.isEmpty()) continue;

            String parentAlias = getMappingAlias(fetch.getParentNode());
            String alias = getMappingAlias(fetch);
            if (isNativeQuery) {
                QJoin associated = join.getAssociativeJoin();
                writeJoinStatement(join, parentAlias, associated == null ? alias : "p" + alias);
                if (associated != null) {
                    writeJoinStatement(associated, "p" + alias, alias);
                }
            }
            else {
                sw.write((fetch.getSelectedColumns().size() > 0 || fetch.hasChildMappings()) ? " join fetch " : " join ");
                sw.write(parentAlias).write('.').write(join.getJsonKey()).write(" ").write(alias).write("\n");
            }
        }

//        if (!isNativeQuery) {
//            sw.replaceTrailingComma("");
//        }
    }


    private void writeJoinStatement(QJoin join, String baseAlias, String alias) {
        boolean isInverseMapped = join.isInverseMapped();
        String mediateTable = join.getLinkedSchema().getTableName();
        sw.write("\nleft join ").write(mediateTable).write(" as ").write(alias).write(" on\n\t");
        for (QColumn fk : join.getJoinConstraint()) {
            QColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sw.write(baseAlias).write(".").write(anchor.getPhysicalName());
            sw.write(" = ").write(alias).write(".").write(linked.getPhysicalName()).write(" and\n\t");
        }
        sw.shrinkLength(6);
    }

    private void setResultMappings(List<QResultMapping> resultMappings) {
        this.resultMappings = resultMappings;

        for (QResultMapping mapping : resultMappings) {
            mappingAliases.put(mapping.getEntityMappingPath().toString(), "t_" + cntMappingAlias);
            cntMappingAlias ++;
        }
    }
    public String createCountQuery(JqlFilter where) {
        sw.write("\nSELECT count(*) ");
        this.resultMappings = where.getResultMappings();
        writeFrom(where);
        writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    private boolean needDistinctPagination(JqlFilter where) {
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
        sw.reset();
        JqlFilter where = query.getFilter();
        this.resultMappings = query.getResultMappings();
        //where.setSelectedProperties(query.getSelection().getPropertyNames());

        String tableName = isNativeQuery ? where.getTableName() : where.getSchema().getEntityType().getName();
        boolean need_complex_pagination = isNativeQuery && query.getLimit() > 0 && needDistinctPagination(where);
        if (need_complex_pagination) {
            sw.write("\nWITH _cte AS (\n"); // WITH _cte AS NOT MATERIALIZED
            sw.incTab();
            sw.write("SELECT DISTINCT t_0.* ");
            writeFrom(where, tableName, true);
            writeWhere(where);
            tableName = "_cte";
            writeOrderBy(where, query.getSort(), false);
            writePagination(query);
            sw.decTab();
            sw.write("\n)");
        }

        sw.write("\nSELECT DISTINCT \n");
        if (!isNativeQuery) {
            sw.write(getMappingAlias(where)).write(',');
        }
        else {
            for (QResultMapping mapping : this.resultMappings) {//
                sw.write('\t');
                String alias = getMappingAlias(mapping);
                for (QColumn col : mapping.getSelectedColumns()) {
                    sw.write(alias).write('.').write(col.getPhysicalName()).write(", ");
                }
                sw.write('\n');
            }
        }
        sw.replaceTrailingComma("\n");
        writeFrom(where, tableName, false);
        writeWhere(where);
        writeOrderBy(where, query.getSort(), false);//where.hasArrayDescendantNode());
//        if (!need_complex_pagination && isNativeQuery) {
//            writePagination(query);
//        }
        String sql = sw.reset();
        return sql;
    }

    private void writeOrderBy(JqlFilter where, Sort sort, boolean need_joined_result_set_ordering) {
        if (!need_joined_result_set_ordering) {
            if (sort == null || sort.isUnsorted()) return;
        }

        sw.write("\nORDER BY ");
        final HashSet<String> explicitSortColumns = new HashSet<>();
        if (sort != null) {
            QSchema schema = where.getSchema();
            sort.forEach(order -> {
                String p = order.getProperty();
                String qname = getMappingAlias(where) + '.' + resolveColumnName(schema.getColumn(p));
                explicitSortColumns.add(qname);
                sw.write(qname);
                sw.write(order.isAscending() ? " asc" : " desc").write(", ");
            });
        }
        if (isNativeQuery && need_joined_result_set_ordering) {
            for (QResultMapping mapping : this.resultMappings) {
                if (!mapping.hasArrayDescendantNode()) continue;
                if (mapping != where && !mapping.isArrayNode()) continue;
                String table = getMappingAlias(mapping);
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
    private void writePagination(JqlQuery pagination) {
        int offset = pagination.getOffset();
        int limit  = pagination.getLimit();
        if (offset > 0) sw.write("\nOFFSET " + offset);
        if (limit > 0) sw.write("\nLIMIT " + limit);
    }


    public String createUpdateQuery(JqlFilter where, Map<String, Object> updateSet) {
        sw.write("\nUPDATE ").write(where.getTableName()).write(" ").write(getMappingAlias(where)).writeln(" SET");

        for (Map.Entry<String, Object> entry : updateSet.entrySet()) {
            String key = entry.getKey();
            QColumn col = where.getSchema().getColumn(key);
            Object value = BatchUpsert.convertJsonValueToColumnValue(col, entry.getValue());
            sw.write("  ");
            sw.write(key).write(" = ").writeValue(value);
            sw.write(",\n");
        }
        sw.replaceTrailingComma("\n");
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;

    }

    public String createDeleteQuery(JqlFilter where) {
        sw.write("\nDELETE ");
        this.writeFrom(where);
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

    public String createInsertStatement(QSchema schema, Map entity, boolean ignoreConflict) {

        Set<String> keys = ((Map<String, ?>)entity).keySet();
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
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
        if (ignoreConflict) {
            sw.write("\nON CONFLICT DO NOTHING");
        }
        String sql = sw.reset();
        return sql;
    }

    public String prepareBatchInsertStatement(QSchema schema, boolean ignoreConflict) {
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        for (QColumn col : schema.getWritableColumns()) {
            sw.write(col.getPhysicalName()).write(", ");
        }
        sw.replaceTrailingComma("\n) VALUES (");
        for (QColumn column : schema.getWritableColumns()) {
            sw.write(column.isJsonNode() ? "?::jsonb, " : "?,");
        }
        sw.replaceTrailingComma(")");
        if (ignoreConflict) {
            sw.write("\nON CONFLICT DO NOTHING");
        }
        String sql = sw.reset();
        return sql;
    }
}
