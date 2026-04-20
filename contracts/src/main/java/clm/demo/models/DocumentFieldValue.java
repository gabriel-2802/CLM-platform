package clm.demo.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit trail record for each field value injected into any generated document.
 *
 * @see Document
 * @see TemplateField
 */
@Entity
@Table(name = "document_field_value", schema = "clm", indexes = {
        @Index(name = "idx_document_field_value_document", columnList = "document_id"),
        @Index(name = "idx_document_field_value_field",    columnList = "template_field_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id", nullable = false)
    private TemplateField templateField;

    @Column(name = "field_value", nullable = false, columnDefinition = "TEXT")
    private String fieldValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
