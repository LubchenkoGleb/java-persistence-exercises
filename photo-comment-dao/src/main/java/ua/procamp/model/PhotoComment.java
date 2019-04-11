package ua.procamp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * todo:
 * - implement no argument constructor
 * - implement getters and setters
 * - implement equals and hashCode based on identifier field
 * <p>
 * - configure JPA entity
 * - specify table name: "photo_comment"
 * - configure auto generated identifier
 * - configure not nullable column: text
 * <p>
 * - map relation between Photo and PhotoComment using foreign_key column: "photo_id"
 * - configure relation as mandatory (not optional)
 */
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@Entity
@Table(name = "photo_comment")
public class PhotoComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    public PhotoComment(String text) {
        this.text = text;
    }
}
