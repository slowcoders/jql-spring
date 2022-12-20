package org.eipgrid.jql.sample.jpa.domain;

import lombok.*;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "character_episode_link", schema = "starwars",
        uniqueConstraints = {
                @UniqueConstraint(name ="character_id__episode_id__uindex", columnNames = {"character_id", "episode_id"})
        }
)
public class CharacterEpisodeLink implements Serializable {


    @Getter @Setter
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", referencedColumnName = "id")
    Character character;

    @Getter @Setter
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", referencedColumnName = "title")
    Episode episode;

}
