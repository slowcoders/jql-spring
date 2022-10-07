package org.slowcoders.demo.domain;

import javax.persistence.*;
import java.util.List;

public class HumanAndStarship extends Character {
    @Id
    Long humanId;

    @Id
    String starshipId;
}
