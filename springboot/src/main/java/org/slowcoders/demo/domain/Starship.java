package org.slowcoders.demo.domain;

import javax.persistence.*;
import java.util.List;

//@Entity
public class Starship {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    Float length;

    @ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinTable(name = "human_starship_link",
            joinColumns = @JoinColumn(name="starship_id"),  //외래키
            inverseJoinColumns = @JoinColumn(name="human_id"))
    List<Human> humans;
}
