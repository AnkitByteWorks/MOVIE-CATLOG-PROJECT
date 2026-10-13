package com.movietracker.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Base abstract persistence class for all JPA entities in the user service.
 * <p>
 * Provides standard primary key generation using UUIDs and automatic auditing
 * tracking creation timestamps, modification timestamps, creator identity,
 * modifier identity, and optimistic locking versioning via Spring Data JPA.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the entity, represented as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Timestamp indicating when the entity was first persisted.
     * Managed automatically by Spring Data JPA Auditing.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    /**
     * Timestamp indicating when the entity was last updated.
     * Managed automatically by Spring Data JPA Auditing.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Identifier of the user or system principal that created this record.
     * Populated via {@link org.springframework.data.domain.AuditorAware}.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Identifier of the user or system principal that last updated this record.
     * Populated via {@link org.springframework.data.domain.AuditorAware}.
     */
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    /**
     * Optimistic locking version field used to prevent concurrent update conflicts.
     */
    @Version
    @Column(name = "version")
    private Long version;
}
