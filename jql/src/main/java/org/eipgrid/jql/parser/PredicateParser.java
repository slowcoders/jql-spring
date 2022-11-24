package org.eipgrid.jql.parser;

import org.eipgrid.jql.util.ClassUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class PredicateParser {

    static PredicateParser defaultParser = new MatchAny(CompareOperator.EQ, true);

    public Class<?> getAccessType(Object value, Class<?> fieldType) {
        return fieldType;
    }

    public boolean isAttributeNameRequired() { return false; }

    public abstract Predicate createPredicate(QAttribute column, Object value);

    public static PredicateParser getParser(String function) {
        if (function == null) return defaultParser;
        return operators.get(function);
    }

    public PredicateSet getPredicateNode(PredicateSet baseScope, ValueNodeType nodeType) {
        switch (nodeType) {
            case Entities: {
                PredicateSet or_qs = new PredicateSet(Conjunction.OR, baseScope.getBaseFilter());
                baseScope.add(this.isNotOperation() ? new Predicate.Not(or_qs) : or_qs);
                return or_qs;
            }
            case Entity:
                if (this.isNotOperation()) {
                    PredicateSet and_qs = new PredicateSet(Conjunction.AND, baseScope.getBaseFilter());
                    baseScope.add(new Predicate.Not(and_qs));
                    return and_qs;
                }
            default:
                return baseScope;
        }
    }

    protected boolean isNotOperation() { return false; }

    public boolean needFetchData() { return true; }

    private static final HashMap<String, PredicateParser> operators = new HashMap<>();

    //=======================================================//
    // Operators
    // ------------------------------------------------------//

    private static class Compare extends PredicateParser {
        final CompareOperator operator;

        Compare(CompareOperator operator) {
            this.operator = operator;
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            return new Predicate.Compare(column, value, operator);
        }
    }

    private static class MatchAny extends PredicateParser {
        final CompareOperator operator;
        final boolean fetchData;

        MatchAny(CompareOperator operator, boolean fetchData) {
            this.operator = operator;
            this.fetchData = fetchData;
        }

        protected boolean isNotOperation() { return operator == CompareOperator.NE || operator == CompareOperator.NOT_LIKE; }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            /**
             * IN operator(!fetchData) 는 항상 Array 를 받는다.
             */
            if (!fetchData || (value.getClass().isArray() || value instanceof Collection)) {
                fieldType = ClassUtils.getArrayType(fieldType);
            }
            return fieldType;
        }

        public boolean needFetchData() { return fetchData; }

        public Predicate createPredicate(QAttribute column, Object value) {
            Predicate cond;
            Collection values = value == null ? null : ClassUtils.asCollection(value);
            if (values != null) {
                cond = new Predicate.MatchAny(column, operator, values);
            }
            else {
                cond = new Predicate.Compare(column, value, operator);
            }
            return cond;
        }
    };

//    static class Excepts extends MatchAny {
//        Excepts() {
//            super(CompareOperator.EQ, true);
//        }
//
//        public PredicateSet getParserNode(PredicateSet baseScope, ValueNodeType nodeType) {
//            if (nodeType == ValueNodeType.Entities) {
//                PredicateSet or_qs = new PredicateSet(Conjunction.OR, baseScope.getBaseFilter());
//                baseScope.add(or_qs);
//                return or_qs;
//            }
//            return baseScope;
//        }
//
//    };

    static class PairedPredicate extends PredicateParser {
        private final PredicateParser operator1;
        private final PredicateParser operator2;
        private final Conjunction conjunction;

        PairedPredicate(PredicateParser operator1, PredicateParser operator2, Conjunction conjunction) {
            this.operator1 = operator1;
            this.operator2 = operator2;
            this.conjunction = conjunction;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            return ClassUtils.getArrayType(fieldType);
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            Object[] range = (Object[])value;
            PredicateSet predicates = new PredicateSet(conjunction);
            predicates.add(operator1.createPredicate(column, range[0]));
            predicates.add(operator2.createPredicate(column, range[1]));
            return predicates;
        }
    }

    static {
        operators.put("is", new MatchAny(CompareOperator.EQ, false));
        operators.put("not", new MatchAny(CompareOperator.NE, false));

        operators.put("like", new MatchAny(CompareOperator.LIKE, true));
        operators.put("not like", new MatchAny(CompareOperator.NOT_LIKE, true));

        Compare GT = new Compare(CompareOperator.GT);
        Compare LT = new Compare(CompareOperator.LT);
        Compare GE = new Compare(CompareOperator.GE);
        Compare LE = new Compare(CompareOperator.LE);

        operators.put("gt", GT);
        operators.put("lt", LT);
        operators.put("ge", GE);
        operators.put("le", LE);
        operators.put("between", new PairedPredicate(GE, LE, Conjunction.AND));
        operators.put("not between", new PairedPredicate(LT, GT, Conjunction.OR));
    }
}
