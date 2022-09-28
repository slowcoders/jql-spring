package org.slowcoders.jql.parser;

import org.slowcoders.jql.util.ClassUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.slowcoders.jql.parser.Predicate.*;

abstract class PredicateParser {

    public Class<?> getAccessType(Object value, Class<?> fieldType) {
        return fieldType;
    }

    public boolean isAttributeNameRequired() { return false; }

    public abstract Predicate createPredicate(QAttribute column, Object value);

    public static PredicateParser getParser(String function) {
        if (function == null) return EQ;
        return operators.get(function);
    }

    public Predicate parse(JqlParser parser, QueryNode baseNode, Map<String, Object> filter) {
        return null;
    }

    public Predicate parse(JqlParser parser, QueryNode baseNode, Collection<Map<String, Object>> filters) {
        return null;
    }

    public boolean needFetchData() { return true; }

    private static final HashMap<String, PredicateParser> operators = new HashMap<>();

    //=======================================================//
    // Operators
    // ------------------------------------------------------//

    static class Compare extends PredicateParser {
        final String operator;

        Compare(String operator) {
            this.operator = operator;
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            return new BinaryOp(column, value.toString(), operator);
        }
    }

    static abstract class CompareAny extends PredicateParser {
        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            if ((value.getClass().isArray() || value instanceof Collection)) {
                fieldType = ClassUtils.getArrayType(fieldType);
            }
            return fieldType;
        }
    }

    static class Examines extends CompareAny {
        final String function;

        Examines(String function) {
            this.function = function;
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            Predicate cond;
            Collection values = ClassUtils.asCollection(value);
            if (values != null) {
                PredicateSet or_predicates = new PredicateSet(Conjunction.OR);
                for (Object s : (Collection)value) {
                    cond = new BinaryOp(column, s.toString(), function);
                    or_predicates.add(cond);
                }
                cond = or_predicates;
            }
            else {
                cond = new BinaryOp(column, value.toString(), function);
            }
            return cond;
        }
    }

    private static class Matches extends CompareAny {

        public Predicate parse(JqlParser parser, QueryNode baseNode, Map<String, Object> filter) {
            parser.parse(baseNode, filter);
            return baseNode;
        }

        public Predicate parse(JqlParser parser, QueryNode baseNode, Collection<Map<String, Object>> filters) {
            QueryNode or_qs = baseNode.createQuerySet(Conjunction.OR);
            for (Map<String, Object> filter : filters) {
                parser.parse(or_qs, (Map)filter);
            }
            return or_qs;
        }
        
        public Predicate createPredicate(QAttribute column, Object value) {
            Predicate cond;
            Collection values = ClassUtils.asCollection(value);
            if (values != null) {
                cond = new FilterOp(column, " in ", values);
            }
            else {
                cond = new BinaryOp(column, value, " = ");
            }
            return cond;
        }
    };

    private static final PredicateParser EQ = new Matches();
    private static final PredicateParser IN = new Matches() {
        public boolean needFetchData() { return false; }
    };

    private static final PredicateParser ISNULL = new PredicateParser() {
        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            return boolean.class;
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            return new PostOp(column,
                    value == Boolean.TRUE ? " IS NULL " : " IS NOT NULL ");
        }
    };

    static class Not extends PredicateParser {
        private final PredicateParser operator;

        Not(PredicateParser operator) {
            this.operator = operator;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            return operator.getAccessType(value, fieldType);
        }

        public boolean isAttributeNameRequired() {
            return this.operator.isAttributeNameRequired();
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            return UnaryOp.not(operator.createPredicate(column, value));
        }

        public Predicate parse(JqlParser parser, QueryNode baseNode, Map<String, Object> filter) {
            QueryNode node = baseNode.createQuerySet(Conjunction.AND);
            Expression result = operator.parse(parser, node, filter);
            return UnaryOp.not(result);
        }

        public Predicate parse(JqlParser parser, QueryNode baseNode, Collection<Map<String, Object>> filters) {
            QueryNode node = baseNode.createQuerySet(Conjunction.AND);
            Expression result = operator.parse(parser, node, filters);
            return UnaryOp.not(result);
        }
    };

    static class PairedPredicate extends PredicateParser {
        private final PredicateParser operator1;
        private final PredicateParser operator2;

        PairedPredicate(PredicateParser operator1, PredicateParser operator2) {
            this.operator1 = operator1;
            this.operator2 = operator2;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            return ClassUtils.getArrayType(fieldType);
        }

        public Predicate createPredicate(QAttribute column, Object value) {
            Object[] range = (Object[])value;
            PredicateSet predicates = new PredicateSet(Conjunction.AND);
            predicates.add(operator1.createPredicate(column, range[0]));
            predicates.add(operator2.createPredicate(column, range[1]));
            return predicates;
        }
    }

    static {
        operators.put("in", IN);
        operators.put("!in", new Not(IN));

        Examines LINE = new Examines(" like ");
        operators.put("like", LINE);
        operators.put("!like", new Not(LINE));

        operators.put("isnull", ISNULL);

        operators.put("eq", EQ);
        Not NOT = new Not(EQ);
        operators.put("ne", NOT);
        operators.put("not", NOT);

        Compare GT = new Compare(" > ");
        Compare LT = new Compare(" < ");
        Compare GE = new Compare(" >= ");
        Compare LE = new Compare(" <= ");

        operators.put("gt", GT);
        operators.put("lt", LT);
        operators.put("ge", GE);
        operators.put("le", LE);
        operators.put("between", new PairedPredicate(GE, LT));
    }
}
