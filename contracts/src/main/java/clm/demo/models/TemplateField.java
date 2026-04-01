package clm.demo.models;

import clm.demo.models.enums.DataType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents a single placeholder (e.g., '.....') extracted from a template.
 * <p><b>Identification:</b> Fields are identified by their occurrence index
 * (top-to-bottom) since the same pattern can appear multiple times.</p>
 */
@Entity
@Table(name = "template_field", schema = "contracts", indexes = {
        @Index(name = "idx_template_field_template_position", columnList = "template_id, field_position")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent template; lazy loaded */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template contractTemplate;

    /** Display name used in the admin mapping UI (e.g., "Client Name"). */
    @Column(name = "field_label", length = 255)
    @Builder.Default
    private String fieldLabel = null;

    /** Expected format (STRING, DATE, etc.) used for validation and parsing logic. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "data_type", nullable = false)
    @Builder.Default
    private DataType dataType = DataType.STRING;


    /** Zero-based index of appearance in the document.*/
    @Column(name = "field_position")
    private Integer fieldPosition;

    /** If true, generation fails if no value is provided for this field. */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /** * Formatting mask applied at generation (e.g., "dd/MM/yyyy" or "#,##0.00").
     * Uses Java DateTimeFormatter or DecimalFormat syntax.
     */
    @Column(name = "format_pattern", length = 255)
    private String formatPattern;


}