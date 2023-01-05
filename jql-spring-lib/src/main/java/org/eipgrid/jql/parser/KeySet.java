package org.eipgrid.jql.parser;

enum KeySet {
    Auto("@"),
    All("*"),
    PrimaryKeys("0"),
    Nothing("_");

    private final String text;

    private final String[] keys;

    KeySet(String key) {
        this.text = key;
        this.keys = new String[]{this.text};
    }

    public static KeySet toAlias(char c) {
        switch (c) {
            case '*':
                return All;
            case '0':
                return PrimaryKeys;
            case '@':
                return Auto;
            case '_':
                return Nothing;
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
