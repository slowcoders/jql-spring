package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlQuery;

enum KeySet {
    Auto(JqlQuery.Auto),
    All(JqlQuery.All),
    PrimaryKeys(JqlQuery.PrimaryKeys);
    private final String text;

    private final String[] keys;

    KeySet(char key) {
        this.text = Character.toString(key);
        this.keys = new String[]{this.text};
    }

    public static KeySet toAlias(char key) {
        switch (key) {
            case JqlQuery.All:
                return All;
            case JqlQuery.PrimaryKeys:
                return PrimaryKeys;
            case JqlQuery.Auto:
                return Auto;
            default:
                return null;
        }
    }

    public static KeySet toAlias(String key) {
        if (key.length() != 1) return null;
        return toAlias(key.charAt(0));
    }

    @Override
    public String toString() {
        return text;
    }

    public String[] asKeyArray() {
        return keys;
    }

    public int bit() { return 1 << ordinal(); }
}
