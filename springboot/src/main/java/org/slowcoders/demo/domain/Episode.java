package org.slowcoders.demo.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

//@Entity
public class Episode {
    @Id
    String title;

    Timestamp published;
}
