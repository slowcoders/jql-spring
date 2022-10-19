package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class JqlQuery extends EntityFilter {

    private ArrayList<JqlEntityJoin> joinMap = new ArrayList<>();
    private ArrayList<JqlSchema> fetchTables = new ArrayList<>();

    private ArrayList<FetchInfo> fetchInfos = new ArrayList<>();
    private String[] emptyJsonPath = new String[0];

    public JqlQuery(JqlSchema schema) {
        super(schema);
        this.fetchTables.add(schema);
        this.fetchInfos.add(new FetchInfo(emptyJsonPath, schema));
    }

    protected EntityFilter addTableJoin(JqlEntityJoin joinKeys, boolean fetchData) {
        JqlSchema schema = joinKeys.getJoinedSchema();
        if (joinMap.indexOf(joinKeys) < 0) {
            joinMap.add(joinKeys);
        }
        if (fetchData && fetchTables.indexOf(schema) < 0) {
            String[] basePath = getJsonPath(joinKeys.getAnchorSchema());

            String[] jsonPath = toJsonPath(basePath, joinKeys.getJsonName());
            this.fetchInfos.add(new FetchInfo(jsonPath, schema));
            this.fetchTables.add(schema);
        }
        return new EntityFilter(schema);
    }

    private String[] getJsonPath(JqlSchema anchorSchema) {
        for (FetchInfo fetch : fetchInfos) {
            if (fetch.schema == anchorSchema) {
                return fetch.jsonPath;
            }
        }
        return null;
    }

    private String[] toJsonPath(String[] basePath, String jsonName) {
        String[] path = jsonName.split("\\.");
        if (basePath != null && basePath.length > 0) {
            String[] jsonPath = new String[basePath.length + path.length];
            System.arraycopy(basePath, 0, jsonPath, 0, basePath.length);
            System.arraycopy(path, 0, jsonPath, basePath.length, path.length);
            path = jsonPath;
        }
        return path;
    }


    public Iterable<JqlSchema> getFetchTables() {
        return fetchTables;
    }

    public List<JqlEntityJoin> getJoinList() {
        return joinMap;
    }

    public List<FetchInfo> getFetchList() {
        return this.fetchInfos;
    }

    public static class FetchInfo {
        public final String[] jsonPath;
        public final JqlSchema schema;
        public final String alias;

        public FetchInfo(String[] jsonPath, JqlSchema schema) {
            this.jsonPath = jsonPath;
            this.schema = schema;
            this.alias = null;
        }
    }
}
