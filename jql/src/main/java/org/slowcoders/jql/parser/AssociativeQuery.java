package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.JqlSchema;

public class AssociativeQuery extends JqlQuery {


    private final JqlSchemaJoin associativeJoin;
    private final String targetPath;

    public AssociativeQuery(JqlSchema schema, JqlSchemaJoin associativeJoin, String targetPath) {
        super(schema);
        this.associativeJoin = associativeJoin;
        this.targetPath = targetPath;
    }

//    public
}
