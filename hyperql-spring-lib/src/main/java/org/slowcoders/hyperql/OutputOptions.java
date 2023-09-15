package org.slowcoders.hyperql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OutputOptions {

    OutputFormat output;
    String select;
    @Schema(implementation = String.class)
    String[] sort;
    Integer page;
    Integer limit;
    Boolean distinct;
}
