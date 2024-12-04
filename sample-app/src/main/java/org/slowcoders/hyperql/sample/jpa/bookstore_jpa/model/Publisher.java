package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Table(name = "publisher", schema = "bookstore_jpa", catalog = "bookstore_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="publisher_pkey", columnNames = {"id"})
        }
)
public class Publisher implements java.io.Serializable {
    @Getter @Setter
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "memo", nullable = true, columnDefinition = "jsonb")
    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private com.fasterxml.jackson.databind.JsonNode memo;

}