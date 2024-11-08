package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import jakarta.persistence.*;


@Entity
@Table(name = "episode", schema = "bookstore_jpa", catalog = "bookstore_jpa",
        uniqueConstraints = {
                @UniqueConstraint(name ="episode_pkey", columnNames = {"title"})
        }
)
public class Episode implements java.io.Serializable {
    @Getter @Setter
    @Id
    @Column(name = "title", nullable = false)
    private String title;

    @Getter @Setter
    @Column(name = "published", nullable = true, columnDefinition = "timestamp")
    private java.sql.Timestamp published;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "customer_episode_link", schema = "bookstore_jpa", catalog = "bookstore_jpa",
            uniqueConstraints = {
                    @UniqueConstraint(name ="customer_id__episode_id__uindex", columnNames = {"customer_id", "episode_id"})
            },
            joinColumns = @JoinColumn(name="episode_id"), inverseJoinColumns = @JoinColumn(name="customer_id"))
    private Set<Customer> customer_;

}
