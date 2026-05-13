package clm.demo.models;

import clm.demo.models.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base entity for all managed documents (contracts, appendices).
 * Uses JOINED inheritance so each subclass stores its own columns in a dedicated table
 * while sharing the common fields here.
 *
 * <p>template is null for non-fillable documents uploaded directly.</p>
 */
@Entity
@Table(name = "document", schema = "clm")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "document_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The template this document was generated from.
     * NULL for non-fillable documents (direct uploads).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private DocumentTemplate documentTemplate;

    /**
     * Auto-detected from magic bytes on upload/generation.
     * Nullable until the document binary is produced.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_format")
    private DocumentFormat documentFormat;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "document_content")
    private byte[] documentContent;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "signed_document_content")
    private byte[] signedDocumentContent;

    /* audit */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by_user")
    private Integer generatedByUser;

    @Column(name = "uploaded_signed_at")
    private LocalDateTime uploadedSignedAt;

    @Column(name = "uploaded_signed_by_user")
    private Integer uploadedSignedByUser;

    @Column(name = "notes", length = 1000)
    private String notes;

    @PrePersist
    protected void onDocumentCreate() {
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
    }

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<DocumentFieldValue> fieldValues = new ArrayList<>();
}
