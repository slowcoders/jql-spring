package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.Set;


@Entity
@Table(name = "book", schema = "bookstore_jpa", catalog = "bookstore_jpa",
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
    @Column(name = "title", nullable = false)
    private String title;

    @Getter @Setter
    @Column(name = "price", nullable = true)
    private Float price;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_order", schema = "bookstore_jpa", catalog = "bookstore_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="student_id__book_id__uindex", columnNames = {"student_id", "book_id"})
            },
            joinColumns = @JoinColumn(name="book_id"), inverseJoinColumns = @JoinColumn(name="student_id"))
    private Set<Student> student_;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = true, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_author_id_2_pk_author__id"))
    private Author author;
}
