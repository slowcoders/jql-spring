package org.eipgrid.jql.sample.jpa.starwars.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import javax.persistence.*;

/** This is generated by JQL-JDBC */

@Entity
@Table(name = "starship", schema = "starwars_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="starship_pkey", columnNames = {"id"})
        }
)
public class Starship implements java.io.Serializable {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Integer id;


    @Getter @Setter
    @Column(name = "length")
    Float length;

    @Getter @Setter
    @Column(name = "name", nullable = false)
    String name;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilot_id", referencedColumnName = "id")
    Character pilot_;

}
