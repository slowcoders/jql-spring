package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import jakarta.persistence.*;


@Entity
@Table(name = "author", schema = "bookstore_jpa", catalog = "bookstore_jpa",
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
    @Column(name = "name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "profile", nullable = true, columnDefinition = "jsonb")
    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private com.fasterxml.jackson.databind.JsonNode metadata;

    @Getter @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Book> book_;
}
