package org.eipgrid.jql.sample.jpa.domain;

import lombok.*;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "character_friend_link", schema = "starwars",
        uniqueConstraints = {
                @UniqueConstraint(name ="character_id__friend_id__uindex", columnNames = {"character_id", "friend_id"})
        }
)
public class CharacterFriendLink implements Serializable {


    @Getter @Setter
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", referencedColumnName = "id")
    Character character;

    @Getter @Setter
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", referencedColumnName = "id")
    Character friend;

}
