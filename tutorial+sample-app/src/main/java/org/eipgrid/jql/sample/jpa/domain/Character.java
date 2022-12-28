package org.eipgrid.jql.sample.jpa.domain;

import lombok.Getter;
import lombok.Setter;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.spring.JQLService;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "character", schema = "starwars",
        uniqueConstraints = {
                @UniqueConstraint(name ="character_pkey", columnNames = {"id"})}
)
@Profile("jpa")
public class Character {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    java.lang.Long id;

    @Getter @Setter
    @Column(name = "species")
    java.lang.String species;

    @Getter @Setter
    @Column(name = "name")
    java.lang.String name;

    @Getter @Setter
    @Column(name = "primary_function")
    java.lang.String primaryFunction;

    @Getter @Setter
    @Column(name = "height")
    java.lang.Float height;

    @Getter @Setter
    @Column(name = "home_planet")
    java.lang.String homePlanet;

    @Getter @Setter
    @Column(name = "mass")
    java.lang.Float mass;

    @Getter @Setter
    @Column(name = "note")
    String note;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pilot")
    List<Starship> starship;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_episode_link",
            joinColumns = @JoinColumn(name="character_id"),
            inverseJoinColumns = @JoinColumn(name="episode_id"))
    List<Episode> episode;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_friend_link", joinColumns = @JoinColumn(name="character_id"), inverseJoinColumns = @JoinColumn(name="friend_id"))
    List<Character> friend;

    @Profile("jpa")
    public static class Repository extends JPARepositoryBase<Character, Long> {

        public Repository(JQLService service) {
            super(service);
        }

        @Override
        public Long getEntityId(Character character) {
            return character.getId();
        }
    }
}
