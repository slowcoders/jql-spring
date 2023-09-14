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

    public static PredicateFactory getFactory(String function) {
        if (function == null) return IS;
        return operators.get(function);
    }

    public PredicateSet getPredicates(EntityFilter node, HqlParser.NodeType nodeType) {
        PredicateSet basePredicates = node.getPredicateSet();
        switch (nodeType) {
            case Entities: {
                PredicateSet or_qs = new PredicateSet(Conjunction.OR, basePredicates.getBaseFilter());
                basePredicates.add(or_qs);
                return or_qs;
            }
            case Entity: default:
                return basePredicates;
        }
    }

    private static final HashMap<String, PredicateFactory> operators = new HashMap<>();

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

    static class NotMatch extends MatchAny {

        NotMatch(HqlOp operator) {
            super(operator);
        }

        public boolean isAttributeNameRequired() { return false; }

        public PredicateSet getPredicates(EntityFilter node, HqlParser.NodeType nodeType) {
            PredicateSet baseScope = node.getPredicateSet();
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

    static {
        MatchAny EQ = new MatchAny(HqlOp.EQ);
        MatchAny NE = new NotMatch(HqlOp.NE);
        operators.put("is", EQ);
        operators.put("not", NE);
        operators.put("eq", EQ);
        operators.put("ne", NE);

        operators.put("like", new MatchAny(HqlOp.LIKE));
        operators.put("not like", new NotMatch(HqlOp.NOT_LIKE));

        Compare GT = new Compare(HqlOp.GT);
        Compare LT = new Compare(HqlOp.LT);
        Compare GE = new Compare(HqlOp.GE);
        Compare LE = new Compare(HqlOp.LE);

        operators.put("gt", GT);
        operators.put("lt", LT);
        operators.put("ge", GE);
        operators.put("le", LE);
        operators.put("between", new PairedPredicate(GE, LE, Conjunction.AND));
        operators.put("not between", new PairedPredicate(LT, GT, Conjunction.OR));
    }
}
