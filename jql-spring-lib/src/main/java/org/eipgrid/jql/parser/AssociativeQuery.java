package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.JqlSchema;

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
