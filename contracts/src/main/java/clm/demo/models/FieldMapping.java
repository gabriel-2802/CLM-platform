package clm.demo.models;

import clm.demo.models.enums.DataTransformation;
import clm.demo.models.enums.MappingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Maps a template field to a source database column for automated contract generation.
 * Stores the database table and column name, plus optional data transformation rules.
 * 
 * @see TemplateField
 * @see ContractTemplate
 */
@Entity
@Table(name = "field_mapping", schema = "contracts", indexes = {
    @Index(name = "idx_field_mapping_template", columnList = "template_id, source_table, source_column"),
    @Index(name = "idx_field_mapping_field", columnList = "template_field_id"),
    @Index(name = "idx_field_mapping_unique", columnList = "template_id, template_field_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMapping {

    /** Unique identifier for this mapping. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the contract template this mapping belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ContractTemplate contractTemplate;

    /** Reference to the specific template field being mapped. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id", nullable = false)
    private TemplateField templateField;
    
    /** Source database table name (e.g., "users", "clients", "punct_de_lucru"). */
    @Column(name = "source_table", nullable = false, length = 100)
    private String sourceTable;

    /** Column name in the source table to fetch data from (e.g., "first_name", "company_address"). */
    @Column(name = "source_column", nullable = false, length = 100)
    private String sourceColumn;

    /** Optional data transformation to apply before inserting value into contract (e.g., uppercase, format date). */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_transformation", length = 255)
    private DataTransformation dataTransformation;

    /** Status of this mapping (MAPPED, PENDING_REVIEW, INVALID). Defaults to MAPPED. */
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 50)
    @Builder.Default
    private MappingStatus mappingStatus = MappingStatus.MAPPED;

    /** Optional notes or configuration details for this mapping. */
    @Column(name = "notes", length = 500)
    private String notes;

    /** Creation timestamp, auto-set by Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp, auto-maintained by Hibernate. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
