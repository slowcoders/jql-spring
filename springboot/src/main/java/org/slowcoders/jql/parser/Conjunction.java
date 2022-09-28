package org.slowcoders.jql.parser;

enum Conjunction {
    AND(" and "),
    OR(" or ");

    private final String delimiter;

    Conjunction(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String toString() {
        return delimiter;
    }
}
