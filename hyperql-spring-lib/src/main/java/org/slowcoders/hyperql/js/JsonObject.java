package org.slowcoders.hyperql.js;

public class JsonObject {
    private final String json;

    public JsonObject(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return json;
    }
}
