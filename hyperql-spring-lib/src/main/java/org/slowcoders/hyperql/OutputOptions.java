package org.slowcoders.hyperql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OutputOptions {

    OutputFormat output;
    @Schema(implementation = String.class, defaultValue = "*")
    String select;
    @Schema(implementation = String.class, defaultValue = "")
    String[] sort;
    Integer page;
    Integer limit;
    @Schema(implementation = Boolean.class)
    Boolean distinct;
    @Schema(implementation = String.class, defaultValue = "")
    String[] viewParams;
}
