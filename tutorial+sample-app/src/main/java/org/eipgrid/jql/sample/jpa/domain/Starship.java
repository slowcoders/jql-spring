package org.eipgrid.jql.sample.jpa.domain;

import lombok.*;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "starship", schema = "starwars",
        uniqueConstraints = {
                @UniqueConstraint(name ="starship_pkey", columnNames = {"id"})}
)
public class Starship implements Serializable {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    java.lang.Long id;


    @Getter @Setter
    @Column(name = "length")
    java.lang.Float length;

    @Getter @Setter
    @Column(name = "name")
    java.lang.String name;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilot_id", referencedColumnName = "id")
    Character pilot;

}

