package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import jakarta.persistence.*;


@Entity
@Table(name = "customer", schema = "bookstore_jpa", catalog = "bookstore_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="customer_pkey", columnNames = {"id"})
        }
)
public class Customer implements java.io.Serializable {
    @Getter
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

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
    @JoinTable(name = "customer_friend_link", schema = "bookstore_jpa", catalog = "bookstore_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="customer_id__friend_id__uindex", columnNames = {"customer_id", "friend_id"})
            },
            joinColumns = @JoinColumn(name="customer_id"), inverseJoinColumns = @JoinColumn(name="friend_id"))
    private Set<Customer> friend_;

    @Getter @Setter
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name = "book_order", schema = "bookstore_jpa", catalog = "bookstore_jpa", 
        uniqueConstraints = {
                @UniqueConstraint(name = "customer_id__book_id__uindex", columnNames ={"customer_id", "book_id"})
        },
        joinColumns = @JoinColumn(name = "customer_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
    private Set<Book>book_;
}
