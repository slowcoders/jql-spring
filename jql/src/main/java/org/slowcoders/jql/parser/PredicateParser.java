package org.slowcoders.jql.parser;

import org.slowcoders.jql.util.ClassUtils;

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

//    public Predicate parse(JqlParser parser, QScope baseScope, Map<String, Object> filter) {
//        return null;
//    }
//
//    public Predicate parse(JqlParser parser, QScope baseScope, Collection<Map<String, Object>> filters) {
//        return null;
//    }

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

//        public Predicate parse(JqlParser parser, QScope baseScope, Map<String, Object> filter) {
//            parser.parse(baseScope, filter);
//            return baseScope;
//        }
//
//        public Predicate parse(JqlParser parser, QScope baseScope, Collection<Map<String, Object>> filters) {
//            QScope or_qs = baseScope.createQueryScope(Conjunction.OR);
//            for (Map<String, Object> filter : filters) {
//                parser.parse(or_qs, (Map)filter);
//            }
//            return or_qs;
//        }

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
//        public Predicate parse(JqlParser parser, QScope baseScope, Map<String, Object> filter) {
//            Expression result = super.parse(parser, baseScope, filter);
//            return new Predicate.Not(result);
//        }
//
//        public Predicate parse(JqlParser parser, QScope baseScope, Collection<Map<String, Object>> filters) {
//            Expression result = super.parse(parser, baseScope, filters);
//            return new Predicate.Not(result);
//        }
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
        operators.put("in", new MatchAny(CompareOperator.EQ, false));
        operators.put("not in", new MatchAny(CompareOperator.NE, false));

        operators.put("like", new MatchAny(CompareOperator.LIKE, true));
        operators.put("not like", new MatchAny(CompareOperator.NOT_LIKE, true));

        operators.put("eq", new MatchAny(CompareOperator.EQ, true));
        operators.put("ne", new MatchAny(CompareOperator.NE, true));

        Compare GT = new Compare(CompareOperator.GT);
        Compare LT = new Compare(CompareOperator.LT);
        Compare GE = new Compare(CompareOperator.GE);
        Compare LE = new Compare(CompareOperator.LE);

        operators.put("gt", GT);
        operators.put("lt", LT);
        operators.put("ge", GE);
        operators.put("le", LE);
        operators.put("between", new PairedPredicate(GE, LT, Conjunction.AND));
        operators.put("not between", new PairedPredicate(LT, GE, Conjunction.OR));

//        operators.put("excepts", new Excepts());
    }
}
