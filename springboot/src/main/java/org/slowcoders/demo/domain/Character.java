package org.slowcoders.demo.domain;

import javax.persistence.*;
import java.util.List;

//@Entity
@DiscriminatorColumn
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Character {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinTable(name = "character_friend_link",
            joinColumns = @JoinColumn(name="character_id"),  //외래키
            inverseJoinColumns = @JoinColumn(name="friend_id"))
    List<Character> friends;

    @ManyToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinTable(name = "character_episode_link",
            joinColumns = @JoinColumn(name="character_id"),  //외래키
            inverseJoinColumns = @JoinColumn(name="episode_id"))
    List<Episode> appearsIn;
}
