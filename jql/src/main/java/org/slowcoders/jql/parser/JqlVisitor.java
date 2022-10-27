package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.Collection;

public interface JqlVisitor {

    void visitCompare(QAttribute column, CompareOperator operator, Object value);

    void visitNot(Expression statement);

    void visitMatchAny(QAttribute key, CompareOperator operator, Collection values);

    void visitIsNull(QAttribute key, boolean isNull);

    void visitAlwaysTrue();

    void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction);

    JqlSchema setWorkingSchema(JqlSchema schema);
}
