package org.slowcoders.hyperql;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;

@Getter @Setter
@ParameterObject
public class OutputOptions {

    @Parameter(example = "Object")
    OutputFormat output;
    @Parameter(example = "*")
    String select;
    @Parameter(example = "")
    String[] sort;
    @Parameter(example = "0")
    Integer page;
    @Parameter(example = "0")
    Integer limit;
    @Parameter(example = "false")
    Boolean distinct;
    @Parameter(example = "")
    String[] viewParams;
}
