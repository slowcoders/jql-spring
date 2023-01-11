package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSelect;

enum KeySet {
    Auto(JqlSelect.Auto),
    All(JqlSelect.All),
    PrimaryKeys(JqlSelect.PrimaryKeys);
    private final String text;

    private final String[] keys;

    KeySet(String key) {
        this.text = key;
        this.keys = new String[]{this.text};
    }

    public static KeySet toAlias(String key) {
        if (key.length() != 1) return null;
        switch (key) {
            case JqlSelect.All:
                return All;
            case JqlSelect.PrimaryKeys:
                return PrimaryKeys;
            case JqlSelect.Auto:
                return Auto;
            default:
                return null;
        }
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
