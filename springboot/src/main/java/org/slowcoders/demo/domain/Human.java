package org.slowcoders.demo.domain;

import javax.persistence.*;
import java.util.List;

//@Entity
public class Human extends Character {
    String homePlanet;

    Float height;

    Float mass;

    @ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinTable(name = "human_starship_link",
            joinColumns = @JoinColumn(name="human_id"),  //외래키
            inverseJoinColumns = @JoinColumn(name="starship_id"))
    List<Starship> starships;
}
