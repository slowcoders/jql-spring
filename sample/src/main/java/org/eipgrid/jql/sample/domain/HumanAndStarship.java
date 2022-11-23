package org.eipgrid.jql.sample.domain;

import javax.persistence.*;

public class HumanAndStarship extends Character {
    @Id
    Long humanId;

    @Id
    String starshipId;
}
