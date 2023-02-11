package org.eipgrid.jql.sample.jpa.starwars.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import java.util.*;
import javax.persistence.*;

/** This source is generated by JQL-JDBC.
 * And replaced schema and catalog from "starwars" to "starwars_jpa".
 */

@Entity
@Table(name = "character", schema = "starwars_jpa", catalog = "starwars_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="character_pkey", columnNames = {"id"})
        }
)
public class Character implements java.io.Serializable {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter @Setter
    @Column(name = "species", nullable = false)
    private String species;

    @Getter @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "height", nullable = true)
    private Float height;

    @Getter @Setter
    @Column(name = "mass", nullable = true)
    private Float mass;

    @Getter @Setter
    @Column(name = "metadata", nullable = true, columnDefinition = "jsonb")
    @org.hibernate.annotations.Type(type = "io.hypersistence.utils.hibernate.type.json.JsonType")
    private JsonNode metadata;

    @Getter @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pilot")
    private List<Starship> starship_;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_episode_link", schema = "starwars_jpa", catalog = "starwars_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="character_id__episode_id__uindex", columnNames = {"character_id", "episode_id"})
            },
            joinColumns = @JoinColumn(name="character_id"), inverseJoinColumns = @JoinColumn(name="episode_id"))
    private List<Episode> episode_;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_friend_link", schema = "starwars_jpa", catalog = "starwars_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="character_id__friend_id__uindex", columnNames = {"character_id", "friend_id"})
            },
            joinColumns = @JoinColumn(name="character_id"), inverseJoinColumns = @JoinColumn(name="friend_id"))
    private List<Character> friend_;

}
