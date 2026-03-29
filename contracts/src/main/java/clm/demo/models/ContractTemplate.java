package clm.demo.models;

import clm.demo.models.enums.DocumentFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a contract blueprint used for automated generation.
 * <p><b>Workflow:</b>
 * <ul>
 * <li><b>Parse:</b> Extracts placeholders (e.g., '.....') from uploaded ZIP/PDF/DOCX.</li>
 * <li><b>Generate:</b> Merges template with client data provided at generation time.</li>
 * </ul>
 */
@Entity
@Table(name = "contract_template", schema = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier for the template (e.g., "Enterprise NDA v2"). Max 255 chars. */
    @Column(name = "template_name", nullable = false, unique = true, length = 255)
    private String templateName;

    /** Administrative notes regarding the template's purpose or specific use cases. */
    @Column(name = "description", length = 500)
    private String description;

    /** Format type; dictates whether the system uses PDFBox or Apache POI for processing. */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_format", nullable = false, length = 10)
    private DocumentFormat documentFormat;

    /** * The raw source file.
     * <b>Note:</b> Stored as BYTEA to prevent memory spikes during list queries.
     * Not lazily loaded - better handled at service layer if needed.
     */
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "document_content", nullable = false)
    private byte[] documentContent;

    /** Cached count of markers found in the doc. */
    @Column(name = "field_count", nullable = false)
    @Builder.Default
    private Integer fieldCount = 0;

    /** Readiness flag: True only when every TemplateField has an associated FieldMapping. */
    @Column(name = "is_fully_mapped", nullable = false)
    @Builder.Default
    private Boolean isFullyMapped = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** The individual placeholders extracted during the Parse phase. */
    @OneToMany(mappedBy = "contractTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> templateFields = new ArrayList<>();


    /** Audit trail of every contract generated using this specific template. */
    @OneToMany(mappedBy = "contractTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GeneratedContract> generatedContracts = new ArrayList<>();
}