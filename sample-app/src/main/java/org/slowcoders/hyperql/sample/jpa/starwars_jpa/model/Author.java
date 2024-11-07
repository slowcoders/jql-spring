package org.slowcoders.hyperql.sample.jpa.starwars_jpa.model;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import jakarta.persistence.*;


@Entity
@Table(name = "author", schema = "starwars_jpa", catalog = "starwars_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="author_pkey", columnNames = {"id"})
        }
)
public class Author implements java.io.Serializable {
    @Getter
    @Setter
    @Id
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
    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private com.fasterxml.jackson.databind.JsonNode metadata;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "author_episode_link", schema = "starwars_jpa", catalog = "starwars_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="author_id__episode_id__uindex", columnNames = {"author_id", "episode_id"})
            },
            joinColumns = @JoinColumn(name="author_id"), inverseJoinColumns = @JoinColumn(name="episode_id"))
    private Set<Episode> episode_;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "author_friend_link", schema = "starwars_jpa", catalog = "starwars_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="author_id__friend_id__uindex", columnNames = {"author_id", "friend_id"})
            },
            joinColumns = @JoinColumn(name="author_id"), inverseJoinColumns = @JoinColumn(name="friend_id"))
    private Set<Author> friend_;

    @Getter @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Book> book_;
}
