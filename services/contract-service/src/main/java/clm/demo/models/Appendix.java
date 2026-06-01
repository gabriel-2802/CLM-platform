package clm.demo.models;

import clm.demo.models.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

/**
 * Auxiliary document attached to a {@link Contract}.
 * Can be fillable (generated from a template, then signed) or non-fillable
 * (uploaded directly). Fillability is determined by whether {@code documentTemplate}
 * is set in the parent {@link Document}.
 */
@Entity
@Table(name = "appendix", schema = "clm", indexes = {
        @Index(name = "idx_appendix_contract", columnList = "contract_id")
})
@DiscriminatorValue("APPENDIX")
@PrimaryKeyJoinColumn(name = "document_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class Appendix extends Document {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "appendix_status", nullable = false)
    @Builder.Default
    private AppendixStatus appendixStatus = AppendixStatus.DRAFT;

    @Column(name = "sign_date")
    private LocalDate signDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;
}
