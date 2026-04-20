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

@Entity
@Table(name = "document_template", schema = "clm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, unique = true, length = 255)
    private String templateName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_format", nullable = false)
    private DocumentFormat documentFormat;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "document_content", nullable = false)
    private byte[] documentContent;

    @Column(name = "field_count", nullable = false)
    @Builder.Default
    private Integer fieldCount = 0;

    @Column(name = "is_fully_mapped", nullable = false)
    @Builder.Default
    private Boolean isFullyMapped = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "documentTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> templateFields = new ArrayList<>();
}
