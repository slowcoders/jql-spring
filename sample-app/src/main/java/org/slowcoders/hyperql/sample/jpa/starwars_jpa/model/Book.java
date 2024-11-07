package org.slowcoders.hyperql.sample.jpa.starwars_jpa.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Table(name = "book", schema = "starwars_jpa", catalog = "starwars_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="book_pkey", columnNames = {"id"})
        }
)
public class Book implements java.io.Serializable {
    @Getter @Setter
    @Id
    @Column(name = "id", nullable = false)
    private Long id;


    @Getter @Setter
    @Column(name = "length", nullable = true)
    private Float length;

    @Getter @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = true, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_author_id_2_pk_author__id"))
    private Author author;

}
