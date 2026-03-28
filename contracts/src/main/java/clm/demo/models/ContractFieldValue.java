package clm.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit trail record for each field value inserted into a generated contract.
 * Tracks the original database value, final transformed value, and transformation applied.
 * 
 * @see GeneratedContract
 * @see TemplateField
 */
@Entity
@Table(name = "contract_field_value", schema = "contracts", indexes = {
    @Index(name = "idx_contract_field_value_contract", columnList = "generated_contract_id"),
    @Index(name = "idx_contract_field_value_field", columnList = "template_field_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractFieldValue {

    /** Unique identifier for this field value record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the generated contract this field value belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_contract_id", nullable = false)
    private GeneratedContract generatedContract;

    /** Reference to the template field that was filled. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id", nullable = false)
    private TemplateField templateField;

    /** Final value inserted into the contract document (may be transformed from sourceValue). */
    @Column(name = "field_value", nullable = false, columnDefinition = "TEXT")
    private String fieldValue;

    /** Original value fetched from the database before any transformation. */
    @Column(name = "source_value", length = 1000)
    private String sourceValue;

    /** Name of the transformation applied (e.g., "UPPERCASE", "DATE_FORMAT_DD_MM_YYYY"). Null if no transformation. */
    @Column(name = "transformation_applied", length = 255)
    private String transformationApplied;

    /** Timestamp when this field was filled into the contract. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
