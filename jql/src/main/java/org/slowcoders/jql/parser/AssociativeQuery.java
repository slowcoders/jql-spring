package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.List;

public class AssociativeQuery extends JqlQuery {


    private final JqlEntityJoin associativeJoin;
    private final String targetPath;

    public AssociativeQuery(JqlSchema schema, JqlEntityJoin associativeJoin, String targetPath) {
        super(schema);
        this.associativeJoin = associativeJoin;
        this.targetPath = targetPath;
    }

//    public
}
