package org.slowcoders.hyperql.parser;

import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.util.ClassUtils;

import java.util.Collection;
import java.util.HashMap;

abstract class PredicateFactory {


    static PredicateFactory IS = new MatchAny(HqlOp.EQ);

    public Class<?> getAccessType(Object value, Class<?> fieldType) {
        return fieldType;
    }

    public boolean isAttributeNameRequired() { return true; }

    public abstract Predicate createPredicate(QColumn column, Object value);

    public static PredicateFactory getFactory(String operator) {
        return operators.get(operator);
    }

    public static PredicateFactory getUnaryFactory(String operator) {
        return unaryOperators.get(operator);
    }

    public PredicateSet getPredicates(PredicateSet basePredicates, HqlParser.NodeType nodeType) {
        Conjunction conjunction = nodeType == HqlParser.NodeType.Entities ? Conjunction.OR : Conjunction.AND;
        if (basePredicates.getConjunction() == conjunction) return basePredicates;
        PredicateSet or_qs = new PredicateSet(conjunction, basePredicates.getBaseFilter());
        basePredicates.add(or_qs);
        return or_qs;
    }

    private static final HashMap<String, PredicateFactory> operators = new HashMap<>();
    private static final HashMap<String, PredicateFactory> unaryOperators = new HashMap<>();

    //=======================================================//
    // Operators
    // ------------------------------------------------------//

    private static class Compare extends PredicateFactory {
        final HqlOp operator;

        Compare(HqlOp operator) {
            super();
            this.operator = operator;
        }

        public Predicate createPredicate(QColumn column, Object value) {
            return new Predicate.Compare(column, operator, value);
        }
    }

    private static class MatchAny extends PredicateFactory {
        final HqlOp operator;

        MatchAny(HqlOp operator) {
            super();
            this.operator = operator;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            if (value.getClass().isArray() || value instanceof Collection) {
                fieldType = ClassUtils.getArrayType(fieldType);
            }
            return fieldType;
        }


        public Predicate createPredicate(QColumn column, Object value) {
            Predicate cond;
            Collection values = value == null ? null : ClassUtils.asCollection(value);
            if (values != null) {
                cond = new Predicate.MatchAny(column, operator, values);
            }
            else {
                cond = new Predicate.Compare(column, operator, value);
            }
            return cond;
        }
    };

    private static class CompareArray extends PredicateFactory {
        HqlOp operator;
        CompareArray(HqlOp operator) {
            super();
            this.operator = operator;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            if (value.getClass().isArray() || value instanceof Collection) {
                fieldType = ClassUtils.getArrayType(fieldType);
            } else {
                fieldType = ClassUtils.getArrayType(Object.class);
            }
            return fieldType;
        }


        public Predicate createPredicate(QColumn column, Object value) {
            Predicate cond = new Predicate.CompareArray(column, operator, (Collection) value);
            return cond;
        }
    };

    static class NotMatch extends MatchAny {

        NotMatch(HqlOp operator) {
            super(operator);
        }

        public boolean isAttributeNameRequired() { return false; }

        public PredicateSet getPredicates(PredicateSet baseScope, HqlParser.NodeType nodeType) {
            switch (nodeType) {
                case Entities: {
                    PredicateSet or_qs = new PredicateSet(Conjunction.OR, baseScope.getBaseFilter());
                    baseScope.add(new Predicate.Not(or_qs));
                    return or_qs;
                }
                case Entity:
                    PredicateSet and_qs = new PredicateSet(Conjunction.AND, baseScope.getBaseFilter());
                    baseScope.add(new Predicate.Not(and_qs));
                    return and_qs;
                default:
                    return baseScope;
            }
        }
    }

    static class ExplicitConjunction extends PredicateFactory {

        private final Conjunction conjunction;

        ExplicitConjunction(Conjunction conjunction) { this.conjunction = conjunction; }

        public boolean isAttributeNameRequired() { return false; }

        public PredicateSet getPredicates(PredicateSet baseScope, HqlParser.NodeType nodeType) {
            if (baseScope.getConjunction() == conjunction) {
                return baseScope;
            }

            switch (nodeType) {
                case Entity:
                case Entities: {
                    PredicateSet qs = new PredicateSet(this.conjunction, baseScope.getBaseFilter());
                    baseScope.add(qs);
                    return qs;
                }
                default:
                    throw new RuntimeException("'and' and 'or' operators take only condition node(s)");
            }
        }

        public Predicate createPredicate(QColumn column, Object value) {
            throw new RuntimeException("'and' and 'or' operators take only condition node(s)");
        }

    }

    static class PairedPredicate extends PredicateFactory {
        private final PredicateFactory operator1;
        private final PredicateFactory operator2;
        private final Conjunction conjunction;

        PairedPredicate(PredicateFactory operator1, PredicateFactory operator2, Conjunction conjunction) {
            super();
            this.operator1 = operator1;
            this.operator2 = operator2;
            this.conjunction = conjunction;
        }

        public Class<?> getAccessType(Object value, Class<?> fieldType) {
            return ClassUtils.getArrayType(fieldType);
        }

        public Predicate createPredicate(QColumn column, Object value) {
            Object[] range = (Object[])value;
            PredicateSet predicates = new PredicateSet(conjunction);
            predicates.add(operator1.createPredicate(column, range[0]));
            predicates.add(operator2.createPredicate(column, range[1]));
            return predicates;
        }
    }

    public static final ExplicitConjunction AND;
    public static final ExplicitConjunction  OR;
    public static final NotMatch NOT;

    static {
        MatchAny EQ = new MatchAny(HqlOp.EQ);
        MatchAny NE = new MatchAny(HqlOp.NE);
        operators.put("", EQ);
        operators.put("==", EQ);
        operators.put("!=", NE);

        operators.put("like", new MatchAny(HqlOp.LIKE));
        operators.put("!like", new MatchAny(HqlOp.NOT_LIKE));

        operators.put("re", new MatchAny(HqlOp.RE));
        operators.put("!re", new MatchAny(HqlOp.NOT_RE));

        operators.put("re/i", new MatchAny(HqlOp.RE_ignoreCase));
        operators.put("!re/i", new MatchAny(HqlOp.NOT_RE_ignoreCase));

        Compare GT = new Compare(HqlOp.GT);
        Compare LT = new Compare(HqlOp.LT);
        Compare GE = new Compare(HqlOp.GE);
        Compare LE = new Compare(HqlOp.LE);

        operators.put(">", GT);
        operators.put("<", LT);
        operators.put(">=", GE);
        operators.put("<=", LE);
        operators.put("between", new PairedPredicate(GE, LE, Conjunction.AND));
        operators.put("!between", new PairedPredicate(LT, GT, Conjunction.OR));

        operators.put("contains", new CompareArray(HqlOp.CONTAINS));
        operators.put("!contains", new CompareArray(HqlOp.NOT_CONTAINS));
        operators.put("overlaps", new CompareArray(HqlOp.OVERLAPS));
        operators.put("!overlaps", new CompareArray(HqlOp.NOT_OVERLAPS));

        AND = new ExplicitConjunction(Conjunction.AND);
        OR = new ExplicitConjunction(Conjunction.OR);
        NOT = new NotMatch(HqlOp.NE);

//        operators.put("and", AND);
//        operators.put("or", OR);
//        operators.put("not", new NotMatch(HqlOp.NE));

        unaryOperators.put("and", AND);
        unaryOperators.put("or", OR);
        unaryOperators.put("not", NOT);
    }
}
