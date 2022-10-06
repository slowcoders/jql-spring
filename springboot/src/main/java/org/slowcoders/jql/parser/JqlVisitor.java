package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.Collection;

public interface JqlVisitor {
    void writeColumnNames(Iterable<String> names, boolean withTableName);

    void writeWhere(JqlQuery where, boolean includeTableName);

    QueryBuilder writeColumnName(String name, boolean withTableName);

    QueryBuilder writeColumnName(String name);

    QueryBuilder writeTableName();

    void visitCompare(QAttribute column, CompareOperator operator, Object value);

    void visitNot(Expression statement);

    void visitMatchAny(QAttribute key, CompareOperator operator, Collection values);

    void visitIsNull(QAttribute key, boolean isNull);

    void visitAlwaysTrue();

    void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction);

    JqlSchema setWorkingSchema(JqlSchema schema);
}
