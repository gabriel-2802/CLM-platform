package clm.demo.models;

import clm.demo.models.enums.DataType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single placeholder field extracted from a {@link DocumentTemplate}.
 *
 * <p>{@code @Getter}/{@code @Setter} are used instead of {@code @Data} to avoid
 * generating {@code equals/hashCode} over all fields, including the lazy-loaded
 * {@code documentTemplate} relationship.</p>
 */
@Entity
@Table(name = "template_field", schema = "clm", indexes = {
        @Index(name = "idx_template_field_template_position", columnList = "template_id, field_position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"documentTemplate"})
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate documentTemplate;

    @Column(name = "field_label", length = 255)
    @Builder.Default
    private String fieldLabel = null;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "data_type", nullable = false)
    @Builder.Default
    private DataType dataType = DataType.STRING;

    @Column(name = "field_position")
    private Integer fieldPosition;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "format_pattern", length = 255)
    private String formatPattern;
}
