package org.eipgrid.jql.parser;

import java.util.Collection;

public interface PredicateVisitor {

    void visitPredicate(String column, JqlOp operator, Object value);

    void visitNot(Expression statement);

    void visitMatchAny(String column, JqlOp operator, Collection values);

    void visitCompareNull(String column, JqlOp operator);

    void visitPredicates(Collection<Expression> predicates, Conjunction conjunction);

    void visitAlwaysTrue();
}
