package clm.demo.models;

import clm.demo.models.enums.DataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single placeholder (e.g., '.....') extracted from a template.
 * <p><b>Identification:</b> Fields are identified by their occurrence index
 * (top-to-bottom) since the same pattern can appear multiple times.</p>
 */
@Entity
@Table(name = "template_field", schema = "contracts", indexes = {
        @Index(name = "idx_template_field_template_position", columnList = "template_id, field_position"),
        @Index(name = "idx_template_field_template_name", columnList = "template_id, field_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent template; lazy loaded to optimize bulk field operations. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ContractTemplate contractTemplate;

    /** Programmatic ID used for mapping (e.g., "client_name"). snake_case recommended. */
    @Column(name = "field_name", nullable = false, length = 255)
    private String fieldName;

    /** Display name used in the admin mapping UI (e.g., "Client Name"). */
    @Column(name = "field_label", length = 255)
    private String fieldLabel;

    /** Expected format (STRING, DATE, etc.) used for validation and parsing logic. */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 50)
    @Builder.Default
    private DataType dataType = DataType.STRING;

    /** * Zero-based index of appearance in the document.
     * Essential for DOCX where fixed coordinates aren't available.
     */
    @Column(name = "field_position")
    private Integer fieldPosition;

    /** 1-based page index. Populated for PDF templates; usually null for DOCX. */
    @Column(name = "page_number")
    private Integer pageNumber;

    /** If true, generation fails if no value is resolved via FieldMapping. */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /** * Formatting mask applied at generation (e.g., "dd/MM/yyyy" or "#,##0.00").
     * Uses Java DateTimeFormatter or DecimalFormat syntax.
     */
    @Column(name = "format_pattern", length = 255)
    private String formatPattern;

    /** Definitions linking this field to specific source database columns. */
    @OneToMany(mappedBy = "templateField", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FieldMapping> fieldMappings = new ArrayList<>();

}