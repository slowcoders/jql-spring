package org.eipgrid.jql.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

/**
 *     species varchar(31) not null,
 *     id bigserial not null
 *         constraint character_pkey
 *             primary key,
 *     name varchar(255) not null,
 *     primary_function varchar(255),
 *     height real,
 *     home_planet varchar(255),
 *     mass real,
 *     note jsonb
 * */
@Getter
public class Character {
    private Long id;
    private String species;
    private String name;
    private String primaryFunction;
    private Float height;
    private String homePlanet;
    private Float mass;
    private String note;
}
