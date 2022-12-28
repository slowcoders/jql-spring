package org.eipgrid.jql.sample.jpa.domain;


import lombok.*;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "episode", schema = "starwars",
        uniqueConstraints = {
                @UniqueConstraint(name ="episode_pkey", columnNames = {"title"})}
)
public class Episode implements Serializable {
    @Getter @Setter
    @Id
    @Column(name = "title")
    java.lang.String title;

    @Getter @Setter
    @Column(name = "published")
    java.sql.Timestamp published;

    @Getter @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_episode_link", joinColumns = @JoinColumn(name="episode_id"), inverseJoinColumns = @JoinColumn(name="character_id"))
    List<Character> character;

}